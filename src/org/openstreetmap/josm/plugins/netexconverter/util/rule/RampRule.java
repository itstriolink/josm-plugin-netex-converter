package org.openstreetmap.josm.plugins.netexconverter.util.rule;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.netexconverter.util.OSMHelper;
import org.openstreetmap.josm.plugins.netexconverter.util.OSMTags;

public class RampRule implements FilterRule {

    @Override
    public boolean evaluate(OsmPrimitive primitive) {
        return OSMHelper.hasKeyWithValue(primitive, OSMTags.RAMP_TAG, OSMTags.YES_TAG_VALUE)
                || (OSMHelper.hasKey(primitive, OSMTags.INCLINE_TAG)
                && !OSMHelper.hasKeyWithValue(primitive, OSMTags.HIGHWAY_TAG, OSMTags.STEPS_TAG_VALUE)
                && !OSMHelper.hasKeyWithValue(primitive, OSMTags.HIGHWAY_TAG, OSMTags.RESIDENTIAL_TAG_VALUE)
                && !OSMHelper.hasKeyWithValue(primitive, OSMTags.HIGHWAY_TAG, OSMTags.UNCLASSIFIED_TAG_VALUE));
    }

}
