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
import org.cbioportal.cdd.model.CancerStudy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MSKVocabStudyUtil {

    @Value("#{'${mskvocabulary.studyid_list}'.split(',')}")
    private Set<String> studyIdSet;

    public Boolean useMskVocabularyForStudy(String studyId) {
        if (studyId == null || studyId.trim().isEmpty()) {
            return false;
        }
        return studyIdSet.contains(studyId.toLowerCase());
    }

    public List<CancerStudy> getMskVocabularyStudyList() {
        List<CancerStudy> studyList = new ArrayList<>();
        for (String studyId : studyIdSet) {
            studyList.add(new CancerStudy(studyId));
        }
        return studyList;
    }

}
