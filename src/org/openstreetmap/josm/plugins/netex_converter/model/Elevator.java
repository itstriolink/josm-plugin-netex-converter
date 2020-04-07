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

/**
 *
 * @author Labian Gashi
 */
public class Elevator {

    private PathJunction pathJunction;
    private EquipmentPlace equipmentPlace;

    public Elevator(PathJunction pathJunction, EquipmentPlace equipmentPlace) {
        this.pathJunction = pathJunction;
        this.equipmentPlace = equipmentPlace;
    }

    public PathJunction getPathJunction() {
        return pathJunction;
    }

    public void setPathJunction(PathJunction pathJunction) {
        this.pathJunction = pathJunction;
    }

    public EquipmentPlace getEquipmentPlace() {
        return equipmentPlace;
    }

    public void setEquipmentPlace(EquipmentPlace equipmentPlace) {
        this.equipmentPlace = equipmentPlace;
    }
}
