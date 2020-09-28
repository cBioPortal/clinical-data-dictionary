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

package org.cbioportal.cdd.repository;

import java.util.List;

import org.cbioportal.cdd.model.ClinicalAttributeMetadata;
import java.util.*;

/**
 * @author Manda Wilson
 */
public interface ClinicalAttributeMetadataRepository {
    ArrayList<ClinicalAttributeMetadata> getClinicalAttributeMetadata();
    Map<String, ArrayList<ClinicalAttributeMetadata>> getClinicalAttributeMetadataOverrides();
}