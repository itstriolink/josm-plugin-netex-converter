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
public class Steps
{

    private List<PathJunction> pathJunctions;
    private EquipmentPlace equipmentPlace;
    private SitePathLink sitePathLink;

    public Steps(List<PathJunction> pathJunctions, EquipmentPlace equipmentPlace, SitePathLink sitePathLink)
    {
        this.pathJunctions = pathJunctions;
        this.equipmentPlace = equipmentPlace;
        this.sitePathLink = sitePathLink;
    }

    public List<PathJunction> getPathJunctions()
    {
        return pathJunctions;
    }

    public void setPathJunction(List<PathJunction> pathJunctions)
    {
        this.pathJunctions = pathJunctions;
    }

    public EquipmentPlace getEquipmentPlace()
    {
        return equipmentPlace;
    }

    public void setEquipmentPlace(EquipmentPlace equipmentPlace)
    {
        this.equipmentPlace = equipmentPlace;
    }

    public SitePathLink getSitePathLink()
    {
        return sitePathLink;
    }

    public void setSitePathLink(SitePathLink sitePathLink)
    {
        this.sitePathLink = sitePathLink;
    }
}
