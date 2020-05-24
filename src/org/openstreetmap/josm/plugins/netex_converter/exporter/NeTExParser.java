/* 
 * Copyright (C) 2020 Labian Gashi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 */
package org.openstreetmap.josm.plugins.netex_converter.exporter;

import com.netex.model.AccessFeatureEnumeration;
import com.netex.model.AccessibilityAssessment;
import com.netex.model.AccessibilityLimitation;
import com.netex.model.AccessibilityLimitations_RelStructure;
import com.netex.model.CompositeFrame;
import com.netex.model.EquipmentPlace;
import com.netex.model.EquipmentPlaceRefStructure;
import com.netex.model.EquipmentPlaces_RelStructure;
import com.netex.model.Equipments_RelStructure;
import com.netex.model.Frames_RelStructure;
import com.netex.model.LevelRefStructure;
import com.netex.model.LiftEquipment;
import com.netex.model.LimitationStatusEnumeration;
import com.netex.model.LocationStructure;
import com.netex.model.MultilingualString;
import com.netex.model.ObjectFactory;
import com.netex.model.PathJunction;
import com.netex.model.PathLinkEndStructure;
import com.netex.model.PlaceRefStructure;
import com.netex.model.PointRefStructure;
import com.netex.model.PointRefs_RelStructure;
import com.netex.model.PrivateCodeStructure;
import com.netex.model.PublicationDeliveryStructure;
import com.netex.model.Quay;
import com.netex.model.QuayTypeEnumeration;
import com.netex.model.RampEquipment;
import com.netex.model.ResourceFrame;
import com.netex.model.SimplePoint_VersionStructure;
import com.netex.model.SiteFrame;
import com.netex.model.SitePathLink;
import com.netex.model.StaircaseEquipment;
import com.netex.model.StopPlace;
import com.netex.model.StopPlacesInFrame_RelStructure;
import com.netex.model.StopTypeEnumeration;
import com.netex.model.TypeOfPointRefStructure;
import com.netex.model.TypeOfPointRefs_RelStructure;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.opengis.gml._3.AbstractRingPropertyType;
import net.opengis.gml._3.DirectPositionListType;
import net.opengis.gml._3.LinearRingType;
import net.opengis.gml._3.PolygonType;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.netex_converter.model.josm.PrimitiveLogMessage;
import org.openstreetmap.josm.plugins.netex_converter.model.netex.Elevator;
import org.openstreetmap.josm.plugins.netex_converter.model.netex.FootPath;
import org.openstreetmap.josm.plugins.netex_converter.model.netex.Steps;
import org.openstreetmap.josm.plugins.netex_converter.util.OSMHelper;
import org.openstreetmap.josm.plugins.netex_converter.util.OSMTags;

/**
 *
 * @author Labian Gashi
 */
public class NeTExParser {

    private final ObjectFactory neTExFactory;
    private static final net.opengis.gml._3.ObjectFactory GML_FACTORY = new net.opengis.gml._3.ObjectFactory();

    public NeTExParser() {
        neTExFactory = new ObjectFactory();
    }

