/*
 * Copyright (c) 2024 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * Memorial Sloan-Kettering Cancer Center
 * has no obligations to provide maintenance, support,
 * updates, enhancements or modifications.  In no event shall
 * Memorial Sloan-Kettering Cancer Center
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * Memorial Sloan-Kettering Cancer Center
 * has been advised of the possibility of such damage.
*/

package org.cbioportal.cdd.repository.graphite.jsonmodeling.overrides;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OverridesBinding {
    private OverridesAttribute studyId;
    private OverridesAttribute columnHeader;
    private OverridesAttribute displayName;
    private OverridesAttribute attributeType;
    private OverridesAttribute datatype;
    private OverridesAttribute description;
    private OverridesAttribute priority;

    @JsonProperty("study_id")
    public OverridesAttribute getStudyId() {
        return studyId;
    }

    public void setStudyId(OverridesAttribute studyId) {
        this.studyId = studyId;
    }

    @JsonProperty("column_header")
    public OverridesAttribute getColumnHeader() {
        return columnHeader;
    }

    public void setColumnHeader(OverridesAttribute columnHeader) {
        this.columnHeader = columnHeader;
    }

    @JsonProperty("display_name")
    public OverridesAttribute getDisplayName() {
        return displayName;
    }

    public void setDisplayName(OverridesAttribute displayName) {
        this.displayName = displayName;
    }

    @JsonProperty("attribute_type")
    public OverridesAttribute getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(OverridesAttribute attributeType) {
        this.attributeType = attributeType;
    }

    public OverridesAttribute getDatatype() {
        return datatype;
    }

    public void setDatatype(OverridesAttribute datatype) {
        this.datatype = datatype;
    }

    public OverridesAttribute getDescription() {
        return description;
    }

    public void setDescription(OverridesAttribute description) {
        this.description = description;
    }

    public OverridesAttribute getPriority() {
        return priority;
    }

    public void setPriority(OverridesAttribute priority) {
        this.priority = priority;
    }

}
