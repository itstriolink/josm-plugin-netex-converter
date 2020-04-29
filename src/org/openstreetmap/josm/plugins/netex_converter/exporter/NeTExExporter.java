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

import org.openstreetmap.josm.tools.Logging;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import com.netex.model.*;
import com.netex.validation.NeTExValidator;

import java.awt.Point;
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
import javax.swing.JOptionPane;

import jaxb.CustomMarshaller;
import net.opengis.gml._3.AbstractRingPropertyType;
import net.opengis.gml._3.DirectPositionListType;
import net.opengis.gml._3.LinearRingType;
import net.opengis.gml._3.PolygonType;
import org.openstreetmap.josm.data.coor.EastNorth;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.MainApplication;

import static org.openstreetmap.josm.tools.I18n.tr;

import org.openstreetmap.josm.data.coor.LatLon;
import org.openstreetmap.josm.data.osm.BBox;
import org.openstreetmap.josm.data.osm.RelationMember;
import org.openstreetmap.josm.gui.MapView;
import static org.openstreetmap.josm.gui.NavigatableComponent.PROP_SNAP_DISTANCE;

import org.openstreetmap.josm.plugins.netex_converter.model.Elevator;
import org.openstreetmap.josm.plugins.netex_converter.model.FootPath;
import org.openstreetmap.josm.plugins.netex_converter.model.Steps;

