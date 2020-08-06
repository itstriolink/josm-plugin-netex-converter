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

import org.openstreetmap.josm.plugins.netexconverter.ui.ExportToNeTExDialog;
import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import org.openstreetmap.josm.tools.Logging;
import org.openstreetmap.josm.actions.JosmAction;
import org.openstreetmap.josm.tools.Shortcut;
import org.xml.sax.SAXException;


public class NeTExConverterAction extends JosmAction {

    public NeTExConverterAction() {
        super(tr("Export to NeTEx..."), "netexconverter",
                tr("Export to NeTEx..."),
                Shortcut.registerShortcut("tools:NeTExConverter", tr("Tool: {0}", tr("Export to NeTEx...")),
                        KeyEvent.VK_N, Shortcut.ALT_CTRL_SHIFT), true);

    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            ExportToNeTExDialog dialog = new ExportToNeTExDialog();
            dialog.setTitle(tr("Save as NeTEx"));
        }
        catch (IOException ex) {
            Logging.error(tr("IOException occured: {0}", ex.getMessage()));
        }
        catch (SAXException ex) {
            Logging.error(tr("SAXException occured: {0}", ex.getMessage()));
        }
    }

    @Override
    protected void updateEnabledState() {
        setEnabled(getLayerManager().getEditDataSet() != null);
    }
}
