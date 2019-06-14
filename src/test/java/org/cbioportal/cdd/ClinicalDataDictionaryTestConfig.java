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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClinicalDataDictionaryTestConfig {

    @Bean
    public ClinicalAttributeMetadataRepository clinicalAttributesRepository() {
        ClinicalAttributeMetadataRepository clinicalAttributesRepository = Mockito.mock(ClinicalAttributeMetadataRepository.class);
        return clinicalAttributesRepository;
    }

    public void resetWorkingClinicalAttributesRepository(ClinicalAttributeMetadataRepository clinicalAttributesRepository) {
        Mockito.reset(clinicalAttributesRepository);
        Mockito.when(clinicalAttributesRepository.getClinicalAttributeMetadata()).thenReturn(makeMockAttributeList());
        Mockito.when(clinicalAttributesRepository.getClinicalAttributeMetadataOverrides()).thenReturn(makeMockOverridesMap());
    }

    public void resetUpdatedClinicalAttributesRepository(ClinicalAttributeMetadataRepository clinicalAttributesRepository) {
        Mockito.reset(clinicalAttributesRepository);
        Mockito.when(clinicalAttributesRepository.getClinicalAttributeMetadata()).thenReturn(makeUpdatedMockAttributeList());
        Mockito.when(clinicalAttributesRepository.getClinicalAttributeMetadataOverrides()).thenReturn(makeUpdatedMockOverridesMap());
    }

    public void resetNotWorkingClinicalAttributesRepository(ClinicalAttributeMetadataRepository clinicalAttributesRepository) {
        Mockito.reset(clinicalAttributesRepository);
        Mockito.when(clinicalAttributesRepository.getClinicalAttributeMetadata()).thenThrow(new RuntimeException("faking a problem getting the clinical attribute data"));
        Mockito.when(clinicalAttributesRepository.getClinicalAttributeMetadataOverrides()).thenThrow(new RuntimeException("faking a problem getting the clinical attribute data"));
    }

    private ArrayList<ClinicalAttributeMetadata> makeMockAttributeList() {
        ArrayList<ClinicalAttributeMetadata> attributeList = new ArrayList<>();
        attributeList.add(new ClinicalAttributeMetadata("AGE", "Diagnosis Age", "Age at which a condition or disease was first diagnosed.", "NUMBER", "PATIENT", "1"));
        attributeList.add(new ClinicalAttributeMetadata("BONE_MARROW_SAMPLE_HISTOLOGY", "Bone Marrow Sample Histology", "Bone Marrow Sample Histology", "STRING", "SAMPLE", "1"));
        attributeList.add(new ClinicalAttributeMetadata("CLIN_M_STAGE", "Neoplasm American Joint Committee on Cancer Clinical Distant Metastasis M Stage", "Extent of the distant metastasis for the cancer based on evidence obtained from clinical assessment parameters determined prior to treatment.", "STRING", "PATIENT", "1"));
        attributeList.add(new ClinicalAttributeMetadata("DISEASE_STAGE", "Disease Stage", "Disease Stage", "STRING", "SAMPLE", "1"));
        attributeList.add(new ClinicalAttributeMetadata("LAST_STATUS", "Last Status", "Last Status.", "STRING", "PATIENT", "1"));
        return attributeList;
    }

    private ArrayList<ClinicalAttributeMetadata> makeUpdatedMockAttributeList() {
        ArrayList<ClinicalAttributeMetadata> attributeList = new ArrayList<>();
        attributeList.add(new ClinicalAttributeMetadata("AGE", "Diagnosis Age", "Age at which a condition or disease was first diagnosed.", "NUMBER", "PATIENT", "1"));
        attributeList.add(new ClinicalAttributeMetadata("NECROSIS", "Necrosis", "Death of cells in organ/tissue due to lack of blood supply", "STRING", "SAMPLE", "1"));
        return attributeList;
    }

    private HashMap<String, ArrayList<ClinicalAttributeMetadata>> makeMockOverridesMap() {
        ArrayList<ClinicalAttributeMetadata> testPolicyAttributeList = new ArrayList<> ();
        testPolicyAttributeList.add(new ClinicalAttributeMetadata("AGE", "Diagnosis Age", "Age at which a condition or disease was first diagnosed.", "NUMBER", "PATIENT", "100"));
        testPolicyAttributeList.add(new ClinicalAttributeMetadata("DISEASE_STAGE", "Disease Stage", "Disease Stage", "STRING", "PATIENT", "10"));

        // there are special rules for mskimpact
        ArrayList<ClinicalAttributeMetadata> impactPolicyAttributeList = new ArrayList<> ();
        impactPolicyAttributeList.add(new ClinicalAttributeMetadata("LAST_STATUS", "Last Status", "Last Status.", "STRING", "PATIENT", "1"));

        HashMap<String, ArrayList<ClinicalAttributeMetadata>> overridesMap = new HashMap<> ();
        overridesMap.put("test_override_study", testPolicyAttributeList);
        overridesMap.put("mskimpact", impactPolicyAttributeList);
        return overridesMap;
    }

    private HashMap<String, ArrayList<ClinicalAttributeMetadata>> makeUpdatedMockOverridesMap() {
        ArrayList<ClinicalAttributeMetadata> updatedPolicyList = new ArrayList<> ();
        updatedPolicyList.add(new ClinicalAttributeMetadata("OS_MONTHS", "Overall Survival Status", "Overall survival status in months.", "NUMBER", "PATIENT", "250"));

        HashMap<String, ArrayList<ClinicalAttributeMetadata>> overridesMap = new HashMap<> ();
        overridesMap.put("updated_override_study", updatedPolicyList);
        return overridesMap;
    }
}
