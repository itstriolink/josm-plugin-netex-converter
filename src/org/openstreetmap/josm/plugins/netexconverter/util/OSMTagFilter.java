package org.openstreetmap.josm.plugins.netexconverter.util;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.plugins.netexconverter.util.rule.ElevatorRule;
import org.openstreetmap.josm.plugins.netexconverter.util.rule.FilterRule;
import org.openstreetmap.josm.plugins.netexconverter.util.rule.FootPathRule;
import org.openstreetmap.josm.plugins.netexconverter.util.rule.PlatformRule;
import org.openstreetmap.josm.plugins.netexconverter.util.rule.RampRule;
import org.openstreetmap.josm.plugins.netexconverter.util.rule.StepsRule;
import org.openstreetmap.josm.plugins.netexconverter.util.rule.StopOrStationRule;

/**
 * helper class/methods for filtering OSM data according to the Rules defined in org.openstreetmap.josm.plugins.netexconverter.util.rule
 */
public class OSMTagFilter {

    private static final List<FilterRule> RULES = new ArrayList<>();
	 
    static {
        RULES.add(new StopOrStationRule());
        RULES.add(new PlatformRule());
        RULES.add(new ElevatorRule());
        RULES.add(new StepsRule());
        RULES.add(new RampRule());
        RULES.add(new FootPathRule());
    }
 
    public static boolean primitiveIsRelevantForExport(OsmPrimitive primitive) {
        return RULES.stream()
        			.filter(rule -> rule.evaluate(primitive))
        			.findFirst()
        			.isPresent();
    }
	

}