    public StopPlace createStopPlace(OsmPrimitive primitive, StopTypeEnumeration stopType) {
        TagMap keys = primitive.getKeys();

        String primitiveName = primitive.getName();

        if (primitiveName == null || primitiveName.trim().isEmpty()) {
            NeTExExporter.logMessage(primitive.getId(), primitive.getType(), new HashMap<String, String>() {
                {
                    put(PrimitiveLogMessage.Tags.NAME_TAG, PrimitiveLogMessage.Messages.NAME_TAG_MISSING_MESSAGE);
                }
            });
        }

        long primitiveId = primitive.getId();

        String uicRef = OSMHelper.getUicRef(primitive);

        if (uicRef == null || uicRef.trim().isEmpty()) {
            NeTExExporter.logMessage(primitive.getId(), primitive.getType(), new HashMap<String, String>() {
                {
                    put(PrimitiveLogMessage.Tags.UIC_REF_TAG, PrimitiveLogMessage.Messages.UIC_REF_MISSING_MESSAGE);
                }
            });
        }

        BigDecimal altitude = null;

        if (keys.containsKey(OSMTags.ELE_TAG)) {
            try {
                altitude = new BigDecimal(keys.get(OSMTags.ELE_TAG));
            }
            catch (NumberFormatException nfe) {
                NeTExExporter.logMessage(primitive.getId(), primitive.getType(), new HashMap<String, String>() {
                    {
                        put(PrimitiveLogMessage.Tags.ELE_TAG, PrimitiveLogMessage.Messages.ELE_TAG_NOT_NUMBER_MESSAGE);
                    }
                });
            }
        }

        LimitationStatusEnumeration wheelchairAccess = OSMHelper.getWheelchairLimitation(primitive);

        if (primitive instanceof Node) {
            Node node = (Node) primitive;

            LatLon coordinates = node.getCoor();
            if (coordinates == null) {
                NeTExExporter.logMessage(primitive.getId(), primitive.getType(), new HashMap<String, String>() {
                    {
                        put(PrimitiveLogMessage.Tags.UNKNOWN_COORDS_TAG, PrimitiveLogMessage.Messages.UNKNOWN_COORDS_MESSAGE);
                    }
                });

                return new StopPlace();
            }

            double lat = coordinates.lat();
            double lon = coordinates.lon();

            return new StopPlace()
                    .withId(String.format("ch:1:StopPlace:%s", uicRef != null && !uicRef.trim().isEmpty() ? uicRef : node.getId()))
                    .withName(new MultilingualString()
                            .withValue(primitiveName))
                    .withPrivateCode(new PrivateCodeStructure().withValue(String.format("org:osm:node:%s", node.getId())))
                    .withPublicCode(uicRef)
                    .withCentroid(new SimplePoint_VersionStructure()
                            .withLocation(new LocationStructure()
                                    .withLatitude(BigDecimal.valueOf(lat))
                                    .withLongitude(BigDecimal.valueOf(lon))
                                    .withAltitude(altitude)))
                    .withAccessibilityAssessment(new AccessibilityAssessment()
                            .withLimitations(new AccessibilityLimitations_RelStructure()
                                    .withAccessibilityLimitation(new AccessibilityLimitation()
                                            .withWheelchairAccess(wheelchairAccess))))
                    .withStopPlaceType(stopType);
        }
        else if (primitive instanceof Way) {

            return new StopPlace()
                    .withId(String.format("ch:1:StopPlace:%s", uicRef != null && !uicRef.trim().isEmpty() ? uicRef : primitiveId))
                    .withName(new MultilingualString()
                            .withValue(primitiveName))
                    .withPrivateCode(new PrivateCodeStructure().withValue(String.format("org:osm:way:%s", primitiveId)))
                    .withPublicCode(uicRef)
                    .withPolygon(createPolygonType(primitive))
                    .withAccessibilityAssessment(new AccessibilityAssessment()
                            .withLimitations(new AccessibilityLimitations_RelStructure()
                                    .withAccessibilityLimitation(new AccessibilityLimitation()
                                            .withWheelchairAccess(wheelchairAccess))))
                    .withStopPlaceType(stopType);
        }
        else if (primitive instanceof Relation) {

            return new StopPlace()
                    .withId(String.format("ch:1:StopPlace:%s", uicRef != null && !uicRef.trim().isEmpty() ? uicRef : primitiveId))
                    .withName(new MultilingualString()
                            .withValue(primitiveName))
                    .withPrivateCode(new PrivateCodeStructure().withValue(String.format("org:osm:relation:%s", primitiveId)))
                    .withPublicCode(uicRef)
                    .withPolygon(createPolygonType(primitive))
                    .withAccessibilityAssessment(new AccessibilityAssessment()
                            .withLimitations(new AccessibilityLimitations_RelStructure()
                                    .withAccessibilityLimitation(new AccessibilityLimitation()
                                            .withWheelchairAccess(wheelchairAccess))))
                    .withStopPlaceType(stopType);
        }
        else {
            return new StopPlace();
        }
    }

