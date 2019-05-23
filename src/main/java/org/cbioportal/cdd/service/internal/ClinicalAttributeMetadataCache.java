/*
 * Copyright (c) 2018-2019 Memorial Sloan-Kettering Cancer Center.
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.impl.client.*;

import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.repository.ClinicalAttributeMetadataRepository;
import org.cbioportal.cdd.service.exception.FailedCacheRefreshException;

import com.google.common.base.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author Robert Sheridan, Avery Wang, Manda Wilson
 */
@Component
@EnableScheduling
public class ClinicalAttributeMetadataCache {

    @Value("${slack.url}")
    private String slackURL;

    // if clinicalAttributeCache is null it means we could not populate it, there was an error
    private static HashMap<String, ClinicalAttributeMetadata> clinicalAttributeCache;
    // if overridesCache is null it means we could not populate it, there was an error
    private static HashMap<String, Map<String, ClinicalAttributeMetadata>> overridesCache;
    private static Date dateOfLastCacheRefresh = new Date();

    public static final Integer MAXIMUM_CACHE_AGE_IN_DAYS = 3;
    private static final Logger logger = LoggerFactory.getLogger(ClinicalAttributeMetadataCache.class);

    @Autowired
    private ClinicalAttributeMetadataPersistentCache clinicalAttributeMetadataPersistentCache;

    public Date getDateOfLastCacheRefresh() {
        return dateOfLastCacheRefresh;
    }

    public void setDateOfLastCacheRefresh(Date date) {
        this.dateOfLastCacheRefresh = date;
    }

    public Map<String, ClinicalAttributeMetadata> getClinicalAttributeMetadata() {
        if (clinicalAttributeCache != null) {
            return Collections.unmodifiableMap(clinicalAttributeCache);
        }
        return null;
    }

    public Map<String, Map<String, ClinicalAttributeMetadata>> getClinicalAttributeMetadataOverrides() {
        if (overridesCache != null) {
            return Collections.unmodifiableMap(overridesCache);
        }
        return null;
    }

