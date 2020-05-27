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

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.netex.model.*;

import org.xml.sax.SAXException;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.swing.JOptionPane;

import jaxb.CustomMarshaller;
import net.opengis.gml._3.PolygonType;

import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.OsmPrimitiveType;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.data.osm.WaySegment;
import org.openstreetmap.josm.gui.MapView;
import org.openstreetmap.josm.tools.Geometry;

import org.openstreetmap.josm.tools.Logging;
import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.plugins.netex_converter.model.netex.Elevator;
import org.openstreetmap.josm.plugins.netex_converter.model.netex.FootPath;
import org.openstreetmap.josm.plugins.netex_converter.model.netex.Steps;

import org.openstreetmap.josm.plugins.netex_converter.model.josm.PrimitiveLogMessage;

import org.openstreetmap.josm.plugins.netex_converter.util.OSMHelper;

/**
 *
 * @author Labian Gashi
 */
public class NeTExExporter {

    private final NeTExParser neTExParser;
    private final ObjectFactory neTExFactory;
    private final CustomMarshaller customMarshaller;

    private final HashMap<OsmPrimitive, StopPlace> stopPlaces;
    private final HashMap<OsmPrimitive, Quay> quays;
    private final HashMap<Node, Elevator> elevators;
    private final HashMap<Way, Steps> steps;
    private final HashMap<Way, FootPath> footPaths;

    private final DataSet ds;

    private static final List<PrimitiveLogMessage> LOG_MESSAGES = new ArrayList<>();
    private static final int SNAP_DISTANCE = 25;

    public NeTExExporter() throws IOException, SAXException {
        neTExParser = new NeTExParser();
        neTExFactory = new ObjectFactory();
        customMarshaller = new CustomMarshaller(PublicationDeliveryStructure.class);

        ds = MainApplication.getLayerManager().getEditDataSet();

        stopPlaces = new HashMap<>();
        quays = new HashMap<>();
        elevators = new HashMap<>();
        steps = new HashMap<>();
        footPaths = new HashMap<>();
    }

