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

import com.netex.model.MultilingualString;
import com.netex.model.PublicationDeliveryStructure;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import org.openstreetmap.josm.data.osm.Node;

/**
 *
 * @author Labian Gashi
 */
public class NeTExParser {

    public NeTExParser() {

    }

    public PublicationDeliveryStructure createPublicationDeliveryObject(Node node) {
        PublicationDeliveryStructure publicationDelivery = new PublicationDeliveryStructure().withDescription(new MultilingualString()
                .withValue(node.getName() + " Railway Station")
                .withLang("en"))
                .withPublicationTimestamp(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .withParticipantRef("participantRef")
                .withDataObjects(new PublicationDeliveryStructure.DataObjects());

        return publicationDelivery;
    }
}
