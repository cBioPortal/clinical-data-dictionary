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

import javax.annotation.PostConstruct;

import org.mskcc.clinical_attributes.model.ClinicalAttribute;
import org.mskcc.clinical_attributes.repository.ClinicalAttributesRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Robert Sheridan, Manda Wilson
 */
@Component
@EnableScheduling
public class ClinicalAttributesCache {

    private static HashMap<String, ClinicalAttribute> clinicalAttributeCache = new HashMap<String, ClinicalAttribute>();

    private static final Logger logger = LoggerFactory.getLogger(ClinicalAttributesCache.class);

    @Autowired
    private ClinicalAttributesRepository clinicalAttributesRepository;

    public Map<String, ClinicalAttribute> getClinicalAttributes() {
        return Collections.unmodifiableMap(clinicalAttributeCache);
    }

    @PostConstruct // call when constructed
    @Scheduled(cron="0 */5 * * * *") // call every 5 minutes
    private void resetCache() {
        // TODO make sure we don't have two scheduled calls run simultaneously
        logger.info("resetCache(): refilling clinical attribute cache");
        List<ClinicalAttribute> latestClinicalAttributes = clinicalAttributesRepository.getClinicalAttribute();
        HashMap<String, ClinicalAttribute> latestClinicalAttributesCache = new HashMap<String, ClinicalAttribute>();
        if (latestClinicalAttributes.size() > 0) {
            for (ClinicalAttribute clinicalAttribute : latestClinicalAttributes) {
                latestClinicalAttributesCache.put(clinicalAttribute.getNormalizedColumnHeader(), clinicalAttribute);
            }
            clinicalAttributeCache = latestClinicalAttributesCache;
            logger.info("resetCache(): refillled cache with " + latestClinicalAttributes.size() + " clinical attributes");
        } else {
            // what if cache never gets updated because we break something?
            logger.error("resetCache(): failed to pull clinical attributes from repository, not updating cache");
        }
    }
}
