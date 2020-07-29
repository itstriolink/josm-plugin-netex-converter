/* 
 * Copyright (C) 2020 Labian Gashi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 */
package org.openstreetmap.josm.plugins.netexconverter;

import org.openstreetmap.josm.gui.MainApplication;
import org.openstreetmap.josm.gui.MainMenu;
import org.openstreetmap.josm.plugins.Plugin;
import org.openstreetmap.josm.plugins.PluginInformation;

/**
 *
 * @author Labian Gashi
 */
public class NeTExConverterPlugin extends Plugin {

    public NeTExConverterPlugin(final PluginInformation info) {
        super(info);

        MainMenu.addAfter(MainApplication.getMenu().fileMenu, new NeTExConverterAction(), false, MainApplication.getMenu().gpxExport);
    }
}
