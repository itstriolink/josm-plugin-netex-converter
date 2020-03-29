/*
 * Copyright (C) 2020 Labian Gashi
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 */
package unit.org.openstreetmap.josm.plugins.netex_converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import org.junit.Test;

/**
 *
 * @author Labian Gashi
 */

public class ExporterTest {
    
    @Test
    public void testSample(){
        int a = 2;
        int b = 2;
        
        assertEquals(a, b);
        assertSame(a, b);
    }
}
