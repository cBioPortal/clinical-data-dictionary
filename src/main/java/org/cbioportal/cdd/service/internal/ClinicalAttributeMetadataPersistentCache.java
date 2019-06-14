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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;

import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.repository.ClinicalAttributeMetadataRepository;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Robert Sheridan, Avery Wang, Manda Wilson
 */
@Component
public class ClinicalAttributeMetadataPersistentCache {

    private final static Logger logger = LoggerFactory.getLogger(ClinicalAttributeMetadataPersistentCache.class);

    @Autowired
    private ClinicalAttributeMetadataRepository clinicalAttributesRepository;

    public static final String CLINICAL_ATTRIBUTES_METADATA_CACHE_KEY = "CLINICAL_ATTRIBUTES_METADATA_CACHE_KEY";
    public static final String OVERRIDES_CACHE_KEY = "OVERRIDES_CACHE_KEY";

    @Cacheable(value = "clinicalAttributeMetadataEHCache", key = "#root.target.CLINICAL_ATTRIBUTES_METADATA_CACHE_KEY", unless = "#result==null")
    public ArrayList<ClinicalAttributeMetadata> getClinicalAttributeMetadataFromPersistentCache() {
        return clinicalAttributesRepository.getClinicalAttributeMetadata();
    }

    @CachePut(value = "clinicalAttributeMetadataEHCache", key = "#root.target.CLINICAL_ATTRIBUTES_METADATA_CACHE_KEY", unless = "#result==null")
    public ArrayList<ClinicalAttributeMetadata> updateClinicalAttributeMetadataInPersistentCache() {
        logger.info("updating EHCache with updated clinical attribute metadata from TopBraid");
        return  clinicalAttributesRepository.getClinicalAttributeMetadata();
    }

    @Cacheable(value = "clinicalAttributeMetadataOverridesEHCache", key = "#root.target.OVERRIDES_CACHE_KEY", unless = "#result==null")
    public Map<String, ArrayList<ClinicalAttributeMetadata>> getClinicalAttributeMetadataOverridesFromPersistentCache() {
        return clinicalAttributesRepository.getClinicalAttributeMetadataOverrides();
    }

    @CachePut(value = "clinicalAttributeMetadataOverridesEHCache", key = "#root.target.OVERRIDES_CACHE_KEY", unless = "#result==null")
    public Map<String, ArrayList<ClinicalAttributeMetadata>> updateClinicalAttributeMetadataOverridesInPersistentCache() {
        logger.info("updating EHCache with updated overrides from TopBraid");
        return clinicalAttributesRepository.getClinicalAttributeMetadataOverrides();
    }
}