    public Quay createQuay(OsmPrimitive primitive) {
        long primitiveId = primitive.getId();

        QuayTypeEnumeration quayTypeEnumeration = OSMHelper.getQuayTypeEnumeration(primitive);

        if (primitive instanceof Node) {
            Node node = (Node) primitive;

            LatLon coordinates = node.getCoor();
            if (coordinates == null) {
                NeTExExporter.logMessage(primitive.getId(), primitive.getType(), new HashMap<String, String>() {
                    {
                        put(PrimitiveLogMessage.Tags.UNKNOWN_COORDS_TAG, PrimitiveLogMessage.Messages.UNKNOWN_COORDS_MESSAGE);
                    }
                });

                return new Quay();
            }

            double lat = coordinates.lat();
            double lon = coordinates.lon();

            return new Quay()
                    .withPrivateCode(new PrivateCodeStructure().withValue(String.format("org:osm:node:%s", primitiveId)))
                    .withCentroid(new SimplePoint_VersionStructure()
                            .withLocation(new LocationStructure()
                                    .withLatitude(BigDecimal.valueOf(lat))
                                    .withLongitude(BigDecimal.valueOf(lon))))
                    .withQuayType(quayTypeEnumeration);
        }
        else if (primitive instanceof Way) {
            return new Quay()
                    .withPrivateCode(new PrivateCodeStructure().withValue(String.format("org:osm:way:%s", primitiveId)))
                    .withPolygon(createPolygonType(primitive))
                    .withQuayType(quayTypeEnumeration);

        }
        else if (primitive instanceof Relation) {
            return new Quay()
                    .withPrivateCode(new PrivateCodeStructure().withValue(String.format("org:osm:relation:%s", primitiveId)))
                    .withPolygon(createPolygonType(primitive))
                    .withQuayType(quayTypeEnumeration);
        }
        else {
            return new Quay();
        }
    }

    public Elevator createElevator(Node node) {
        long nodeId = node.getId();
        LatLon coordinates = node.getCoor();

        if (coordinates == null) {
            NeTExExporter.logMessage(node.getId(), node.getType(), new HashMap<String, String>() {
                {
                    put(PrimitiveLogMessage.Tags.UNKNOWN_COORDS_TAG, PrimitiveLogMessage.Messages.UNKNOWN_COORDS_MESSAGE);
                }
            });

            return null;
        }

        PathJunction pathJunction = new PathJunction()
                .withId(String.format("ch:1:PathJunction:%s", node.getId()))
                .withLocation(new LocationStructure()
                        .withLatitude(BigDecimal.valueOf(coordinates.lat()))
                        .withLongitude(BigDecimal.valueOf(coordinates.lon())))
                .withTypes(new TypeOfPointRefs_RelStructure()
                        .withTypeOfPointRef(new TypeOfPointRefStructure()
                                .withRef("ch:1:TypeOfPoint:floor_change_endpoint")));

        EquipmentPlace equipmentPlace = new EquipmentPlace()
                .withId(String.format("ch:1:EquipmentPlace:%s", nodeId))
                .withMembers(new PointRefs_RelStructure()
                        .withPointRef(neTExFactory.createPointRef(new PointRefStructure()
                                .withRef(pathJunction.getId()))))
                .withPlaceEquipments(new Equipments_RelStructure()
                        .withEquipmentRefOrEquipment(neTExFactory.createLiftEquipment(new LiftEquipment()
                                .withId(String.format("ch:1:LiftEquipment:%s", nodeId)))));

        return new Elevator(pathJunction, equipmentPlace);
    }

