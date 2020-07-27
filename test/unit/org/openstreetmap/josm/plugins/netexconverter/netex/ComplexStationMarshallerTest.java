/*
 * Copyright (C) 2020 Labian Gashi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 */
package unit.org.openstreetmap.josm.plugins.netex_converter.netex;

import com.netex.model.AccessFeatureEnumeration;
import com.netex.model.AccessSpace;
import com.netex.model.AccessSpaceTypeEnumeration;
import com.netex.model.AccessibilityAssessment;
import com.netex.model.AccessibilityInfoFacilityEnumeration;
import com.netex.model.AccessibilityLimitation;
import com.netex.model.AccessibilityLimitations_RelStructure;
import com.netex.model.AssistanceAvailabilityEnumeration;
import com.netex.model.AssistanceFacilityEnumeration;
import com.netex.model.AssistanceService;
import com.netex.model.CompositeFrame;
import com.netex.model.CoveredEnumeration;
import com.netex.model.CrossingEquipment;
import com.netex.model.CrossingTypeEnumeration;
import com.netex.model.EntranceEnumeration;
import com.netex.model.EquipmentPlace;
import com.netex.model.Equipments_RelStructure;
import com.netex.model.Frames_RelStructure;
import com.netex.model.HandrailEnumeration;
import com.netex.model.Level;
import com.netex.model.LevelRefStructure;
import com.netex.model.LimitationStatusEnumeration;
import com.netex.model.LocationStructure;
import com.netex.model.MultilingualString;
import com.netex.model.ObjectFactory;
import com.netex.model.PassengerInformationEquipment;
import com.netex.model.PassengerInformationFacilityEnumeration;
import com.netex.model.PathJunction;
import com.netex.model.PathJunctions_RelStructure;
import com.netex.model.PathLinkEndStructure;
import com.netex.model.PlaceEquipments_RelStructure;
import com.netex.model.PlaceRefStructure;
import com.netex.model.PrivateCodeStructure;
import com.netex.model.PublicationDeliveryStructure;
import com.netex.model.Quay;
import com.netex.model.QuayTypeEnumeration;
import com.netex.model.Quays_RelStructure;
import com.netex.model.ResourceFrame;
import com.netex.model.ShelterEquipment;
import com.netex.model.SimplePoint_VersionStructure;
import com.netex.model.SiteFrame;
import com.netex.model.SitePathLink;
import com.netex.model.SitePathLinks_RelStructure;
import com.netex.model.StairEndStructure;
import com.netex.model.StaircaseEquipment;
import com.netex.model.StaircaseEquipmentRefStructure;
import com.netex.model.StopPlace;
import com.netex.model.StopPlaceEntrance;
import com.netex.model.StopPlacesInFrame_RelStructure;
import com.netex.model.StopTypeEnumeration;
import com.netex.model.TicketingEquipment;
import com.netex.model.TicketingEquipmentRefStructure;
import com.netex.model.TypeOfPointRefStructure;
import com.netex.model.TypeOfPointRefs_RelStructure;
import com.netex.model.WaitingRoomEquipment;
import com.netex.model.ZoneRefStructure;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import javax.xml.bind.JAXBException;
import jaxb.CustomMarshaller;
import net.opengis.gml._3.AbstractRingPropertyType;
import net.opengis.gml._3.DirectPositionType;
import net.opengis.gml._3.LinearRingType;
import net.opengis.gml._3.PolygonType;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Labian Gashi
 */
public class ComplexStationMarshallerTest {

    private static ObjectFactory neTExFactory;
    private static net.opengis.gml._3.ObjectFactory gmlFactory;

    @Before
    public void setUp() {
        neTExFactory = new ObjectFactory();
        gmlFactory = new net.opengis.gml._3.ObjectFactory();
    }

    @Test
    public void exportComplexStationTest() throws JAXBException {

        CustomMarshaller customMarshaller = new CustomMarshaller(PublicationDeliveryStructure.class);

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
                .withId("ch:1:CrossingEquipment:fussgängerstreifen")
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
                .withId("ch:1:PassengerInformationEquipment:SÜV_abfahrten_dynamisch")
                .withPassengerInformationFacilityList(PassengerInformationFacilityEnumeration.STOP_ANNOUNCEMENTS)
                .withAccessibilityInfoFacilityList(Arrays.asList(
                        AccessibilityInfoFacilityEnumeration.AUDIO_INFORMATION,
                        AccessibilityInfoFacilityEnumeration.DISPLAYS_FOR_VISUALLY_IMPAIRED));

        StaircaseEquipment treppe_S_P0 = new StaircaseEquipment()
                .withId("ch:1:StaircaseEquipment:S_P0")
                .withDescription(new MultilingualString().withValue("Top: Overpass, Bottom: Max-Dätwyler-Platz"))
                .withHandrailType(HandrailEnumeration.BOTH_SIDES)
                .withTopEnd(new StairEndStructure().withTexturedSurface(true))
                .withBottomEnd(new StairEndStructure().withTexturedSurface(true));

        Level level_1 = new Level();
        Level level_2 = new Level();

        AccessSpace ueberfuehrung = new AccessSpace()
                .withId("ch:1:AccessSpace:8516161:Überführung")
                .withDescription(new MultilingualString().withValue("Überführung"))
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
                .withDataObjects(new PublicationDeliveryStructure.DataObjects()
                        .withCompositeFrameOrCommonFrame(Arrays.asList(
                                neTExFactory.createCompositeFrame(compositeFrame))));
        try {

            File neTExFile = File.createTempFile("ComplexStationMarshaller", ".xml");
            customMarshaller.marshal(neTExFactory.createPublicationDelivery(publicationDelivery), neTExFile);

            boolean deleted = false;
            deleted = neTExFile.delete();

            if (!deleted) {
                neTExFile.deleteOnExit();
            }
        }
        catch (IOException | SecurityException ioe) {
            ioe.printStackTrace(System.out);
        }
    }
}
