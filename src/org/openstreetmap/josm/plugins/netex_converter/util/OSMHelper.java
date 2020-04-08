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

import com.netex.model.LimitationStatusEnumeration;
import com.netex.model.QuayTypeEnumeration;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;

/**
 *
 * @author Labian Gashi
 */
public final class OSMHelper {

    public static boolean isTrainStation(Node node) {
        if (node != null) {
            TagMap keys = node.getKeys();
            return ((keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.STATION_TAG_VALUE))
                    || (keys.containsKey(OSMTags.RAILWAY_TAG) && keys.get(OSMTags.RAILWAY_TAG).equals(OSMTags.STATION_TAG_VALUE)))
                    && !(keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
        }
        else {
            return false;
        }
    }

    public static boolean isTrainStation(Node node, boolean checkIfPlatformToo) {
        if (node != null) {
            TagMap keys = node.getKeys();

            if (checkIfPlatformToo) {
                return ((keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.STATION_TAG_VALUE))
                        || (keys.containsKey(OSMTags.RAILWAY_TAG) && keys.get(OSMTags.RAILWAY_TAG).equals(OSMTags.STATION_TAG_VALUE)))
                        && !(keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
            }
            else {

                return ((keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.STATION_TAG_VALUE))
                        || (keys.containsKey(OSMTags.RAILWAY_TAG) && keys.get(OSMTags.RAILWAY_TAG).equals(OSMTags.STATION_TAG_VALUE)));
            }
        }
        else {
            return false;
        }
    }

    public static boolean isBusStation(Node node) {
        if (node != null) {
            TagMap keys = node.getKeys();
            return keys.containsKey(OSMTags.AMENITY_TAG)
                    && keys.get(OSMTags.AMENITY_TAG).equals(OSMTags.BUS_STATION_TAG_VALUE)
                    && !(keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
        }
        else {
            return false;
        }
    }

    public static boolean isBusStation(Node node, boolean checkIfPlatformToo) {
        if (node != null) {
            TagMap keys = node.getKeys();

            if (checkIfPlatformToo) {
                return keys.containsKey(OSMTags.AMENITY_TAG)
                        && keys.get(OSMTags.AMENITY_TAG).equals(OSMTags.BUS_STATION_TAG_VALUE)
                        && !(keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
            }
            else {
                return keys.containsKey(OSMTags.AMENITY_TAG)
                        && keys.get(OSMTags.AMENITY_TAG).equals(OSMTags.BUS_STATION_TAG_VALUE);
            }
        }
        else {
            return false;
        }
    }

    public static boolean isBusStop(Node node) {
        if (node != null) {
            TagMap keys = node.getKeys();
            return keys.containsKey(OSMTags.HIGHWAY_TAG)
                    && keys.get(OSMTags.HIGHWAY_TAG).equals(OSMTags.BUS_STOP_TAG_VALUE)
                    && !(keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
        }
        else {
            return false;
        }
    }

    public static boolean isBusStop(Node node, boolean checkIfPlatformToo) {
        if (node != null) {
            TagMap keys = node.getKeys();

            if (checkIfPlatformToo) {
                return keys.containsKey(OSMTags.HIGHWAY_TAG)
                        && keys.get(OSMTags.HIGHWAY_TAG).equals(OSMTags.BUS_STOP_TAG_VALUE)
                        && !(keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
            }
            else {
                return keys.containsKey(OSMTags.HIGHWAY_TAG)
                        && keys.get(OSMTags.HIGHWAY_TAG).equals(OSMTags.BUS_STOP_TAG_VALUE);
            }
        }
        else {
            return false;
        }
    }

    public static boolean isPlatform(Node node) {
        if (node != null) {
            TagMap keys = node.getKeys();
            return (keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE))
                    || (keys.containsKey(OSMTags.RAILWAY_TAG) && keys.get(OSMTags.RAILWAY_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
        }
        else {
            return false;
        }
    }

    public static boolean isPlatform(Way way) {
        if (way != null) {
            TagMap keys = way.getKeys();
            return (keys.containsKey(OSMTags.HIGHWAY_TAG) && keys.get(OSMTags.HIGHWAY_TAG).equals(OSMTags.PLATFORM_TAG_VALUE))
                    || (keys.containsKey(OSMTags.RAILWAY_TAG) && keys.get(OSMTags.RAILWAY_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
        }
        else {
            return false;
        }
    }

    public static boolean isPlatform(Relation relation) {
        if (relation != null) {
            TagMap keys = relation.getKeys();
            return (keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE))
                    || (keys.containsKey(OSMTags.RAILWAY_TAG) && keys.get(OSMTags.RAILWAY_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
        }
        else {
            return false;
        }
    }

    public static boolean isElevator(Node node) {
        if (node != null) {
            TagMap keys = node.getKeys();
            return keys.containsKey(OSMTags.HIGHWAY_TAG) && keys.get(OSMTags.HIGHWAY_TAG).equals(OSMTags.ELEVATOR_TAG_VALUE);
        }
        else {
            return false;
        }
    }

    public static boolean isSteps(Way way) {
        if (way != null) {
            TagMap keys = way.getKeys();
            return keys.containsKey(OSMTags.HIGHWAY_TAG) && keys.get(OSMTags.HIGHWAY_TAG).equals(OSMTags.STEPS_TAG_VALUE);
        }
        else {
            return false;
        }
    }

    public static boolean isFootPath(Way way) {
        if (way != null) {
            TagMap keys = way.getKeys();
            return keys.containsKey(OSMTags.HIGHWAY_TAG) && keys.get(OSMTags.HIGHWAY_TAG).equals(OSMTags.FOOTWAY_TAG_VALUE);
        }
        else {
            return false;
        }
    }

    public static boolean isRamp(Way way) {
        if (way != null) {
            TagMap keys = way.getKeys();

            return (keys.containsKey(OSMTags.RAMP_TAG) && keys.get(OSMTags.RAMP_TAG).equals(OSMTags.YES_TAG_VALUE))
                    || keys.containsKey(OSMTags.INCLINE_TAG);
        }
        else {
            return false;
        }
    }

    public static String getUicRef(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            if (keys.containsKey(OSMTags.UIC_REF_TAG)) {
                return keys.get(OSMTags.UIC_REF_TAG);
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    public static String getRef(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            if (keys.containsKey(OSMTags.REF_TAG)) {
                return keys.get(OSMTags.REF_TAG);
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    public static String getLevel(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            if (keys.containsKey(OSMTags.LEVEL_TAG)) {
                return keys.get(OSMTags.LEVEL_TAG);
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    public static LimitationStatusEnumeration getWheelchairLimitation(OsmPrimitive primitive) {
        LimitationStatusEnumeration wheelChairLimitation = LimitationStatusEnumeration.FALSE;

        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            if (keys.containsKey(OSMTags.WHEELCHAIR_TAG)) {
                String wheelchairTagAccess = keys.get(OSMTags.WHEELCHAIR_TAG);

                switch (wheelchairTagAccess.toLowerCase()) {
                    case "yes":
                        wheelChairLimitation = LimitationStatusEnumeration.TRUE;
                        break;
                    case "limited":
                        wheelChairLimitation = LimitationStatusEnumeration.PARTIAL;
                        break;
                    default:
                        wheelChairLimitation = LimitationStatusEnumeration.FALSE;
                        break;
                }
            }

            return wheelChairLimitation;
        }
        else {
            return wheelChairLimitation;
        }
    }

    public static QuayTypeEnumeration getQuayTypeEnumeration(OsmPrimitive primitive) {
        QuayTypeEnumeration quayTypeEnumeration = QuayTypeEnumeration.OTHER;

        if (primitive instanceof Node) {
            Node node = (Node) primitive;

            if (OSMHelper.isBusStation(node, false)) {
                quayTypeEnumeration = QuayTypeEnumeration.BUS_PLATFORM;
            }
            else if (OSMHelper.isBusStop(node, false)) {
                quayTypeEnumeration = QuayTypeEnumeration.BUS_STOP;
            }
            else if (OSMHelper.isTrainStation(node, false)) {
                quayTypeEnumeration = QuayTypeEnumeration.RAIL_PLATFORM;
            }
            else {
                quayTypeEnumeration = QuayTypeEnumeration.OTHER;
            }
        }

        return quayTypeEnumeration;
    }
}
