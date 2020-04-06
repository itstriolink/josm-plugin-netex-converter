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
import com.netex.model.Level;
import com.netex.model.LevelRefStructure;
import com.netex.model.LiftEquipment;
import com.netex.model.LimitationStatusEnumeration;
import com.netex.model.LocationStructure;
import com.netex.model.MultilingualString;
import com.netex.model.ObjectFactory;
import com.netex.model.PathJunction;
import com.netex.model.PathJunctions_RelStructure;
import com.netex.model.PathLink;
import com.netex.model.PathLinkEndStructure;
import com.netex.model.PlaceRefStructure;
import com.netex.model.PointRefStructure;
import com.netex.model.PointRefs_RelStructure;
import com.netex.model.PrivateCodeStructure;
import com.netex.model.PublicationDeliveryStructure;
import com.netex.model.Quay;
import com.netex.model.QuayTypeEnumeration;
import com.netex.model.ResourceFrame;
import com.netex.model.SimplePoint_VersionStructure;
import com.netex.model.SiteFrame;
import com.netex.model.SitePathLink;
import com.netex.model.SitePathLinks_RelStructure;
import com.netex.model.StaircaseEquipment;
import com.netex.model.StaircaseEquipmentRefStructure;
import com.netex.model.StopPlace;
import com.netex.model.StopPlacesInFrame_RelStructure;
import com.netex.model.StopTypeEnumeration;
import com.netex.model.TypeOfPointRefStructure;
import com.netex.model.TypeOfPointRefs_RelStructure;
import com.netex.model.ZoneRefStructure;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.plugins.netex_converter.model.Elevator;
import org.openstreetmap.josm.plugins.netex_converter.model.FootPath;
import org.openstreetmap.josm.plugins.netex_converter.model.Steps;
import org.openstreetmap.josm.plugins.netex_converter.util.OSMHelper;
import org.openstreetmap.josm.plugins.netex_converter.util.OSMTags;

/**
 *
 * @author Labian Gashi
 */
public class NeTExParser
{

    private final ObjectFactory neTExFactory;

    private final Level level_minus_2 = new Level();
    private final Level level_minus_1 = new Level();
    private final Level level_0 = new Level();
    private final Level level_1 = new Level();
    private final Level level_2 = new Level();

    public NeTExParser()
    {
        neTExFactory = new ObjectFactory();
    }

    public StopPlace createStopPlace(Node node, StopTypeEnumeration stopType)
    {
        return createStopPlace(node, stopType, null);
    }

    public StopPlace createStopPlace(Node node, StopTypeEnumeration stopType, ArrayList<PathLink> pathLinks)
    {
        return createStopPlace(node, stopType, pathLinks, null);
    }

    public StopPlace createStopPlace(Node node, StopTypeEnumeration stopType, ArrayList<PathLink> pathLinks, ArrayList<PathJunction> pathJunctions)
    {
        TagMap keys = node.getKeys();
        LatLon coordinates = node.getCoor();

        double lat = coordinates.lat();
        double lon = coordinates.lon();

        String nodeName = node.getName();

        String uic_ref = "";
        if (keys.containsKey(OSMTags.UIC_REF_TAG))
        {
            uic_ref = keys.get(OSMTags.UIC_REF_TAG);
        }

        BigDecimal altitude = null;

        if (keys.containsKey(OSMTags.ELE_TAG))
        {
            try
            {
                altitude = new BigDecimal(keys.get(OSMTags.ELE_TAG));
            }
            catch (NumberFormatException nfe)
            {

            }
        }

        LimitationStatusEnumeration wheelchairAccess = LimitationStatusEnumeration.UNKNOWN;

        if (keys.containsKey(OSMTags.WHEELCHAIR_TAG))
        {
            String wheelchairTagAccess = keys.get(OSMTags.WHEELCHAIR_TAG);

            switch (wheelchairTagAccess.toLowerCase())
            {
                case "yes":
                    wheelchairAccess = LimitationStatusEnumeration.TRUE;
                    break;
                case "limited":
                    wheelchairAccess = LimitationStatusEnumeration.PARTIAL;
                    break;
                case "no":
                    wheelchairAccess = LimitationStatusEnumeration.FALSE;
                    break;
                default:
                    wheelchairAccess = LimitationStatusEnumeration.UNKNOWN;
                    break;

            }
        }

        return new StopPlace()
                .withId(String.format("ch:1:StopPlace:%s", uic_ref))
                .withName(new MultilingualString()
                        .withValue(nodeName))
                .withPrivateCode(new PrivateCodeStructure().withValue(String.format("org:osm:node:%s", node.getId())))
                .withPublicCode(uic_ref)
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
//                .withPathLinks(new SitePathLinks_RelStructure().withPathLinkRefOrSitePathLink(pathLinks))
//                .withPathJunctions(new PathJunctions_RelStructure().withPathJunctionRefOrPathJunction(pathJunctions));
    }

