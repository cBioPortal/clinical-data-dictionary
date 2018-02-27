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

package org.mskcc.clinical_attributes;

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
@Import(ClinicalAttributesTestConfig.class)
public class ClinicalAttributesTests {

    // TODO this is using the real Google spreadsheet, mock that instead

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void getClinicalAttributesTest() throws Exception {
        // now test all clinical attributes are returned by GET /api/
        ResponseEntity<String> response = restTemplate.getForEntity("/api/fake_study_id", String.class);
        assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));

        ObjectMapper mapper = new ObjectMapper();
        JsonNode responseJSON = mapper.readTree(response.getBody());

        assertThat(responseJSON.size(), equalTo(3533));
        assertThat(response.getBody(), containsString("{\"normalized_column_header\":\"DX_PERCENT_PR\",\"display_name\":\"MedR Diagnostic PR %\",\"descriptions\":\"Progesterone Receptor (PR) percentage reported in diagnostic sample. A value of >=1% indicates a \\\"PR Positive\\\" tumor.\",\"datatype\":\"STRING\",\"attribute_type\":\"PATIENT\",\"priority\":\"1\"}")); }
    }
