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
import org.cbioportal.cdd.model.CancerStudy;
import org.cbioportal.cdd.service.ClinicalDataDictionaryService;
import org.cbioportal.cdd.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.cdd.service.exception.ClinicalMetadataSourceUnresponsiveException;
import org.cbioportal.cdd.service.exception.CancerStudyNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Note this class relies on the ClinicalAttributeMetadataCache class which will frequently generate a
 * new cache of clinical attributes.  Each method in this class should only get the cache once
 * and should not attempt to make any modifications to the cache.
 *
 * @author Robert Sheridan, Avery Wang, Manda Wilson
 */
@Service
public class ClinicalDataDictionaryServiceImpl implements ClinicalDataDictionaryService {

    @Autowired
    private ClinicalAttributeMetadataCache clinicalAttributesCache; 

    private static final Logger logger = LoggerFactory.getLogger(ClinicalDataDictionaryServiceImpl.class);

    @Override
    public List<ClinicalAttributeMetadata> getClinicalAttributeMetadata(String cancerStudy)
        throws ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        assertCacheIsValid();
        assertCancerStudyIsValid(cancerStudy);
        List<String> columnHeaders = new ArrayList<>(clinicalAttributesCache.getClinicalAttributeMetadata().keySet());
        List<ClinicalAttributeMetadata> clinicalAttributes = getMetadataByColumnHeaders(cancerStudy, columnHeaders);
        return clinicalAttributes; 
    }

    @Override
    public List<ClinicalAttributeMetadata> getMetadataByColumnHeaders(String cancerStudy, List<String> columnHeaders)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        assertCacheIsValid();
        assertCancerStudyIsValid(cancerStudy);
        List<ClinicalAttributeMetadata> clinicalAttributes = new ArrayList<ClinicalAttributeMetadata>();
        Map<String, ClinicalAttributeMetadata> defaultClinicalAttributeCache = clinicalAttributesCache.getClinicalAttributeMetadata();
        Map<String, Map<String, ClinicalAttributeMetadata>> overridesCache = clinicalAttributesCache.getClinicalAttributeMetadataOverrides();
        Map<String, ClinicalAttributeMetadata> overrideClinicalAttributeCache = null;
        if (cancerStudy != null) { // cancer study has already been validated
            overrideClinicalAttributeCache = overridesCache.get(cancerStudy);
        }
        for (String columnHeader : columnHeaders) {
            // first check in our overrideClinicalAttributeCache for column header
            if (cancerStudy != null && overrideClinicalAttributeCache.containsKey(columnHeader.toUpperCase())) {
                clinicalAttributes.add(getMetadataByColumnHeader(overrideClinicalAttributeCache, columnHeader));
            } else {
                // otherwise use the defaultClinicalAttributeCache
                ClinicalAttributeMetadata cachedClinicalAttribute = getMetadataByColumnHeader(defaultClinicalAttributeCache, columnHeader);
                if (cancerStudy == null || !cancerStudy.equals("mskimpact")) {
                    clinicalAttributes.add(cachedClinicalAttribute);
                } else {
                    // when cancerStudy is 'mskimpact' - copy created so modification (i.e reset priority to 0) does not get applied to object stored in cache
                    ClinicalAttributeMetadata resetClinicalAttribute = new ClinicalAttributeMetadata(cachedClinicalAttribute.getColumnHeader(),
                        cachedClinicalAttribute.getDisplayName(),
                        cachedClinicalAttribute.getDescription(),
                        cachedClinicalAttribute.getDatatype(),
                        cachedClinicalAttribute.getAttributeType(),
                        "0");
                    clinicalAttributes.add(resetClinicalAttribute);
                }
            }
        }
        return clinicalAttributes;
    }

    @Override
    public ClinicalAttributeMetadata getMetadataByColumnHeader(String cancerStudy, String columnHeader)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        assertCacheIsValid();
        assertCancerStudyIsValid(cancerStudy);
        Map<String, ClinicalAttributeMetadata> clinicalAttributeCache = clinicalAttributesCache.getClinicalAttributeMetadata();
        Map<String, Map<String, ClinicalAttributeMetadata>> overridesCache = clinicalAttributesCache.getClinicalAttributeMetadataOverrides();
        if (cancerStudy != null && overridesCache.get(cancerStudy).containsKey(columnHeader.toUpperCase())) {
            return getMetadataByColumnHeader(overridesCache.get(cancerStudy), columnHeader);
        }
        return getMetadataByColumnHeader(clinicalAttributeCache, columnHeader);
    }

    private ClinicalAttributeMetadata getMetadataByColumnHeader(Map<String, ClinicalAttributeMetadata> clinicalAttributeCache, String columnHeader)
        throws ClinicalAttributeNotFoundException {
        if (clinicalAttributeCache.containsKey(columnHeader.toUpperCase())) {
            return clinicalAttributeCache.get(columnHeader.toUpperCase());
        }
        throw new ClinicalAttributeNotFoundException(columnHeader);
    }

    @Override
    public List<CancerStudy> getCancerStudies() throws ClinicalMetadataSourceUnresponsiveException {
        assertCacheIsValid();
        List<CancerStudy> cancerStudies = new ArrayList<CancerStudy>();
        Map<String, Map<String, ClinicalAttributeMetadata>> overridesCache = clinicalAttributesCache.getClinicalAttributeMetadataOverrides();
        for (String cancerStudyName : overridesCache.keySet()) {
            cancerStudies.add(new CancerStudy(cancerStudyName));
        }
        return cancerStudies;
    }

    private void assertCacheIsValid() throws ClinicalMetadataSourceUnresponsiveException {
        if (clinicalAttributesCache.getClinicalAttributeMetadata() == null || clinicalAttributesCache.getClinicalAttributeMetadataOverrides() == null) {
            logger.debug("assertCacheIsValid() -- cache is invalid");
            throw new ClinicalMetadataSourceUnresponsiveException("Attempted to access cache while ClinicalAttributeMetadata cache or ClinicalAttributeMetadataOverrides cache was invalid");
        }
        logger.debug("assertCacheIsValid() -- cache is valid");
    }

    private void assertCancerStudyIsValid(String cancerStudy) throws CancerStudyNotFoundException {
        if (cancerStudy != null && !clinicalAttributesCache.getClinicalAttributeMetadataOverrides().containsKey(cancerStudy)) {
            logger.debug("assertCancerStudyIsValid() -- cancer study '" + cancerStudy + "' is invalid");
            throw new CancerStudyNotFoundException(cancerStudy);
        }
        // null is valid
        logger.debug("assertCancerStudyIsValid() -- cancer study '" + cancerStudy + "' is valid");
    }
}
