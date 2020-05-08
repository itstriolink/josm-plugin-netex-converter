/*
 * Copyright (C) 2020 Labian Gashi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 */
package org.openstreetmap.josm.plugins.netex_converter.model.josm;

import java.util.Map;
import java.util.Objects;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
/**
 *
 * @author Labian Gashi
 */
public class PrimitiveLogMessage {

    private long primitiveId;
    private OsmPrimitiveType primitiveType;
    private Map<String, String> keys;

    public PrimitiveLogMessage(long primitiveId, OsmPrimitiveType primitiveType) {
        this.primitiveId = primitiveId;
        this.primitiveType = primitiveType;
    }

    public PrimitiveLogMessage(long primitiveId, OsmPrimitiveType primitiveType, Map<String, String> keys) {
        this.primitiveId = primitiveId;
        this.primitiveType = primitiveType;
        this.keys = keys;
    }

    public long getPrimitiveId() {
        return primitiveId;
    }

    public void setPrimitiveId(long primitiveId) {
        this.primitiveId = primitiveId;
    }

    public OsmPrimitiveType getPrimitiveType() {
        return primitiveType;
    }

    public void setPrimitiveType(OsmPrimitiveType primitiveType) {
        this.primitiveType = primitiveType;
    }

    public Map<String, String> getKeys() {
        return keys;
    }

    public void setKeys(Map<String, String> keys) {
        this.keys = keys;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final PrimitiveLogMessage other = (PrimitiveLogMessage) obj;

        if (this.primitiveId != other.primitiveId) {
            return false;
        }
        if (this.primitiveType != other.primitiveType) {
            return false;
        }
        return Objects.equals(this.keys, other.keys);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (this.primitiveId ^ (this.primitiveId >>> 32));
        hash = 97 * hash + Objects.hashCode(this.primitiveType);
        hash = 97 * hash + Objects.hashCode(this.keys);
        return hash;
    }

    public static final class Messages {
        
        public static final String LOG_MESSAGE_SUFFIX = " - (NeTEx Converter)";
        
        public static final String REF_MISSING_MESSAGE = "Please enter a required \"ref\" or \"local_ref\" tag for the platform number" + LOG_MESSAGE_SUFFIX;
        public static final String UIC_REF_MISSING_MESSAGE = "Please enter a required \"uic_ref\" tag for the station" + LOG_MESSAGE_SUFFIX;

    }
}
