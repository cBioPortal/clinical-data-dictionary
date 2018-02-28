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

package org.cbioportal.cam.service;

import java.util.List;

import org.cbioportal.cam.model.ClinicalAttributeMetadata;
import org.cbioportal.cam.service.exception.ClinicalAttributeNotFoundException;

/**
 * @author Manda Wilson 
 */
public interface ClinicalAttributeMetadataService {
    List<ClinicalAttributeMetadata> getClinicalAttributeMetadata(String studyId);
    List<ClinicalAttributeMetadata> getMetadataByNormalizedColumnHeaders(String studyId, List<String> normalizedColumnHeaders) throws ClinicalAttributeNotFoundException;
    ClinicalAttributeMetadata getMetadataByNormalizedColumnHeader(String studyId, String normalizedColumnHeader) throws ClinicalAttributeNotFoundException;
}
