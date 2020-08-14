package org.openstreetmap.josm.plugins.netexconverter.util.rule;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.netexconverter.util.OSMHelper;
import org.openstreetmap.josm.plugins.netexconverter.util.OSMTags;

public class FootPathRule implements FilterRule {

    @Override
    public boolean evaluate(OsmPrimitive primitive) {
        return OSMHelper.hasKeyWithValue(primitive, OSMTags.HIGHWAY_TAG, OSMTags.FOOTWAY_TAG_VALUE)
                || OSMHelper.hasKeyWithValue(primitive, OSMTags.FOOTWAY_TAG_VALUE, OSMTags.SIDEWALK_TAG_VALUE)
                || (OSMHelper.hasKeyWithValue(primitive, OSMTags.HIGHWAY_TAG, OSMTags.PEDESTRIAN_TAG_VALUE)
                && !OSMHelper.hasKey(primitive, OSMTags.BUILDING_TAG))
                || (OSMHelper.hasKeyWithValue(primitive, OSMTags.FOOT_TAG, OSMTags.YES_TAG_VALUE)
                && !OSMHelper.hasKeyWithValue(primitive, OSMTags.HIGHWAY_TAG, OSMTags.SERVICE_TAG_VALUE)
                && !OSMHelper.hasKeyWithValue(primitive, OSMTags.HIGHWAY_TAG, OSMTags.RESIDENTIAL_TAG_VALUE));
    }

}
