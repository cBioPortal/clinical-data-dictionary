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

import java.lang.String;
import java.util.*;
import javax.annotation.Resource;

import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.model.MSKVocabResponse;
import org.cbioportal.cdd.model.MSKClinicalAttributeMetadata;
import org.cbioportal.cdd.repository.ClinicalAttributeMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Repository;

import java.net.URI;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Repository;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author Manda Wilson
 **/
@Repository
public class ClinicalAttributeMetadataRepositoryMSKVocabImpl implements ClinicalAttributeMetadataRepository {

    private final static Logger logger = LoggerFactory.getLogger(ClinicalAttributeMetadataRepositoryMSKVocabImpl.class);

    @Value("${topbraid.url}")
    private String topBraidURL;

    private String mskVocabAPI = "http://dev.evn.mskcc.org/edg/api/projects";
 
    @Autowired
    private TopBraidSessionConfiguration topBraidSessionConfiguration;
    
    public ArrayList<ClinicalAttributeMetadata> getClinicalAttributeMetadata() {
        logger.info("Fetching clinical attribute metadata from MSKVocab...");
        
        String sessionId = topBraidSessionConfiguration.getSessionId();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JSESSIONID=" + sessionId);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        
        URI uri = UriComponentsBuilder.fromHttpUrl(mskVocabAPI)
            .queryParam("projectGraph", "urn:x-evn-master:redcap_projects")
            .queryParam("projectId", "http://mskcc.org/ontologies/redcap#MSKO0012623")
            .build()
            .toUri();
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri,
                HttpMethod.GET,
                request,
                String.class);
            ObjectMapper mapper = new ObjectMapper();
            MSKVocabResponse mskVocabResponse = mapper.readValue(response.getBody(), MSKVocabResponse.class);
            ArrayList<MSKClinicalAttributeMetadata> mskClinicalAttributeMetadata = mskVocabResponse.getMSKClinicalAttributeMetadata();
            int count = 0;
            for (MSKClinicalAttributeMetadata a : mskClinicalAttributeMetadata) {
                logger.info("\n\nPRINTING >>>>>>>>>>>>>>>>>");
                logger.info(a.getDisplayName() + "	|	" + a.getAttributeType() + "	|	" + a.getDataType() + "	|	" + a.getColumnHeader() + "	|	" + a.getDescription() + "	|	" + a.getPriority());
            }
        } catch (RestClientException e) {
            logger.info("query() -- caught RestClientException");
            logger.info(e.getMessage());
            throw new TopBraidException("Failed to connect to TopBraid", e);
        } catch (Exception e) {
            logger.info("SOMETHING ELSE BROKE");
            logger.info(e.getMessage());
        }
        return new ArrayList<ClinicalAttributeMetadata>();
    }

    public Map<String, ArrayList<ClinicalAttributeMetadata>> getClinicalAttributeMetadataOverrides() {
        return new HashMap<String, ArrayList<ClinicalAttributeMetadata>>();
    }

    
}
