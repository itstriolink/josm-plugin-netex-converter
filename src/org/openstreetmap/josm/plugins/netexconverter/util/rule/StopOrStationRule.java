package org.openstreetmap.josm.plugins.netexconverter.util.rule;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.netexconverter.util.OSMHelper;
import org.openstreetmap.josm.plugins.netexconverter.util.OSMTags;

public class StopOrStationRule implements FilterRule {

	@Override
	public boolean evaluate(OsmPrimitive primitive) {
		return OSMHelper.hasKeyWithValue(primitive, OSMTags.RAILWAY_TAG, OSMTags.STATION_TAG_VALUE)
				|| OSMHelper.hasKeyWithValue(primitive, OSMTags.RAILWAY_TAG, OSMTags.STOP_TAG_VALUE)
				|| OSMHelper.hasKeyWithValue(primitive, OSMTags.RAILWAY_TAG, OSMTags.HALT_TAG_VALUE)
				|| OSMHelper.hasKeyWithValue(primitive, OSMTags.RAILWAY_TAG, OSMTags.TRAM_STOP_TAG_VALUE)
				|| OSMHelper.hasKeyWithValue(primitive, OSMTags.HIGHWAY_TAG, OSMTags.BUS_STOP_TAG_VALUE)
				|| OSMHelper.hasKeyWithValue(primitive, OSMTags.AMENITY_TAG, OSMTags.BUS_STATION_TAG_VALUE)
				|| OSMHelper.hasKeyWithValue(primitive, OSMTags.AMENITY_TAG, OSMTags.FERRY_TERMINAL_TAG_VALUE);
	}

}
