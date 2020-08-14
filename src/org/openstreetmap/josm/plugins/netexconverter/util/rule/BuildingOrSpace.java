package org.openstreetmap.josm.plugins.netexconverter.util.rule;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.netexconverter.util.OSMHelper;
import org.openstreetmap.josm.plugins.netexconverter.util.OSMTags;

public class BuildingOrSpace implements FilterRule {

    @Override
    public boolean evaluate(OsmPrimitive primitive) {
        return OSMHelper.hasKeyWithValue(primitive, OSMTags.BUILDING_TAG, OSMTags.TRAIN_STATION_TAG_VALUE)
                || OSMHelper.hasKeyWithValue(primitive, OSMTags.SHELTER_TYPE_TAG, OSMTags.PUBLIC_TRANSPORT_TAG_VALUE);
    }

}
