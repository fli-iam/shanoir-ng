/**
 * Shanoir NG - Import, manage and share neuroimaging data
 * Copyright (C) 2009-2019 Inria - https://www.inria.fr/
 * Contact us on https://project.inria.fr/shanoir/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html
 */

package org.shanoir.ng.importer.eeg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.shanoir.ng.importer.eeg.brainvision.BrainVisionReader;
import org.shanoir.ng.importer.model.Channel;
import org.shanoir.ng.importer.model.Event;
import org.shanoir.ng.shared.exception.ShanoirImportException;

/**
 * Test class for Brainvision reader.
 * @author fli
 *
 */
public class BrainVisionReaderTest {

    BrainVisionReader reader;
    File vhdrFile = new File("./src/main/resources/tests/eeg/ROBEEG_BACGU020_dlpfc_l_0002.vhdr");

    @Test
    public void testReadChannels() throws ShanoirImportException {
        assertNotNull(vhdrFile);
        assertTrue(vhdrFile.exists());

        // File reading
        reader = new BrainVisionReader(vhdrFile);
        assertNotNull(reader.getEegFile());
        assertNotNull(reader.getChannels());

        // Channel parsing
        assertEquals(64, reader.getChannels().size());
        Channel channel1 = reader.getChannels().get(0);
        assertNotNull(channel1);
        assertEquals("Fp1", channel1.getName());
        assertEquals(1000, channel1.getHighCutoff());
        assertEquals(0, channel1.getLowCutoff());
        assertEquals(Float.valueOf(0), Float.valueOf(channel1.getNotch()));
        assertEquals(Float.valueOf((float) 0.5), Float.valueOf(channel1.getResolution()));
        assertEquals("µV", channel1.getReferenceUnits());

        // Position parsing
        // 9.27344073    2.48990783    2.58100338
        assertEquals(Float.valueOf((float) 9.27344073), Float.valueOf(channel1.getX()));
        assertEquals(Float.valueOf((float) 2.48990783), Float.valueOf(channel1.getY()));
        assertEquals(Float.valueOf((float) 2.58100338), Float.valueOf(channel1.getZ()));

        // Events parsing
        List<Event> events = reader.getEvents();
        assertNotNull(events);
        assertEquals(88, events.size());

        Event event1 = events.get(0);

        assertNotNull(event1);
        assertEquals(0, event1.getChannelNumber());
        assertEquals("", event1.getDescription());
        assertEquals(1, event1.getPoints());
        assertEquals("1", event1.getPosition());
        assertEquals("New Segment", event1.getType());
        assertNotNull(event1.getDate());

        Event event2 = events.get(1);

        assertNotNull(event1);
        assertEquals(0, event2.getChannelNumber());
        assertEquals("R128", event2.getDescription());
        assertEquals(1, event2.getPoints());
        assertEquals("1440", event2.getPosition());
        assertEquals("Response", event2.getType());
        assertNull(event2.getDate());

    }

}
