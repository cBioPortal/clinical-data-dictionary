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
@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({
    "column_header",
    "display_name",
    "description",
    "datatype",
    "attribute_type",
    "priority",
})
public class ClinicalAttributeMetadata implements Serializable {

    private String studyId;
    @ApiModelProperty(value = "The column header")
    @JsonProperty("column_header")
    private String columnHeader;
    @ApiModelProperty(value = "The name to be displayed in the cBio Portal")
    @JsonProperty("display_name")
    private String displayName;
    @ApiModelProperty(value = "The description")
    @JsonProperty("description")
    private String description;
    @ApiModelProperty(value = "The data type", allowableValues = "NUMBER,STRING")
    @JsonProperty("datatype")
    private String datatype;
    @ApiModelProperty(value = "The attribute type", allowableValues = "PATIENT,SAMPLE")
    @JsonProperty("attribute_type")
    private String attributeType;
    @ApiModelProperty(value = "Higher priority attributes are given prominence in the cBio Portal, zero priority attributes are hidden")
    @JsonProperty("priority")
    private String priority;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
    * No args constructor for use in serialization
    *
    */
    public ClinicalAttributeMetadata() {}

    /** Primitive Arguments Constructor
    *
    * @param columnHeader
    * @param priority
    * @param attributeType
    * @param datatype
    * @param displayName
    * @param description
    */
    public ClinicalAttributeMetadata(String columnHeader, String displayName, String description, String datatype, String attributeType, String priority) {
        this.columnHeader = columnHeader;
        this.displayName = displayName;
        this.description = description;
        this.datatype = datatype;
        this.attributeType = attributeType;
        this.priority = priority;
    }

    /** Copy Constructor
    *
    * @param clinicalAttributeMetadata
    */
    public ClinicalAttributeMetadata(ClinicalAttributeMetadata otherObject) {
        this.studyId = otherObject.getStudyId();
        this.columnHeader = otherObject.getColumnHeader();
        this.displayName = otherObject.getDisplayName();
        this.description = otherObject.getDescription();
        this.datatype = otherObject.getDatatype();
        this.attributeType = otherObject.getAttributeType();
        this.priority = otherObject.getPriority();
        Map<String, Object> otherObjectAdditionalProperties = otherObject.getAdditionalProperties();
        if (otherObjectAdditionalProperties != null) {
            for (String key : otherObjectAdditionalProperties.keySet()) {
                Object value = otherObjectAdditionalProperties.get(key);
                this.setAdditionalProperty(key, value);
            }
        }
    }

    /**
    *
    * @return
    * The studyId
    */
    @JsonIgnore
    public String getStudyId() {
        return studyId;
    }

    /**
    *
    * @param studyId
    * The studyId
    */
    @JsonProperty("study_id")
    public void setStudyId(String studyId) {
        this.studyId = studyId;
    }

    /**
    *
    * @return
    * The columnHeader
    */
    @JsonProperty("column_header")
    public String getColumnHeader() {
        return columnHeader;
    }

    /**
    *
    * @param normalizedColumnHeader
    * The normalized_column_header
    */
    @JsonProperty("normalized_column_header")
    public void setNormalizedColumnHeader(String normalizedColumnHeader) {
        this.columnHeader = normalizedColumnHeader;
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
    public void setDescription(String description) {
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

    public boolean containsSearchTerm(String searchTerm) {
        return (StringUtils.containsIgnoreCase(this.columnHeader, searchTerm) ||
            StringUtils.containsIgnoreCase(this.displayName, searchTerm) ||
            StringUtils.containsIgnoreCase(this.description, searchTerm));
    }

    public boolean containsAllSearchTerms(List<String> searchTerms) {
        for (String searchTerm : searchTerms) {
            if (!containsSearchTerm(searchTerm)) {
                return false;
            }
        }
        return true;
    }

    public Integer levenshteinDistanceFromSearchTerm(String searchTerm) {
        Integer levenshteinDistance = Integer.MAX_VALUE;
        if (StringUtils.containsIgnoreCase(this.columnHeader, searchTerm)) {
            levenshteinDistance = Math.min(levenshteinDistance, StringUtils.getLevenshteinDistance(searchTerm, this.columnHeader));
        }
        if (StringUtils.containsIgnoreCase(this.displayName, searchTerm)) {
            levenshteinDistance = Math.min(levenshteinDistance, StringUtils.getLevenshteinDistance(searchTerm, this.displayName));
        }
        if (StringUtils.containsIgnoreCase(this.description, searchTerm)) {
            levenshteinDistance = Math.min(levenshteinDistance, StringUtils.getLevenshteinDistance(searchTerm, this.description));
        }
        return levenshteinDistance;
    }

    public boolean matchesAttributeType(String attributeType) {
        return attributeType == null || attributeType.toUpperCase().equals(this.attributeType);
    }
}
