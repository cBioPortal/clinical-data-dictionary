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

package org.mskcc.clinical_attributes.service.internal;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;

import org.mskcc.clinical_attributes.model.ClinicalAttribute;
import org.mskcc.clinical_attributes.service.ClinicalAttributesService;
import org.mskcc.clinical_attributes.service.exception.ClinicalAttributeNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Note this class relies on the ClinicalAttributesCache class which will frequently generate a
 * new cache of clinical attributes.  Each method in this class should only get the cache once
 * and should not attempt to make any modifications to the cache.
 *
 * @author Robert Sheridan, Manda Wilson
 */
@Service
public class ClinicalAttributesServiceImpl implements ClinicalAttributesService {

    @Autowired
    private ClinicalAttributesCache clinicalAttributesCache; 

    private static final Logger logger = LoggerFactory.getLogger(ClinicalAttributesServiceImpl.class);

    @Override
    public List<ClinicalAttribute> getClinicalAttributes(String studyId) {
        return new ArrayList(clinicalAttributesCache.getClinicalAttributes().values());
    }

    @Override
    public List<ClinicalAttribute> getMetadataByNormalizedColumnHeaders(String studyId, List<String> normalizedColumnHeaders)
        throws ClinicalAttributeNotFoundException {
        List<ClinicalAttribute> clinicalAttributes = new ArrayList<ClinicalAttribute>();
        Map<String, ClinicalAttribute> clinicalAttributeCache = clinicalAttributesCache.getClinicalAttributes();
        for (String normalizedColumnHeader : normalizedColumnHeaders) {
            clinicalAttributes.add(getMetadataByNormalizedColumnHeader(clinicalAttributeCache, studyId, normalizedColumnHeader));
        }
        return clinicalAttributes;
    }

    @Override
    public ClinicalAttribute getMetadataByNormalizedColumnHeader(String studyId, String normalizedColumnHeader)
        throws ClinicalAttributeNotFoundException {
        return getMetadataByNormalizedColumnHeader(clinicalAttributesCache.getClinicalAttributes(), studyId, normalizedColumnHeader);
    }

    private ClinicalAttribute getMetadataByNormalizedColumnHeader(Map<String, ClinicalAttribute> clinicalAttributeCache, String studyId, String normalizedColumnHeader)
        throws ClinicalAttributeNotFoundException {
        if (clinicalAttributeCache.containsKey(normalizedColumnHeader.toUpperCase())) {
            return clinicalAttributeCache.get(normalizedColumnHeader.toUpperCase());
        }
        throw new ClinicalAttributeNotFoundException(normalizedColumnHeader);
    }

}