    public Elevator createElevator(Node node)
    {
        long nodeId = node.getId();
        LatLon coordinates = node.getCoor();

        PathJunction pathJunction = new PathJunction()
                .withId(String.format("ch:1:PathJunction:%s", node.getId()))
                .withLocation(new LocationStructure()
                        .withLatitude(BigDecimal.valueOf(coordinates.lat()))
                        .withLongitude(BigDecimal.valueOf(coordinates.lon())))
                .withTypes(new TypeOfPointRefs_RelStructure()
                        .withTypeOfPointRef(new TypeOfPointRefStructure()
                                .withRef("ch:1:TypeOfPoint:floor_change_endpoint")));
        /* missing quay parentZoneRef() */

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

    public FootPath createFootPath(Way way)
    {
        long wayId = way.getId();
        ArrayList<PathJunction> pathJunctions = new ArrayList<>();

        String fromId = null;
        String toId = null;
        boolean start = true;

        List<Node> nodes = Arrays.asList(way.firstNode(), way.lastNode());

        for (Node node : nodes)
        {
            LatLon coordinates = node.getCoor();
            long nodeId = node.getId();

            PathJunction pathJunction = new PathJunction()
                    .withId(String.format("ch:1:PathJunction:%s", nodeId))
                    .withLocation(new LocationStructure()
                            .withLatitude(BigDecimal.valueOf(coordinates.lat()))
                            .withLongitude(BigDecimal.valueOf(coordinates.lon())))
                    .withTypes(new TypeOfPointRefs_RelStructure()
                            .withTypeOfPointRef(new TypeOfPointRefStructure()
                                    .withRef("ch:1:TypeOfPoint:artificial_waypoint")));

            pathJunctions.add(pathJunction);

            if (start)
            {
                fromId = pathJunction.getId();
                start = false;
            }
            else
            {
                toId = pathJunction.getId();
            }
        }

        String level = OSMHelper.getLevel(way);

        Level currentLevel = null;

        switch (level != null ? level : "")
        {
            case "-2":
                currentLevel = level_minus_2;
                break;
            case "-1":
                currentLevel = level_minus_1;
                break;
            case "0":
                currentLevel = level_0;
                break;
            case "1":
                currentLevel = level_1;
                break;
            case "2":
                currentLevel = level_2;
                break;
            default:
                currentLevel = null;
                break;
        }

        String levelId = currentLevel != null ? currentLevel.getId() : null;

        SitePathLink sitePathLink = new SitePathLink()
                .withId(String.format("ch:1:SitePathLink:%s", wayId))
                .withFrom(new PathLinkEndStructure()
                        .withPlaceRef(new PlaceRefStructure().
                                withRef(fromId)))
                .withTo(new PathLinkEndStructure()
                        .withPlaceRef(new PlaceRefStructure()
                                .withRef(toId)))
                .withAccessFeatureType(AccessFeatureEnumeration.FOOTPATH)
                .withLevelRef(new LevelRefStructure()
                        .withRef(levelId));

        return new FootPath(pathJunctions, sitePathLink);
    }

    public Steps createSteps(Way way)
    {
        long wayId = way.getId();
        ArrayList<PathJunction> pathJunctions = new ArrayList<>();

        String fromId = null;
        String toId = null;

        boolean start = true;

        List<Node> nodes = Arrays.asList(way.firstNode(), way.lastNode());

        for (Node node : nodes)
        {
            LatLon coordinates = node.getCoor();
            long nodeId = node.getId();

            PathJunction pathJunction = new PathJunction()
                    .withId(String.format("ch:1:PathJunction:%s", nodeId))
                    .withLocation(new LocationStructure()
                            .withLatitude(BigDecimal.valueOf(coordinates.lat()))
                            .withLongitude(BigDecimal.valueOf(coordinates.lon())))
                    .withTypes(new TypeOfPointRefs_RelStructure()
                            .withTypeOfPointRef(new TypeOfPointRefStructure()
                                    .withRef("ch:1:TypeOfPoint:floor_change_endpoint")));
            /* missing quay */

            pathJunctions.add(pathJunction);

            if (start)
            {
                fromId = pathJunction.getId();
                start = false;
            }
            else
            {
                toId = pathJunction.getId();
            }
        }

        EquipmentPlace equipmentPlace = new EquipmentPlace()
                .withId(String.format("ch:1:EquipmentPlace:%s", wayId))
                .withMembers(new PointRefs_RelStructure()
                        .withPointRef(Arrays.asList(
                                neTExFactory.createPointRef(
                                        new PointRefStructure().withRef(fromId)),
                                neTExFactory.createPointRef(new PointRefStructure().withRef(toId)))))
                .withPlaceEquipments(new Equipments_RelStructure()
                        .withEquipmentRefOrEquipment(neTExFactory.createStaircaseEquipment(new StaircaseEquipment()
                                .withId(String.format("ch:1:StaircaseEquipment:%s", way.getId())))));

        SitePathLink sitePathLink = new SitePathLink()
                .withId(String.format("ch:1:SitePathLink:%s", wayId))
                .withFrom(new PathLinkEndStructure()
                        .withPlaceRef(new PlaceRefStructure().
                                withRef(fromId))
                        .withLevelRef(new LevelRefStructure()
                                .withRef(/*fromLevel*/level_minus_1.getId())))
                .withTo(new PathLinkEndStructure()
                        .withPlaceRef(new PlaceRefStructure()
                                .withRef(toId))
                        .withLevelRef(new LevelRefStructure()
                                .withRef(/*endLevel*/level_0.getId())))
                .withAccessibilityAssessment(new AccessibilityAssessment()
                        .withLimitations(new AccessibilityLimitations_RelStructure()
                                .withAccessibilityLimitation(new AccessibilityLimitation()
                                        .withStepFreeAccess(LimitationStatusEnumeration.UNKNOWN)
                                        .withWheelchairAccess(LimitationStatusEnumeration.UNKNOWN))))
                .withAccessFeatureType(AccessFeatureEnumeration.STAIRS)
                .withEquipmentPlaces(new EquipmentPlaces_RelStructure()
                        .withEquipmentPlaceRefOrEquipmentPlace(new EquipmentPlaceRefStructure()
                                .withRef(equipmentPlace.getId())));

        return new Steps(pathJunctions, equipmentPlace, sitePathLink);
    }

    public CompositeFrame createCompositeFrame(ResourceFrame resourceFrame, SiteFrame siteFrame)
    {
        return new CompositeFrame()
                .withId("ch:1:CompositeFrame")
                .withFrames(new Frames_RelStructure().withCommonFrame(Arrays.asList(
                        neTExFactory.createResourceFrame(resourceFrame),
                        neTExFactory.createSiteFrame(siteFrame))));
    }

    public SiteFrame createSiteFrame(ArrayList<StopPlace> stopPlaces)
    {
        return new SiteFrame()
                .withId("ch:1:SiteFrame")
                .withStopPlaces(new StopPlacesInFrame_RelStructure()
                        .withStopPlace(stopPlaces));
    }

    public ResourceFrame createResourceFrame()
    {
        return new ResourceFrame().withId("ch:1:ResourceFrame");
    }

    public PublicationDeliveryStructure createPublicationDeliveryStsructure(CompositeFrame compositeFrame)
    {
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

    public Quay createPlatform(Node node)
    {
        long nodeId = node.getId();

        LatLon coordinates = node.getCoor();
        double lat = coordinates.lat();
        double lon = coordinates.lon();

        QuayTypeEnumeration quayTypeEnumeration;

        if (OSMHelper.isBusStation(node, false))
        {
            quayTypeEnumeration = QuayTypeEnumeration.BUS_PLATFORM;
        }
        else if (OSMHelper.isBusStop(node, false))
        {
            quayTypeEnumeration = QuayTypeEnumeration.BUS_STOP;
        }
        else if (OSMHelper.isTrainStation(node, false))
        {
            quayTypeEnumeration = QuayTypeEnumeration.RAIL_PLATFORM;
        }
        else
        {
            quayTypeEnumeration = QuayTypeEnumeration.OTHER;
        }

        TagMap keys = node.getKeys();

        return new Quay()
                .withPrivateCode(new PrivateCodeStructure().withValue(String.format("org:osm:node:%s", nodeId)))
                .withCentroid(new SimplePoint_VersionStructure()
                        .withLocation(new LocationStructure()
                                .withLatitude(BigDecimal.valueOf(lat))
                                .withLongitude(BigDecimal.valueOf(lon))))
                .withQuayType(quayTypeEnumeration);
    }
}
