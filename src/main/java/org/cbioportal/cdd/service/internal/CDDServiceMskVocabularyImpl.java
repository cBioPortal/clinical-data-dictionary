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
import org.cbioportal.cdd.model.MskVocabularyField;
import org.cbioportal.cdd.repository.topbraid.MskVocabularyRepository;
import org.cbioportal.cdd.service.ClinicalDataDictionaryService;
import org.cbioportal.cdd.service.exception.CancerStudyNotFoundException;
import org.cbioportal.cdd.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.cdd.service.exception.ClinicalMetadataSourceUnresponsiveException;
import org.cbioportal.cdd.service.exception.FailedCacheRefreshException;
import org.cbioportal.cdd.service.util.MSKVocabStudyUtil;
import org.cbioportal.cdd.service.util.MskVocabularyConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("mskvocabulary")
public class CDDServiceMskVocabularyImpl implements ClinicalDataDictionaryService {

    @Autowired
    private MSKVocabStudyUtil mskVocabStudyUtil;

    @Autowired
    private MskVocabularyConverter mskVocabularyConverter;

    @Autowired
    private MskVocabularyRepository mskVocabularyRepository;

    private static final Logger logger = LoggerFactory.getLogger(CDDServiceMskVocabularyImpl.class);

    private Map<String, ClinicalAttributeMetadata> clinicalAttributeMetadataCache = new HashMap<String, ClinicalAttributeMetadata>();

    private void fillClinicalAttributeMetadataCache() {
        List<MskVocabularyField> mskVocabularyList = mskVocabularyRepository.getClinicalAttributeMetadata();
        if (mskVocabularyList.size() < clinicalAttributeMetadataCache.keySet().size() || mskVocabularyList == null) {
            throw new ClinicalMetadataSourceUnresponsiveException("MskVocabulary system did not return valid set of terms");
        }
        for (MskVocabularyField mskVocabularyField : mskVocabularyList) {
            ClinicalAttributeMetadata clinicalAttributeMetadata = mskVocabularyConverter.convertToClinicalAttributeMetadata(mskVocabularyField);
            clinicalAttributeMetadataCache.put(clinicalAttributeMetadata.getColumnHeader(), clinicalAttributeMetadata);
        }
    }

    @Override
    public List<ClinicalAttributeMetadata> getClinicalAttributeMetadata(String cancerStudy)
        throws ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        if (clinicalAttributeMetadataCache.isEmpty()) {
            fillClinicalAttributeMetadataCache();
        }
        return new ArrayList<ClinicalAttributeMetadata>(clinicalAttributeMetadataCache.values());
    }

    @Override
    public List<ClinicalAttributeMetadata> getMetadataByColumnHeaders(String cancerStudy, List<String> columnHeaders)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        ArrayList<ClinicalAttributeMetadata> clinicalAttributeMetadata = new ArrayList<ClinicalAttributeMetadata>();
        ArrayList<String> invalidColumnHeaders = new ArrayList<String>();
        if (clinicalAttributeMetadataCache.isEmpty()) {
            fillClinicalAttributeMetadataCache();
        }
        for (String columnHeader : columnHeaders) {
            try {
                clinicalAttributeMetadata.add(getMetadataByColumnHeader(columnHeader));
            } catch (ClinicalAttributeNotFoundException e) {
                invalidColumnHeaders.add(columnHeader);
            }
        }
        if (invalidColumnHeaders.size() > 0) {
            throw new ClinicalAttributeNotFoundException(invalidColumnHeaders);
        }
        return clinicalAttributeMetadata;
    }

    @Override
    public List<ClinicalAttributeMetadata> getMetadataBySearchTerms(List<String> searchTerms, String attributeType, boolean inclusiveSearch)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException {
        throw new UnsupportedOperationException("search for similar terms within the MSK Standard Vocabulary is not yet implemented");
    }

    @Override
    public ClinicalAttributeMetadata getMetadataByColumnHeader(String cancerStudy, String columnHeader)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        if (clinicalAttributeMetadataCache.isEmpty()) {
            fillClinicalAttributeMetadataCache();
        }
        return getMetadataByColumnHeader(columnHeader);
    }

    @Override
    public List<CancerStudy> getCancerStudies() throws ClinicalMetadataSourceUnresponsiveException {
        return mskVocabStudyUtil.getMskVocabularyStudyList();
    }

    @Override
    public Map<String, String> forceResetCache() throws FailedCacheRefreshException {
        try {
            fillClinicalAttributeMetadataCache();
        } catch (ClinicalMetadataSourceUnresponsiveException e) {
            throw new FailedCacheRefreshException("failed to refresh cache", e);
        }
        return Collections.singletonMap("response", "Success!");
    }

    private ClinicalAttributeMetadata getMetadataByColumnHeader(String columnHeader)
        throws ClinicalAttributeNotFoundException {
        if (clinicalAttributeMetadataCache.containsKey(columnHeader.toUpperCase())) {
            return clinicalAttributeMetadataCache.get(columnHeader.toUpperCase());
        }
        throw new ClinicalAttributeNotFoundException(columnHeader);
    }

}
