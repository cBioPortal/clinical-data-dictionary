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

package org.cbioportal.cdd.service.internal;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.service.ClinicalDataDictionaryService;
import org.cbioportal.cdd.service.exception.ClinicalAttributeNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Note this class relies on the ClinicalAttributeMetadataCache class which will frequently generate a
 * new cache of clinical attributes.  Each method in this class should only get the cache once
 * and should not attempt to make any modifications to the cache.
 *
 * @author Robert Sheridan, Manda Wilson
 */
@Service
public class ClinicalDataDictionaryServiceImpl implements ClinicalDataDictionaryService {

    @Autowired
    private ClinicalAttributeMetadataCache clinicalAttributesCache; 

    private static final Logger logger = LoggerFactory.getLogger(ClinicalDataDictionaryServiceImpl.class);

    @Override
    public List<ClinicalAttributeMetadata> getClinicalAttributeMetadata(String studyId) {
        List<String> normalizedColumnHeaders = new ArrayList<>(clinicalAttributesCache.getClinicalAttributeMetadata().keySet());
        List<ClinicalAttributeMetadata> clinicalAttributes = getMetadataByNormalizedColumnHeaders(studyId, normalizedColumnHeaders);
        return clinicalAttributes; 
    }

    @Override
    public List<ClinicalAttributeMetadata> getMetadataByNormalizedColumnHeaders(String studyId, List<String> normalizedColumnHeaders)
        throws ClinicalAttributeNotFoundException {
        List<ClinicalAttributeMetadata> clinicalAttributes = new ArrayList<ClinicalAttributeMetadata>();
        Map<String, ClinicalAttributeMetadata> clinicalAttributeCache = clinicalAttributesCache.getClinicalAttributeMetadata();
        Map<String, Map<String, ClinicalAttributeMetadata>> overridesCache = clinicalAttributesCache.getClinicalAttributeMetadataOverrides();
        for (String normalizedColumnHeader : normalizedColumnHeaders) {
            if (overridesCache.containsKey(studyId) && overridesCache.get(studyId).containsKey(normalizedColumnHeader.toUpperCase())) {
                clinicalAttributes.add(getMetadataByNormalizedColumnHeader(overridesCache.get(studyId), studyId, normalizedColumnHeader));
            } else {
                ClinicalAttributeMetadata cachedClinicalAttribute = getMetadataByNormalizedColumnHeader(clinicalAttributeCache, studyId, normalizedColumnHeader);
                if (!studyId.equals("mskimpact")) {
                    clinicalAttributes.add(cachedClinicalAttribute);
                } else {
                    // when studyId is 'mskimpact' - copy created so modification (i.e reset priority to 0) does not get applied to object stored in cache
                    ClinicalAttributeMetadata resetClinicalAttribute = new ClinicalAttributeMetadata(cachedClinicalAttribute.getNormalizedColumnHeader(),
                        cachedClinicalAttribute.getDisplayName(),
                        cachedClinicalAttribute.getDescription(),
                        cachedClinicalAttribute.getDatatype(),
                        cachedClinicalAttribute.getAttributeType(),
                        cachedClinicalAttribute.getPriority());
                    resetClinicalAttribute.setPriority("0");
                    clinicalAttributes.add(resetClinicalAttribute);
                }
            }
        }
        return clinicalAttributes;
    }

    @Override
    public ClinicalAttributeMetadata getMetadataByNormalizedColumnHeader(String studyId, String normalizedColumnHeader)
        throws ClinicalAttributeNotFoundException {
        Map<String, ClinicalAttributeMetadata> clinicalAttributeCache = clinicalAttributesCache.getClinicalAttributeMetadata();
        Map<String, Map<String, ClinicalAttributeMetadata>> overridesCache = clinicalAttributesCache.getClinicalAttributeMetadataOverrides();
        if (overridesCache.containsKey(studyId) && overridesCache.get(studyId).containsKey(normalizedColumnHeader.toUpperCase())) {
            return getMetadataByNormalizedColumnHeader(overridesCache.get(studyId), studyId, normalizedColumnHeader);
        }
        return getMetadataByNormalizedColumnHeader(clinicalAttributeCache, studyId, normalizedColumnHeader);
    }

    private ClinicalAttributeMetadata getMetadataByNormalizedColumnHeader(Map<String, ClinicalAttributeMetadata> clinicalAttributeCache, String studyId, String normalizedColumnHeader)
        throws ClinicalAttributeNotFoundException {
        if (clinicalAttributeCache.containsKey(normalizedColumnHeader.toUpperCase())) {
            return clinicalAttributeCache.get(normalizedColumnHeader.toUpperCase());
        }
        throw new ClinicalAttributeNotFoundException(normalizedColumnHeader);
    }

    @Override
    public List<String> getStudyIdsWithOverrides() {
        List<String> studyIdsWithOverrides = new ArrayList<String>();
        Map<String, Map<String, ClinicalAttributeMetadata>> overridesCache = clinicalAttributesCache.getClinicalAttributeMetadataOverrides();
        for (String studyId : overridesCache.keySet()) {
            studyIdsWithOverrides.add(studyId);
        }
        return studyIdsWithOverrides;
    }
}
