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

package org.cbioportal.cdd.repository.localfiles;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

/**
 * Repository that reads clinical attribute metadata from JSON files packaged
 * as classpath resources under src/main/resources/jsonfiles/.
 *
 * Base attributes file: jsonfiles/clinical_attributes.json
 * Per-study override files: jsonfiles/<study_id>_clinical_attributes.json
 *
 * @author Manda Wilson
 **/
@Repository
public class JsonFileRepository {

    private static final Logger logger = LoggerFactory.getLogger(JsonFileRepository.class);

    private static final String BASE_ATTRIBUTES_RESOURCE = "jsonfiles/clinical_attributes.json";
    private static final String OVERRIDES_RESOURCE_PATTERN = "classpath:jsonfiles/*_clinical_attributes.json";
    private static final String OVERRIDES_FILENAME_SUFFIX = "_clinical_attributes.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ArrayList<ClinicalAttributeMetadata> getClinicalAttributeMetadata() {
        logger.info("Reading clinical attribute metadata from classpath: " + BASE_ATTRIBUTES_RESOURCE);
        try (InputStream is = new ClassPathResource(BASE_ATTRIBUTES_RESOURCE).getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<ArrayList<ClinicalAttributeMetadata>>(){});
        } catch (IOException e) {
            logger.error("Failed to read clinical attribute metadata from " + BASE_ATTRIBUTES_RESOURCE);
            throw new RuntimeException(e);
        }
    }

    public HashMap<String, ArrayList<ClinicalAttributeMetadata>> getClinicalAttributeMetadataOverrides() {
        logger.info("Reading clinical attribute metadata overrides matching: " + OVERRIDES_RESOURCE_PATTERN);
        HashMap<String, ArrayList<ClinicalAttributeMetadata>> overridesMap = new HashMap<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] overrideResources = resolver.getResources(OVERRIDES_RESOURCE_PATTERN);
            for (Resource resource : overrideResources) {
                String filename = resource.getFilename();
                String studyId = filename.substring(0, filename.length() - OVERRIDES_FILENAME_SUFFIX.length());
                try (InputStream is = resource.getInputStream()) {
                    ArrayList<ClinicalAttributeMetadata> overrides = objectMapper.readValue(is, new TypeReference<ArrayList<ClinicalAttributeMetadata>>(){});
                    overridesMap.put(studyId, overrides);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read override files matching: " + OVERRIDES_RESOURCE_PATTERN);
            throw new RuntimeException(e);
        }
        return overridesMap;
    }
}
