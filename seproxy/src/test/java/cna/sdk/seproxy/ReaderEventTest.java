package cna.sdk.seproxy;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mockito.Mockito;

import cna.sdk.seproxy.ObservableReader;
import cna.sdk.seproxy.ReaderEvent;

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
        ObservableReader or = Mockito.mock(ObservableReader.class);
        ReaderEvent event = new ReaderEvent(or, ReaderEvent.EventType.IO_ERROR);
        assertEquals(or, event.getReader());
    }

    @Test
    public void testGetEvent() {
        ReaderEvent event = new ReaderEvent(null, ReaderEvent.EventType.IO_ERROR);
        assertEquals(ReaderEvent.EventType.IO_ERROR, event.getEvent());
    }

}