    public FootPath createFootPath(Way way) {
        long wayId = way.getId();

        List<PathJunction> pathJunctions = new ArrayList<>();
        List<SitePathLink> sitePathLinks = new ArrayList<>();

        List<Node> nodes = way.getNodes();

        String level = OSMHelper.getLevel(way);
        String fromLevel = null;
        String toLevel = null;
        String endLevel = null;

        String[] levels = level != null ? level.split(";") : new String[0];

        String incline = OSMHelper.getIncline(way);

        if (levels.length == 2) {
            fromLevel = String.format("ch:1:Level:%s", levels[0].trim());
            toLevel = String.format("ch:1:Level:%s", levels[1].trim());
        }
        else if (level != null && !level.isEmpty() && OSMHelper.isNumeric(level)) {
            if (incline != null && !incline.isEmpty()) {
                if (incline.equals(OSMTags.DOWN_TAG_VALUE)) {
                    fromLevel = String.format("ch:1:Level:%s", Integer.parseInt(level) + 1);
                    toLevel = String.format("ch:1:Level:%s", level);
                }
                else if (incline.equals(OSMTags.UP_TAG_VALUE)) {
                    fromLevel = String.format("ch:1:Level:%s", Integer.parseInt(level) - 1);
                    toLevel = String.format("ch:1:Level:%s", level);
                }
            }
            else {
                endLevel = String.format("ch:1:Level:%s", level);
            }
        }

        for (Node node : nodes) {
            LatLon coordinates = node.getCoor();
            if (coordinates == null) {
                NeTExExporter.logMessage(node.getId(), node.getType(), new HashMap<String, String>() {
                    {
                        put(PrimitiveLogMessage.Tags.UNKNOWN_COORDS_TAG, PrimitiveLogMessage.Messages.UNKNOWN_COORDS_MESSAGE);
                    }
                });

                continue;
            }
            long nodeId = node.getId();

            String ref = "artificial_waypoint";

            if (node.equals(way.firstNode()) || node.equals(way.lastNode())) {
                ref = "floor_change_endpoint";
            }

            pathJunctions.add(new PathJunction()
                    .withId(String.format("ch:1:PathJunction:%s", nodeId))
                    .withLocation(new LocationStructure()
                            .withLatitude(BigDecimal.valueOf(coordinates.lat()))
                            .withLongitude(BigDecimal.valueOf(coordinates.lon())))
                    .withTypes(new TypeOfPointRefs_RelStructure()
                            .withTypeOfPointRef(new TypeOfPointRefStructure()
                                    .withRef(String.format("ch:1:TypeOfPoint:%s", ref)))));
        }

        LimitationStatusEnumeration wheelchairAccess = OSMHelper.getWheelchairLimitation(way);

        EquipmentPlace equipmentPlace = null;

        boolean isRamp = OSMHelper.isRamp(way);

        if (isRamp) {
            equipmentPlace = new EquipmentPlace()
                    .withId(String.format("ch:1:EquipmentPlace:%s", wayId))
                    .withPlaceEquipments(new Equipments_RelStructure()
                            .withEquipmentRefOrEquipment(neTExFactory.createRampEquipment(new RampEquipment()
                                    .withId(String.format("ch:1:RampEquipment:%s", way.getId())))));

            PointRefs_RelStructure pointRefs = new PointRefs_RelStructure();

            for (PathJunction pathJunction : pathJunctions) {
                pointRefs.withPointRef(Arrays.asList(
                        neTExFactory.createPointRef(new PointRefStructure()
                                .withRef(pathJunction.getId()))));
            }

            equipmentPlace.withMembers(pointRefs);

            for (int i = 0; i < pathJunctions.size() - 1; i++) {

                PathJunction firstJunction = pathJunctions.get(i);
                PathJunction secondJunction = pathJunctions.get(i + 1);

                SitePathLink sitePathLink = new SitePathLink()
                        .withId(String.format("ch:1:SitePathLink:%1$s%2$s", wayId, pathJunctions.size() > 2 ? "_" + (i + 1) : ""))
                        .withFrom(new PathLinkEndStructure()
                                .withPlaceRef(new PlaceRefStructure().
                                        withRef(firstJunction.getId())))
                        .withTo(new PathLinkEndStructure()
                                .withPlaceRef(new PlaceRefStructure()
                                        .withRef(secondJunction.getId())))
                        .withAccessibilityAssessment(new AccessibilityAssessment()
                                .withLimitations(new AccessibilityLimitations_RelStructure()
                                        .withAccessibilityLimitation(new AccessibilityLimitation()
                                                .withStepFreeAccess(LimitationStatusEnumeration.FALSE)
                                                .withWheelchairAccess(wheelchairAccess))))
                        .withAccessFeatureType(AccessFeatureEnumeration.RAMP)
                        .withEquipmentPlaces(new EquipmentPlaces_RelStructure()
                                .withEquipmentPlaceRefOrEquipmentPlace(new EquipmentPlaceRefStructure()
                                        .withRef(equipmentPlace.getId())));

                if (fromLevel != null && toLevel != null) {
                    if (i == 0) {
                        sitePathLink.withFrom(new PathLinkEndStructure()
                                .withLevelRef(new LevelRefStructure()
                                        .withRef(fromLevel)));
                    }

                    if (i == pathJunctions.size() - 2) {
                        sitePathLink.withTo(new PathLinkEndStructure()
                                .withLevelRef(new LevelRefStructure()
                                        .withRef(toLevel)));
                    }
                }
                else if (endLevel != null) {
                    sitePathLink.withLevelRef(new LevelRefStructure()
                            .withRef(endLevel));
                }

                sitePathLinks.add(sitePathLink);
            }
        }

        else {
            for (int i = 0; i < pathJunctions.size() - 1; i++) {

                PathJunction firstJunction = pathJunctions.get(i);
                PathJunction secondJunction = pathJunctions.get(i + 1);

                sitePathLinks.add(new SitePathLink()
                        .withId(String.format("ch:1:SitePathLink:%1$s%2$s", wayId, pathJunctions.size() > 2 ? "_" + (i + 1) : ""))
                        .withFrom(new PathLinkEndStructure()
                                .withPlaceRef(new PlaceRefStructure().
                                        withRef(firstJunction.getId())))
                        .withTo(new PathLinkEndStructure()
                                .withPlaceRef(new PlaceRefStructure()
                                        .withRef(secondJunction.getId())))
                        .withAccessFeatureType(AccessFeatureEnumeration.FOOTPATH));
            }
        }

        if (isRamp) {
            return new FootPath(pathJunctions, equipmentPlace, sitePathLinks);
        }

        else {
            return new FootPath(pathJunctions, sitePathLinks);
        }
    }

