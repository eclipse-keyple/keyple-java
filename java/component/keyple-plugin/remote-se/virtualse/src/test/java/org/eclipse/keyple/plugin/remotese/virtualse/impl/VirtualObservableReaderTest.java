package org.eclipse.keyple.plugin.remotese.virtualse.impl;

import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.impl.AbstractKeypleNode;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class VirtualObservableReaderTest {


    static final String pluginName = "pluginName";
    static final String nativeReaderName = "nativeReaderName";

    VirtualObservableReader reader;
    AbstractKeypleNode node;
    MockObserver observer;
    ReaderEvent event;
    AbstractDefaultSelectionsRequest abstractDefaultSelectionsRequest;
    ObservableReader.NotificationMode notificationMode;
    ObservableReader.PollingMode pollingMode;

    @Before
    public void setUp() {
        node = mock(AbstractKeypleNode.class);
        reader = new VirtualObservableReader(pluginName, nativeReaderName, node);
        observer = new MockObserver();
        event = new ReaderEvent(pluginName, nativeReaderName, ReaderEvent.EventType.SE_INSERTED, null);
        abstractDefaultSelectionsRequest = SampleFactory.getSelectionRequest();
        notificationMode = SampleFactory.getNotificationMode();
        pollingMode = ObservableReader.PollingMode.REPEATING;

    }

    @Test
    public void addObserver_count_removeObserver(){
        assertThat(reader.countObservers()).isEqualTo(0);
        reader.addObserver(observer);
        assertThat(reader.countObservers()).isEqualTo(1);
        reader.removeObserver(observer);
        assertThat(reader.countObservers()).isEqualTo(0);
    }

    @Test
    public void notifyEvent_to_OneObserver(){
        reader.addObserver(observer);
        reader.notifyObservers(event);
        assertThat(observer.readerEvents.get(0)).isEqualToComparingFieldByFieldRecursively(event);
    }

    @Test
    public void notifyEvent_to_ZeroObserver_doNothing(){
        reader.notifyObservers(event);
    }

    @Test
    public void setDefaultSelectionRequest_shouldSendDto(){
        reader.setDefaultSelectionRequest(abstractDefaultSelectionsRequest, notificationMode, pollingMode);
    }

    /*
    private helpers
     */


    private class MockObserver implements ObservableReader.ReaderObserver {

        public List<ReaderEvent> readerEvents = new ArrayList<ReaderEvent>();

        @Override
        public void update(ReaderEvent event) {
            readerEvents.add(event);
        };
    };

}
