/*
 * Copyright (c) 2018-2020 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.cdd.repository.topbraid;

import java.util.*;
import javax.annotation.Resource;

import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.repository.ClinicalAttributeMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Manda Wilson
 **/
@Repository
public class ClinicalAttributeMetadataRepositoryTopBraidImpl extends TopBraidRepository<ClinicalAttributeMetadata> implements ClinicalAttributeMetadataRepository {

    private final static Logger logger = LoggerFactory.getLogger(ClinicalAttributeMetadataRepositoryTopBraidImpl.class);


    @Value("${topbraid.cdd_namespace_prefix:http://data.mskcc.org/ontologies/ClinicalDataDictionary#}")
    private String topBraidCddNamespacePrefix;

    @Value("${topbraid.cdd_graph_id:urn:x-evn-master:cdd}")
    private String topBraidCddGraphId;

    private String overridesQuery = null;
    private String attributesQuery = null;

    private String getOverridesQuery() {
        if (overridesQuery == null) {
            overridesQuery = 
                    "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                    "PREFIX cdd:<" + topBraidCddNamespacePrefix + "> " +
                    "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
                    "SELECT DISTINCT ?study_id ?column_header (SAMPLE(?PriorityValue) AS ?priority) (SAMPLE(?AttributeTypeValue) AS ?attribute_type) (SAMPLE(?DatatypeValue) AS ?datatype) (SAMPLE(?DescriptionValue) AS ?description) (SAMPLE(?DisplayNameValue) AS ?display_name) " +
                    "WHERE { " +
                    "    GRAPH <" + topBraidCddGraphId + "> { " +
                    "        ?node rdf:type ?type. " +
                    "        ?node skos:broader ?parent. " +
                    "        ?parent cdd:StudyId ?study_id. " +
                    "        ?parent skos:broader ?grandparent. " +
                    "        ?grandparent skos:prefLabel ?column_header. " +
                    "        OPTIONAL{?node cdd:PriorityValue ?PriorityValue}. " +
                    "        OPTIONAL{?node cdd:AttributeTypeValue ?AttributeTypeValue}. " +
                    "        OPTIONAL{?node cdd:DatatypeValue ?DatatypeValue}. " +
                    "        OPTIONAL{?node cdd:DescriptionValue ?DescriptionValue}. " +
                    "        OPTIONAL{?node cdd:DisplayNameValue ?DisplayNameValue}. " +
                    "    } " +
                    "} " +
                    "GROUP BY ?study_id ?column_header " +
                    "ORDER BY ?study_id ?column_header " +
                    "VALUES ?type {cdd:ClinicalAttributeOverridePriorityValue cdd:ClinicalAttributeOverrideAttributeTypeValue cdd:ClinicalAttributeOverrideDatatypeValue cdd:ClinicalAttributeOverrideDescriptionValue cdd:ClinicalAttributeOverrideDisplayNameValue}";
        }
        return overridesQuery;
    }

    private String getAttributesQuery() {
        if (attributesQuery == null) {
            attributesQuery =
                    "PREFIX cdd:<" + topBraidCddNamespacePrefix + "> " +
                    "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
                    "SELECT ?column_header ?display_name ?attribute_type ?datatype ?description ?priority " +
                    "WHERE { " +
                    "    GRAPH <" + topBraidCddGraphId + "> { " +
                    "        ?subject skos:prefLabel ?column_header. " +
                    "        ?subject cdd:AttributeType ?attribute_type. " +
                    "        ?subject cdd:Datatype ?datatype. " +
                    "        ?subject cdd:Description ?description. " +
                    "        ?subject cdd:DisplayName ?display_name. " +
                    "        ?subject cdd:Priority ?priority. " +
                    "    } " +
                    "}";
        }
        return attributesQuery;
    }

    public ArrayList<ClinicalAttributeMetadata> getClinicalAttributeMetadata() {
        logger.info("Fetching clinical attribute metadata from TopBraid...");
        try {
            ArrayList<ClinicalAttributeMetadata> list = new ArrayList<ClinicalAttributeMetadata>(super.query(getAttributesQuery(), new ParameterizedTypeReference<List<ClinicalAttributeMetadata>>(){}));
            return list;
        } catch (TopBraidException e) {
            logger.error("Problem connecting to TopBraid");
            throw new RuntimeException(e);
        }
    }

    public Map<String, ArrayList<ClinicalAttributeMetadata>> getClinicalAttributeMetadataOverrides() {
        logger.info("Fetch clinical attribute metadata overrides from TopBraid...");
        try {
            List<ClinicalAttributeMetadata> overridesList = super.query(getOverridesQuery(), new ParameterizedTypeReference<List<ClinicalAttributeMetadata>>(){});
            Map<String, ArrayList<ClinicalAttributeMetadata>> overridesStudyMap = new HashMap<>();
            for (ClinicalAttributeMetadata clinicalAttributeMetadata : overridesList) {
                if (!overridesStudyMap.containsKey(clinicalAttributeMetadata.getStudyId())) {
                    overridesStudyMap.put(clinicalAttributeMetadata.getStudyId(), new ArrayList<ClinicalAttributeMetadata>());
                }
                overridesStudyMap.get(clinicalAttributeMetadata.getStudyId()).add(clinicalAttributeMetadata);
            }
            return overridesStudyMap;
        } catch (TopBraidException e) {
            logger.error("Problem connecting to TopBraid");
            throw new RuntimeException(e);
        }
    }
}
