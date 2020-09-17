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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;
import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.service.exception.FailedCacheRefreshException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Avery Wang
 */
@Component
public class LevenshteinDistanceCache {

    @Autowired
    private ClinicalAttributeMetadataCache clinicalAttributesCache;

    private Map<ClinicalAttributeMetadata, Map<String, Integer>> levenshteinDistanceCache = new HashMap<ClinicalAttributeMetadata, Map<String, Integer>>();

    private static final Logger logger = LoggerFactory.getLogger(LevenshteinDistanceCache.class);

    public boolean containsClinicalAttribute(ClinicalAttributeMetadata clinicalAttributeMetadata) {
        return levenshteinDistanceCache.containsKey(clinicalAttributeMetadata);
    }

    public void addClinicalAttribute(ClinicalAttributeMetadata clinicalAttributeMetadata) {
        this.levenshteinDistanceCache.put(clinicalAttributeMetadata, new HashMap<String, Integer>());
    }

    public boolean containsClinicalAttributeToSearchTermMapping(ClinicalAttributeMetadata clinicalAttributeMetadata, String searchTerm) {
        return levenshteinDistanceCache.get(clinicalAttributeMetadata).containsKey(searchTerm);
    }

    public void addClinicalAttributeToSearchTermMapping(ClinicalAttributeMetadata clinicalAttributeMetadata, String searchTerm) {
        this.levenshteinDistanceCache.get(clinicalAttributeMetadata).put(searchTerm, clinicalAttributeMetadata.levenshteinDistanceFromSearchTerm(searchTerm));
    }

    public Integer getLevenshteinDistanceForMapping(ClinicalAttributeMetadata clinicalAttributeMetadata, String searchTerm) {
        return levenshteinDistanceCache.get(clinicalAttributeMetadata).get(searchTerm);
    }

    public void resetCache() {
        logger.info("resetCache(): recalculating Levenshtein Distances with refreshed CDD attributes");
        Map<String, ClinicalAttributeMetadata> defaultClinicalAttributeCache = clinicalAttributesCache.getClinicalAttributeMetadata();
        Map<ClinicalAttributeMetadata, List<String>> searchTermMappingsToRemove = new HashMap<ClinicalAttributeMetadata, List<String>>();
        List<ClinicalAttributeMetadata> clinicalAttributeMetadataToRemove = new ArrayList<ClinicalAttributeMetadata>();

        for (ClinicalAttributeMetadata cachedClinicalAttributeMetadata : levenshteinDistanceCache.keySet()) {
            ClinicalAttributeMetadata currentClinicalAttributeMetadata;
            // during cache refresh - remove levenshtein distance mappings if the clinical attribute has been removed from CDD
            if (!defaultClinicalAttributeCache.containsKey(cachedClinicalAttributeMetadata.getColumnHeader())) {
                clinicalAttributeMetadataToRemove.add(cachedClinicalAttributeMetadata);
                continue;
            } else {
                currentClinicalAttributeMetadata = defaultClinicalAttributeCache.get(cachedClinicalAttributeMetadata.getColumnHeader());
            }
            // recalculate and set levenshtein distances to new values (calculated from updated displays, descriptions, etc...)
            // remove search term mappings which are no longer contained in the display, description, etc...
            for (String searchTerm : levenshteinDistanceCache.get(cachedClinicalAttributeMetadata).keySet()) {
                if (!currentClinicalAttributeMetadata.containsSearchTerm(searchTerm)) {
                    if(!searchTermMappingsToRemove.containsKey(cachedClinicalAttributeMetadata)) {
                        searchTermMappingsToRemove.put(cachedClinicalAttributeMetadata, new ArrayList<String>());
                    }
                    searchTermMappingsToRemove.get(cachedClinicalAttributeMetadata).add(searchTerm);
                    continue;
                }
                Integer levenshteinDistance = currentClinicalAttributeMetadata.levenshteinDistanceFromSearchTerm(searchTerm);
                levenshteinDistanceCache.get(cachedClinicalAttributeMetadata).put(searchTerm, levenshteinDistance);
            }
        }
        for (ClinicalAttributeMetadata clinicalAttributeMetadata : clinicalAttributeMetadataToRemove) {
            levenshteinDistanceCache.remove(clinicalAttributeMetadata);
        }
        for (ClinicalAttributeMetadata clinicalAttributeMetadata : searchTermMappingsToRemove.keySet()) {
            for (String searchTerm : searchTermMappingsToRemove.get(clinicalAttributeMetadata)) {
                levenshteinDistanceCache.get(clinicalAttributeMetadata).remove(searchTerm);
            }
        }
    }
}