    public Steps createSteps(Way way) {
        long wayId = way.getId();

        List<PathJunction> pathJunctions = new ArrayList<>();
        List<SitePathLink> sitePathLinks = new ArrayList<>();

        LimitationStatusEnumeration wheelchairAccess = OSMHelper.getWheelchairLimitation(way);

        List<Node> nodes = way.getNodes();

        String level = OSMHelper.getLevel(way);
        String fromLevel = null;
        String toLevel = null;
        String endLevel = null;

        String[] levels = level != null ? level.split(";") : new String[0];

        String incline = OSMHelper.getIncline(way);

        if (levels.length == 2) {
            fromLevel = String.format("ch:1:Level:%s", levels[0].trim());
            toLevel = String.format("ch:1:Level:%s", levels[1].trim());
        }
        else if (level != null && !level.isEmpty() && OSMHelper.isNumeric(level)) {
            if (incline != null && !incline.isEmpty()) {
                if (incline.equals(OSMTags.DOWN_TAG_VALUE)) {
                    fromLevel = String.format("ch:1:Level:%s", Integer.parseInt(level) + 1);
                    toLevel = String.format("ch:1:Level:%s", level);
                }
                else if (incline.equals(OSMTags.UP_TAG_VALUE)) {
                    fromLevel = String.format("ch:1:Level:%s", Integer.parseInt(level) - 1);
                    toLevel = String.format("ch:1:Level:%s", level);
                }
            }
            else {
                endLevel = String.format("ch:1:Level:%s", level);
            }
        }

        for (Node node : nodes) {
            LatLon coordinates = node.getCoor();
            if (coordinates == null) {
                NeTExExporter.logMessage(node.getId(), node.getType(), new HashMap<String, String>() {
                    {
                        put(PrimitiveLogMessage.Tags.UNKNOWN_COORDS_TAG, PrimitiveLogMessage.Messages.UNKNOWN_COORDS_MESSAGE);
                    }
                });

                continue;
            }
            long nodeId = node.getId();

            String ref = "artificial_waypoint";

            if (node.equals(way.firstNode()) || node.equals(way.lastNode())) {
                ref = "floor_change_endpoint";
            }

            pathJunctions.add(new PathJunction()
                    .withId(String.format("ch:1:PathJunction:%s", nodeId))
                    .withLocation(new LocationStructure()
                            .withLatitude(BigDecimal.valueOf(coordinates.lat()))
                            .withLongitude(BigDecimal.valueOf(coordinates.lon())))
                    .withTypes(new TypeOfPointRefs_RelStructure()
                            .withTypeOfPointRef(new TypeOfPointRefStructure()
                                    .withRef(String.format("ch:1:TypeOfPoint:%s", ref)))));
        }

        EquipmentPlace equipmentPlace = new EquipmentPlace()
                .withId(String.format("ch:1:EquipmentPlace:%s", wayId))
                .withPlaceEquipments(new Equipments_RelStructure()
                        .withEquipmentRefOrEquipment(neTExFactory.createStaircaseEquipment(new StaircaseEquipment()
                                .withId(String.format("ch:1:StaircaseEquipment:%s", way.getId())))));

        PointRefs_RelStructure pointRefs = new PointRefs_RelStructure();

        for (PathJunction pathJunction : pathJunctions) {
            pointRefs.withPointRef(Arrays.asList(
                    neTExFactory.createPointRef(new PointRefStructure()
                            .withRef(pathJunction.getId()))));
        }

        equipmentPlace.withMembers(pointRefs);

        for (int i = 0; i < pathJunctions.size() - 1; i++) {
            PathJunction firstJunction = pathJunctions.get(i);
            PathJunction secondJunction = pathJunctions.get(i + 1);

            SitePathLink sitePathLink = new SitePathLink()
                    .withId(String.format("ch:1:SitePathLink:%1$s%2$s", wayId, pathJunctions.size() > 2 ? "_" + (i + 1) : ""))
                    .withFrom(new PathLinkEndStructure()
                            .withPlaceRef(new PlaceRefStructure().
                                    withRef(firstJunction.getId())))
                    .withTo(new PathLinkEndStructure()
                            .withPlaceRef(new PlaceRefStructure()
                                    .withRef(secondJunction.getId())))
                    .withAccessibilityAssessment(new AccessibilityAssessment()
                            .withLimitations(new AccessibilityLimitations_RelStructure()
                                    .withAccessibilityLimitation(new AccessibilityLimitation()
                                            .withStepFreeAccess(LimitationStatusEnumeration.FALSE)
                                            .withWheelchairAccess(wheelchairAccess))))
                    .withAccessFeatureType(AccessFeatureEnumeration.STAIRS)
                    .withEquipmentPlaces(new EquipmentPlaces_RelStructure()
                            .withEquipmentPlaceRefOrEquipmentPlace(new EquipmentPlaceRefStructure()
                                    .withRef(equipmentPlace.getId())));

            if (fromLevel != null && toLevel != null) {
                if (i == 0) {
                    sitePathLink.withFrom(new PathLinkEndStructure()
                            .withLevelRef(new LevelRefStructure()
                                    .withRef(fromLevel)));
                }

                if (i == pathJunctions.size() - 2) {
                    sitePathLink.withTo(new PathLinkEndStructure()
                            .withLevelRef(new LevelRefStructure()
                                    .withRef(toLevel)));
                }
            }
            else if (endLevel != null) {
                sitePathLink.withLevelRef(new LevelRefStructure()
                        .withRef(endLevel));
            }

            sitePathLinks.add(sitePathLink);
        }

        return new Steps(pathJunctions, equipmentPlace, sitePathLinks);
    }

