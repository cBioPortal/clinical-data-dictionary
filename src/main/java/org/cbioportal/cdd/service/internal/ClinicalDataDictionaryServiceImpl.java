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

    @Autowired
    private LevenshteinDistanceCache levenshteinDistanceCache;

    private static final Logger logger = LoggerFactory.getLogger(ClinicalDataDictionaryServiceImpl.class);

    private static final Set<String> CANCER_STUDIES_WITH_ALTERED_DEFAULT_METADATA;
    static {
        Set<String> cancerStudySet = new HashSet<>();
        cancerStudySet.add("mskimpact");
        cancerStudySet.add("sclc_mskimpact_2017");
        cancerStudySet.add("mskarcher");
        cancerStudySet.add("mskraindance");
        cancerStudySet.add("mskimpact_heme");
        cancerStudySet.add("msk_solid_heme");
        CANCER_STUDIES_WITH_ALTERED_DEFAULT_METADATA = Collections.unmodifiableSet(cancerStudySet);
    }

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
        List<String> invalidClinicalAttributes = new ArrayList<String>();
        for (String columnHeader : columnHeaders) {
            try {
                ClinicalAttributeMetadata defaultClinicalAttributeMetadataForColumnHeader = getMetadataByColumnHeader(defaultClinicalAttributeCache, columnHeader);
                // if there is no cancerStudyArgument, simply add the default metaData for each columnHeader
                if (cancerStudy == null) {
                    clinicalAttributes.add(defaultClinicalAttributeMetadataForColumnHeader);
                    continue;
                }
                // if there is an explicit override for a columnHeader in this study, add the explicit override
                if (overrideClinicalAttributeCache.containsKey(columnHeader.toUpperCase())) {
                    clinicalAttributes.add(getMetadataByColumnHeader(overrideClinicalAttributeCache, columnHeader));
                    continue;
                }
                // without an explicit override for the study, use the default metadata or a modified version for certain special studies
                ClinicalAttributeMetadata clinicalAttributeMetadataForColumnHeader = defaultClinicalAttributeMetadataForColumnHeader;
                if (CANCER_STUDIES_WITH_ALTERED_DEFAULT_METADATA.contains(cancerStudy)) {
                    clinicalAttributeMetadataForColumnHeader = getAlteredDefaultMetadataByColumnHeader(defaultClinicalAttributeCache, columnHeader);
                }
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
        assertCacheIsValid();
        Map<ClinicalAttributeMetadata, Integer> clinicalAttributesToLevenshteinDistance = new HashMap<ClinicalAttributeMetadata, Integer>();
        List<ClinicalAttributeMetadata> defaultClinicalAttributeCache = new ArrayList<ClinicalAttributeMetadata>(clinicalAttributesCache.getClinicalAttributeMetadata().values());

        // check each attribute for match - skip if doesn't match specified attribute type OR inclusive search that doesn't have all search terms present
        for (ClinicalAttributeMetadata clinicalAttributeMetadata : defaultClinicalAttributeCache) {
            if (!clinicalAttributeMetadata.matchesAttributeType(attributeType)) {
                continue;
            }
            if (inclusiveSearch && !clinicalAttributeMetadata.containsAllSearchTerms(searchTerms)) {
                continue;
            }
            mapMinimumLevenshteinDistanceFromClinicalAttributeMetadataToSearchTerms(clinicalAttributesToLevenshteinDistance, searchTerms, clinicalAttributeMetadata);
        }
        List<ClinicalAttributeMetadata> clinicalAttributes = new ArrayList<ClinicalAttributeMetadata>(clinicalAttributesToLevenshteinDistance.keySet());
        Collections.sort(clinicalAttributes, (a, b) -> {
            return clinicalAttributesToLevenshteinDistance.get(a).compareTo(clinicalAttributesToLevenshteinDistance.get(b));
        });
        if (clinicalAttributes.size() == 0) {
            throw new ClinicalAttributeNotFoundException(String.join(", ", searchTerms));
        }
        return clinicalAttributes;
    }

    private void mapMinimumLevenshteinDistanceFromClinicalAttributeMetadataToSearchTerms(Map<ClinicalAttributeMetadata, Integer> clinicalAttributesToLevenshteinDistance, List<String> searchTerms, ClinicalAttributeMetadata clinicalAttributeMetadata) {
        Integer levenshteinDistance;
        // matching clinical attributes added to map with shortest levenshtein distance
        for (String searchTerm : searchTerms) {
            if (clinicalAttributeMetadata.containsSearchTerm(searchTerm)) {
                // attempt to get levenshtein distance from cache - if not present, add to cache
                if (!levenshteinDistanceCache.containsClinicalAttribute(clinicalAttributeMetadata)) {
                    levenshteinDistanceCache.addClinicalAttribute(clinicalAttributeMetadata);
                }
                if (!levenshteinDistanceCache.containsClinicalAttributeToSearchTermMapping(clinicalAttributeMetadata, searchTerm)) {
                    levenshteinDistanceCache.addClinicalAttributeToSearchTermMapping(clinicalAttributeMetadata, searchTerm);
                }
                levenshteinDistance = levenshteinDistanceCache.getLevenshteinDistanceForMapping(clinicalAttributeMetadata, searchTerm);
                if (!clinicalAttributesToLevenshteinDistance.containsKey(clinicalAttributeMetadata)) {
                    clinicalAttributesToLevenshteinDistance.put(clinicalAttributeMetadata, levenshteinDistance);
                } else {
                    clinicalAttributesToLevenshteinDistance.put(clinicalAttributeMetadata, Math.min(clinicalAttributesToLevenshteinDistance.get(clinicalAttributeMetadata), levenshteinDistance));
                }
            }
        }
        if (clinicalAttributeMetadata.containsAllSearchTerms(searchTerms)) {
            levenshteinDistance = clinicalAttributeMetadata.levenshteinDistanceFromSearchTerm(String.join(" ", searchTerms));
            clinicalAttributesToLevenshteinDistance.put(clinicalAttributeMetadata, Math.min(clinicalAttributesToLevenshteinDistance.get(clinicalAttributeMetadata), levenshteinDistance));
        }
    }

    @Override
    public ClinicalAttributeMetadata getMetadataByColumnHeader(String cancerStudy, String columnHeader)
        throws ClinicalAttributeNotFoundException, ClinicalMetadataSourceUnresponsiveException, CancerStudyNotFoundException {
        List<String> columnHeaders = Collections.singletonList(columnHeader);
        List<ClinicalAttributeMetadata> clinicalAttributeMetadataList = getMetadataByColumnHeaders(cancerStudy, columnHeaders);
        return clinicalAttributeMetadataList.get(0);
    }

    private ClinicalAttributeMetadata getMetadataByColumnHeader(Map<String, ClinicalAttributeMetadata> clinicalAttributeCache, String columnHeader)
        throws ClinicalAttributeNotFoundException {
        if (clinicalAttributeCache.containsKey(columnHeader.toUpperCase())) {
            return clinicalAttributeCache.get(columnHeader.toUpperCase());
        }
        throw new ClinicalAttributeNotFoundException(columnHeader);
    }

    private ClinicalAttributeMetadata getAlteredDefaultMetadataByColumnHeader(Map<String, ClinicalAttributeMetadata> clinicalAttributeCache, String columnHeader)
        throws ClinicalAttributeNotFoundException {
            ClinicalAttributeMetadata defaultClinicalAttribute = getMetadataByColumnHeader(clinicalAttributeCache, columnHeader);
            ClinicalAttributeMetadata alteredClinicalAttribute = new ClinicalAttributeMetadata(defaultClinicalAttribute);
            alteredClinicalAttribute.setPriority("0");
            return alteredClinicalAttribute;
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

    @Override
    public Map<String, String> forceResetCache() throws FailedCacheRefreshException {
        clinicalAttributesCache.resetCache();
        levenshteinDistanceCache.resetCache();
        return Collections.singletonMap("response", "Success!");
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
