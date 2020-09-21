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

package org.cbioportal.cdd.repository.mskvocabulary;

import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.model.MskVocabulary;
import org.cbioportal.cdd.model.MskVocabularyResponse;
import org.cbioportal.cdd.repository.topbraid.TopBraidSessionConfiguration;
import org.cbioportal.cdd.repository.topbraid.TopBraidException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author Avery Wang
 **/
@Repository
public class MskVocabularyRepository {

    private final static Logger logger = LoggerFactory.getLogger(MskVocabularyRepository.class);

    @Autowired
    private TopBraidSessionConfiguration topBraidSessionConfiguration;
    
    private String mskVocabAPI = "http://dev.evn.mskcc.org/edg/api/projects";
 
    public ArrayList<MskVocabulary> getClinicalAttributeMetadata() {
        logger.info("Fetching clinical attribute metadata from MskVocabulary...");
        
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
       
         ArrayList<MskVocabulary> mskVocabulary = new ArrayList<MskVocabulary>();
        try {
            ResponseEntity<String> response = restTemplate.exchange(uri,
                HttpMethod.GET,
                request,
                String.class);
            ObjectMapper mapper = new ObjectMapper();
            MskVocabularyResponse mskVocabularyResponse = mapper.readValue(response.getBody(), MskVocabularyResponse.class);
            mskVocabulary = mskVocabularyResponse.getMskVocabulary();
        } catch (RestClientException e) {
            logger.info("query() -- caught RestClientException");
            logger.info(e.getMessage());
            throw new TopBraidException("Failed to connect to TopBraid", e);
        } catch (Exception e) {}
        return mskVocabulary;
    }

}
