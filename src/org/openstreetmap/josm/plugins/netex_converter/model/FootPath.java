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

import com.netex.model.PathJunction;
import com.netex.model.SitePathLink;
import java.util.List;

/**
 *
 * @author Labian Gashi
 */
public class FootPath
{

    private List<PathJunction> pathJunctions;
    private SitePathLink sitePathLink;

    public FootPath(List<PathJunction> pathJunctions, SitePathLink sitePathLink)
    {
        this.pathJunctions = pathJunctions;
        this.sitePathLink = sitePathLink;
    }

    public List<PathJunction> getPathJunctions()
    {
        return pathJunctions;
    }

    public void setPathJunctions(List<PathJunction> pathJunctions)
    {
        this.pathJunctions = pathJunctions;
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
