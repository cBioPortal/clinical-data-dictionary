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
import org.mskcc.clinical_attributes.service.ClinicalAttributesService;
import org.mskcc.clinical_attributes.service.exception.ClinicalAttributeNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Manda Wilson 
 */
@EnableScheduling
@Service
public class ClinicalAttributesServiceImpl implements ClinicalAttributesService {

    private static HashMap<String, ClinicalAttribute> clinicalAttributeCashe = new HashMap<String, ClinicalAttribute>();

    private static final Logger logger = LoggerFactory.getLogger(ClinicalAttributesServiceImpl.class);

    @Autowired
    private ClinicalAttributesRepository clinicalAttributesRepository;

    @Override
    public List<ClinicalAttribute> getClinicalAttributes() {
        return new ArrayList(clinicalAttributeCashe.values());
    }

    @Override
    public List<ClinicalAttribute> getMetadataByNormalizedColumnHeaders(List<String> normalizedColumnHeaders)
        throws ClinicalAttributeNotFoundException {
        List<ClinicalAttribute> clinicalAttributes = new ArrayList<ClinicalAttribute>();
        for (String normalizedColumnHeader : normalizedColumnHeaders) {
            clinicalAttributes.add(getMetadataByNormalizedColumnHeader(normalizedColumnHeader)); 
        }
        return clinicalAttributes;
    }

    @Override
    public ClinicalAttribute getMetadataByNormalizedColumnHeader(String normalizedColumnHeader) 
        throws ClinicalAttributeNotFoundException {
        if (clinicalAttributeCashe.containsKey(normalizedColumnHeader.toUpperCase())) {
            return clinicalAttributeCashe.get(normalizedColumnHeader.toUpperCase());
        }
        throw new ClinicalAttributeNotFoundException(normalizedColumnHeader);
    }

    @PostConstruct // call when constructed
    @Scheduled(cron="0 */5 * * * *") // call every 5 minutes (TODO change this?)
    private void resetCache() {
        logger.info("resetCache(): refilling clinical attribute cache");
        List<ClinicalAttribute> latestClinicalAttributes = clinicalAttributesRepository.getClinicalAttribute(); 
        if (latestClinicalAttributes.size() > 0) {
            // TODO delete values not used anymore
            for (ClinicalAttribute clinicalAttribute : latestClinicalAttributes) {
                clinicalAttributeCashe.put(clinicalAttribute.getNormalizedColumnHeader(), clinicalAttribute);
            }
            logger.info("resetCache(): refillled cache with " + latestClinicalAttributes.size() + " clinical attributes");
        } else {
            // what if cache never gets updated because we break something?
            logger.error("resetCache(): failed to pull clinical attributes from repository, not updating cache");
        }
    }
}
