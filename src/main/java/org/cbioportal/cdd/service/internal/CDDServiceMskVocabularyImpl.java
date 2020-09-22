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
import org.cbioportal.cdd.repository.topbraid.MskVocabularyClinicalAttributeMetadataRepository;
import org.cbioportal.cdd.service.ClinicalDataDictionaryService;
import org.cbioportal.cdd.service.exception.CancerStudyNotFoundException;
import org.cbioportal.cdd.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.cdd.service.exception.ClinicalMetadataSourceUnresponsiveException;
import org.cbioportal.cdd.service.exception.FailedCacheRefreshException;
import org.cbioportal.cdd.service.util.MSKVocabStudyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("mskvocabulary")
public class CDDServiceMskVocabularyImpl implements ClinicalDataDictionaryService {

    @Autowired
    private MskVocabularyClinicalAttributeMetadataRepository clinicalAttributeRepository;

    @Autowired
    private MSKVocabStudyUtil mskVocabStudyUtil;

    private static final Logger logger = LoggerFactory.getLogger(CDDServiceMskVocabularyImpl.class);

    @Override
    public List<ClinicalAttributeMetadata> getClinicalAttributeMetadata(String cancerStudy)
        throws ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        HashMap<String, ClinicalAttributeMetadata> clinicalAttributeMetadataMap = getClinicalAttributeMetadataMap();
        List<String> columnHeaders = new ArrayList<>(clinicalAttributeMetadataMap.keySet());
        List<ClinicalAttributeMetadata> clinicalAttributes = getMetadataByColumnHeaders(cancerStudy, columnHeaders);
        return clinicalAttributes;
    }

    @Override
    public List<ClinicalAttributeMetadata> getMetadataByColumnHeaders(String cancerStudy, List<String> columnHeaders)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        HashMap<String, ClinicalAttributeMetadata> clinicalAttributeMetadataMap = getClinicalAttributeMetadataMap();
        List<ClinicalAttributeMetadata> clinicalAttributes = new ArrayList<>();
        List<String> invalidClinicalAttributes = new ArrayList<String>();
        for (String columnHeader : columnHeaders) {
            try {
                ClinicalAttributeMetadata clinicalAttributeMetadataForColumnHeader = getMetadataByColumnHeader(clinicalAttributeMetadataMap, columnHeader);
                clinicalAttributes.add(clinicalAttributeMetadataForColumnHeader);
            } catch (ClinicalAttributeNotFoundException e) {
                invalidClinicalAttributes.add(columnHeader);
            }
        }
        if (invalidClinicalAttributes.size() > 0) {
            throw new ClinicalAttributeNotFoundException(invalidClinicalAttributes);
        }
        return clinicalAttributes;
    }

    @Override
    public List<ClinicalAttributeMetadata> getMetadataBySearchTerms(List<String> searchTerms, String attributeType, boolean inclusiveSearch)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException {
        throw new UnsupportedOperationException("search for similar terms within the MSK Standard Vocabulary is not yet implemented");
    }

    @Override
    public ClinicalAttributeMetadata getMetadataByColumnHeader(String cancerStudy, String columnHeader)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        HashMap<String, ClinicalAttributeMetadata> clinicalAttributeMetadataMap = getClinicalAttributeMetadataMap();
        return getMetadataByColumnHeader(clinicalAttributeMetadataMap, columnHeader);
    }

    @Override
    public List<CancerStudy> getCancerStudies() throws ClinicalMetadataSourceUnresponsiveException {
        return mskVocabStudyUtil.getMskVocabularyStudyList();
    }

    @Override
    public Map<String, String> forceResetCache() throws FailedCacheRefreshException {
        return Collections.singletonMap("response", "No Cache!");
    }

    private HashMap<String, ClinicalAttributeMetadata> getClinicalAttributeMetadataMap() throws ClinicalMetadataSourceUnresponsiveException {
        List<ClinicalAttributeMetadata> clinicalAttributes = clinicalAttributeRepository.getClinicalAttributeMetadata(); // TODO : add a caching layer so refetching for every request is not needed
        HashMap<String, ClinicalAttributeMetadata> clinicalAttributeMetadataMap = new HashMap<String, ClinicalAttributeMetadata>();
        for (ClinicalAttributeMetadata clinicalAttributeMetadata : clinicalAttributes) {
            clinicalAttributeMetadataMap.put(clinicalAttributeMetadata.getColumnHeader(), clinicalAttributeMetadata);
        }
        return clinicalAttributeMetadataMap;
    }

    private ClinicalAttributeMetadata getMetadataByColumnHeader(Map<String, ClinicalAttributeMetadata> clinicalAttributeMetadataMap, String columnHeader)
        throws ClinicalAttributeNotFoundException {
        if (clinicalAttributeMetadataMap.containsKey(columnHeader.toUpperCase())) {
            return clinicalAttributeMetadataMap.get(columnHeader.toUpperCase());
        }
        throw new ClinicalAttributeNotFoundException(columnHeader);
    }


}