    private void sendStaleCacheSlackNotification() {
        String payload = "payload={\"channel\": \"#msk-pipeline-logs\", \"username\": \"cbioportal_importer\", \"text\": \"*URGENT: CDD Error* - an attempt to refresh an outdated or null cache failed.\", \"icon_emoji\": \":rotating_light:\"}";
        StringEntity entity = new StringEntity(payload, ContentType.APPLICATION_FORM_URLENCODED);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(slackURL);
        request.setEntity(entity);
        HttpResponse response = null;
        try {
            response = httpClient.execute(request);
        } catch (Exception e) {
            logger.error("failed to send slack notification -- cache is outdated and failed to refresh");
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron="0 */10 * * * *") // call every 10 minutes
    private void validateAndResetCache() {
        if (cacheIsStale() || clinicalAttributeCache == null || overridesCache == null) {
            try {
                resetCache();
            } catch (FailedCacheRefreshException e) {
                sendStaleCacheSlackNotification();
            }
        }
    }

    /**
    * This method does not need to be called, it will automatically be called by validateAndResetCache().
    * It is a public method so that it can be easily tested.
    */
    public void resetCache() {
        logger.info("resetCache(): refilling clinical attribute cache");

        Date dateOfCurrentCacheRefresh = new Date();
        ArrayList<ClinicalAttributeMetadata> latestClinicalAttributeMetadata = null;
        // latestOverrides is a map of study-id to list of overridden ClinicalAttributeMetadata objects
        Map<String, ArrayList<ClinicalAttributeMetadata>> latestOverrides = null;

        // attempt to refresh ehcache stores seperately and store success status
        boolean failedClinicalAttributeMetadataCacheRefresh = false;
        boolean failedOverridesCacheRefresh = false;
        try {
            clinicalAttributeMetadataPersistentCache.updateClinicalAttributeMetadataInPersistentCache();
        } catch (RuntimeException e) {
            logger.error("resetCache(): failed to pull clinical attributes from repository. Error message returned: " + e.getMessage());
            failedClinicalAttributeMetadataCacheRefresh = true;
        }

        try {
            clinicalAttributeMetadataPersistentCache.updateClinicalAttributeMetadataOverridesInPersistentCache();
        } catch (RuntimeException e) {
            logger.error("resetCache(): failed to pull overrides from repository. Error message returned: " + e.getMessage());
            failedOverridesCacheRefresh = true;
        }

        // regardless of whether ehcache was updated with new data - use that data to populate modeled object caches
        // ensures app starts up (between tomcat restarts) if TopBraid is down
        logger.info("Loading modeled object cache from EHCache");
        try {
            // this will throw an exception if we cannot connect to TopBraid AND cache is corrupt
            latestClinicalAttributeMetadata = clinicalAttributeMetadataPersistentCache.getClinicalAttributeMetadataFromPersistentCache();
            latestOverrides = clinicalAttributeMetadataPersistentCache.getClinicalAttributeMetadataOverridesFromPersistentCache();
        } catch (Exception e) {
            try {
                // this will throw an exception if backup is unavailable
                logger.error("Unable to load modeled object cache from default EHCache... attempting to read from backup");
                latestClinicalAttributeMetadata = clinicalAttributeMetadataPersistentCache.getClinicalAttributeMetadataFromPersistentCacheBackup();
                latestOverrides = clinicalAttributeMetadataPersistentCache.getClinicalAttributeMetadataOverridesFromPersistentCacheBackup();
                if (latestClinicalAttributeMetadata == null || latestOverrides == null) {
                    throw new FailedCacheRefreshException("No data found in specified backup cache location...", new Exception());
                }
            } catch (Exception e2) {
                logger.error("Unable to load modeled object cache from backup EHCache...");
                throw new FailedCacheRefreshException("Unable to load data from all backup caches...", new Exception());
            }
        }

        // backup cache at this point (maybe backup after each successful update above?)
        if (!failedClinicalAttributeMetadataCacheRefresh && !failedOverridesCacheRefresh) {
            logger.info("resetCache(): cache update succeeded, backing up cache...");
            try {
                clinicalAttributeMetadataPersistentCache.backupClinicalAttributeMetadataPersistentCache(latestClinicalAttributeMetadata);
                clinicalAttributeMetadataPersistentCache.backupClinicalAttributeMetadataOverridesPersistentCache(latestOverrides);
                logger.info("resetCache(): succesfully backed up cache");
            } catch (Exception e) {
                logger.error("resetCache(): failed to backup cache: " + e.getMessage());
            }
        }

        HashMap<String, ClinicalAttributeMetadata> latestClinicalAttributeMetadataCache = new HashMap<String, ClinicalAttributeMetadata>();
        for (ClinicalAttributeMetadata clinicalAttributeMetadata : latestClinicalAttributeMetadata) {
            latestClinicalAttributeMetadataCache.put(clinicalAttributeMetadata.getColumnHeader(), clinicalAttributeMetadata);
        }

        // latestOverridesCache is a map of study-id to map of clinical attribute name to overridden ClinicalAttributeMetadata object
        HashMap<String, Map<String,ClinicalAttributeMetadata>> latestOverridesCache = new HashMap<String, Map<String, ClinicalAttributeMetadata>>();
        for (Map.Entry<String, ArrayList<ClinicalAttributeMetadata>> entry : latestOverrides.entrySet()) {
            HashMap<String, ClinicalAttributeMetadata> clinicalAttributesMetadataMapping = new HashMap<String, ClinicalAttributeMetadata>();
            for (ClinicalAttributeMetadata clinicalAttributeMetadata : entry.getValue()) {
                fillOverrideAttributeWithDefaultValues(clinicalAttributeMetadata, latestClinicalAttributeMetadataCache.get(clinicalAttributeMetadata.getColumnHeader()));
                clinicalAttributesMetadataMapping.put(clinicalAttributeMetadata.getColumnHeader(), clinicalAttributeMetadata);
            }
            latestOverridesCache.put(entry.getKey(), clinicalAttributesMetadataMapping);
        }

        clinicalAttributeCache = latestClinicalAttributeMetadataCache;
        logger.info("resetCache(): refilled cache with " + latestClinicalAttributeMetadata.size() + " clinical attributes");
        overridesCache = latestOverridesCache;
        logger.info("resetCache(): refilled overrides cache with " + latestOverrides.size() + " overrides");

        if (failedClinicalAttributeMetadataCacheRefresh || failedOverridesCacheRefresh) {
            logger.info("Unable to update cache with latest data from TopBraid... falling back on EHCache store.");
            throw new FailedCacheRefreshException("Failed to refresh cache", new Exception());
        } else {
            dateOfLastCacheRefresh = dateOfCurrentCacheRefresh;
            logger.info("resetCache(): cache last refreshed on: " + dateOfLastCacheRefresh.toString());
        }
    }

    public boolean cacheIsStale() {
        ZonedDateTime currentDate = ZonedDateTime.now();
        ZonedDateTime dateOfCacheExpiration = currentDate.plusDays(- MAXIMUM_CACHE_AGE_IN_DAYS);
        if (dateOfLastCacheRefresh.toInstant().isBefore(dateOfCacheExpiration.toInstant())) {
            return true;
        } else {
            return false;
        }
    }

    private void fillOverrideAttributeWithDefaultValues(ClinicalAttributeMetadata overrideClinicalAttribute, ClinicalAttributeMetadata defaultClinicalAttribute) {
        logger.debug("fillOverrideAttributeWithDefaultValues()");
        if (Strings.isNullOrEmpty(overrideClinicalAttribute.getDisplayName())) {
            overrideClinicalAttribute.setDisplayName(defaultClinicalAttribute.getDisplayName());
        }
        if (Strings.isNullOrEmpty(overrideClinicalAttribute.getDescription())) {
            overrideClinicalAttribute.setDescription(defaultClinicalAttribute.getDescription());
        }
        if (Strings.isNullOrEmpty(overrideClinicalAttribute.getDatatype())) {
            overrideClinicalAttribute.setDatatype(defaultClinicalAttribute.getDatatype());
        }
        if (Strings.isNullOrEmpty(overrideClinicalAttribute.getAttributeType())) {
            overrideClinicalAttribute.setAttributeType(defaultClinicalAttribute.getAttributeType());
        }
        if (Strings.isNullOrEmpty(overrideClinicalAttribute.getPriority())) {
            overrideClinicalAttribute.setPriority(defaultClinicalAttribute.getPriority());
        }
    }

}
