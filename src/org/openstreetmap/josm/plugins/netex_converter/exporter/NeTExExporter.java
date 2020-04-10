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

import com.netex.model.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import javax.swing.JOptionPane;

import jaxb.CustomMarshaller;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.plugins.netex_converter.util.OSMHelper;
import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.gui.MapView;
import static org.openstreetmap.josm.gui.NavigatableComponent.PROP_SNAP_DISTANCE;
import org.openstreetmap.josm.plugins.netex_converter.model.Elevator;
import org.openstreetmap.josm.plugins.netex_converter.model.FootPath;
import org.openstreetmap.josm.plugins.netex_converter.model.Steps;

/**
 *
 * @author Labian Gashi
 */
public class NeTExExporter {

    private final NeTExParser neTExParser;
    private final ObjectFactory neTExFactory;
    private final CustomMarshaller customMarshaller;

    private final static Logger LOGGER = Logger.getLogger(NeTExExporter.class);

    private final HashMap<Node, StopPlace> stopPlaces;
    private final HashMap<OsmPrimitive, Quay> quays;
    private final HashMap<Node, Elevator> elevators;
    private final HashMap<Way, Steps> steps;
    private final HashMap<Way, FootPath> footPaths;
    private final DataSet ds;
    //private final NeTExValidator neTExValidator = NeTExValidator.getNeTExValidator();

    public NeTExExporter() {
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

    public void exportToNeTEx(File neTExFile) {

        Collection<OsmPrimitive> primitives = null;

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

                if (OSMHelper.isSteps(way)) {
                    steps.put(way, neTExParser.createSteps(way));
                }
                else if (OSMHelper.isFootPath(way)) {
                    footPaths.put(way, neTExParser.createFootPath(way));
                }
                else if (OSMHelper.isPlatform(way)) {
                    quays.put(way, neTExParser.createQuay(way));
                }
            }
            else if (primitive instanceof Relation) {
                Relation relation = (Relation) primitive;

                if (relation.isMultipolygon()) {
                    if (OSMHelper.isPlatform(relation)) {
                        quays.put(relation, neTExParser.createQuay(relation));
                    }
                }
            }
            else {
                LOGGER.warn(tr("The OSM primitive type could not be determined."));
            }
        }