    public void exportToNeTEx(File neTExFile) throws IOException, org.xml.sax.SAXException, org.xml.sax.SAXException {

        Collection<OsmPrimitive> primitives;

        if (ds == null) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("No data has been loaded into JOSM"));
            return;
        }
        else if (ds.isEmpty()) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("No primitives have been found in the currently loaded data"));
            return;
        }
        else {
            primitives = ds.allPrimitives();
        }

        for (OsmPrimitive primitive : primitives) {
            if (primitive instanceof Node) {
                Node node = (Node) primitive;

                if (OSMHelper.isTrainStation(node)) {
                    stopPlaces.put(node, neTExParser.createStopPlace(node, StopTypeEnumeration.RAIL_STATION));
                }
                else if (OSMHelper.isBusStation(node)) {
                    stopPlaces.put(node, neTExParser.createStopPlace(node, StopTypeEnumeration.BUS_STATION));
                }
                else if (OSMHelper.isBusStop(node)) {
                    stopPlaces.put(node, neTExParser.createStopPlace(node, StopTypeEnumeration.ONSTREET_BUS));
                }
                else if (OSMHelper.isPlatform(node)) {
                    quays.put(node, neTExParser.createQuay(node));
                }
                else if (OSMHelper.isElevator(node)) {
                    elevators.put(node, neTExParser.createElevator(node));
                }
            }
            else if (primitive instanceof Way) {
                Way way = (Way) primitive;

                if (OSMHelper.isBusStation(way)) {
                    stopPlaces.put(way, neTExParser.createStopPlace(way, StopTypeEnumeration.BUS_STATION));
                }
                else if (OSMHelper.isBusStation(way)) {
                    stopPlaces.put(way, neTExParser.createStopPlace(way, StopTypeEnumeration.BUS_STATION));
                }
                else if (OSMHelper.isBusStop(way)) {
                    stopPlaces.put(way, neTExParser.createStopPlace(way, StopTypeEnumeration.ONSTREET_BUS));
                }
                else if (OSMHelper.isSteps(way)) {
                    steps.put(way, neTExParser.createSteps(way));
                }
                else if (OSMHelper.isFootPath(way)) {
                    footPaths.put(way, neTExParser.createFootPath(way));
                }
                else if (OSMHelper.isPlatform(way)) {
                    quays.put(way, neTExParser.createQuay(way));
                }
            }
            else {
                Relation relation = (Relation) primitive;

                if (OSMHelper.isTrainStation(relation)) {
                    stopPlaces.put(relation, neTExParser.createStopPlace(relation, StopTypeEnumeration.RAIL_STATION));
                }
                else if (OSMHelper.isBusStation(relation)) {
                    stopPlaces.put(relation, neTExParser.createStopPlace(relation, StopTypeEnumeration.BUS_STATION));
                }
                else if (OSMHelper.isBusStop(relation)) {
                    stopPlaces.put(relation, neTExParser.createStopPlace(relation, StopTypeEnumeration.ONSTREET_BUS));
                }
                else if (OSMHelper.isPlatform(relation)) {
                    quays.put(relation, neTExParser.createQuay(relation));
                }
            }
        }

        if (stopPlaces.isEmpty()) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("File has not been exported because no stop places have been found in the currently loaded map data."));
            return;
        }

        Map<OsmPrimitive, StopPlace> stopPlacesClone = new HashMap<>(stopPlaces);

        Map<StopPlace, List<OsmPrimitive>> stopsToCorrect = new HashMap<>();
        List<OsmPrimitive> stopsToDelete = new ArrayList<>();

        Iterator<Map.Entry<OsmPrimitive, StopPlace>> stopPlacesIterator = stopPlaces.entrySet().iterator();
        Iterator<Map.Entry<OsmPrimitive, StopPlace>> stopPlacesCloneIterator = stopPlacesClone.entrySet().iterator();

        while (stopPlacesIterator.hasNext()) {
            Map.Entry<OsmPrimitive, StopPlace> entry = stopPlacesIterator.next();

            while (stopPlacesCloneIterator.hasNext()) {
                Map.Entry<OsmPrimitive, StopPlace> entryClone = stopPlacesCloneIterator.next();

                if (entry.getKey().getId() != entryClone.getKey().getId()) {
                    String id = entry.getValue().getId();
                    String publicCode = entry.getValue().getPublicCode();
                    StopTypeEnumeration stopType = entry.getValue().getStopPlaceType();

                    if (entryClone.getValue().getId() != null && entryClone.getValue().getPublicCode() != null) {
                        String cloneId = entryClone.getValue().getId();
                        String clonePublicCode = entryClone.getValue().getPublicCode();
                        StopTypeEnumeration cloneStopType = entryClone.getValue().getStopPlaceType();

                        if (id.equals(cloneId) && publicCode.equals(clonePublicCode) && stopType.equals(cloneStopType)) {
                            stopsToDelete.add(entryClone.getKey());

                            if (stopsToCorrect.containsKey(entryClone.getValue())) {
                                List<OsmPrimitive> innerList = stopsToCorrect.get(entryClone.getValue());
                                innerList.add(entry.getKey());
                                innerList.add(entryClone.getKey());

                                stopsToCorrect.replace(entryClone.getValue(), innerList);
                            }
                            else {
                                List<OsmPrimitive> innerList = new ArrayList<>();
                                innerList.add(entry.getKey());
                                innerList.add(entryClone.getKey());

                                stopsToCorrect.put(entryClone.getValue(), innerList);

                            }

                            stopPlacesCloneIterator.remove();
                        }
                    }
                }
            }

            stopPlacesCloneIterator = stopPlacesClone.entrySet().iterator();
        }

        for (Iterator<Map.Entry<OsmPrimitive, StopPlace>> it = stopPlaces.entrySet().iterator(); it.hasNext();) {
            Map.Entry<OsmPrimitive, StopPlace> entry = it.next();
            for (OsmPrimitive primitive : stopsToDelete) {
                if (entry.getKey().getId() == primitive.getId()) {
                    it.remove();
                }
            }
        }

        for (Map.Entry<StopPlace, List<OsmPrimitive>> entry : stopsToCorrect.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                for (OsmPrimitive primitive : entry.getValue()) {
                    quays.put(primitive, neTExParser.createQuay(primitive));
                }
            }
        }

        HashMap<StopPlace, Quays_RelStructure> currentQuays = new HashMap<>();

        List<StopTypeEnumeration> stopTypesWithRef = new ArrayList<>(
                Arrays.asList(StopTypeEnumeration.RAIL_STATION, StopTypeEnumeration.BUS_STATION)
        );

        for (Iterator<Map.Entry<OsmPrimitive, Quay>> it = quays.entrySet().iterator(); it.hasNext();) {
            Map.Entry<OsmPrimitive, Quay> quayEntry = it.next();
            String quayUicRef = OSMHelper.getUicRef(quayEntry.getKey());
            String quayRef = OSMHelper.getRef(quayEntry.getKey());
            String modifiedQuayRef;

            if (quayRef != null && !quayRef.trim().isEmpty()) {
                modifiedQuayRef = OSMHelper.switchRefDelimiter(quayRef);
            }
            else {
                modifiedQuayRef = Long.toString(quayEntry.getKey().getId());
            }

            QuayTypeEnumeration quayType = QuayTypeEnumeration.OTHER;

            Node firstNode = null;
            Node lastNode = null;

            if (quayEntry.getKey() instanceof Node) {
                firstNode = (Node) quayEntry.getKey();
            }
            else if (quayEntry.getKey() instanceof Way) {
                firstNode = ((Way) quayEntry.getKey()).firstNode();
                lastNode = ((Way) quayEntry.getKey()).lastNode();
            }
            else {
                RelationMember firstMember = ((Relation) quayEntry.getKey()).firstMember();

                if (firstMember.isNode()) {
                    firstNode = firstMember.getNode();
                }
                else if (firstMember.isWay()) {
                    firstNode = firstMember.getWay().firstNode();
                    lastNode = firstMember.getWay().lastNode();
                }
                else {
                    continue; //implement while loop for relation inside relations until a node is found...
                }
            }

            LatLon firstCoord = firstNode.getCoor();

            if (firstCoord == null) {
                logMessage(firstNode.getId(), firstNode.getType(), new HashMap<String, String>() {
                    {
                        put(PrimitiveLogMessage.Tags.UNKNOWN_COORDS_TAG, PrimitiveLogMessage.Messages.UNKNOWN_COORDS_MESSAGE);
                    }
                });
                continue;
            }

            Point p = MainApplication.getMap().mapView.getPoint(firstCoord);

            List<Way> nearestWays = MainApplication.getMap().mapView.getNearestWays(p, OsmPrimitive::isTagged);

            PolygonType polygonType = null;
            for (Way way : nearestWays) {
                if (OSMHelper.isHighwayPlatform(way) && Geometry.nodeInsidePolygon(firstNode, way.getNodes())) {
                    polygonType = neTExParser.createPolygonType(way);
                    break;
                }
            }

            boolean foundStopPlace = false;

            if (quayUicRef != null && !quayUicRef.trim().isEmpty()) {
                for (StopPlace stopPlace : stopPlaces.values()) {
                    if (stopPlace.getPublicCode() != null && stopPlace.getPublicCode().equals(quayUicRef)) {
                        foundStopPlace = true;
                        boolean modifiedQuayType = false;
                        if (quayEntry.getValue().getQuayType().equals(QuayTypeEnumeration.OTHER)) {
                            modifiedQuayType = true;
                            switch (stopPlace.getStopPlaceType()) {
                                case ONSTREET_BUS:
                                    quayType = QuayTypeEnumeration.BUS_STOP;
                                    break;
                                case BUS_STATION:
                                    quayType = QuayTypeEnumeration.BUS_PLATFORM;
                                    break;
                                case RAIL_STATION:
                                    quayType = QuayTypeEnumeration.RAIL_PLATFORM;
                                    break;
                                default:
                                    quayType = QuayTypeEnumeration.OTHER;
                            }
                        }

                        QuayTypeEnumeration currentQuayType = modifiedQuayType ? quayType : quayEntry.getValue().getQuayType();

                        if (currentQuays.containsKey(stopPlace)) {
                            currentQuays.replace(stopPlace, currentQuays.get(stopPlace).withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                    .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, modifiedQuayRef))
                                    .withPublicCode(modifiedQuayRef)
                                    .withPolygon(polygonType != null ? polygonType : quayEntry.getValue().getPolygon())
                                    .withQuayType(currentQuayType))));
                        }
                        else {
                            currentQuays.put(stopPlace, new Quays_RelStructure().withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                    .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, modifiedQuayRef))
                                    .withPublicCode(modifiedQuayRef)
                                    .withPolygon(polygonType != null ? polygonType : quayEntry.getValue().getPolygon())
                                    .withQuayType(currentQuayType))));
                        }

                        if ((quayRef == null || quayRef.trim().isEmpty()) && stopTypesWithRef.contains(stopPlace.getStopPlaceType())) {
                            logMessage(quayEntry.getKey().getId(), quayEntry.getKey().getType(), new HashMap<String, String>() {
                                {
                                    put(PrimitiveLogMessage.Tags.REF_TAG, PrimitiveLogMessage.Messages.REF_MISSING_MESSAGE);
                                }
                            });
                        }
                    }
                }
            }
            else {
                OsmPrimitive closestStopPlace;

                if (quayEntry.getValue().getQuayType().equals(QuayTypeEnumeration.RAIL_PLATFORM)) {
                    closestStopPlace = findNearestTrainStation(firstCoord);
                    if (closestStopPlace == null && lastNode != null && lastNode.getCoor() != null) {
                        closestStopPlace = findNearestTrainStation(lastNode.getCoor());
                    }
                }
                else {
                    closestStopPlace = findNearestStopPlace(firstCoord);
                }

                if (closestStopPlace != null) {
                    foundStopPlace = true;
                    quayUicRef = OSMHelper.getUicRef(closestStopPlace);

                    boolean modifiedQuayType = false;
                    if (quayEntry.getValue().getQuayType().equals(QuayTypeEnumeration.OTHER)) {
                        modifiedQuayType = true;

                        if (OSMHelper.isBusStop(closestStopPlace)) {
                            quayType = QuayTypeEnumeration.BUS_STOP;
                        }
                        else if (OSMHelper.isBusStation(closestStopPlace)) {
                            quayType = QuayTypeEnumeration.BUS_PLATFORM;
                        }
                        else if (OSMHelper.isTrainStation(closestStopPlace)) {
                            quayType = QuayTypeEnumeration.RAIL_PLATFORM;
                        }
                        else {
                            quayType = QuayTypeEnumeration.OTHER;
                        }
                    }

                    for (StopPlace stopPlace : stopPlaces.values()) {
                        if (stopPlace.getPublicCode() != null && stopPlace.getPublicCode().equals(quayUicRef)) {
                            QuayTypeEnumeration currentQuayType = modifiedQuayType ? quayType : quayEntry.getValue().getQuayType();

                            if (currentQuays.containsKey(stopPlace)) {
                                currentQuays.replace(stopPlace, currentQuays.get(stopPlace).withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                        .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, modifiedQuayRef))
                                        .withPublicCode(modifiedQuayRef)
                                        .withQuayType(currentQuayType))));
                            }
                            else {
                                currentQuays.put(stopPlace, new Quays_RelStructure().withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                        .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, modifiedQuayRef))
                                        .withPublicCode(modifiedQuayRef)
                                        .withQuayType(currentQuayType))));
                            }

                            if ((quayRef == null || quayRef.trim().isEmpty()) && stopTypesWithRef.contains(stopPlace.getStopPlaceType())) {
                                logMessage(quayEntry.getKey().getId(), quayEntry.getKey().getType(), new HashMap<String, String>() {
                                    {
                                        put(PrimitiveLogMessage.Tags.REF_TAG, PrimitiveLogMessage.Messages.REF_MISSING_MESSAGE);
                                    }
                                });
                            }
                        }
                    }
                }
            }

            if (!foundStopPlace) {
                StopTypeEnumeration stopType;
                boolean modifiedQuayType = false;

                if (quayEntry.getValue().getQuayType().equals(QuayTypeEnumeration.OTHER)) {
                    modifiedQuayType = true;
                }

                if (OSMHelper.isTrainStation(quayEntry.getKey(), false)) {
                    stopType = StopTypeEnumeration.RAIL_STATION;
                    quayType = QuayTypeEnumeration.RAIL_PLATFORM;
                }
                else if (OSMHelper.isBusStation(quayEntry.getKey(), false)) {
                    stopType = StopTypeEnumeration.BUS_STATION;
                    quayType = QuayTypeEnumeration.BUS_PLATFORM;
                }
                else if (OSMHelper.isBusStop(quayEntry.getKey(), false)) {
                    stopType = StopTypeEnumeration.ONSTREET_BUS;
                    quayType = QuayTypeEnumeration.BUS_STOP;
                }
                else {
                    stopType = StopTypeEnumeration.OTHER;
                    quayType = QuayTypeEnumeration.OTHER;
                }

                QuayTypeEnumeration currentQuayType = modifiedQuayType ? quayType : quayEntry.getValue().getQuayType();

                if (OSMHelper.isBusStop(quayEntry.getKey(), false)) {
                    StopPlace stopPlace = neTExParser.createStopPlace(quayEntry.getKey(), stopType);

                    if (!stopPlaces.containsKey(quayEntry.getKey())) {
                        stopPlaces.put(quayEntry.getKey(), stopPlace);

                        if (stopPlace != null && stopPlace.getPublicCode() == null) {
                            logMessage(quayEntry.getKey().getId(), quayEntry.getKey().getType(), new HashMap<String, String>() {
                                {
                                    put(PrimitiveLogMessage.Tags.UIC_REF_TAG, PrimitiveLogMessage.Messages.NO_STOP_IN_VICINITY_MESSAGE);
                                }
                            });
                        }
                    }

                    if (currentQuays.containsKey(stopPlace)) {
                        currentQuays.replace(stopPlace, currentQuays.get(stopPlace).withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, modifiedQuayRef))
                                .withPublicCode(modifiedQuayRef)
                                .withQuayType(currentQuayType))));
                    }
                    else {
                        currentQuays.put(stopPlace, new Quays_RelStructure().withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, modifiedQuayRef))
                                .withPublicCode(modifiedQuayRef)
                                .withQuayType(currentQuayType))));
                    }
                }

                it.remove();
            }
        }

        for (Map.Entry<StopPlace, Quays_RelStructure> entry : currentQuays.entrySet()) {
            for (Map.Entry<OsmPrimitive, StopPlace> stopEntry : stopPlaces.entrySet()) {
                if (stopEntry.getValue().equals(entry.getKey())) {
                    stopPlaces.replace(stopEntry.getKey(), stopEntry.getValue().withQuays(entry.getValue()));
                }
            }
        }

        for (Relation relation : ds.getRelations()) {
            if (OSMHelper.isStopArea(relation)) {
                boolean foundStopPlace = false;
                String areaUicRef = OSMHelper.getUicRef(relation);

                if (areaUicRef != null && !areaUicRef.trim().isEmpty()) {
                    for (Map.Entry<OsmPrimitive, StopPlace> entry : stopPlaces.entrySet()) {
                        String stopUicRef = OSMHelper.getUicRef(entry.getKey());

                        if (areaUicRef.equals(stopUicRef)) {
                            foundStopPlace = true;

                            int membersCount = relation.getMembersCount();
                            int platformsCount = entry.getValue().getQuays() != null ? entry.getValue().getQuays().getQuayRefOrQuay().size() : 0;

                            if (membersCount != platformsCount) {

                            }
                        }
                    }

                    if (!foundStopPlace) {
                        StopPlace stopPlace = neTExParser.createStopPlace(relation, StopTypeEnumeration.OTHER);

                        QuayTypeEnumeration quayType = QuayTypeEnumeration.OTHER;
                        Quays_RelStructure quaysStructure = new Quays_RelStructure();

                        for (RelationMember relationMember : relation.getMembers()) {
                            OsmPrimitive member = relationMember.getMember();
                            String quayUicRef = OSMHelper.getUicRef(member);
                            String quayRef = OSMHelper.getRef(member);
                            if (quayRef != null && !quayRef.trim().isEmpty()) {
                                quayRef = OSMHelper.switchRefDelimiter(quayRef);
                            }
                            else {
                                quayRef = Long.toString(member.getId());
                            }

                            Quay quay = neTExParser.createQuay(member);
                            quays.put(member, quay);

                            boolean modifiedQuayType = false;

                            if (quay.getQuayType() != null && quay.getQuayType().equals(QuayTypeEnumeration.OTHER)) {
                                modifiedQuayType = true;
                                switch (stopPlace.getStopPlaceType()) {
                                    case ONSTREET_BUS:
                                        quayType = QuayTypeEnumeration.BUS_STOP;
                                        break;
                                    case BUS_STATION:
                                        quayType = QuayTypeEnumeration.BUS_PLATFORM;
                                        break;
                                    case RAIL_STATION:
                                        quayType = QuayTypeEnumeration.RAIL_PLATFORM;
                                        break;
                                    default:
                                        quayType = QuayTypeEnumeration.OTHER;
                                }
                            }

                            QuayTypeEnumeration currentQuayType = modifiedQuayType ? quayType : quay.getQuayType();

                            quaysStructure.withQuayRefOrQuay(Arrays.asList(quay
                                    .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, quayRef))
                                    .withPublicCode(quayRef)
                                    .withQuayType(currentQuayType)));
                        }

                        stopPlace = stopPlace.withQuays(quaysStructure);

                        stopPlaces.put(relation, stopPlace);
                    }
                }
            }
        }

        HashMap<StopPlace, SitePathLinks_RelStructure> pathLinks = new HashMap<>();
        HashMap<StopPlace, PathJunctions_RelStructure> pathJunctions = new HashMap<>();
        HashMap<StopPlace, EquipmentPlaces_RelStructure> equipmentPlaces = new HashMap<>();

        for (Map.Entry<Node, Elevator> elevatorEntry : elevators.entrySet()) {
            LatLon coord = elevatorEntry.getKey().getCoor();
            if (coord == null) {
                logMessage(elevatorEntry.getKey().getId(), elevatorEntry.getKey().getType(), new HashMap<String, String>() {
                    {
                        put(PrimitiveLogMessage.Tags.UNKNOWN_COORDS_TAG, PrimitiveLogMessage.Messages.UNKNOWN_COORDS_MESSAGE);
                    }
                });

                continue;
            }

            OsmPrimitive nearestStopPlace = findNearestStopPlace(coord);

            if (nearestStopPlace != null && !OSMHelper.isBusStop(nearestStopPlace)) {
                OsmPrimitive nearestQuay = findNearestPlatform(coord);

                for (Map.Entry<OsmPrimitive, StopPlace> stopEntry : stopPlaces.entrySet()) {
                    StopPlace stopPlace = stopEntry.getValue();

                    if (stopEntry.getKey().equals(nearestStopPlace)) {
                        if (equipmentPlaces.containsKey(stopPlace)) {
                            equipmentPlaces.replace(stopPlace, equipmentPlaces.get(stopPlace).withEquipmentPlaceRefOrEquipmentPlace(Arrays.asList(elevatorEntry.getValue().getEquipmentPlace())));
                        }
                        else {
                            equipmentPlaces.put(stopPlace, new EquipmentPlaces_RelStructure().withEquipmentPlaceRefOrEquipmentPlace(Arrays.asList(elevatorEntry.getValue().getEquipmentPlace())));
                        }

                        if (pathJunctions.containsKey(stopPlace)) {
                            pathJunctions.replace(stopPlace, pathJunctions.get(stopPlace).withPathJunctionRefOrPathJunction(Arrays.asList(elevatorEntry.getValue().getPathJunction()
                                    .withParentZoneRef(new ZoneRefStructure()
                                            .withRef(quays.containsKey(nearestQuay) ? quays.get(nearestQuay).getId() : null)))));
                        }
                        else {
                            pathJunctions.put(stopPlace, new PathJunctions_RelStructure().withPathJunctionRefOrPathJunction(Arrays.asList(elevatorEntry.getValue().getPathJunction()
                                    .withParentZoneRef(new ZoneRefStructure()
                                            .withRef(quays.containsKey(nearestQuay) ? quays.get(nearestQuay).getId() : null)))));
                        }
                    }
                }
            }
        }

        for (Map.Entry<Way, Steps> stepsEntry : steps.entrySet()) {
            LatLon coordFirst = stepsEntry.getKey().firstNode().getCoor();
            LatLon coordLast = stepsEntry.getKey().lastNode().getCoor();

            if (coordFirst == null || coordLast == null) {
                logMessage(stepsEntry.getKey().getId(), stepsEntry.getKey().getType(), new HashMap<String, String>() {
                    {
                        put(PrimitiveLogMessage.Tags.UNKNOWN_COORDS_TAG, PrimitiveLogMessage.Messages.UNKNOWN_COORDS_MESSAGE);
                    }
                });

                continue;
            }

            List<OsmPrimitive> nearestStopPlaces = findNearestStopPlaces(coordFirst);
            nearestStopPlaces.addAll(findNearestStopPlaces(coordLast));

            nearestStopPlaces = nearestStopPlaces.stream().distinct().collect(Collectors.toList());

            for (OsmPrimitive nearestStopPlace : nearestStopPlaces) {
                if (!OSMHelper.isBusStop(nearestStopPlace)) {
                    OsmPrimitive nearestQuay = findNearestPlatform(coordFirst);

                    if (nearestQuay == null) {
                        nearestQuay = findNearestPlatform(coordLast);
                    }

                    for (Map.Entry<OsmPrimitive, StopPlace> stopEntry : stopPlaces.entrySet()) {
                        StopPlace stopPlace = stopEntry.getValue();

                        if (stopEntry.getKey().equals(nearestStopPlace)) {

                            for (PathJunction pathJunction : stepsEntry.getValue().getPathJunctions()) {
                                if (pathJunctions.containsKey(stopPlace)) {
                                    pathJunctions.replace(stopPlace, pathJunctions.get(stopPlace).withPathJunctionRefOrPathJunction(Arrays.asList(pathJunction
                                            .withParentZoneRef(new ZoneRefStructure()
                                                    .withRef(quays.containsKey(nearestQuay) ? quays.get(nearestQuay).getId() : null)))));
                                }
                                else {
                                    pathJunctions.put(stopPlace, new PathJunctions_RelStructure().withPathJunctionRefOrPathJunction(Arrays.asList(pathJunction
                                            .withParentZoneRef(new ZoneRefStructure()
                                                    .withRef(quays.containsKey(nearestQuay) ? quays.get(nearestQuay).getId() : null)))));
                                }
                            }

                            if (equipmentPlaces.containsKey(stopPlace)) {
                                equipmentPlaces.replace(stopPlace, equipmentPlaces.get(stopPlace).withEquipmentPlaceRefOrEquipmentPlace(Arrays.asList(stepsEntry.getValue().getEquipmentPlace())));
                            }
                            else {
                                equipmentPlaces.put(stopPlace, new EquipmentPlaces_RelStructure().withEquipmentPlaceRefOrEquipmentPlace(Arrays.asList(stepsEntry.getValue().getEquipmentPlace())));
                            }

                            for (SitePathLink sitePathLink : stepsEntry.getValue().getSitePathLinks()) {
                                if (pathLinks.containsKey(stopPlace)) {
                                    pathLinks.replace(stopPlace, pathLinks.get(stopPlace).withPathLinkRefOrSitePathLink(Arrays.asList(sitePathLink)));
                                }
                                else {
                                    pathLinks.put(stopPlace, new SitePathLinks_RelStructure().withPathLinkRefOrSitePathLink(Arrays.asList(sitePathLink)));
                                }
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<Way, FootPath> footPathEntry : footPaths.entrySet()) {
            LatLon coordFirst = footPathEntry.getKey().firstNode().getCoor();
            LatLon coordLast = footPathEntry.getKey().lastNode().getCoor();

            if (coordFirst == null || coordLast == null) {
                logMessage(footPathEntry.getKey().getId(), footPathEntry.getKey().getType(), new HashMap<String, String>() {
                    {
                        put(PrimitiveLogMessage.Tags.UNKNOWN_COORDS_TAG, PrimitiveLogMessage.Messages.UNKNOWN_COORDS_MESSAGE);
                    }
                });

                continue;
            }

            List<OsmPrimitive> nearestStopPlaces = findNearestStopPlaces(coordFirst);
            nearestStopPlaces.addAll(findNearestStopPlaces(coordLast));

            nearestStopPlaces = nearestStopPlaces.stream().distinct().collect(Collectors.toList());

            for (OsmPrimitive nearestStopPlace : nearestStopPlaces) {
                if (!OSMHelper.isBusStop(nearestStopPlace)) {
                    OsmPrimitive nearestQuay = findNearestPlatform(coordFirst);

                    if (nearestQuay == null) {
                        nearestQuay = findNearestPlatform(coordLast);
                    }

                    for (Map.Entry<OsmPrimitive, StopPlace> stopEntry : stopPlaces.entrySet()) {
                        StopPlace stopPlace = stopEntry.getValue();

                        if (stopEntry.getKey().equals(nearestStopPlace)) {
                            for (PathJunction pathJunction : footPathEntry.getValue().getPathJunctions()) {
                                if (pathJunctions.containsKey(stopPlace)) {
                                    pathJunctions.replace(stopPlace, pathJunctions.get(stopPlace).withPathJunctionRefOrPathJunction(Arrays.asList(pathJunction
                                            .withParentZoneRef(new ZoneRefStructure()
                                                    .withRef(quays.containsKey(nearestQuay) ? quays.get(nearestQuay).getId() : null)))));
                                }
                                else {
                                    pathJunctions.put(stopPlace, new PathJunctions_RelStructure().withPathJunctionRefOrPathJunction(Arrays.asList(pathJunction
                                            .withParentZoneRef(new ZoneRefStructure()
                                                    .withRef(quays.containsKey(nearestQuay) ? quays.get(nearestQuay).getId() : null)))));
                                }
                            }

                            if (equipmentPlaces.containsKey(stopPlace)) {
                                equipmentPlaces.replace(stopPlace, equipmentPlaces.get(stopPlace).withEquipmentPlaceRefOrEquipmentPlace(Arrays.asList(footPathEntry.getValue().getEquipmentPlace())));
                            }
                            else {
                                equipmentPlaces.put(stopPlace, new EquipmentPlaces_RelStructure().withEquipmentPlaceRefOrEquipmentPlace(Arrays.asList(footPathEntry.getValue().getEquipmentPlace())));
                            }

                            for (SitePathLink sitePathLink : footPathEntry.getValue().getSitePathLinks()) {
                                if (pathLinks.containsKey(stopPlace)) {
                                    pathLinks.replace(stopPlace, pathLinks.get(stopPlace).withPathLinkRefOrSitePathLink(Arrays.asList(sitePathLink)));
                                }
                                else {
                                    pathLinks.put(stopPlace, new SitePathLinks_RelStructure().withPathLinkRefOrSitePathLink(Arrays.asList(sitePathLink)));
                                }
                            }

                        }
                    }
                }
            }
        }

        for (Map.Entry<OsmPrimitive, StopPlace> stopEntry : stopPlaces.entrySet()) {
            for (Map.Entry<StopPlace, PathJunctions_RelStructure> pathJunctionEntry : pathJunctions.entrySet()) {
                if (stopEntry.getValue() != null && stopEntry.getValue().equals(pathJunctionEntry.getKey())) {
                    List<PathJunction> currentPathJunctions = pathJunctionEntry.getValue().getPathJunctionRefOrPathJunction().stream()
                            .filter(p -> p instanceof PathJunction)
                            .map(p -> (PathJunction) p)
                            .collect(Collectors.toList());

                    List<PathJunction> pathJunctionsToDelete = new ArrayList<>();
                    PathJunctions_RelStructure relStructure = new PathJunctions_RelStructure();

                    for (int i = 0; i < currentPathJunctions.size(); i++) {
                        PathJunction pathJunctionOuter = currentPathJunctions.get(i);
                        for (int j = i + 1; j < currentPathJunctions.size(); j++) {
                            PathJunction pathJunctionInner = currentPathJunctions.get(j);
                            if (pathJunctionOuter.getId().equals(pathJunctionInner.getId())) {
                                pathJunctionsToDelete.add(pathJunctionInner);

                                TypeOfPointRefs_RelStructure typeOfPointsStructure = new TypeOfPointRefs_RelStructure();

                                List<TypeOfPointRefStructure> typeOfPoints = pathJunctionOuter.getTypes().getTypeOfPointRef();

                                if (pathJunctionInner.getTypes().getTypeOfPointRef() != null && !pathJunctionInner.getTypes().getTypeOfPointRef().isEmpty()) {
                                    typeOfPoints.addAll(pathJunctionInner.getTypes().getTypeOfPointRef());
                                }

                                typeOfPoints = typeOfPoints.stream().distinct().collect(Collectors.toList());

                                for (TypeOfPointRefStructure typeOfPoint : typeOfPoints) {
                                    typeOfPointsStructure.withTypeOfPointRef(Arrays.asList(typeOfPoint));
                                }

                                currentPathJunctions.set(i, pathJunctionOuter.withTypes(typeOfPointsStructure));
                            }
                        }
                    }

                    currentPathJunctions.removeAll(pathJunctionsToDelete);

                    for (PathJunction pathJunction : currentPathJunctions) {
                        relStructure.withPathJunctionRefOrPathJunction(Arrays.asList(pathJunction));
                    }

                    stopPlaces.replace(stopEntry.getKey(),
                            stopEntry.getValue().withPathJunctions(relStructure));
                }
            }
            for (Map.Entry<StopPlace, EquipmentPlaces_RelStructure> entry : equipmentPlaces.entrySet()) {
                if (stopEntry.getValue() != null && stopEntry.getValue().equals(entry.getKey())) {
                    stopPlaces.replace(stopEntry.getKey(), stopEntry.getValue().withEquipmentPlaces(entry.getValue()));
                }
            }
            for (Map.Entry<StopPlace, SitePathLinks_RelStructure> entry : pathLinks.entrySet()) {
                if (stopEntry.getValue() != null && stopEntry.getValue().equals(entry.getKey())) {
                    stopPlaces.replace(stopEntry.getKey(), stopEntry.getValue().withPathLinks(entry.getValue()));
                }
            }
        }

        ResourceFrame resourceFrame = neTExParser.createResourceFrame();

        SiteFrame siteFrame = neTExParser.createSiteFrame(new ArrayList<>(stopPlaces.values()));

        CompositeFrame compositeFrame = neTExParser.createCompositeFrame(resourceFrame, siteFrame);

        PublicationDeliveryStructure publicationDeliveryStructure = neTExParser.createPublicationDeliveryStsructure(compositeFrame);

        customMarshaller.marshal(neTExFactory.createPublicationDelivery(publicationDeliveryStructure), neTExFile);

        for (PrimitiveLogMessage logMessage : LOG_MESSAGES) {

            OsmPrimitive primitive = ds.getPrimitiveById(logMessage.getPrimitiveId(), logMessage.getPrimitiveType());

            if (primitive != null) {
                Logging.warn(tr("Warnings found for the {0} with the id: {1}",
                        primitive.getType(), Long.toString(primitive.getId())));

                primitive.setHighlighted(true);

                for (Map.Entry<String, String> entry : logMessage.getKeys().entrySet()) {
                    primitive.put(entry.getKey(), entry.getValue());
                }
            }
        }

        MainApplication.getMap().mapView.repaint();

        if (LOG_MESSAGES.isEmpty()) {
            Logging.info(tr("NeTEx export has finished successfully."));
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                    tr("NeTEx export has finished successfully."));
        }
        else {
            Logging.warn(tr("NeTEx export has finished with warnings.They are highlighted on the JOSM map layer."));
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(),
                    tr("{0} {1} {2}",
                            "NeTEx export has finished successfully.",
                            "Objects that need improvement have been highlighted on the map,",
                            "please correct them for a more accurate conversion."));
        }
    }

    private OsmPrimitive findNearestStopPlace(LatLon coord) {
        Point p = MainApplication.getMap().mapView.getPoint(coord);
        Map<Double, List<Node>> dist_nodes = getNearestNodesImpl(p, OsmPrimitive::isTagged);
        Double[] distances_nodes = dist_nodes.keySet().toArray(new Double[0]);
        Arrays.sort(distances_nodes);

        int distanceIndex = -1;

        while (++distanceIndex < distances_nodes.length) {
            List<Node> nodes = dist_nodes.get(distances_nodes[distanceIndex]);

            for (Node node : nodes) {
                if (OSMHelper.isStopPlace(node)) {
                    return node;
                }
            }
        }

        Map<Double, List<WaySegment>> dist_ways = getNearestWaySegmentsImpl(p, OsmPrimitive::isTagged);
        Double[] distance_ways = dist_ways.keySet().toArray(new Double[0]);
        Arrays.sort(distance_ways);

        distanceIndex = -1;

        while (++distanceIndex < distance_ways.length) {
            List<WaySegment> waySegments = dist_ways.get(distance_ways[distanceIndex]);
            for (WaySegment ws : waySegments) {
                if (ws != null && ws.way != null) {
                    if (OSMHelper.isStopPlace(ws.way)) {
                        return ws.way;
                    }
                }
            }
        }

        return null;
    }

    private Node findNearestTrainStation(LatLon coord) {
        Point p = MainApplication.getMap().mapView.getPoint(coord);
        Map<Double, List<Node>> dist_nodes = getNearestTrainStationsImpl(p, OsmPrimitive::isTagged);
        Double[] distances = dist_nodes.keySet().toArray(new Double[0]);
        Arrays.sort(distances);

        int distanceIndex = -1;

        while (++distanceIndex < distances.length) {
            List<Node> nodes = dist_nodes.get(distances[distanceIndex]);

            for (Node node : nodes) {
                return node;
            }
        }

        return null;
    }

    private OsmPrimitive findNearestPlatform(LatLon coord) {
        Point p = MainApplication.getMap().mapView.getPoint(coord);
        Map<Double, List<Node>> dist_nodes = getNearestNodesImpl(p, OsmPrimitive::isTagged);
        Double[] distances_nodes = dist_nodes.keySet().toArray(new Double[0]);
        Arrays.sort(distances_nodes);

        int distanceIndex = -1;

        while (++distanceIndex < distances_nodes.length) {
            List<Node> nodes = dist_nodes.get(distances_nodes[distanceIndex]);

            for (Node node : nodes) {
                if (OSMHelper.isPlatform(node)) {
                    return node;
                }
            }
        }

        Map<Double, List<WaySegment>> dist_ways = getNearestWaySegmentsImpl(p, OsmPrimitive::isTagged);
        Double[] distance_ways = dist_ways.keySet().toArray(new Double[0]);
        Arrays.sort(distance_ways);

        distanceIndex = -1;

        while (++distanceIndex < distance_ways.length) {
            List<WaySegment> waySegments = dist_ways.get(distance_ways[distanceIndex]);
            for (WaySegment ws : waySegments) {
                if (ws != null && ws.way != null) {
                    if (OSMHelper.isPlatform(ws.way)) {
                        return ws.way;
                    }
                }
            }
        }

        return null;
    }

    private List<OsmPrimitive> findNearestStopPlaces(LatLon coord) {
        Point p = MainApplication.getMap().mapView.getPoint(coord);
        Map<Double, List<Node>> dist_nodes = getNearestNodesImpl(p, OsmPrimitive::isTagged, 15);
        Double[] distances_nodes = dist_nodes.keySet().toArray(new Double[0]);
        Arrays.sort(distances_nodes);

        int distanceIndex = -1;

        List<OsmPrimitive> stopPlacesList = new ArrayList<>();

        while (++distanceIndex < distances_nodes.length) {
            List<Node> nodes = dist_nodes.get(distances_nodes[distanceIndex]);

            for (Node node : nodes) {
                if (OSMHelper.isStopPlace(node)) {
                    stopPlacesList.add(node);
                }
            }
        }

        Map<Double, List<WaySegment>> dist_ways = getNearestWaySegmentsImpl(p, OsmPrimitive::isTagged, 15);
        Double[] distance_ways = dist_ways.keySet().toArray(new Double[0]);
        Arrays.sort(distance_ways);

        distanceIndex = -1;

        while (++distanceIndex < distance_ways.length) {
            List<WaySegment> waySegments = dist_ways.get(distance_ways[distanceIndex]);
            for (WaySegment ws : waySegments) {
                if (ws != null && ws.way != null) {
                    if (OSMHelper.isStopPlace(ws.way)) {
                        stopPlacesList.add(ws.way);
                    }
                }
            }
        }

        return stopPlacesList;
    }

    private Map<Double, List<Node>> getNearestTrainStationsImpl(Point p, Predicate<OsmPrimitive> predicate) {
        Map<Double, List<Node>> nearestMap = new TreeMap<>();

        if (ds != null) {
            MapView mapView = MainApplication.getMap().mapView;

            int snapDistanceSq = 1400;
            double dist = snapDistanceSq;
            snapDistanceSq *= snapDistanceSq;

            for (Node n : ds.searchNodes(getBBox(p, snapDistanceSq))) {
                if (predicate.test(n) && (dist = mapView.getPoint2D(n).distanceSq(p)) < snapDistanceSq) {
                    if (OSMHelper.isTrainStation(n)) {
                        nearestMap.computeIfAbsent(dist, k -> new LinkedList<>()).add(n);
                    }
                }
            }
        }

        return nearestMap;
    }

    private Map<Double, List<Node>> getNearestNodesImpl(Point p, Predicate<OsmPrimitive> predicate) {
        Map<Double, List<Node>> nearestMap = new TreeMap<>();
        DataSet ds = MainApplication.getLayerManager().getActiveDataSet();

        if (ds != null) {
            MapView mapView = MainApplication.getMap().mapView;

            double dist, snapDistanceSq = SNAP_DISTANCE;
            snapDistanceSq *= snapDistanceSq;

            for (Node n : ds.searchNodes(getBBox(p, SNAP_DISTANCE))) {
                if (predicate.test(n)
                        && (dist = mapView.getPoint2D(n).distanceSq(p)) < snapDistanceSq) {
                    nearestMap.computeIfAbsent(dist, k -> new LinkedList<>()).add(n);
                }
            }
        }

        return nearestMap;
    }

    private Map<Double, List<Node>> getNearestNodesImpl(Point p, Predicate<OsmPrimitive> predicate, int snapDistance) {
        Map<Double, List<Node>> nearestMap = new TreeMap<>();
        DataSet ds = MainApplication.getLayerManager().getActiveDataSet();

        if (ds != null) {
            MapView mapView = MainApplication.getMap().mapView;

            double dist, snapDistanceSq = snapDistance;
            snapDistanceSq *= snapDistanceSq;

            for (Node n : ds.searchNodes(getBBox(p, snapDistance))) {
                if (predicate.test(n)
                        && (dist = mapView.getPoint2D(n).distanceSq(p)) < snapDistanceSq) {
                    nearestMap.computeIfAbsent(dist, k -> new LinkedList<>()).add(n);
                }
            }
        }

        return nearestMap;
    }

    private Map<Double, List<WaySegment>> getNearestWaySegmentsImpl(Point p, Predicate<OsmPrimitive> predicate) {
        Map<Double, List<WaySegment>> nearestMap = new TreeMap<>();
        DataSet ds = MainApplication.getLayerManager().getActiveDataSet();

        if (ds != null) {
            MapView mapView = MainApplication.getMap().mapView;

            double snapDistanceSq = SNAP_DISTANCE;
            snapDistanceSq *= snapDistanceSq;

            for (Way w : ds.searchWays(getBBox(p, SNAP_DISTANCE))) {
                if (!predicate.test(w)) {
                    continue;
                }
                Node lastN = null;
                int i = -2;
                for (Node n : w.getNodes()) {
                    i++;
                    if (n.isDeleted() || n.isIncomplete()) {
                        continue;
                    }
                    if (lastN == null) {
                        lastN = n;
                        continue;
                    }

                    Point2D pA = mapView.getPoint2D(lastN);
                    Point2D pB = mapView.getPoint2D(n);
                    double c = pA.distanceSq(pB);
                    double a = p.distanceSq(pB);
                    double b = p.distanceSq(pA);

                    double perDistSq = Double.longBitsToDouble(
                            Double.doubleToLongBits(a - (a - b + c) * (a - b + c) / 4 / c)
                            >> 32 << 32);

                    if (perDistSq < snapDistanceSq && a < c + snapDistanceSq && b < c + snapDistanceSq) {
                        nearestMap.computeIfAbsent(perDistSq, k -> new LinkedList<>()).add(new WaySegment(w, i));
                    }

                    lastN = n;
                }
            }
        }

        return nearestMap;
    }

    private Map<Double, List<WaySegment>> getNearestWaySegmentsImpl(Point p, Predicate<OsmPrimitive> predicate, int snapDistance) {
        Map<Double, List<WaySegment>> nearestMap = new TreeMap<>();
        DataSet ds = MainApplication.getLayerManager().getActiveDataSet();

        if (ds != null) {
            MapView mapView = MainApplication.getMap().mapView;

            double snapDistanceSq = snapDistance;
            snapDistanceSq *= snapDistanceSq;

            for (Way w : ds.searchWays(getBBox(p, snapDistance))) {
                if (!predicate.test(w)) {
                    continue;
                }
                Node lastN = null;
                int i = -2;
                for (Node n : w.getNodes()) {
                    i++;
                    if (n.isDeleted() || n.isIncomplete()) {
                        continue;
                    }
                    if (lastN == null) {
                        lastN = n;
                        continue;
                    }

                    Point2D pA = mapView.getPoint2D(lastN);
                    Point2D pB = mapView.getPoint2D(n);
                    double c = pA.distanceSq(pB);
                    double a = p.distanceSq(pB);
                    double b = p.distanceSq(pA);

                    double perDistSq = Double.longBitsToDouble(
                            Double.doubleToLongBits(a - (a - b + c) * (a - b + c) / 4 / c)
                            >> 32 << 32);

                    if (perDistSq < snapDistanceSq && a < c + snapDistanceSq && b < c + snapDistanceSq) {
                        nearestMap.computeIfAbsent(perDistSq, k -> new LinkedList<>()).add(new WaySegment(w, i));
                    }

                    lastN = n;
                }
            }
        }

        return nearestMap;
    }

    private BBox getBBox(Point p, int snapDistance) {
        MapView mapView = MainApplication.getMap().mapView;
        return new BBox(mapView.getLatLon(p.x - snapDistance, p.y - snapDistance),
                mapView.getLatLon(p.x + snapDistance, p.y + snapDistance));
    }

    public static PrimitiveLogMessage logMessage(long primitiveId, OsmPrimitiveType primitiveType, HashMap<String, String> keys) {
        PrimitiveLogMessage logMessage = new PrimitiveLogMessage(primitiveId, primitiveType, keys);
        LOG_MESSAGES.add(logMessage);

        return logMessage;
    }

    public static boolean openXMLFile(final File file) {
        if (!Desktop.isDesktopSupported()) {
            return false;
        }

        Desktop desktop = Desktop.getDesktop();

        if (!desktop.isSupported(Desktop.Action.EDIT)) {
            return false;
        }

        try {
            desktop.edit(file);
        }
        catch (IOException e) {
            e.printStackTrace(System.out);
            return false;
        }

        return true;
    }
}
