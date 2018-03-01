/*
 * Copyright (c) 2018 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

package org.cbioportal.cdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import static org.junit.Assert.assertThat;
import org.junit.runner.RunWith;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Manda Wilson 
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(ClinicalDataDictionaryTestConfig.class)
public class ClinicalDataDictionaryTest {

    // TODO this is using the real Google spreadsheet, mock that instead

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void getClinicalAttributeMetadataTest() throws Exception {
        // now test all clinical attributes are returned by GET /api/
        ResponseEntity<String> response = restTemplate.getForEntity("/api/fake_study_id", String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJSON = mapper.readTree(response.getBody());

        assertThat(responseJSON.size(), equalTo(5));
        assertThat(response.getBody(), containsString("{\"normalized_column_header\":\"LAST_STATUS\",\"display_name\":\"Last Status\",\"description\":\"Last Status.\",\"datatype\":\"STRING\",\"attribute_type\":\"PATIENT\",\"priority\":\"1\"}"));
    }

    @Test
    public void getClinicalAttributeMetadataFilteredTest() throws Exception {
        // now test all clinical attributes are returned by GET /api/
        ResponseEntity<String> response = restTemplate.getForEntity("/api/fake_study_id?normalizedColumnHeaders=AGE,LAST_status", String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJSON = mapper.readTree(response.getBody());

        assertThat(responseJSON.size(), equalTo(2));
        assertThat(response.getBody(), containsString("{\"normalized_column_header\":\"LAST_STATUS\",\"display_name\":\"Last Status\",\"description\":\"Last Status.\",\"datatype\":\"STRING\",\"attribute_type\":\"PATIENT\",\"priority\":\"1\"}"));
        assertThat(response.getBody(), containsString("{\"normalized_column_header\":\"AGE\",\"display_name\":\"Diagnosis Age\",\"description\":\"Age at which a condition or disease was first diagnosed.\",\"datatype\":\"NUMBER\",\"attribute_type\":\"PATIENT\",\"priority\":\"1\"}"));
    }

    @Test
    public void getClinicalAttributeMetadataInvalidClinicalAttributeTest() throws Exception {
        // now test all clinical attributes are returned by GET /api/
        ResponseEntity<String> response = restTemplate.getForEntity("/api/fake_study_id?normalizedColumnHeaders=AGE,LAST_status,INVALID_ATTRIBUTE", String.class);
        assertThat(response.getBody(), containsString("org.cbioportal.cdd.service.exception.ClinicalAttributeNotFoundException"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

    @Test
    public void getClinicalAttributeTest() throws Exception {
        // now test all clinical attributes are returned by GET /api/
        ResponseEntity<String> response = restTemplate.getForEntity("/api/fake_study_id/AGE", String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        assertThat(response.getBody(), equalTo("{\"normalized_column_header\":\"AGE\",\"display_name\":\"Diagnosis Age\",\"description\":\"Age at which a condition or disease was first diagnosed.\",\"datatype\":\"NUMBER\",\"attribute_type\":\"PATIENT\",\"priority\":\"1\"}"));
    }

    @Test
    public void getClinicalAttributeInvalidClinicalAttributeTest() throws Exception {
        // now test all clinical attributes are returned by GET /api/
        ResponseEntity<String> response = restTemplate.getForEntity("/api/fake_study_id/INVALID_ATTRIBUTE", String.class);
        assertThat(response.getBody(), containsString("org.cbioportal.cdd.service.exception.ClinicalAttributeNotFoundException"));
        assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
    }

}
