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

package org.cbioportal.cdd.repository.graphite.jsonmodeling.attributes;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Binding {
    private Attribute columnHeader;
    private Attribute displayName;
    private Attribute attributeType;
    private Attribute datatype;
    private Attribute description;
    private Attribute priority;

    @JsonProperty("column_header")
    public Attribute getColumnHeader() {
        return columnHeader;
    }

    public void setColumnHeader(Attribute columnHeader) {
        this.columnHeader = columnHeader;
    }

    @JsonProperty("display_name")
    public Attribute getDisplayName() {
        return displayName;
    }

    public void setDisplayName(Attribute displayName) {
        this.displayName = displayName;
    }

    @JsonProperty("attribute_type")
    public Attribute getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(Attribute attributeType) {
        this.attributeType = attributeType;
    }

    public Attribute getDatatype() {
        return datatype;
    }

    public void setDatatype(Attribute datatype) {
        this.datatype = datatype;
    }

    public Attribute getDescription() {
        return description;
    }

    public void setDescription(Attribute description) {
        this.description = description;
    }

    public Attribute getPriority() {
        return priority;
    }

    public void setPriority(Attribute priority) {
        this.priority = priority;
    }
}
