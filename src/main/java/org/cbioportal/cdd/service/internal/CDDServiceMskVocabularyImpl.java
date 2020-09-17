/*
 * Copyright (c) 2018 - 2020 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.cdd.service.internal;

import java.util.*;
import org.cbioportal.cdd.model.CancerStudy;
import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.service.ClinicalDataDictionaryService;
import org.cbioportal.cdd.service.exception.CancerStudyNotFoundException;
import org.cbioportal.cdd.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.cdd.service.exception.ClinicalMetadataSourceUnresponsiveException;
import org.cbioportal.cdd.service.exception.FailedCacheRefreshException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 */
@Service
@Qualifier("mskvocabulary")
public class CDDServiceMskVocabularyImpl implements ClinicalDataDictionaryService {

    private static final Logger logger = LoggerFactory.getLogger(CDDServiceMskVocabularyImpl.class);

    @Override
    public List<ClinicalAttributeMetadata> getClinicalAttributeMetadata(String cancerStudy)
        throws ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        return null;
    }

    @Override
    public List<ClinicalAttributeMetadata> getMetadataByColumnHeaders(String cancerStudy, List<String> columnHeaders)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        return null;
    }

    @Override
    public List<ClinicalAttributeMetadata> getMetadataBySearchTerms(List<String> searchTerms, String attributeType, boolean inclusiveSearch)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException {
        return null;
    }

    @Override
    public ClinicalAttributeMetadata getMetadataByColumnHeader(String cancerStudy, String columnHeader)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        return null;
    }

    @Override
    public List<CancerStudy> getCancerStudies() throws ClinicalMetadataSourceUnresponsiveException {
        return null;
    }

    @Override
    public Map<String, String> forceResetCache() throws FailedCacheRefreshException {
        return Collections.singletonMap("response", "No Cache!");
    }

}
