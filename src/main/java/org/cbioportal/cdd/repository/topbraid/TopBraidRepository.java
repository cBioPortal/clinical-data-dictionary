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

import java.net.URI;
import java.util.*;
import org.cbioportal.cdd.repository.topbraid.TopBraidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author Manda Wilson
 **/
public abstract class TopBraidRepository<T> {

    private final static Logger logger = LoggerFactory.getLogger(TopBraidRepository.class);

    protected TopBraidSessionManager topBraidSessionManager;

    protected TopBraidSessionManager getTopBraidSessionManager() {
        return topBraidSessionManager;
    }

    protected void setTopBraidSessionManager(TopBraidSessionManager topBraidSessionManager) {
        this.topBraidSessionManager = topBraidSessionManager;
    }

    protected List<T> getSparqlResponse(MultiValueMap<String, String> requestParameters, ParameterizedTypeReference<List<T>> parameterizedType)
            throws TopBraidException {
        return getSparqlResponse(requestParameters, parameterizedType, true);
    }

    private List<T> getSparqlResponse(MultiValueMap<String, String> requestParameters, ParameterizedTypeReference<List<T>> parameterizedType, boolean refreshSessionOnFailure)
            throws TopBraidException {
        logger.debug("getSparqlResponse() -- query: '" + requestParameters.get("query") + "'");
        String sessionId = topBraidSessionManager.getSessionId();
        logger.debug("getSparqlResponse() -- sessionId: " + sessionId);
        RestTemplate restTemplate = new RestTemplate();

        // the default supported types for MappingJackson2HttpMessageConverter are:
        //   application/json and application/*+json
        // our response content type is application/sparql-results+json-simple
        // NOTE: if the response content type was one of the default types we
        //   would not have to add the message converter to the rest template
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setSupportedMediaTypes(Collections.singletonList(
            new MediaType("application","sparql-results+json-simple")));
        restTemplate.getMessageConverters().add(messageConverter);

        // set our JSESSIONID cookie and our params
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JSESSIONID=" + sessionId);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(requestParameters, headers);

        // NOTE ParameterizedTypeReference cannot be made generic, that is why child class passes it
        // See: http://stackoverflow.com/questions/21987295/using-spring-resttemplate-in-generic-method-with-generic-parameter
        try {
            String url = topBraidSessionManager.getConfiguration().getServiceURL();
            ResponseEntity<List<T>> response = restTemplate.exchange(url,
                HttpMethod.POST,
                request,
                parameterizedType);
            logger.debug("getSparqlResponse() -- response.getBody(): '" + response.getBody() + "'");
            return response.getBody();
        } catch (RestClientException e) {
            logger.debug("getSparqlResponse() -- caught RestClientException");
            // see if we should try again, maybe the session expired
            if (refreshSessionOnFailure) {
                // force refresh of the session id
                topBraidSessionManager.getFreshSessionId();
                return getSparqlResponse(requestParameters, parameterizedType, false); // do not make a second attempt
            }
            throw new TopBraidException("Failed to connect to TopBraid", e);
        }
    }

    protected T getApiResponse(MultiValueMap<String, String> requestParameters, ParameterizedTypeReference<T> parameterizedType)
            throws TopBraidException {
        return getApiResponse(requestParameters, parameterizedType, true);
    }

    private T getApiResponse(MultiValueMap<String, String> requestParameters, ParameterizedTypeReference<T> parameterizedType, boolean refreshSessionOnFailure)
            throws TopBraidException {
        logger.debug("getApiResponse() called");
        String sessionId = topBraidSessionManager.getSessionId();
        logger.debug("getApiResponse() -- sessionId: " + sessionId);
        RestTemplate restTemplate = new RestTemplate();

        // set our JSESSIONID cookie and our params
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", "JSESSIONID=" + sessionId);
        HttpEntity<String> request = new HttpEntity<String>(headers);
        try {
            String url = topBraidSessionManager.getConfiguration().getServiceURL();
            URI uri = UriComponentsBuilder.fromHttpUrl(url)
                .queryParams(requestParameters)
                .build()
                .toUri();
            ResponseEntity<T> response = restTemplate.exchange(uri,
                HttpMethod.GET,
                request,
                parameterizedType);
            logger.debug("query() -- response.getBody(): '" + response.getBody() + "'");
            return response.getBody();
        } catch (RestClientException e) {
            logger.debug("query() -- caught RestClientException");
            // see if we should try again, maybe the session expired
            if (refreshSessionOnFailure) {
                // force refresh of the session id
                topBraidSessionManager.getFreshSessionId();
                return getApiResponse(requestParameters, parameterizedType, false); // do not make a second attempt
            }
            throw new TopBraidException("Failed to connect to TopBraid", e);
        }
    }
}
