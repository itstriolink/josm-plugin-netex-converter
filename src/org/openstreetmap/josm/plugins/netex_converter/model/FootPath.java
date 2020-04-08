/*
 * Copyright (C) 2020 Labian Gashi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 */
package org.openstreetmap.josm.plugins.netex_converter.model;

import com.netex.model.EquipmentPlace;
import com.netex.model.PathJunction;
import com.netex.model.SitePathLink;
import java.util.List;

/**
 *
 * @author Labian Gashi
 */
public class FootPath {

    private List<PathJunction> pathJunctions;
    private EquipmentPlace equipmentPlace;
    private List<SitePathLink> sitePathLinks;

    public FootPath(List<PathJunction> pathJunctions, List<SitePathLink> sitePathLinks) {
        this.pathJunctions = pathJunctions;
        this.sitePathLinks = sitePathLinks;
    }

    public FootPath(List<PathJunction> pathJunctions, EquipmentPlace equipmentPlace, List<SitePathLink> sitePathLinks) {
        this.pathJunctions = pathJunctions;
        this.equipmentPlace = equipmentPlace;
        this.sitePathLinks = sitePathLinks;
    }

    public List<PathJunction> getPathJunctions() {
        return pathJunctions;
    }

    public void setPathJunctions(List<PathJunction> pathJunctions) {
        this.pathJunctions = pathJunctions;
    }

    public EquipmentPlace getEquipmentPlace() {
        return equipmentPlace;
    }

    public void setEquipmentPlace(EquipmentPlace equipmentPlace) {
        this.equipmentPlace = equipmentPlace;
    }

    public List<SitePathLink> getSitePathLinks() {
        return sitePathLinks;
    }

    public void setSitePathLink(List<SitePathLink> sitePathLinks) {
        this.sitePathLinks = sitePathLinks;
    }
}
