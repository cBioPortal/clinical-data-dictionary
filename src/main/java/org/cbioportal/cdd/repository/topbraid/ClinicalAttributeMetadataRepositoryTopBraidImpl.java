/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
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

import org.apache.log4j.Logger;

import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.repository.ClinicalAttributeMetadataRepository;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Manda Wilson
 **/
@Repository
public class ClinicalAttributeMetadataRepositoryTopBraidImpl extends TopBraidRepository<ClinicalAttributeMetadata> implements ClinicalAttributeMetadataRepository {

    private final static Logger logger = Logger.getLogger(ClinicalAttributeMetadataRepositoryTopBraidImpl.class);

    private final static String GET_OVERRIDES_SPARQL_QUERY_STRING = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
        "PREFIX cdd:<http://data.mskcc.org/ontologies/clinical_data_dictionary/> " +
        "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
        "SELECT DISTINCT ?study_id ?column_header (SAMPLE(?PriorityValue) AS ?priority) (SAMPLE(?AttributeTypeValue) AS ?attribute_type) (SAMPLE(?DatatypeValue) AS ?datatype) (SAMPLE(?DescriptionValue) AS ?description) (SAMPLE(?DisplayNameValue) AS ?display_name) " +
        "WHERE { " +
        "    GRAPH <urn:x-evn-master:clinical_data_dictionary> { " +
        "        ?node rdf:type ?type. " +
        "        ?node skos:broader ?parent. " +
        "        ?parent cdd:StudyId ?study_id. " +
        "        ?parent skos:broader ?grandparent. " +
        "        ?grandparent rdfs:label ?column_header. " +
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

    private final static String GET_CLINICAL_ATTRIBUTES_SPARQL_QUERY_STRING = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
        "PREFIX cdd:<http://data.mskcc.org/ontologies/clinical_data_dictionary/> " +
        "PREFIX skos:<http://www.w3.org/2004/02/skos/core#> " +
        "SELECT ?column_header ?display_name ?attribute_type ?datatype ?description ?priority " +
        "WHERE { " +
        "    GRAPH <urn:x-evn-master:clinical_data_dictionary> { " +
        "        ?subject rdfs:label ?column_header. " +
        "        ?subject cdd:AttributeType ?attribute_type. " +
        "        ?subject cdd:Datatype ?datatype. " +
        "        ?subject cdd:Description ?description. " +
        "        ?subject cdd:DisplayName ?display_name. " +
        "        ?subject cdd:Priority ?priority. " +
        "    } " +
        "}";

    public List<ClinicalAttributeMetadata> getClinicalAttributeMetadata() {
        try {
            return super.query(GET_CLINICAL_ATTRIBUTES_SPARQL_QUERY_STRING, new ParameterizedTypeReference<List<ClinicalAttributeMetadata>>(){});
        } catch (TopBraidException e) {
            logger.error("Problem connecting to TopBraid");
            throw new RuntimeException(e);
        }
    }

    public Map<String, ArrayList<ClinicalAttributeMetadata>> getClinicalAttributeMetadataOverrides() {
        try {
            List<ClinicalAttributeMetadata> overridesList = super.query(GET_OVERRIDES_SPARQL_QUERY_STRING, new ParameterizedTypeReference<List<ClinicalAttributeMetadata>>(){});
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