    public CompositeFrame createCompositeFrame(ResourceFrame resourceFrame, SiteFrame siteFrame) {
        return new CompositeFrame()
                .withId("ch:1:CompositeFrame")
                .withFrames(new Frames_RelStructure().withCommonFrame(Arrays.asList(
                        neTExFactory.createResourceFrame(resourceFrame),
                        neTExFactory.createSiteFrame(siteFrame))));
    }

    public SiteFrame createSiteFrame(ArrayList<StopPlace> stopPlaces) {
        return new SiteFrame()
                .withId("ch:1:SiteFrame")
                .withStopPlaces(new StopPlacesInFrame_RelStructure()
                        .withStopPlace(stopPlaces));
    }

    public ResourceFrame createResourceFrame() {
        return new ResourceFrame().withId("ch:1:ResourceFrame");
    }

    public PublicationDeliveryStructure createPublicationDeliveryStsructure(CompositeFrame compositeFrame) {
        return new PublicationDeliveryStructure()
                .withDescription(new MultilingualString()
                        .withValue("OSM to NeTEx")
                        .withLang("en"))
                .withPublicationTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .withParticipantRef("participantRef")
                .withDataObjects(new PublicationDeliveryStructure.DataObjects()
                        .withCompositeFrameOrCommonFrame(Arrays.asList(
                                neTExFactory.createCompositeFrame(compositeFrame))));
    }

