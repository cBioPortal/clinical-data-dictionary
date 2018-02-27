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

package org.mskcc.clinical_attributes.service;

import org.mskcc.clinical_attributes.model.ClinicalAttribute;
import org.mskcc.clinical_attributes.service.exception.ClinicalAttributeNotFoundException;

import java.util.List;

/**
 * @author Manda Wilson 
 */
public interface ClinicalAttributesService {
    List<ClinicalAttribute> getClinicalAttributes(String studyId);
    List<ClinicalAttribute> getMetadataByNormalizedColumnHeaders(String studyId, List<String> normalizedColumnHeaders) throws ClinicalAttributeNotFoundException;
    ClinicalAttribute getMetadataByNormalizedColumnHeader(String studyId, String normalizedColumnHeader) throws ClinicalAttributeNotFoundException;
}
