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

package org.cbioportal.cdd;

import java.util.*;
import org.mockito.Mockito;
import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.repository.ClinicalAttributeMetadataRepository;
import org.cbioportal.cdd.service.internal.ClinicalAttributeMetadataCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClinicalDataDictionaryTestConfig {

    @Bean
    public ClinicalAttributeMetadataCache clinicalAttributesCache() {
        Map<String, ClinicalAttributeMetadata> mockAttributeMap = makeMockAttributeMap();
        Map<String, Map<String, ClinicalAttributeMetadata>> mockOverridesMap = makeMockOverridesMap();
        ClinicalAttributeMetadataCache cache = Mockito.mock(ClinicalAttributeMetadataCache.class);
        Mockito.when(cache.getClinicalAttributeMetadata()).thenReturn(mockAttributeMap);
        Mockito.when(cache.getClinicalAttributeMetadataOverrides()).thenReturn(mockOverridesMap);
        return cache;
    }

    @Bean
    public ClinicalAttributeMetadataRepository clinicalAttributesRepository() {
        return Mockito.mock(ClinicalAttributeMetadataRepository.class);
    }

    private Map<String, ClinicalAttributeMetadata> makeMockAttributeMap() {
        Map<String, ClinicalAttributeMetadata> attributeMap = new HashMap<>();
        attributeMap.put("AGE", new ClinicalAttributeMetadata("AGE", "Diagnosis Age", "Age at which a condition or disease was first diagnosed.", "NUMBER", "PATIENT", "1"));
        attributeMap.put("BONE_MARROW_SAMPLE_HISTOLOGY", new ClinicalAttributeMetadata("BONE_MARROW_SAMPLE_HISTOLOGY", "Bone Marrow Sample Histology", "Bone Marrow Sample Histology", "STRING", "SAMPLE", "1"));
        attributeMap.put("CLIN_M_STAGE", new ClinicalAttributeMetadata("CLIN_M_STAGE", "Neoplasm American Joint Committee on Cancer Clinical Distant Metastasis M Stage", "Extent of the distant metastasis for the cancer based on evidence obtained from clinical assessment parameters determined prior to treatment.", "STRING", "PATIENT", "1"));
        attributeMap.put("DISEASE_STAGE", new ClinicalAttributeMetadata("DISEASE_STAGE", "Disease Stage", "Disease Stage", "STRING", "SAMPLE", "1"));
        attributeMap.put("LAST_STATUS", new ClinicalAttributeMetadata("LAST_STATUS", "Last Status", "Last Status.", "STRING", "PATIENT", "1"));
        return Collections.unmodifiableMap(attributeMap);
    }

    private Map<String, Map<String, ClinicalAttributeMetadata>> makeMockOverridesMap() {
        Map<String, ClinicalAttributeMetadata> attributeMap = new HashMap<> ();
        Map<String, Map<String, ClinicalAttributeMetadata>> overridesMap = new HashMap<> ();
        attributeMap.put("AGE", new ClinicalAttributeMetadata("AGE", "Diagnosis Age", "Age at which a condition or disease was first diagnosed.", "NUMBER", "PATIENT", "100"));
        overridesMap.put("test_override_study", attributeMap);
        return Collections.unmodifiableMap(overridesMap);
    }
}
