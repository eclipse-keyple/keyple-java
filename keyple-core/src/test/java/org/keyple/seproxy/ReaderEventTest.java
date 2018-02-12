/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class ReaderEventTest {

    @Test
    public void testReaderEventEnum() {
        for (ReaderEvent.EventType el : ReaderEvent.EventType.values()) {
            assertEquals(el, ReaderEvent.EventType.valueOf(el.toString()));
        }
    }

    @Test
    public void testReaderEvent() {
        ReaderEvent event = new ReaderEvent(null, ReaderEvent.EventType.IO_ERROR);
        assertNotNull(event);
    }

    // TODO: Fix this test
    /*
     * @Test public void testGetReader() { NotifierReader or = Mockito.mock(NotifierReader.class);
     * ReaderEvent event = new ReaderEvent(or, ReaderEvent.EventType.IO_ERROR);
     * Assert.assertEquals(or, event.getReader()); }
     */

    @Test
    public void testGetEvent() {
        ReaderEvent event = new ReaderEvent(null, ReaderEvent.EventType.IO_ERROR);
        assertEquals(ReaderEvent.EventType.IO_ERROR, event.getEventType());
    }

}
