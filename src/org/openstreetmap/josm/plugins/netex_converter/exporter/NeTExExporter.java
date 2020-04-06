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

import org.apache.log4j.Logger;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.netex.model.*;
import com.netex.model.PublicationDeliveryStructure.DataObjects;
import com.netex.validation.NeTExValidator;
import java.awt.Point;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import javax.swing.JOptionPane;
import javax.xml.transform.stream.StreamSource;

import jaxb.CustomMarshaller;
import net.opengis.gml._3.AbstractRingPropertyType;
import net.opengis.gml._3.DirectPositionType;
import net.opengis.gml._3.LinearRingType;
import net.opengis.gml._3.PolygonType;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.netex_converter.util.OSMHelper;
import static org.openstreetmap.josm.tools.I18n.tr;

import org.locationtech.jts.geom.Geometry;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.TagMap;
import org.openstreetmap.josm.gui.MapView;
import static org.openstreetmap.josm.gui.NavigatableComponent.PROP_SNAP_DISTANCE;
import org.openstreetmap.josm.plugins.netex_converter.model.Elevator;
import org.openstreetmap.josm.plugins.netex_converter.model.FootPath;
import org.openstreetmap.josm.plugins.netex_converter.model.Steps;
import org.openstreetmap.josm.plugins.netex_converter.util.OSMTags;
import org.xml.sax.SAXException;

/**
 *
 * @author Labian Gashi
 */
public class NeTExExporter
{

    private final static Logger LOGGER = Logger.getLogger(NeTExExporter.class);
    private final NeTExParser neTExParser;
    private final ObjectFactory neTExFactory;
    private final CustomMarshaller customMarshaller;
    private final net.opengis.gml._3.ObjectFactory gmlFactory;
    private final HashMap<Node, StopPlace> stopPlaces;
    private final HashMap<Node, Quay> quays;
    private final HashMap<Node, Elevator> elevators;
    private final HashMap<Way, Steps> steps;
    private final HashMap<Way, FootPath> footPaths;
    private final DataSet ds;
    //private final NeTExValidator neTExValidator = NeTExValidator.getNeTExValidator();

    public NeTExExporter()
    {
        neTExParser = new NeTExParser();
        neTExFactory = new ObjectFactory();
        gmlFactory = new net.opengis.gml._3.ObjectFactory();
        customMarshaller = new CustomMarshaller(PublicationDeliveryStructure.class);
        stopPlaces = new HashMap<>();
        quays = new HashMap<>();
        elevators = new HashMap<>();
        steps = new HashMap<>();
        footPaths = new HashMap<>();
        ds = MainApplication.getLayerManager().getActiveDataSet();
    }

    public void exportToNeTEx(File neTExFile)
    {
        Collection<OsmPrimitive> primitives = null;

        if (ds == null)
        {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("No data has been loaded into JOSM"));
            return;
        }
        else
        {
            primitives = ds.allNonDeletedPrimitives();
        }

        for (OsmPrimitive primitive : primitives)
        {
            if (primitive instanceof Node)
            {
                Node node = (Node) primitive;

                if (OSMHelper.isTrainStation(node))
                {

                    stopPlaces.put(node, neTExParser.createStopPlace(node, StopTypeEnumeration.RAIL_STATION));
                }
                else if (OSMHelper.isBusStation(node))
                {
                    stopPlaces.put(node, neTExParser.createStopPlace(node, StopTypeEnumeration.BUS_STATION));
                }
                else if (OSMHelper.isBusStop(node))
                {
                    stopPlaces.put(node, neTExParser.createStopPlace(node, StopTypeEnumeration.ONSTREET_BUS));
                }
                else if (OSMHelper.isPlatform(node))
                {
                    quays.put(node, neTExParser.createPlatform(node));
                }
                else if (OSMHelper.isElevator(node))
                {
                    elevators.put(node, neTExParser.createElevator(node));
                }
                else
                {

                }

            }
            else if (primitive instanceof Way)
            {
                Way way = (Way) primitive;
                if (OSMHelper.isSteps(way))
                {
                    steps.put(way, neTExParser.createSteps(way));
                }
                else if (OSMHelper.isFootPath(way))
                {
                    footPaths.put(way, neTExParser.createFootPath(way));
                }
                else if (OSMHelper.isPlatform(way))
                {

                }
            }
            else if (primitive instanceof Relation)
            {

            }
            else
            {
                LOGGER.warn(tr("The OSM primitive type could not be determined."));
            }
        }

