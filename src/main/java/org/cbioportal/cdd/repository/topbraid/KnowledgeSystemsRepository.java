/*
 * Copyright (c) 2018 - 2020 Memorial Sloan-Kettering Cancer Center.
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
import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.repository.topbraid.TopBraidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 *
 * @author Manda Wilson
 **/
public class KnowledgeSystemsRepository extends TopBraidRepository<ClinicalAttributeMetadata> {

    private final static Logger logger = LoggerFactory.getLogger(KnowledgeSystemsRepository.class);

    @Value("${topbraid.knowledgeSystems.cddNamespacePrefix:http://data.mskcc.org/ontologies/ClinicalDataDictionary#}")
    private String topBraidCddNamespacePrefix;

    @Value("${topbraid.knowledgeSystems.cddGraphId:urn:x-evn-master:cdd}")
    private String topBraidCddGraphId;

    private MultiValueMap<String, String> overridesRequestParameters = null;
    private MultiValueMap<String, String> attributesRequestParameters = null;

    private final ParameterizedTypeReference<List<ClinicalAttributeMetadata>> clinicalAttributeMetaDataListType = new ParameterizedTypeReference<List<ClinicalAttributeMetadata>>(){};

    public KnowledgeSystemsRepository(TopBraidSessionManager topBraidSessionManager) {
        super.setTopBraidSessionManager(topBraidSessionManager);
    }

    private String getOverridesQuery() {
        return
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

    private String getAttributesQuery() {
        return
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

    private MultiValueMap<String, String> getOverridesRequestParameters() {
        if (overridesRequestParameters == null) {
            overridesRequestParameters = new LinkedMultiValueMap<String, String>();
            overridesRequestParameters.add("format", "json-simple");
            overridesRequestParameters.add("query", getOverridesQuery());
        }
        return overridesRequestParameters;
    }

    private MultiValueMap<String, String> getAttributesRequestParameters() {
        if (attributesRequestParameters == null) {
            attributesRequestParameters = new LinkedMultiValueMap<String, String>();
            attributesRequestParameters.add("format", "json-simple");
            attributesRequestParameters.add("query", getAttributesQuery());
        }
        return attributesRequestParameters;
    }

    public ArrayList<ClinicalAttributeMetadata> getClinicalAttributeMetadata() {
        logger.info("Fetching clinical attribute metadata from TopBraid...");
        try {
            List<ClinicalAttributeMetadata> responseList = super.getSparqlResponse(getAttributesRequestParameters(), clinicalAttributeMetaDataListType);
            return new ArrayList<ClinicalAttributeMetadata>(responseList);
        } catch (TopBraidException e) {
            logger.error("Problem connecting to TopBraid");
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, ArrayList<ClinicalAttributeMetadata>> getClinicalAttributeMetadataOverrides() {
        logger.info("Fetch clinical attribute metadata overrides from TopBraid...");
        try {
            List<ClinicalAttributeMetadata> overridesList = super.getSparqlResponse(getOverridesRequestParameters(), clinicalAttributeMetaDataListType);
            HashMap<String, ArrayList<ClinicalAttributeMetadata>> overridesStudyMap = new HashMap<>();
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
