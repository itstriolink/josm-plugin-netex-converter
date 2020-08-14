package org.openstreetmap.josm.plugins.netexconverter.util.rule;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.netexconverter.util.OSMHelper;
import org.openstreetmap.josm.plugins.netexconverter.util.OSMTags;

public class StepsRule implements FilterRule {

    @Override
    public boolean evaluate(OsmPrimitive primitive) {
        return OSMHelper.hasKeyWithValue(primitive, OSMTags.HIGHWAY_TAG, OSMTags.STEPS_TAG_VALUE)
                || OSMHelper.hasKeyWithValue(primitive, OSMTags.HIGHWAY_TAG, OSMTags.STAIRS_TAG_VALUE)
                || OSMHelper.hasKeyWithValue(primitive, OSMTags.HIGHWAY_TAG, OSMTags.STAIRWELL_TAG_VALUE);
    }

}