        if (stopPlaces.isEmpty()) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("File has not been exported because no stop places have been found in the currently loaded data."));
            return;
        }

        HashMap<StopPlace, Quays_RelStructure> currentQuays = new HashMap<>();

        for (HashMap.Entry<OsmPrimitive, Quay> quayEntry : quays.entrySet()) {
            String quayUicRef = OSMHelper.getUicRef(quayEntry.getKey());

            String quayRef = OSMHelper.getRef(quayEntry.getKey());

            if (quayUicRef != null && !quayUicRef.trim().isEmpty()) {
                for (StopPlace stopPlace : stopPlaces.values()) {
                    if (stopPlace.getPublicCode().equals(quayUicRef)) {
                        if (currentQuays.containsKey(stopPlace)) {
                            currentQuays.replace(stopPlace, currentQuays.get(stopPlace).withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                    .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, quayRef))
                                    .withPublicCode(quayRef))));
                        }
                        else {
                            currentQuays.put(stopPlace, new Quays_RelStructure().withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                    .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, quayRef))
                                    .withPublicCode(quayRef))));
                        }
                    }
                }
            }
            else {
                LatLon coord = null;
                boolean isRelation = false;

                if (quayEntry.getKey() instanceof Node) {
                    coord = ((Node) quayEntry.getKey()).getCoor();
                }
                else if (quayEntry.getKey() instanceof Way) {
                    coord = ((Way) quayEntry.getKey()).firstNode().getCoor();
                }
                else {
                    isRelation = true;
                    coord = ((Relation) quayEntry.getKey()).firstMember().getWay().firstNode().getCoor();
                }

                Node closestStopPlace = null;

                if (isRelation) {
                    closestStopPlace = findNearestTrainStation(coord);
                }
                else {
                    closestStopPlace = findNearestStopPlace(coord);
                }

                if (closestStopPlace != null) {
                    quayUicRef = OSMHelper.getUicRef(closestStopPlace);

                    quayRef = OSMHelper.switchRefDelimiter(quayRef);

                    QuayTypeEnumeration quayType = QuayTypeEnumeration.OTHER;
                    boolean modifiedQuayType = false;
                    if (quayEntry.getValue().getQuayType() != null && quayEntry.getValue().getQuayType().equals(QuayTypeEnumeration.OTHER)) {

                        if (OSMHelper.isBusStop(closestStopPlace)) {
                            quayType = QuayTypeEnumeration.BUS_STOP;
                            modifiedQuayType = true;
                        }
                        else if (OSMHelper.isBusStation(closestStopPlace)) {
                            quayType = QuayTypeEnumeration.BUS_PLATFORM;
                            modifiedQuayType = true;
                        }
                        else if (OSMHelper.isTrainStation(closestStopPlace)) {
                            quayType = QuayTypeEnumeration.RAIL_PLATFORM;
                            modifiedQuayType = true;
                        }
                    }

                    for (StopPlace stopPlace : stopPlaces.values()) {
                        if (stopPlace.getPublicCode().equals(quayUicRef)) {
                            QuayTypeEnumeration currentQuayType = modifiedQuayType ? quayType : quayEntry.getValue().getQuayType();

                            if (currentQuays.containsKey(stopPlace)) {
                                currentQuays.replace(stopPlace, currentQuays.get(stopPlace).withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                        .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, quayRef))
                                        .withPublicCode(quayRef)
                                        .withQuayType(currentQuayType))));
                            }
                            else {
                                currentQuays.put(stopPlace, new Quays_RelStructure().withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                        .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, quayRef))
                                        .withPublicCode(quayRef)
                                        .withQuayType(currentQuayType))));
                            }
                        }
                    }
                }
            }
        }

        for (HashMap.Entry<StopPlace, Quays_RelStructure> entry : currentQuays.entrySet()) {
            for (HashMap.Entry<Node, StopPlace> stopEntry : stopPlaces.entrySet()) {
                if (stopEntry.getValue().equals(entry.getKey())) {
                    stopPlaces.replace(stopEntry.getKey(), stopEntry.getValue().withQuays(entry.getValue()));
                }
            }
        }

        HashMap<StopPlace, SitePathLinks_RelStructure> pathLinks = new HashMap<>();
        HashMap<StopPlace, PathJunctions_RelStructure> pathJunctions = new HashMap<>();
        HashMap<StopPlace, EquipmentPlaces_RelStructure> equipmentPlaces = new HashMap<>();

        for (HashMap.Entry<Node, Elevator> elevatorEntry : elevators.entrySet()) {
            LatLon coord = elevatorEntry.getKey().getCoor();
            Node nearestStopPlace = findNearestStopPlace(coord);

            if (nearestStopPlace != null) {
                Node nearestQuay = findNearestPlatform(coord);

                for (HashMap.Entry<Node, StopPlace> stopEntry : stopPlaces.entrySet()) {
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

        for (HashMap.Entry<Way, Steps> stepsEntry : steps.entrySet()) {
            LatLon coord = stepsEntry.getKey().firstNode().getCoor();
            Node nearestStopPlace = findNearestStopPlace(coord);

            if (nearestStopPlace != null) {
                Node nearestQuay = findNearestPlatform(coord);

                for (HashMap.Entry<Node, StopPlace> stopEntry : stopPlaces.entrySet()) {
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

        for (HashMap.Entry<Way, FootPath> footPathEntry : footPaths.entrySet()) {
            LatLon coord = footPathEntry.getKey().firstNode().getCoor();
            Node nearestStopPlace = findNearestStopPlace(coord);

            if (nearestStopPlace != null) {
                Node nearestQuay = findNearestPlatform(coord);

                for (HashMap.Entry<Node, StopPlace> stopEntry : stopPlaces.entrySet()) {
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

        for (HashMap.Entry<Node, StopPlace> stopEntry
                : stopPlaces.entrySet()) {
            for (HashMap.Entry<StopPlace, PathJunctions_RelStructure> entry : pathJunctions.entrySet()) {
                if (stopEntry.getValue().equals(entry.getKey())) {
                    stopPlaces.replace(stopEntry.getKey(), stopEntry.getValue().withPathJunctions(entry.getValue()));
                }
            }
            for (HashMap.Entry<StopPlace, EquipmentPlaces_RelStructure> entry : equipmentPlaces.entrySet()) {
                if (stopEntry.getValue().equals(entry.getKey())) {
                    stopPlaces.replace(stopEntry.getKey(), stopEntry.getValue().withEquipmentPlaces(entry.getValue()));
                }
            }
            for (HashMap.Entry<StopPlace, SitePathLinks_RelStructure> entry : pathLinks.entrySet()) {
                if (stopEntry.getValue().equals(entry.getKey())) {
                    stopPlaces.replace(stopEntry.getKey(), stopEntry.getValue().withPathLinks(entry.getValue()));
                }
            }
        }

        ResourceFrame resourceFrame = neTExParser.createResourceFrame();

        SiteFrame siteFrame = neTExParser.createSiteFrame(new ArrayList<>(stopPlaces.values()));

        CompositeFrame compositeFrame = neTExParser.createCompositeFrame(resourceFrame, siteFrame);

        PublicationDeliveryStructure publicationDeliveryStructure = neTExParser.createPublicationDeliveryStsructure(compositeFrame);

        customMarshaller.marshal(neTExFactory.createPublicationDelivery(publicationDeliveryStructure), neTExFile);

        JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("NeTEx export has finished successfully."));
    }

    private Node findNearestStopPlace(LatLon coord) {
        Point p = MainApplication.getMap().mapView.getPoint(coord);
        Map<Double, List<Node>> dist_nodes = getNearestStopsImpl(p, OsmPrimitive::isTagged);
        Double[] distances = dist_nodes.keySet().toArray(new Double[0]);
        Arrays.sort(distances);
        Integer distanceIndex = -1;

        while (++distanceIndex < distances.length) {
            List<Node> nodes = dist_nodes.get(distances[distanceIndex]);

            for (Node node : nodes) {
                return node;
            }
        }

        return null;
    }

    private Node findNearestTrainStation(LatLon coord) {
        Point p = MainApplication.getMap().mapView.getPoint(coord);
        Map<Double, List<Node>> dist_nodes = getNearestStopsImpl(p, OsmPrimitive::isTagged, 100);
        Double[] distances = dist_nodes.keySet().toArray(new Double[0]);
        Arrays.sort(distances);

        int distanceIndex = -1;

        while (++distanceIndex < distances.length) {
            List<Node> nodes = dist_nodes.get(distances[distanceIndex]);

            for (Node node : nodes) {
                if (OSMHelper.isTrainStation(node)) {
                    return node;
                }
            }
        }

        return null;
    }

    private Node findNearestPlatform(LatLon coord) {
        Point p = MainApplication.getMap().mapView.getPoint(coord);
        Map<Double, List<Node>> dist_nodes = getNearestPlatformsImpl(p, OsmPrimitive::isTagged);
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

    private Map<Double, List<Node>> getNearestStopsImpl(Point p, Predicate<OsmPrimitive> predicate) {
        Map<Double, List<Node>> nearestMap = new TreeMap<>();

        if (ds != null) {
            MapView mapView = MainApplication.getMap().mapView;

            double dist, snapDistanceSq = PROP_SNAP_DISTANCE.get();
            snapDistanceSq *= snapDistanceSq;

            for (Node n : ds.searchNodes(getBBox(p, PROP_SNAP_DISTANCE.get()))) {
                if (predicate.test(n) && (dist = mapView.getPoint2D(n).distanceSq(p)) < snapDistanceSq) {
                    if (OSMHelper.isTrainStation(n) || OSMHelper.isBusStop(n) || OSMHelper.isBusStation(n)) {
                        nearestMap.computeIfAbsent(dist, k -> new LinkedList<>()).add(n);
                    }
                }
            }
        }

        return nearestMap;
    }

    private Map<Double, List<Node>> getNearestStopsImpl(Point p, Predicate<OsmPrimitive> predicate, int snapDistanceSq) {
        Map<Double, List<Node>> nearestMap = new TreeMap<>();

        if (ds != null) {
            MapView mapView = MainApplication.getMap().mapView;

            double dist = snapDistanceSq;
            snapDistanceSq *= snapDistanceSq;

            for (Node n : ds.searchNodes(getBBox(p, snapDistanceSq))) {
                if (predicate.test(n) && (dist = mapView.getPoint2D(n).distanceSq(p)) < snapDistanceSq) {
                    if (OSMHelper.isTrainStation(n) || OSMHelper.isBusStop(n) || OSMHelper.isBusStation(n)) {
                        nearestMap.computeIfAbsent(dist, k -> new LinkedList<>()).add(n);
                    }
                }
            }
        }

        return nearestMap;
    }

    private Map<Double, List<Node>> getNearestPlatformsImpl(Point p, Predicate<OsmPrimitive> predicate) {
        Map<Double, List<Node>> nearestMap = new TreeMap<>();

        if (ds != null) {
            MapView mapView = MainApplication.getMap().mapView;

            double dist, snapDistanceSq = PROP_SNAP_DISTANCE.get();
            snapDistanceSq *= snapDistanceSq;

            for (Node n : ds.searchNodes(getBBox(p, PROP_SNAP_DISTANCE.get()))) {
                if (predicate.test(n) && (dist = mapView.getPoint2D(n).distanceSq(p)) < snapDistanceSq) {
                    if (OSMHelper.isPlatform(n)) {
                        nearestMap.computeIfAbsent(dist, k -> new LinkedList<>()).add(n);
                    }
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
