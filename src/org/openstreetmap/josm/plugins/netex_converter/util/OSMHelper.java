/* 
 * Copyright (C) 2020 Labian Gashi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 */
package org.openstreetmap.josm.plugins.netex_converter.util;

import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.TagMap;

/**
 *
 * @author Labian Gashi
 */
public final class OSMHelper {

    public static boolean isTrainStation(Node node) {
        if (node != null) {
            TagMap keys = node.getKeys();
            return keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) 
                    && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.STATION_TAG_VALUE)
                    && keys.containsKey(OSMTags.RAILWAY_TAG) 
                    && keys.get(OSMTags.RAILWAY_TAG).equals(OSMTags.STATION_TAG_VALUE);
        } else {
            return false;
        }
    }
}