import org.openstreetmap.josm.plugins.netex_converter.util.OSMHelper;
import org.xml.sax.SAXException;

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
    //private final NeTExValidator neTExValidator;

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
        //neTExValidator = NeTExValidator.getNeTExValidator();
    }

    public void exportToNeTEx(File neTExFile) throws IOException, org.xml.sax.SAXException, org.xml.sax.SAXException {

        Collection<OsmPrimitive> primitives;

        Logging.info(tr("Grüezi {0}!", "Labian"));

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
            else if (primitive instanceof Relation) {
                Relation relation = (Relation) primitive;

                if (OSMHelper.isTrainStation(relation)) {
                    stopPlaces.put(relation, neTExParser.createStopPlace(relation, StopTypeEnumeration.RAIL_STATION));
                }
                else if (OSMHelper.isBusStation(relation) || OSMHelper.isStopArea(relation)) {
                    stopPlaces.put(relation, neTExParser.createStopPlace(relation, StopTypeEnumeration.BUS_STATION));
                }
                else if (OSMHelper.isBusStop(relation)) {
                    stopPlaces.put(relation, neTExParser.createStopPlace(relation, StopTypeEnumeration.ONSTREET_BUS));
                }
                else if (OSMHelper.isPlatform(relation)) {
                    quays.put(relation, neTExParser.createQuay(relation));
                }
            }
            else {
                //LOGGER.warn(tr("The OSM primitive type could not be determined."));
            }
        }

        if (stopPlaces.isEmpty()) {
            JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("File has not been exported because no stop places have been found in the currently loaded data."));
            return;
        }

        Map<OsmPrimitive, StopPlace> stopPlacesClone = new HashMap<>(stopPlaces);

        Map<StopPlace, List<OsmPrimitive>> stopsToCorrect = new HashMap<>();

        Iterator<Map.Entry<OsmPrimitive, StopPlace>> stopPlacesIterator = stopPlaces.entrySet().iterator();
        Iterator<Map.Entry<OsmPrimitive, StopPlace>> stopPlacesCloneIterator = stopPlacesClone.entrySet().iterator();

        while (stopPlacesIterator.hasNext()) {
            Map.Entry<OsmPrimitive, StopPlace> entry = stopPlacesIterator.next();
            boolean removeEntry = false;

            while (stopPlacesCloneIterator.hasNext()) {
                Map.Entry<OsmPrimitive, StopPlace> entryClone = stopPlacesCloneIterator.next();

                if (entry.getValue() != null && entryClone.getValue() != null && !entry.getValue().getPrivateCode().equals(entryClone.getValue().getPrivateCode())) {
                    String id = entry.getValue().getId();
                    String publicCode = entry.getValue().getPublicCode();
                    StopTypeEnumeration stopType = entry.getValue().getStopPlaceType();

                    if (id != null && publicCode != null) {
                        String cloneId = entryClone.getValue().getId();
                        String clonePublicCode = entryClone.getValue().getPublicCode();
                        StopTypeEnumeration cloneStopType = entryClone.getValue().getStopPlaceType();

                        if (id.equals(cloneId) && publicCode.equals(clonePublicCode) && stopType.equals(cloneStopType)) {
                            //if(OSMHelper.isBusStation(entry.getKey()) && OSMHelper.isStopArea(entryClone.getKey())){
                            //   continue;
                            //}

                            removeEntry = true;

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
                        }
                    }
                }
            }

            if (removeEntry) {
                stopPlacesIterator.remove();
            }

            stopPlacesCloneIterator = stopPlacesClone.entrySet().iterator();
        }

        for (Map.Entry<StopPlace, List<OsmPrimitive>> entry : stopsToCorrect.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                StopTypeEnumeration stopTypeEnumeration = entry.getKey().getStopPlaceType();
                OsmPrimitive firstStopPlace = entry.getValue().get(0);

                stopPlaces.put(firstStopPlace, neTExParser.createStopPlace(firstStopPlace, stopTypeEnumeration));

                for (OsmPrimitive primitive : entry.getValue()) {
                    quays.put(primitive, neTExParser.createQuay(primitive));
                }
            }
        }

        HashMap<StopPlace, Quays_RelStructure> currentQuays = new HashMap<>();

        for (Iterator<Map.Entry<OsmPrimitive, Quay>> it = quays.entrySet().iterator(); it.hasNext();) {
            HashMap.Entry<OsmPrimitive, Quay> quayEntry = it.next();
            String quayUicRef = OSMHelper.getUicRef(quayEntry.getKey());
            String quayRef = OSMHelper.getRef(quayEntry.getKey());
            quayRef = OSMHelper.switchRefDelimiter(quayRef);
            QuayTypeEnumeration quayType = QuayTypeEnumeration.OTHER;

            LatLon coord;

            if (quayEntry.getKey() instanceof Node) {
                coord = ((Node) quayEntry.getKey()).getCoor();
            }
            else if (quayEntry.getKey() instanceof Way) {
                coord = ((Way) quayEntry.getKey()).firstNode().getCoor();
            }
            else {
                RelationMember firstMember = ((Relation) quayEntry.getKey()).firstMember();

                if (firstMember.isNode()) {
                    coord = firstMember.getNode().getCoor();
                }
                else if (firstMember.isWay()) {
                    coord = firstMember.getWay().firstNode().getCoor();
                }
                else {
                    coord = null;
                }
            }

            Point p = MainApplication.getMap().mapView.getPoint(coord);

            List<Way> nearestWays = MainApplication.getMap().mapView.getNearestWays(p, OsmPrimitive::isTagged);

            PolygonType polygonType = null;
            for (Way way : nearestWays) {
                if (OSMHelper.isHighwayPlatform(way)) {
                    polygonType = neTExParser.createPolygonType(way);
                }
            }

            if (quayUicRef != null && !quayUicRef.trim().isEmpty()) {
                for (StopPlace stopPlace : stopPlaces.values()) {
                    if (stopPlace.getPublicCode() != null && stopPlace.getPublicCode().equals(quayUicRef)) {

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

                        if (quayRef != null && !quayRef.isEmpty()) {
                            if (currentQuays.containsKey(stopPlace)) {
                                currentQuays.replace(stopPlace, currentQuays.get(stopPlace).withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                        .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, quayRef))
                                        .withPublicCode(quayRef)
                                        .withPolygon(polygonType)
                                        .withQuayType(currentQuayType))));
                            }
                            else {
                                currentQuays.put(stopPlace, new Quays_RelStructure().withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                        .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, quayRef))
                                        .withPublicCode(quayRef)
                                        .withPolygon(polygonType)
                                        .withQuayType(currentQuayType))));
                            }
                        }
                        else {
                            if (currentQuays.containsKey(stopPlace)) {
                                currentQuays.replace(stopPlace, currentQuays.get(stopPlace).withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                        .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, quayEntry.getKey().getId()))
                                        .withPolygon(polygonType)
                                        .withQuayType(currentQuayType))));
                            }
                            else {
                                currentQuays.put(stopPlace, new Quays_RelStructure().withQuayRefOrQuay(Arrays.asList(quayEntry.getValue()
                                        .withId(String.format("ch:1:Quay:%1$s:%2$s", quayUicRef, quayEntry.getKey().getId()))
                                        .withPolygon(polygonType)
                                        .withQuayType(currentQuayType))));
                            }
                        }
                    }
                }
            }
            else {

                Node closestStopPlace;

                if (quayEntry.getValue().getQuayType().equals(QuayTypeEnumeration.RAIL_PLATFORM)) {
                    closestStopPlace = findNearestTrainStation(coord);
                }
                else {
                    closestStopPlace = findNearestStopPlace(coord);
                }

                if (closestStopPlace != null) {
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
                else {
//                    StopTypeEnumeration stopType;
//
//                    if (OSMHelper.isTrainStation(quayEntry.getKey(), false)) {
//                        stopType = StopTypeEnumeration.RAIL_STATION;
//                    }
//                    else if (OSMHelper.isBusStation(quayEntry.getKey(), false)) {
//                        stopType = StopTypeEnumeration.BUS_STATION;
//                    }
//                    else if (OSMHelper.isBusStop(quayEntry.getKey(), false)) {
//                        stopType = StopTypeEnumeration.ONSTREET_BUS;
//                    }
//                    else {
//                        stopType = StopTypeEnumeration.OTHER;
//                    }
//
//                    stopPlaces.put(quayEntry.getKey(), neTExParser.createStopPlace(quayEntry.getKey(), stopType));
//
//                    it.remove();

                    //...
                }
            }
        }

        for (HashMap.Entry<StopPlace, Quays_RelStructure> entry : currentQuays.entrySet()) {
            for (HashMap.Entry<OsmPrimitive, StopPlace> stopEntry : stopPlaces.entrySet()) {
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

            if (nearestStopPlace != null && !OSMHelper.isBusStop(nearestStopPlace)) {
                Node nearestQuay = findNearestPlatform(coord);

                for (HashMap.Entry<OsmPrimitive, StopPlace> stopEntry : stopPlaces.entrySet()) {
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

            if (nearestStopPlace != null && !OSMHelper.isBusStop(nearestStopPlace)) {
                Node nearestQuay = findNearestPlatform(coord);

                for (HashMap.Entry<OsmPrimitive, StopPlace> stopEntry : stopPlaces.entrySet()) {
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

            if (nearestStopPlace != null && !OSMHelper.isBusStop(nearestStopPlace)) {
                Node nearestQuay = findNearestPlatform(coord);

                for (HashMap.Entry<OsmPrimitive, StopPlace> stopEntry : stopPlaces.entrySet()) {
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

        for (HashMap.Entry<OsmPrimitive, StopPlace> stopEntry : stopPlaces.entrySet()) {
            for (HashMap.Entry<StopPlace, PathJunctions_RelStructure> entry : pathJunctions.entrySet()) {
                if (stopEntry.getValue() != null && stopEntry.getValue().equals(entry.getKey())) {
                    stopPlaces.replace(stopEntry.getKey(), stopEntry.getValue().withPathJunctions(entry.getValue()));
                }
            }
            for (HashMap.Entry<StopPlace, EquipmentPlaces_RelStructure> entry : equipmentPlaces.entrySet()) {
                if (stopEntry.getValue() != null && stopEntry.getValue().equals(entry.getKey())) {
                    stopPlaces.replace(stopEntry.getKey(), stopEntry.getValue().withEquipmentPlaces(entry.getValue()));
                }
            }
            for (HashMap.Entry<StopPlace, SitePathLinks_RelStructure> entry : pathLinks.entrySet()) {
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

        /*try {
            neTExValidator.validate(new StreamSource(neTExFile));
        }
        catch (SAXException ex) {
            Logger.getLogger(NeTExExporter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }*/
        JOptionPane.showMessageDialog(MainApplication.getMainFrame(), tr("NeTEx export has finished successfully."));
    }

    private Node findNearestStopPlace(LatLon coord) {
        Point p = MainApplication.getMap().mapView.getPoint(coord);
        Map<Double, List<Node>> dist_nodes = getNearestStopImpl(p, OsmPrimitive::isTagged);
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
        Map<Double, List<Node>> dist_nodes = getNearestTrainStationImpl(p, OsmPrimitive::isTagged);
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

    private Map<Double, List<Node>> getNearestStopImpl(Point p, Predicate<OsmPrimitive> predicate) {
        Map<Double, List<Node>> nearestMap = new TreeMap<>();

        if (ds != null) {
            MapView mapView = MainApplication.getMap().mapView;

            double dist, snapDistanceSq = 20;
            snapDistanceSq *= snapDistanceSq;

            for (Node n : ds.searchNodes(getBBox(p, 20))) {
                if (predicate.test(n) && (dist = mapView.getPoint2D(n).distanceSq(p)) < snapDistanceSq) {
                    if (OSMHelper.isTrainStation(n) || OSMHelper.isBusStop(n) || OSMHelper.isBusStation(n)) {
                        nearestMap.computeIfAbsent(dist, k -> new LinkedList<>()).add(n);
                        break;
                    }
                }
            }
        }

        return nearestMap;
    }

    private Map<Double, List<Node>> getNearestTrainStationImpl(Point p, Predicate<OsmPrimitive> predicate) {
        Map<Double, List<Node>> nearestMap = new TreeMap<>();

        if (ds != null) {
            MapView mapView = MainApplication.getMap().mapView;

            int snapDistanceSq = 1250;
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