    public PolygonType createPolygonType(OsmPrimitive primitive) {
        PolygonType polygonType = null;
        long primitiveId = primitive.getId();

        if (primitive instanceof Way) {
            Way way = (Way) primitive;

            LinearRingType linearRing = new LinearRingType();

            for (Node node : way.getNodes()) {
                LatLon nodeCoord = node.getCoor();
                if (nodeCoord == null) {
                    NeTExExporter.logMessage(node.getId(), node.getType(), new HashMap<String, String>() {
                        {
                            put(PrimitiveLogMessage.Tags.UNKNOWN_COORDS_TAG, PrimitiveLogMessage.Messages.UNKNOWN_COORDS_MESSAGE);
                        }
                    });

                    continue;
                }

                linearRing.withPosOrPointProperty(Arrays.asList(new DirectPositionListType().withValue(nodeCoord.lat(), nodeCoord.lon())));
            }

            polygonType = new PolygonType()
                    .withId(String.format("org:osm:way:%s", primitiveId))
                    .withExterior(new AbstractRingPropertyType()
                            .withAbstractRing(GML_FACTORY.createLinearRing(linearRing)));
        }
        else if (primitive instanceof Relation) {
            Relation relation = (Relation) primitive;

            polygonType = new PolygonType()
                    .withId(String.format("org:osm:relation:%s", primitiveId));

            for (RelationMember relationMember : relation.getMembers()) {
                String role = relationMember.getRole();

                if (role != null && !role.isEmpty()) {
                    switch (role) {
                        case OSMHelper.INNER_ROLE:
                            LinearRingType linearRingInterior = new LinearRingType();

                            if (relationMember.getMember() instanceof Way) {
                                Way relationWay = relationMember.getWay();

                                for (Node node : relationWay.getNodes()) {
                                    LatLon coord = node.getCoor();
                                    if (coord == null) {
                                        NeTExExporter.logMessage(node.getId(), node.getType(), new HashMap<String, String>() {
                                            {
                                                put(PrimitiveLogMessage.Tags.UNKNOWN_COORDS_TAG, PrimitiveLogMessage.Messages.UNKNOWN_COORDS_MESSAGE);
                                            }
                                        });

                                        continue;
                                    }

                                    linearRingInterior.withPosOrPointProperty(Arrays.asList(new DirectPositionListType().withValue(coord.lat(), coord.lon())));
                                }

                                polygonType.withInterior(new AbstractRingPropertyType()
                                        .withAbstractRing(GML_FACTORY.createLinearRing(linearRingInterior)));
                            }
                            break;
                        case OSMHelper.OUTER_ROLE:
                            LinearRingType linearRingExterior = new LinearRingType();

                            if (relationMember.getMember() instanceof Way) {
                                Way relationWay = relationMember.getWay();

                                for (Node node : relationWay.getNodes()) {
                                    LatLon coord = node.getCoor();
                                    if (coord == null) {
                                        NeTExExporter.logMessage(node.getId(), node.getType(), new HashMap<String, String>() {
                                            {
                                                put(PrimitiveLogMessage.Tags.UNKNOWN_COORDS_TAG, PrimitiveLogMessage.Messages.UNKNOWN_COORDS_MESSAGE);
                                            }
                                        });

                                        continue;
                                    }

                                    linearRingExterior.withPosOrPointProperty(Arrays.asList(new DirectPositionListType().withValue(coord.lat(), coord.lon())));
                                }

                                polygonType.withExterior(new AbstractRingPropertyType()
                                        .withAbstractRing(GML_FACTORY.createLinearRing(linearRingExterior)));
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        return polygonType;
    }
}
