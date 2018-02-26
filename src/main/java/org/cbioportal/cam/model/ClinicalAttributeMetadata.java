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

package org.cbioportal.cam.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.*;

/**
 * @author Avery Wang, Manda Wilson 
 */
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
    "normalized_column_header",
    "display_name",
    "description",
    "datatype",
    "attribute_type",
    "priority",
})
public class ClinicalAttributeMetadata {

    @JsonProperty("normalized_column_header")
    private String normalizedColumnHeader;
    @JsonProperty("display_name")
    private String displayName;
    @JsonProperty("description")
    private String description;
    @JsonProperty("datatype")
    private String datatype;
    @JsonProperty("attribute_type")
    private String attributeType;
    @JsonProperty("priority")
    private String priority;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
    * No args constructor for use in serialization
    *
    */
    private ClinicalAttributeMetadata() {}


    /**
    *
    * @param normalizedColumnHeader
    * @param priority
    * @param attributeType
    * @param datatype
    * @param displayName
    * @param description
    */
    public ClinicalAttributeMetadata(String normalizedColumnHeader, String displayName, String description, String datatype, String attributeType, String priority) {
        this.normalizedColumnHeader = normalizedColumnHeader;
        this.displayName = displayName;
        this.description = description;
        this.datatype = datatype;
        this.attributeType = attributeType;
        this.priority = priority;
    }

    /**
    *
    * @return
    * The normalizedColumnHeader
    */
    @JsonProperty("normalized_column_header")
    public String getNormalizedColumnHeader() {
        return normalizedColumnHeader;
    }

    /**
    *
    * @param normalizedColumnHeader
    * The normalized_column_header
    */
    @JsonProperty("normalized_column_header")
    public void setNormalizedColumnHeader(String normalizedColumnHeader) {
        this.normalizedColumnHeader = normalizedColumnHeader;
    }

    /**
    *
    * @return
    * The displayName
    */
    @JsonProperty("display_name")
    public String getDisplayName() {
        return displayName;
    }

    /**
    *
    * @param displayName
    * The display_name
    */
    @JsonProperty("display_name")
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
    *
    * @return
    * The description
    */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
    *
    * @param description
    * The description
    */
    @JsonProperty("description")
    public void setDescriptions(String description) {
        this.description = description;
    }

    /**
    *
    * @return
    * The datatype
    */
    @JsonProperty("datatype")
    public String getDatatype() {
        return datatype;
    }

    /**
    *
    * @param datatype
    * The datatype
    */
    @JsonProperty("datatype")
    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    /**
    *
    * @return
    * The attributeType
    */
    @JsonProperty("attribute_type")
    public String getAttributeType() {
        return attributeType;
    }

    /**
    *
    * @param attributeType
    * The attribute_type
    */
    @JsonProperty("attribute_type")
    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    /**
    *
    * @return
    * The priority
    */
    @JsonProperty("priority")
    public String getPriority() {
        return priority;
    }

    /**
    *
    * @param priority
    * The priority
    */
    @JsonProperty("priority")
    public void setPriority(String priority) {
        this.priority = priority;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
