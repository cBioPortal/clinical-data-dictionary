/*
 * Copyright (c) 2018 - 2020, 2024 Memorial Sloan Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
*/

package org.cbioportal.cdd.repository.graphite;

import java.util.*;
import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.repository.graphite.GraphiteException;
import org.cbioportal.cdd.repository.graphite.jsonmodeling.attributes.*;
import org.cbioportal.cdd.repository.graphite.jsonmodeling.overrides.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 *
 * @author Manda Wilson
 **/
@Repository
public class KnowledgeSystemsRepository extends GraphiteRepository<ClinicalAttributeMetadata> {

    private final static Logger logger = LoggerFactory.getLogger(KnowledgeSystemsRepository.class);

    @Value("${graphite.cddNamespacePrefix:}")
    private String graphiteCddNamespacePrefix;

    @Value("${graphite.cddGraphId:}")
    private String graphiteCddGraphId;

    private String getOverridesQuery() {
        return "PREFIX cdd: <" + graphiteCddNamespacePrefix + "> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
            "PREFIX g:<http://schema.synaptica.com/oasis#> " +
            "SELECT DISTINCT ?study_id ?column_header (SAMPLE(?PriorityValue) AS ?priority) " +
                "(SAMPLE(?AttributeTypeValue) AS ?attribute_type) " +
                "(SAMPLE(?DatatypeValue) AS ?datatype) " +
                "(SAMPLE(?DescriptionValue) AS ?description) " +
                "(SAMPLE(?DisplayNameValue) AS ?display_name) " +
                "(SAMPLE(?type) AS ?sampled_type) " +
            "WHERE { " +
                "?node skos:inScheme <" + graphiteCddGraphId + ">. " +
                "?node cdd:type ?type. " +
                "?node skos:broader ?parent. " +
                "?parent cdd:studyid ?study_id. " +
                "?parent skos:broader ?grandparent. " +
                "?grandparent rdfs:label ?column_header. " +
                "OPTIONAL { ?node cdd:priorityvalue ?PriorityValue }. " +
                "OPTIONAL { ?node cdd:attributetypevalue ?AttributeTypeValue }. " +
                "OPTIONAL { ?node cdd:datatypevalue ?DatatypeValue }. " +
                "OPTIONAL { ?node cdd:descriptionvalue ?DescriptionValue }. " +
                "OPTIONAL { ?node cdd:displaynamevalue ?DisplayNameValue }. " +
                "OPTIONAL{?node g:conceptStatus ?concept_status_node.} " +
                "OPTIONAL{?parent g:conceptStatus ?concept_status_parent.} " +
                "OPTIONAL{?grandparent g:conceptStatus ?concept_status_grandparent.} " +
            "FILTER (STR(?type) IN ('ClinicalAttributeOverridePriorityValue', " +
               "'ClinicalAttributeOverrideAttributeTypeValue', " +
               "'ClinicalAttributeOverrideDatatypeValue', " +
               "'ClinicalAttributeOverrideDescriptionValue', " +
               "'ClinicalAttributeOverrideDisplayNameValue')) " +
            "FILTER (?concept_status_node = 'Published') " +
            "FILTER (?concept_status_parent = 'Published') " +
            "FILTER (?concept_status_grandparent = 'Published') " +
            "} " + 
            "GROUP BY ?study_id ?column_header " +
            "ORDER BY ?study_id ?column_header ";
    }

    private String getAttributesQuery() {
        return "PREFIX cdd: <" + graphiteCddNamespacePrefix + "> " +
            "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> " +
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
            "PREFIX g:<http://schema.synaptica.com/oasis#> " +
            "SELECT ?column_header ?display_name ?attribute_type ?datatype ?description ?priority WHERE { " +
                "?subject skos:inScheme <" + graphiteCddGraphId + "> . " +
                "?subject rdfs:label ?column_header . " +
                "?subject cdd:attributetype ?attribute_type . " +
                "?subject cdd:datatype ?datatype . " +
                "?subject cdd:description ?description . " +
                "?subject cdd:displayname ?display_name . " +
                "?subject cdd:priority ?priority . " +
                "OPTIONAL{?subject g:conceptStatus ?concept_status.} " +
                "FILTER (?concept_status = 'Published')" +
            "}";
    }

