/* 
 * Copyright (C) 2020 Labian Gashi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 */
package org.openstreetmap.josm.plugins.netex_converter;

import org.openstreetmap.josm.plugins.netex_converter.ui.ExportToNeTExDialog;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;

/**
 * Loads a PDF file into a new layer.
 *
 */
public class NeTExConverterAction extends JosmAction {

    public NeTExConverterAction() {
        super(tr("Export to NeTEx..."), "netex_converter",
                tr("Export to NeTEx..."),
                Shortcut.registerShortcut("tools:netex_converter", tr("Tool: {0}", tr("Export to NeTEx...")),
                        KeyEvent.VK_N, Shortcut.ALT_CTRL_SHIFT), true);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        ExportToNeTExDialog dialog = new ExportToNeTExDialog();
        dialog.setTitle(tr("Save as NeTEx"));
    }
}
