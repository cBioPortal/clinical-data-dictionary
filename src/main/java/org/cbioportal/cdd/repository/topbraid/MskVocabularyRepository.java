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
import org.cbioportal.cdd.model.MskVocabularyField;
import org.cbioportal.cdd.model.MskVocabularyResponse;
import org.cbioportal.cdd.repository.topbraid.TopBraidException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 *
 * @author Avery Wang
 **/

public class MskVocabularyRepository extends TopBraidRepository<MskVocabularyResponse> {

    private final static Logger logger = LoggerFactory.getLogger(MskVocabularyRepository.class);

    private MultiValueMap<String, String> requestParameters = null;

    private final ParameterizedTypeReference<MskVocabularyResponse> mskVocabularyResponseType = new ParameterizedTypeReference<MskVocabularyResponse>(){};

    // All session configuration has been supplied to this class in ClinicalAttributeMetadataRepositoryConfiguration
    public MskVocabularyRepository(TopBraidSessionManager topBraidSessionManager) {
        super.setTopBraidSessionManager(topBraidSessionManager);
    }

    private MultiValueMap<String, String> getRequestParameters() {
        if (requestParameters == null) {
            requestParameters = new LinkedMultiValueMap<String, String>();
            requestParameters.add("projectGraph", "urn:x-evn-master:redcap_projects");
            requestParameters.add("projectId", "http://mskcc.org/ontologies/redcap#MSKO0012623");
        }
        return requestParameters;
    }

    public ArrayList<MskVocabularyField> getClinicalAttributeMetadata() {
        logger.info("Fetching clinical attribute metadata from MskVocabulary...");
        try {
            MskVocabularyResponse mskVocabularyResponse =  super.getApiResponse(getRequestParameters(), mskVocabularyResponseType);
            return mskVocabularyResponse.getMskVocabulary();
        } catch (TopBraidException e) {
            logger.error("Problem connecting to TopBraid");
            throw new RuntimeException(e);
        }
    }
}
