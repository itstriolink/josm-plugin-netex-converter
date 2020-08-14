package org.openstreetmap.josm.plugins.netexconverter.util.rule;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.netexconverter.util.OSMHelper;
import org.openstreetmap.josm.plugins.netexconverter.util.OSMTags;

public class PlatformRule implements FilterRule {

	@Override
	public boolean evaluate(OsmPrimitive primitive) {
		return OSMHelper.hasKeyWithValue(primitive, OSMTags.PUBLIC_TRANSPORT_TAG, OSMTags.PLATFORM_TAG_VALUE)
				|| OSMHelper.hasKeyWithValue(primitive, OSMTags.RAILWAY_TAG, OSMTags.PLATFORM_TAG_VALUE)
				|| OSMHelper.hasKeyWithValue(primitive, OSMTags.HIGHWAY_TAG, OSMTags.PLATFORM_TAG_VALUE);
	}

}
