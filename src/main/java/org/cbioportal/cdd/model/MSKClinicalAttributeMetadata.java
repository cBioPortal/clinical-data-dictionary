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
    "label",
})
public class MSKClinicalAttributeMetadata extends ClinicalAttributeMetadata implements Serializable {

    @JsonProperty("label")
    private String description;

    @JsonProperty("variable")
    private String displayName;
   
    @JsonProperty("blah")
    private String columnHeader;
    
    @JsonProperty("typeLabel")
    private String dataType;
    
    @JsonProperty("formName")
    private String attributeType;
   
    private String priority = "1";
 
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    
    public MSKClinicalAttributeMetadata() {}

    /**
    *
    * @return
    * The fields
    */
    @JsonProperty("label")
    public String getDescription() {
        return description;
    }

    /**
    *
    * @param fields
    * The display_name
    */
    @JsonProperty("label")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("variable")
    public String getDisplayName() {
        return displayName;
    }

    @JsonProperty("variable")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @JsonProperty("typeLabel")
    public String getDataType() {
        return dataType;
    }

    @JsonProperty("typeLabel")
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @JsonProperty("blah")
    public String getColumnHeader() {
        return columnHeader;
    }

    @JsonProperty("blah")
    public void setColumnHeader(String columnHeader) {
        this.columnHeader = columnHeader;
    }

    @JsonProperty("formName")
    public String getAttributeType() {
        return attributeType;
    }

    @JsonProperty("formName")
    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    @JsonProperty("priority")
    public String getPriority() {
        return priority;
    }

    @JsonProperty("priority")
    public void setPriority(String priority) {
        this.priority = priority;
    }
}
