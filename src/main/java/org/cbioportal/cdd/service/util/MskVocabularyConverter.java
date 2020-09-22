/*
 * Copyright (c) 2020 Memorial Sloan-Kettering Cancer Center.
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

package org.cbioportal.cdd.service.util;

import java.util.*;
import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import org.cbioportal.cdd.model.MskVocabulary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MskVocabularyConverter {

    public ClinicalAttributeMetadata convertToClinicalAttributeMetadata(MskVocabulary mskVocabulary) {
        ClinicalAttributeMetadata clinicalAttributeMetadata = new ClinicalAttributeMetadata(
            mskVocabulary.getVariable().toUpperCase(),
            mskVocabulary.getLabel(),
            mskVocabulary.getLabel(),
            mskVocabulary.getTypeLabel(),
            mskVocabulary.getFormName(),
            "1"
        );
        return clinicalAttributeMetadata;
    }

    public List<ClinicalAttributeMetadata> convertToClinicalAttributeMetadata(List<MskVocabulary> mskVocabularyList) {
        List<ClinicalAttributeMetadata> clinicalAttributeMetadataList = new ArrayList<ClinicalAttributeMetadata>();
        for (MskVocabulary mskVocabulary : mskVocabularyList) {
            clinicalAttributeMetadataList.add(convertToClinicalAttributeMetadata(mskVocabulary));
        }
        return clinicalAttributeMetadataList;
    }
}