    public ArrayList<ClinicalAttributeMetadata> getClinicalAttributeMetadata() {
        logger.info("Fetching clinical attribute metadata from Graphite...");
        try {
            return mapAttributes(super.query(getAttributesQuery(), new ParameterizedTypeReference<Response>(){}));
        } catch (GraphiteException e) {
            logger.error("Problem connecting to Graphite");
            throw new RuntimeException(e);
        }
    }

    private ArrayList<ClinicalAttributeMetadata> mapAttributes(Response attributeResponse) {
        List<Binding> bindings = attributeResponse.getResults().getBindings();
        ArrayList<ClinicalAttributeMetadata> clinicalAttributeMetadataList = new ArrayList<ClinicalAttributeMetadata>(bindings.size());
        for (Binding binding : bindings) {
            ClinicalAttributeMetadata clinicalAttributeMetadata = new ClinicalAttributeMetadata();
            if (binding.getColumnHeader() != null) {
                clinicalAttributeMetadata.setColumnHeader(binding.getColumnHeader().getValue());
            }
            if (binding.getDisplayName() != null) {
                clinicalAttributeMetadata.setDisplayName(binding.getDisplayName().getValue());
            }
            if (binding.getDescription() != null) {
                clinicalAttributeMetadata.setDescription(binding.getDescription().getValue());
            }
            if (binding.getDatatype() != null) {
                clinicalAttributeMetadata.setDatatype(binding.getDatatype().getValue());
            }
            if (binding.getAttributeType() != null) {
                clinicalAttributeMetadata.setAttributeType(binding.getAttributeType().getValue());
            }
            if (binding.getPriority() != null) {
                clinicalAttributeMetadata.setPriority(binding.getPriority().getValue());
            }
            clinicalAttributeMetadataList.add(clinicalAttributeMetadata);
        }
        return clinicalAttributeMetadataList;
    }

    public HashMap<String, ArrayList<ClinicalAttributeMetadata>> getClinicalAttributeMetadataOverrides() {
        logger.info("Fetch clinical attribute metadata overrides from Graphite...");
        try {
            List<ClinicalAttributeMetadata> overridesList = mapOverrides(super.query(getOverridesQuery(), new ParameterizedTypeReference<OverridesResponse>(){}));
            HashMap<String, ArrayList<ClinicalAttributeMetadata>> overridesStudyMap = new HashMap<>();
            for (ClinicalAttributeMetadata clinicalAttributeMetadata : overridesList) {
                if (!overridesStudyMap.containsKey(clinicalAttributeMetadata.getStudyId())) {
                    overridesStudyMap.put(clinicalAttributeMetadata.getStudyId(), new ArrayList<ClinicalAttributeMetadata>());
                }
                overridesStudyMap.get(clinicalAttributeMetadata.getStudyId()).add(clinicalAttributeMetadata);
            }
            return overridesStudyMap;
        } catch (GraphiteException e) {
            logger.error("Problem connecting to Graphite");
            throw new RuntimeException(e);
        }
    }

    private ArrayList<ClinicalAttributeMetadata> mapOverrides(OverridesResponse overridesResponse) {
        List<OverridesBinding> bindings = overridesResponse.getResults().getBindings();
        ArrayList<ClinicalAttributeMetadata> clinicalAttributeMetadataList = new ArrayList<ClinicalAttributeMetadata>(bindings.size());
        for (OverridesBinding binding : bindings) {
            ClinicalAttributeMetadata clinicalAttributeMetadata = new ClinicalAttributeMetadata();
            if (binding.getStudyId() != null) {
                clinicalAttributeMetadata.setStudyId(binding.getStudyId().getValue());
            }
            if (binding.getColumnHeader() != null) {
                clinicalAttributeMetadata.setColumnHeader(binding.getColumnHeader().getValue());
            }
            if (binding.getDisplayName() != null) {
                clinicalAttributeMetadata.setDisplayName(binding.getDisplayName().getValue());
            }
            if (binding.getDescription() != null) {
                clinicalAttributeMetadata.setDescription(binding.getDescription().getValue());
            }
            if (binding.getDatatype() != null) {
                clinicalAttributeMetadata.setDatatype(binding.getDatatype().getValue());
            }
            if (binding.getAttributeType() != null) {
                clinicalAttributeMetadata.setAttributeType(binding.getAttributeType().getValue());
            }
            if (binding.getPriority() != null) {
                clinicalAttributeMetadata.setPriority(binding.getPriority().getValue());
            }
            clinicalAttributeMetadataList.add(clinicalAttributeMetadata);
        }
        return clinicalAttributeMetadataList;
    }
}
