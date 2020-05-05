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
    private LogLevel logLevel;
    private String message;
    private Map<String, String> keys;

    public PrimitiveLogMessage(long primitiveId, OsmPrimitiveType primitiveType, LogLevel logLevel) {
        this.primitiveId = primitiveId;
        this.primitiveType = primitiveType;
        this.logLevel = logLevel;
    }

    public PrimitiveLogMessage(long primitiveId, OsmPrimitiveType primitiveType, LogLevel logLevel, String message) {
        this.primitiveId = primitiveId;
        this.primitiveType = primitiveType;
        this.logLevel = logLevel;
        this.message = message;
    }

    public PrimitiveLogMessage(long primitiveId, OsmPrimitiveType primitiveType, LogLevel logLevel, Map<String, String> keys) {
        this.primitiveId = primitiveId;
        this.primitiveType = primitiveType;
        this.logLevel = logLevel;
        this.keys = keys;
    }

    public PrimitiveLogMessage(long primitiveId, OsmPrimitiveType primitiveType, LogLevel logLevel, String message, Map<String, String> keys) {
        this.primitiveId = primitiveId;
        this.primitiveType = primitiveType;
        this.logLevel = logLevel;
        this.message = message;
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

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

        return this.logLevel == other.logLevel;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (int) (this.primitiveId ^ (this.primitiveId >>> 32));
        hash = 53 * hash + Objects.hashCode(this.primitiveType);
        hash = 53 * hash + Objects.hashCode(this.logLevel);
        return hash;
    }

    public enum LogLevel {
        INFO,
        WARNING,
        CRITICAL
    }

    public static final class Messages {
        public static final String REF_MISSING_MESSAGE = "Please enter a required \"ref\" or \"local_ref\" tag for the platform number.";
        public static final String UIC_REF_MISSING_MESSAGE = "Please enter a required \"uic_ref\" tag for the station.";
    }

}
