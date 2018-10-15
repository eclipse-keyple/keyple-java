/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.seproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.junit.Test;

/**
 */
public class ReaderEventTest {

    @Test
    public void testReaderEvent() {
        ReaderEvent event = new ReaderEvent("plugin", "reader", ReaderEvent.EventType.IO_ERROR);
        assertNotNull(event);
    }

    @Test
    public void testGetEvent() {
        ReaderEvent event = new ReaderEvent("plugin", "reader", ReaderEvent.EventType.IO_ERROR);
        assertEquals(event.getReaderName(), "reader");
        assertEquals(event.getPluginName(), "plugin");
        assertEquals(event.getEventType(), ReaderEvent.EventType.IO_ERROR);
    }

}