        for (HashMap.Entry<Node, StopPlace> stopEntry : stopPlaces.entrySet())
        {
            StopPlace stopPlace = stopEntry.getValue();
            boolean modified = false;

            Quays_RelStructure currentQuays = new Quays_RelStructure();

            for (HashMap.Entry<Node, Quay> quayEntry : quays.entrySet())
            {
                String uic_ref = OSMHelper.getUicRef(quayEntry.getKey());

                String nodeRef = quayEntry.getKey().getKeys().containsKey(OSMTags.REF_TAG) ? quayEntry.getKey().getKeys().get(OSMTags.REF_TAG) : null;

                if (uic_ref != null && !uic_ref.trim().isEmpty())
                {
                    if (OSMHelper.getUicRef(stopEntry.getKey()).equals(uic_ref))
                    {
                        currentQuays.withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                .withId(String.format("ch:1:Quay:%1$s:%2$s", uic_ref, nodeRef))
                                .withPublicCode(nodeRef)));
                        modified = true;
                    }
                }
                else
                {
                    /*Point point = MainApplication.getMap().mapView.getPoint(entry.getValue().getCoor());
                    Node nearestNode = MainApplication.getMap().mapView.getNearestNode(point, OsmPrimitive::isTagged, false);
                    ... */
                }
            }

            if (modified)
            {
                stopPlaces.replace(stopEntry.getKey(), stopPlace.withQuays(currentQuays));
            }
        }

        for (HashMap.Entry<Node, StopPlace> stopPlaceEntry : stopPlaces.entrySet())
        {
            boolean modified = false;

            SitePathLinks_RelStructure currentPathLinks = new SitePathLinks_RelStructure();
            PathJunctions_RelStructure currentPathJunctions = new PathJunctions_RelStructure();
            EquipmentPlaces_RelStructure currentEquipmentPlaces = new EquipmentPlaces_RelStructure();

            for (HashMap.Entry<Node, Elevator> elevatorEntry : elevators.entrySet())
            {
                Point point = MainApplication.getMap().mapView.getPoint(elevatorEntry.getKey().getCoor());
                List<Node> closestNodes = getAllNearestNodes(point, OsmPrimitive::isTagged);

                for (Node node : closestNodes)
                {
                    if (node.getId() == stopPlaceEntry.getKey().getId())
                    {
                        if (OSMHelper.isBusStation(node) || OSMHelper.isBusStop(node) || OSMHelper.isTrainStation(node))
                        {
                            currentEquipmentPlaces.withEquipmentPlaceRefOrEquipmentPlace(Arrays.asList(elevatorEntry.getValue().getEquipmentPlace()));
                            currentPathJunctions.withPathJunctionRefOrPathJunction(Arrays.asList(elevatorEntry.getValue().getPathJunction()));
                            modified = true;
                            break;
                        }
                    }
                }
            }

            for (HashMap.Entry<Way, Steps> stepsEntry : steps.entrySet())
            {
                Point point = MainApplication.getMap().mapView.getPoint(stepsEntry.getKey().firstNode().getCoor());
                List<Node> closestNodes = getAllNearestNodes(point, OsmPrimitive::isTagged);

                for (Node node : closestNodes)
                {
                    if (node.getId() == stopPlaceEntry.getKey().getId())
                    {
                        if (OSMHelper.isBusStation(node) || OSMHelper.isBusStop(node) || OSMHelper.isTrainStation(node))
                        {
                            for (PathJunction pathJunction : stepsEntry.getValue().getPathJunctions())
                            {
                                currentPathJunctions.withPathJunctionRefOrPathJunction(Arrays.asList(pathJunction));
                            }

                            currentPathLinks.withPathLinkRefOrSitePathLink(Arrays.asList(stepsEntry.getValue().getSitePathLink()));
                            currentEquipmentPlaces.withEquipmentPlaceRefOrEquipmentPlace(Arrays.asList(stepsEntry.getValue().getEquipmentPlace()));

                            modified = true;
                            break;
                        }
                    }
                }
            }

            for (HashMap.Entry<Way, FootPath> footPathEntry : footPaths.entrySet())
            {
                Point point = MainApplication.getMap().mapView.getPoint(footPathEntry.getKey().firstNode().getCoor());
                List<Node> closestNodes = getAllNearestNodes(point, OsmPrimitive::isTagged);

                for (Node node : closestNodes)
                {
                    if (node.getId() == stopPlaceEntry.getKey().getId())
                    {
                        if (OSMHelper.isBusStation(node) || OSMHelper.isBusStop(node) || OSMHelper.isTrainStation(node))
                        {
                            for (PathJunction pathJunction : footPathEntry.getValue().getPathJunctions())
                            {
                                currentPathJunctions.withPathJunctionRefOrPathJunction(Arrays.asList(pathJunction));
                            }

                            currentPathLinks.withPathLinkRefOrSitePathLink(Arrays.asList(footPathEntry.getValue().getSitePathLink()));
                            modified = true;
                            break;
                        }

                    }
                }
            }

            if (modified)
            {
                stopPlaces.replace(stopPlaceEntry.getKey(), stopPlaceEntry.getValue()
                        .withEquipmentPlaces(currentEquipmentPlaces)
                        .withPathJunctions(currentPathJunctions)
                        .withPathLinks(currentPathLinks));/*.withPlaceEquipments(currentEquipmentPlaces))*/
            }
        }

        if (stopPlaces.isEmpty())
        {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("File has not been exported because no stop places have been found in the currently loaded data.", JOptionPane.INFORMATION_MESSAGE));
            return;
        }

        ArrayList<StopPlace> stopPlacesAsList = new ArrayList<>(stopPlaces.values());

        ResourceFrame resourceFrame = neTExParser.createResourceFrame();
        SiteFrame siteFrame = neTExParser.createSiteFrame(stopPlacesAsList);

        CompositeFrame compositeFrame = neTExParser.createCompositeFrame(resourceFrame, siteFrame);

        PublicationDeliveryStructure publicationDeliveryStructure = neTExParser.createPublicationDeliveryStsructure(compositeFrame);

        customMarshaller.marshal(neTExFactory.createPublicationDelivery(publicationDeliveryStructure), neTExFile);
    }

    public void exportComplexStationSample(File neTExFile)
    {
        PathJunction bordsteinabsenkung = new PathJunction()
                .withId("ch:1:PathJunction:8590129:C:dropped_kerb")
                .withLocation(new LocationStructure()
                        .withLatitude(BigDecimal.valueOf(46.96675449))
                        .withLongitude(BigDecimal.valueOf(7.46420677))
                        .withAltitude(BigDecimal.valueOf(549)))
                .withTypes(new TypeOfPointRefs_RelStructure()
                        .withTypeOfPointRef(new TypeOfPointRefStructure()
                                .withRef("ch:1:TypeOfPoint:dropped_kerb")));

        CrossingEquipment fussgaengerstreifen = new CrossingEquipment()
                .withId("ch:1:CrossingEquipment:fussg�ngerstreifen")
                .withCrossingType(CrossingTypeEnumeration.ROAD_CROSSING)
                .withZebraCrossing(true)
                .withPedestrianLights(true)
                .withAcousticDeviceSensors(false)
                .withAcousticCrossingAids(false)
                .withTactileGuidanceStrips(false)
                .withVisualGuidanceBands(false)
                .withDroppedKerb(true);

        Quay quay_3_4 = new Quay()
                .withPublicCode("3/4")
                .withPolygon(new PolygonType())
                .withQuayType(QuayTypeEnumeration.RAIL_PLATFORM)
                .withAccessibilityAssessment(new AccessibilityAssessment()
                        .withLimitations(new AccessibilityLimitations_RelStructure()
                                .withAccessibilityLimitation(new AccessibilityLimitation()
                                        .withWheelchairAccess(LimitationStatusEnumeration.TRUE)
                                        .withLiftFreeAccess(LimitationStatusEnumeration.FALSE))));

        Quay platform_2 = new Quay()
                .withPublicCode("3")
                .withPolygon(new PolygonType()
                        .withId("org:osm:way:391450917")
                        .withExterior(new AbstractRingPropertyType().withAbstractRing(
                                gmlFactory.createLinearRing(new LinearRingType().withPosOrPointProperty(Arrays.asList(
                                        new DirectPositionType().withValue(46.9665522, 7.4642906),
                                        new DirectPositionType().withValue(46.9665603, 7.4649245),
                                        new DirectPositionType().withValue(46.9665399, 7.4649250),
                                        new DirectPositionType().withValue(46.9665318, 7.4642911))))))
                        .withInterior(new AbstractRingPropertyType())
                        .withInterior(new AbstractRingPropertyType()));

        AccessibilityAssessment accessibilityAssessment = new AccessibilityAssessment()
                .withLimitations(new AccessibilityLimitations_RelStructure()
                        .withAccessibilityLimitation(new AccessibilityLimitation()));

        TicketingEquipment billetautomat_bernmobil = new TicketingEquipment()
                .withId("ch:1:TicketingEquipment:billetautomat_bernmobil")
                .withTicketMachines(true)
                .withNumberOfMachines(BigInteger.valueOf(2))
                .withHeightOfMachineInterface(BigDecimal.valueOf(1.3))
                .withInductionLoops(false);

        EquipmentPlace billetautomat_XY = new EquipmentPlace()
                .withId("ch:1:EquipmentPlace:billetautomat_XY")
                .withCentroid(new SimplePoint_VersionStructure()
                        .withLocation(new LocationStructure()
                                .withLatitude(BigDecimal.valueOf(46.96675449))
                                .withLongitude(BigDecimal.valueOf(7.46420677))
                                .withAltitude(BigDecimal.valueOf(549))))
                .withPlaceEquipments(new Equipments_RelStructure().withEquipmentRefOrEquipment(
                        neTExFactory.createTicketingEquipmentRef(new TicketingEquipmentRefStructure()
                                .withRef(billetautomat_bernmobil.getId()))));

        TicketingEquipment billetschalter_bls = new TicketingEquipment()
                .withId("ch:1:TicketingEquipment:billetschalter_bls")
                .withTicketMachines(false)
                .withTicketOffice(false)
                .withTicketCounter(true)
                .withLowCounterAccess(true)
                .withInductionLoops(false);

        EquipmentPlace billetschalter_XY = new EquipmentPlace()
                .withId("ch:1:EquipmentPlace:billetschalter_XY")
                .withCentroid(new SimplePoint_VersionStructure()
                        .withLocation(new LocationStructure()
                                .withLatitude(BigDecimal.valueOf(46.96675449))
                                .withLongitude(BigDecimal.valueOf(7.46420677))
                                .withAltitude(BigDecimal.valueOf(549))))
                .withPlaceEquipments(new Equipments_RelStructure().withEquipmentRefOrEquipment(
                        neTExFactory.createTicketingEquipmentRef(new TicketingEquipmentRefStructure()
                                .withRef(billetschalter_bls.getId()))));

        AssistanceService ein_ausstiegshilfe = new AssistanceService()
                .withAssistanceFacilityList(Arrays.asList(AssistanceFacilityEnumeration.BOARDING_ASSISTANCE, AssistanceFacilityEnumeration.WHEECHAIR_ASSISTANCE))
                .withAssistanceAvailability(AssistanceAvailabilityEnumeration.AVAILABLE_IF_BOOKED);

        WaitingRoomEquipment wartesaal = new WaitingRoomEquipment()
                .withSeats(BigInteger.valueOf(8))
                .withStepFree(true);

        ShelterEquipment unterstand = new ShelterEquipment()
                .withSeats(BigInteger.valueOf(6))
                .withStepFree(true);

        PassengerInformationEquipment audio_info_point = new PassengerInformationEquipment()
                .withId("ch:1:PassengerInformationEquipment:S�V_abfahrten_dynamisch")
                .withPassengerInformationFacilityList(PassengerInformationFacilityEnumeration.STOP_ANNOUNCEMENTS)
                .withAccessibilityInfoFacilityList(Arrays.asList(
                        AccessibilityInfoFacilityEnumeration.AUDIO_INFORMATION,
                        AccessibilityInfoFacilityEnumeration.DISPLAYS_FOR_VISUALLY_IMPAIRED));

        StaircaseEquipment treppe_S_P0 = new StaircaseEquipment()
                .withId("ch:1:StaircaseEquipment:S_P0")
                .withDescription(new MultilingualString().withValue("Top: Overpass, Bottom: Max-D�twyler-Platz"))
                .withHandrailType(HandrailEnumeration.BOTH_SIDES)
                .withTopEnd(new StairEndStructure().withTexturedSurface(true))
                .withBottomEnd(new StairEndStructure().withTexturedSurface(true));

        Level level_1 = new Level();
        Level level_2 = new Level();

        AccessSpace ueberfuehrung = new AccessSpace()
                .withId("ch:1:AccessSpace:8516161:�berf�hrung")
                .withDescription(new MultilingualString().withValue("�berf�hrung"))
                .withCentroid(new SimplePoint_VersionStructure()
                        .withLocation(new LocationStructure()
                                .withLatitude(BigDecimal.valueOf(46.96675449))
                                .withLongitude(BigDecimal.valueOf(7.46420677))
                                .withAltitude(BigDecimal.valueOf(549))))
                .withAccessibilityAssessment(new AccessibilityAssessment()
                        .withLimitations(new AccessibilityLimitations_RelStructure()
                                .withAccessibilityLimitation(new AccessibilityLimitation()
                                        .withWheelchairAccess(LimitationStatusEnumeration.TRUE)
                                        .withLiftFreeAccess(LimitationStatusEnumeration.FALSE))))
                .withCovered(CoveredEnumeration.MIXED)
                .withLevelRef(new LevelRefStructure().withRef(level_1.getId()))
                .withAccessSpaceType(AccessSpaceTypeEnumeration.OVERPASS);

        PathJunction treppe_bottom = new PathJunction()
                .withId("ch:1:PathJunction:8516161:S_P0:top")
                .withLocation(new LocationStructure()
                        .withLatitude(BigDecimal.valueOf(46.96675449))
                        .withLongitude(BigDecimal.valueOf(7.46420677))
                        .withAltitude(BigDecimal.valueOf(549)))
                .withTypes(new TypeOfPointRefs_RelStructure()
                        .withTypeOfPointRef(new TypeOfPointRefStructure()
                                .withRef("ch:1:TypeOfPoint:floor_change_endpoint")))
                .withParentZoneRef(new ZoneRefStructure().withRef(ueberfuehrung.getId()));

        PathJunction treppe_top = new PathJunction();

        SitePathLink treppe_link = new SitePathLink()
                .withId("ch:1:SitePathLink:treppe")
                .withFrom(new PathLinkEndStructure()
                        .withPlaceRef(new PlaceRefStructure()
                                .withRef(treppe_bottom.getId()))
                        .withLevelRef(new LevelRefStructure()
                                .withRef(level_1.getId())))
                .withTo(new PathLinkEndStructure()
                        .withPlaceRef(new PlaceRefStructure()
                                .withRef(treppe_top.getId()))
                        .withLevelRef(new LevelRefStructure()
                                .withRef(level_2.getId())))
                .withAccessibilityAssessment(new AccessibilityAssessment()
                        .withLimitations(new AccessibilityLimitations_RelStructure()
                                .withAccessibilityLimitation(new AccessibilityLimitation()
                                        .withStepFreeAccess(LimitationStatusEnumeration.FALSE)
                                        .withWheelchairAccess(LimitationStatusEnumeration.FALSE))))
                .withAccessFeatureType(AccessFeatureEnumeration.STAIRS)
                .withLevelRef(new LevelRefStructure().withRef(level_1.getId()))
                .withPlaceEquipments(new PlaceEquipments_RelStructure()
                        .withInstalledEquipmentRefOrInstalledEquipment(neTExFactory.createEquipmentRef(
                                new StaircaseEquipmentRefStructure()
                                        .withRef(treppe_S_P0.getId()))));

        SitePathLink fussweg_abschnitt = new SitePathLink();

        StopPlaceEntrance eingang = new StopPlaceEntrance()
                .withId("ch:1:StopPlaceEntrance:8507000:haupteingang")
                .withCentroid(new SimplePoint_VersionStructure()
                        .withLocation(new LocationStructure()
                                .withLatitude(BigDecimal.valueOf(46.96675449))
                                .withLongitude(BigDecimal.valueOf(7.46420677))
                                .withAltitude(BigDecimal.valueOf(549))))
                .withEntranceType(EntranceEnumeration.OPENING)
                .withDroppedKerbOutside(true)
                .withAllAreasWheelchairAccessible(true);

        StopPlace wankdorf_bahnhof = new StopPlace()
                .withId("ch:1:StopPlace:8516161")
                .withPrivateCode(new PrivateCodeStructure().withValue("8516161"))
                .withCentroid(new SimplePoint_VersionStructure()
                        .withLocation(new LocationStructure()
                                .withLatitude(BigDecimal.valueOf(46.96675449))
                                .withLongitude(BigDecimal.valueOf(7.46420677))
                                .withAltitude(BigDecimal.valueOf(549))))
                .withAccessibilityAssessment(new AccessibilityAssessment()
                        .withLimitations(new AccessibilityLimitations_RelStructure()
                                .withAccessibilityLimitation(new AccessibilityLimitation()
                                        .withWheelchairAccess(LimitationStatusEnumeration.TRUE)
                                        .withLiftFreeAccess(LimitationStatusEnumeration.FALSE))))
                .withStopPlaceType(StopTypeEnumeration.RAIL_STATION)
                .withQuays(new Quays_RelStructure().
                        withQuayRefOrQuay(Arrays.asList(
                                quay_3_4,
                                platform_2)))
                .withPathLinks(new SitePathLinks_RelStructure().withPathLinkRefOrSitePathLink(Arrays.asList(treppe_link)))
                .withPathJunctions(new PathJunctions_RelStructure()
                        .withPathJunctionRefOrPathJunction(Arrays.asList(
                                bordsteinabsenkung,
                                treppe_bottom,
                                treppe_top)));

        ResourceFrame resourceFrame = new ResourceFrame().withId("ch:1:ResourceFrame");
        SiteFrame siteFrame = new SiteFrame()
                .withId("ch:1:SiteFrame")
                .withStopPlaces(new StopPlacesInFrame_RelStructure()
                        .withStopPlace(Arrays.asList(wankdorf_bahnhof)));

        CompositeFrame compositeFrame = new CompositeFrame()
                .withId("ch:1:CompositeFrame")
                .withFrames(new Frames_RelStructure().withCommonFrame(Arrays.asList(
                        neTExFactory.createResourceFrame(resourceFrame),
                        neTExFactory.createSiteFrame(siteFrame))));

        PublicationDeliveryStructure publicationDelivery = new PublicationDeliveryStructure()
                .withDescription(new MultilingualString()
                        .withValue("Description...")
                        .withLang("en"))
                .withPublicationTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .withParticipantRef("participantRef")
                .withDataObjects(new DataObjects()
                        .withCompositeFrameOrCommonFrame(Arrays.asList(
                                neTExFactory.createCompositeFrame(compositeFrame))));

        customMarshaller.marshal(neTExFactory.createPublicationDelivery(publicationDelivery), neTExFile);
    }

    public List<Node> getAllNearestNodes(Point p, Predicate<OsmPrimitive> predicate)
    {
        List<Node> nearestList = new ArrayList<>();

        for (List<Node> nlist : getNearestNodesImpl(p, predicate).values())
        {
            nearestList.addAll(nlist);
        }

        return nearestList;
    }

    private Node getNearestStop(LatLon coord, Predicate<OsmPrimitive> predicate, long correspondentNodeId)
    {
        Point p = MainApplication.getMap().mapView.getPoint(coord);
        Map<Double, List<Node>> dist_nodes = getNearestNodesImpl(p, predicate);
        Double[] distances = dist_nodes.keySet().toArray(new Double[0]);
        Arrays.sort(distances);
        Integer distanceIndex = -1;

        while (++distanceIndex < distances.length)
        {
            List<Node> nodes = dist_nodes.get(distances[distanceIndex]);

            for (Node node : nodes)
            {
                if (node.getId() == correspondentNodeId)
                {
                    if (OSMHelper.isBusStation(node) || OSMHelper.isBusStop(node) || OSMHelper.isTrainStation(node))
                    {
                        return node;
                    }
                }
            }
        }

        return null;
    }

    private Map<Double, List<Node>> getNearestNodesImpl(Point p, Predicate<OsmPrimitive> predicate)
    {
        Map<Double, List<Node>> nearestMap = new TreeMap<>();
        MapView mapView = MainApplication.getMap().mapView;

        if (ds != null)
        {
            double dist, snapDistanceSq = PROP_SNAP_DISTANCE.get();
            snapDistanceSq *= snapDistanceSq;

            for (Node n : ds.searchNodes(getBBox(p, PROP_SNAP_DISTANCE.get())))
            {
                if (predicate.test(n)
                        && (dist = mapView.getPoint2D(n).distanceSq(p)) < snapDistanceSq)
                {
                    nearestMap.computeIfAbsent(dist, k -> new LinkedList<>()).add(n);
                }
            }
        }

        return nearestMap;
    }

    private BBox getBBox(Point p, int snapDistance)
    {
        MapView mapView = MainApplication.getMap().mapView;
        return new BBox(mapView.getLatLon(p.x - snapDistance, p.y - snapDistance),
                mapView.getLatLon(p.x + snapDistance, p.y + snapDistance));
    }

    public static boolean openXMLFile(final File file)
    {
        if (!Desktop.isDesktopSupported())
        {
            return false;
        }

        Desktop desktop = Desktop.getDesktop();

        if (!desktop.isSupported(Desktop.Action.EDIT))
        {
            return false;
        }

        try
        {
            desktop.edit(file);
        }
        catch (IOException e)
        {
            e.printStackTrace(System.out);
            return false;
        }

        return true;
    }

}
