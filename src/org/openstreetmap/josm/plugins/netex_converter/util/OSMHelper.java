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
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;

/**
 *
 * @author Labian Gashi
 */
public final class OSMHelper {

    public static final String OUTER_ROLE = "outer";
    public static final String INNER_ROLE = "inner";

    public static boolean isStopPlace(OsmPrimitive primitive) {
        if (primitive != null) {
            return isTrainStation(primitive) || isBusStation(primitive) || isBusStop(primitive);
        }
        else {
            return false;
        }
    }

    public static boolean isTrainStation(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            return (keys.containsKey(OSMTags.RAILWAY_TAG) && keys.get(OSMTags.RAILWAY_TAG).equals(OSMTags.STATION_TAG_VALUE))
                    && !(keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
        }

        return false;
    }

    public static boolean isTrainStation(OsmPrimitive primitive, boolean checkIfPlatformToo) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            if (checkIfPlatformToo) {
                return keys.containsKey(OSMTags.RAILWAY_TAG) && keys.get(OSMTags.RAILWAY_TAG).equals(OSMTags.STATION_TAG_VALUE)
                        && !(keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
            }
            else {
                return keys.containsKey(OSMTags.RAILWAY_TAG) && keys.get(OSMTags.RAILWAY_TAG).equals(OSMTags.STATION_TAG_VALUE);
            }
        }

        return false;
    }

    public static boolean isBusStation(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();
            return keys.containsKey(OSMTags.AMENITY_TAG)
                    && keys.get(OSMTags.AMENITY_TAG).equals(OSMTags.BUS_STATION_TAG_VALUE)
                    && !(keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
        }

        return false;
    }

    public static boolean isBusStation(OsmPrimitive primitive, boolean checkIfPlatformToo) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

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

        return false;
    }

    public static boolean isBusStop(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            return keys.containsKey(OSMTags.HIGHWAY_TAG)
                    && keys.get(OSMTags.HIGHWAY_TAG).equals(OSMTags.BUS_STOP_TAG_VALUE)
                    && !(keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
        }

        return false;
    }

    public static boolean isBusStop(OsmPrimitive primitive, boolean checkIfPlatformToo) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

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

        return false;
    }

    public static boolean isStopArea(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            return keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.STOP_AREA_TAG_VALUE);
        }

        return false;
    }

    public static boolean isPlatform(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            return (keys.containsKey(OSMTags.PUBLIC_TRANSPORT_TAG) && keys.get(OSMTags.PUBLIC_TRANSPORT_TAG).equals(OSMTags.PLATFORM_TAG_VALUE))
                    || (keys.containsKey(OSMTags.RAILWAY_TAG) && keys.get(OSMTags.RAILWAY_TAG).equals(OSMTags.PLATFORM_TAG_VALUE));
        }

        return false;
    }

    public static boolean isHighwayPlatform(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            return keys.containsKey(OSMTags.HIGHWAY_TAG) && keys.get(OSMTags.HIGHWAY_TAG).equals(OSMTags.PLATFORM_TAG_VALUE);
        }

        return false;
    }

    public static boolean isRailwayPlatform(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            return keys.containsKey(OSMTags.RAILWAY_TAG) && keys.get(OSMTags.RAILWAY_TAG).equals(OSMTags.PLATFORM_TAG_VALUE);
        }

        return false;
    }

    public static boolean isElevator(Node node) {
        if (node != null) {
            TagMap keys = node.getKeys();
            return keys.containsKey(OSMTags.HIGHWAY_TAG) && keys.get(OSMTags.HIGHWAY_TAG).equals(OSMTags.ELEVATOR_TAG_VALUE);
        }

        return false;
    }

    public static boolean isSteps(Way way) {
        if (way != null) {
            TagMap keys = way.getKeys();
            return keys.containsKey(OSMTags.HIGHWAY_TAG) && keys.get(OSMTags.HIGHWAY_TAG).equals(OSMTags.STEPS_TAG_VALUE);
        }

        return false;
    }

    public static boolean isFootPath(Way way) {
        if (way != null) {
            TagMap keys = way.getKeys();
            return keys.containsKey(OSMTags.HIGHWAY_TAG) && keys.get(OSMTags.HIGHWAY_TAG).equals(OSMTags.FOOTWAY_TAG_VALUE);
        }

        return false;
    }

    public static boolean isRamp(Way way) {
        if (way != null) {
            TagMap keys = way.getKeys();

            return (keys.containsKey(OSMTags.RAMP_TAG) && keys.get(OSMTags.RAMP_TAG).equals(OSMTags.YES_TAG_VALUE))
                    || keys.containsKey(OSMTags.INCLINE_TAG);
        }

        return false;
    }

    public static String getUicRef(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            if (keys.containsKey(OSMTags.UIC_REF_TAG)) {
                return keys.get(OSMTags.UIC_REF_TAG);
            }
        }

        return null;
    }

    public static String getRef(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            if (keys.containsKey(OSMTags.REF_TAG)) {
                return keys.get(OSMTags.REF_TAG);
            }
            else if (keys.containsKey(OSMTags.LOCAL_REF_TAG)) {
                return keys.get(OSMTags.LOCAL_REF_TAG);
            }
        }

        return null;
    }

    public static String getLevel(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            if (keys.containsKey(OSMTags.LEVEL_TAG)) {
                return keys.get(OSMTags.LEVEL_TAG);
            }
        }

        return null;
    }

    public static String getIncline(OsmPrimitive primitive) {
        if (primitive != null) {
            TagMap keys = primitive.getKeys();

            if (keys.containsKey(OSMTags.INCLINE_TAG)) {
                return keys.get(OSMTags.INCLINE_TAG);
            }
        }

        return null;
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
        }

        return wheelChairLimitation;
    }

    public static QuayTypeEnumeration getQuayTypeEnumeration(OsmPrimitive primitive) {
        QuayTypeEnumeration quayTypeEnumeration;

        if (OSMHelper.isRailwayPlatform(primitive)) {
            quayTypeEnumeration = QuayTypeEnumeration.RAIL_PLATFORM;
        }
        else if (OSMHelper.isBusStop(primitive, false)) {
            quayTypeEnumeration = QuayTypeEnumeration.BUS_STOP;
        }
        else {
            quayTypeEnumeration = QuayTypeEnumeration.OTHER;
        }

        return quayTypeEnumeration;
    }

    public static String switchRefDelimiter(String quayRef) {
        if (quayRef != null) {
            return quayRef.replace(";", "/");
        }
        else {
            return quayRef;
        }
    }
}
