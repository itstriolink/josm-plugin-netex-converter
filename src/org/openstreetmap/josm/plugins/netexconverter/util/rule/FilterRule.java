package org.openstreetmap.josm.plugins.netexconverter.util.rule;

import org.openstreetmap.josm.data.osm.OsmPrimitive;

public interface FilterRule {

    boolean evaluate(OsmPrimitive primitive);
}
