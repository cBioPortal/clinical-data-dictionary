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
import org.cbioportal.cdd.model.OverridePolicy;
import org.cbioportal.cdd.service.ClinicalDataDictionaryService;
import org.cbioportal.cdd.service.exception.ClinicalAttributeNotFoundException;
import org.cbioportal.cdd.service.exception.ClinicalMetadataSourceUnresponsiveException;
import org.cbioportal.cdd.service.exception.OverridePolicyNotFoundException;

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
    public List<ClinicalAttributeMetadata> getClinicalAttributeMetadata(String overridePolicy)
        throws ClinicalMetadataSourceUnresponsiveException, OverridePolicyNotFoundException {
        assertCacheIsValid();
        assertOverridePolicyIsValid(overridePolicy);
        List<String> columnHeaders = new ArrayList<>(clinicalAttributesCache.getClinicalAttributeMetadata().keySet());
        List<ClinicalAttributeMetadata> clinicalAttributes = getMetadataByColumnHeaders(overridePolicy, columnHeaders);
        return clinicalAttributes; 
    }

    @Override
    public List<ClinicalAttributeMetadata> getMetadataByColumnHeaders(String overridePolicy, List<String> columnHeaders)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException, OverridePolicyNotFoundException {
        assertCacheIsValid();
        assertOverridePolicyIsValid(overridePolicy);
        List<ClinicalAttributeMetadata> clinicalAttributes = new ArrayList<ClinicalAttributeMetadata>();
        Map<String, ClinicalAttributeMetadata> defaultClinicalAttributeCache = clinicalAttributesCache.getClinicalAttributeMetadata();
        Map<String, Map<String, ClinicalAttributeMetadata>> overridesCache = clinicalAttributesCache.getClinicalAttributeMetadataOverrides();
        Map<String, ClinicalAttributeMetadata> overrideClinicalAttributeCache = null;
        if (overridePolicy != null) { // override policy has already been validated
            overrideClinicalAttributeCache = overridesCache.get(overridePolicy);
        }
        for (String columnHeader : columnHeaders) {
            // first check in our overrideClinicalAttributeCache for column header
            if (overridePolicy != null && overrideClinicalAttributeCache.containsKey(columnHeader.toUpperCase())) {
                clinicalAttributes.add(getMetadataByColumnHeader(overrideClinicalAttributeCache, columnHeader));
            } else {
                // otherwise use the defaultClinicalAttributeCache
                ClinicalAttributeMetadata cachedClinicalAttribute = getMetadataByColumnHeader(defaultClinicalAttributeCache, columnHeader);
                if (overridePolicy == null || !overridePolicy.equals("mskimpact")) {
                    clinicalAttributes.add(cachedClinicalAttribute);
                } else {
                    // when overridePolicy is 'mskimpact' - copy created so modification (i.e reset priority to 0) does not get applied to object stored in cache
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
    public ClinicalAttributeMetadata getMetadataByColumnHeader(String overridePolicy, String columnHeader)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException, OverridePolicyNotFoundException {
        assertCacheIsValid();
        assertOverridePolicyIsValid(overridePolicy);
        Map<String, ClinicalAttributeMetadata> clinicalAttributeCache = clinicalAttributesCache.getClinicalAttributeMetadata();
        Map<String, Map<String, ClinicalAttributeMetadata>> overridesCache = clinicalAttributesCache.getClinicalAttributeMetadataOverrides();
        if (overridePolicy != null && overridesCache.get(overridePolicy).containsKey(columnHeader.toUpperCase())) {
            return getMetadataByColumnHeader(overridesCache.get(overridePolicy), columnHeader);
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
    public List<OverridePolicy> getOverridePolicies() throws ClinicalMetadataSourceUnresponsiveException {
        assertCacheIsValid();
        List<OverridePolicy> overridePolicies = new ArrayList<OverridePolicy>();
        Map<String, Map<String, ClinicalAttributeMetadata>> overridesCache = clinicalAttributesCache.getClinicalAttributeMetadataOverrides();
        for (String overridePolicyName : overridesCache.keySet()) {
            overridePolicies.add(new OverridePolicy(overridePolicyName));
        }
        return overridePolicies;
    }

    private void assertCacheIsValid() throws ClinicalMetadataSourceUnresponsiveException {
        if (clinicalAttributesCache.getClinicalAttributeMetadata() == null || clinicalAttributesCache.getClinicalAttributeMetadataOverrides() == null) {
            logger.debug("assertCacheIsValid() -- cache is invalid");
            throw new ClinicalMetadataSourceUnresponsiveException("Attempted to access cache while ClinicalAttributeMetadata cache or ClinicalAttributeMetadataOverrides cache was invalid");
        }
        logger.debug("assertCacheIsValid() -- cache is valid");
    }

    private void assertOverridePolicyIsValid(String overridePolicy) throws OverridePolicyNotFoundException {
        if (overridePolicy != null && !clinicalAttributesCache.getClinicalAttributeMetadataOverrides().containsKey(overridePolicy)) {
            logger.debug("assertOverridePolicyIsValid() -- override policy '" + overridePolicy + "' is invalid");
            throw new OverridePolicyNotFoundException(overridePolicy);
        }
        // null is valid
        logger.debug("assertOverridePolicyIsValid() -- override policy '" + overridePolicy + "' is valid");
    }
}
