package org.keyple.seproxy;

import static org.junit.Assert.*;

import org.junit.Test;
import org.keyple.seproxy.NotifierReader;
import org.keyple.seproxy.ReaderEvent;
import org.mockito.Mockito;

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

    @Test
    public void testGetReader() {
    	NotifierReader or = Mockito.mock(NotifierReader.class);
        ReaderEvent event = new ReaderEvent(or, ReaderEvent.EventType.IO_ERROR);
        assertEquals(or, event.getReader());
    }

    @Test
    public void testGetEvent() {
        ReaderEvent event = new ReaderEvent(null, ReaderEvent.EventType.IO_ERROR);
        assertEquals(ReaderEvent.EventType.IO_ERROR, event.getEventType());
    }

}
