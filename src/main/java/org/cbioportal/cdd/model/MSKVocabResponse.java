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

package org.cbioportal.cdd.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Avery Wang, Manda Wilson
 */
//@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
    "fields",
})
public class MSKVocabResponse implements Serializable {

    @JsonProperty("fields")
    private ArrayList<MSKClinicalAttributeMetadata> mskClinicalAttributeMetadata = null;

    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    private MSKVocabResponse() {}

    /**
    *
    * @return
    * The fields
    */
    @JsonProperty("fields")
    public ArrayList<MSKClinicalAttributeMetadata> getMSKClinicalAttributeMetadata() {
        return mskClinicalAttributeMetadata;
    }

    /**
    *
    * @param fields
    * The display_name
    */
    @JsonProperty("fields")
    public void setMSKClinicalAttributeMetadata(ArrayList<MSKClinicalAttributeMetadata> mskClinicalAttributeMetadata) {
        this.mskClinicalAttributeMetadata = mskClinicalAttributeMetadata;
    }
}
