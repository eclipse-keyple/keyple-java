package org.eclipse.keyple.core.seproxy.event;

import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.message.DefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.plugin.local.AbsLocalReaderSelectionTest;
import org.eclipse.keyple.core.seproxy.plugin.local.AbsObservableLocalReaderTest;
import org.eclipse.keyple.core.seproxy.plugin.local.AbstractObservableLocalReader;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import static org.eclipse.keyple.core.seproxy.plugin.local.AbsObservableLocalReaderTest.getNotMatchingResponses;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

public class DefaultSelectionTest {

    /** ==== Card event ====================================== */

    final String PLUGIN_NAME = "AbsObservableLocalReaderTestP";
    final String READER_NAME = "AbsObservableLocalReaderTest";

    /*
     * no default selection
     */
    @Test
    public void seInserted() throws Exception {
        // empty reader
        AbstractObservableLocalReader r = AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

        // test
        ReaderEvent event = r.processSeInserted();

        Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());
        Assert.assertNull(event.getDefaultSelectionsResponse());
        Assert.assertEquals(PLUGIN_NAME, event.getPluginName());
        Assert.assertEquals(READER_NAME, event.getReaderName());
    }

    /*
     * selection is not successful
     */
    @Test
    public void seInserted_ALWAYS() throws Exception {
        AbstractObservableLocalReader r = AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

        // configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.ALWAYS;

        // mock return matching selection
        List<SeResponse> responses = getNotMatchingResponses();
        doReturn(responses).when(r).transmitSet(selections, multi, channel);

        // test
        r.setDefaultSelectionRequest(new DefaultSelectionsRequest(selections, multi, channel),
                mode);
        ReaderEvent event = r.processSeInserted();

        // assert
        Assert.assertEquals(ReaderEvent.EventType.SE_INSERTED, event.getEventType());
        Assert.assertEquals(responses,
                event.getDefaultSelectionsResponse().getSelectionSeResponseSet());
        Assert.assertEquals(PLUGIN_NAME, event.getPluginName());
        Assert.assertEquals(READER_NAME, event.getReaderName());

    }

    /*
     * selection is successful
     */
    @Test
    public void seMatched_MATCHED_ONLY() throws Exception {
        AbstractObservableLocalReader r = AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

        // configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.MATCHED_ONLY;

        // mock
        // return success selection
        List<SeResponse> responses =  AbsObservableLocalReaderTest.getMatchingResponses();
        doReturn(responses).when(r).transmitSet(selections, multi, channel);

        // test
        r.setDefaultSelectionRequest(new DefaultSelectionsRequest(selections, multi, channel),
                mode);
        ReaderEvent event = r.processSeInserted();

        Assert.assertEquals(ReaderEvent.EventType.SE_MATCHED, event.getEventType());
        Assert.assertEquals(responses,
                event.getDefaultSelectionsResponse().getSelectionSeResponseSet());
        Assert.assertEquals(PLUGIN_NAME, event.getPluginName());
        Assert.assertEquals(READER_NAME, event.getReaderName());

    }




    /*
     * selection is not successful
     */
    @Test
    public void noEvent_MATCHED_ONLY() throws Exception {
        AbstractObservableLocalReader r = AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

        // configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.MATCHED_ONLY;

        // mock return matching selection
        doReturn(getNotMatchingResponses()).when(r).transmitSet(selections, multi, channel);

        // test
        r.setDefaultSelectionRequest(new DefaultSelectionsRequest(selections, multi, channel),
                mode);
        ReaderEvent event = r.processSeInserted();

        Assert.assertEquals(null, event);
    }




    /*
     * Simulate an IOException while selecting Do not throw any event Nor an exception
     */
    @Test
    public void noEvent_IOError() throws Exception {
        AbstractObservableLocalReader r = AbsObservableLocalReaderTest.getSpy(PLUGIN_NAME, READER_NAME);

        // configure parameters
        Set<SeRequest> selections = new HashSet<SeRequest>();
        MultiSeRequestProcessing multi = MultiSeRequestProcessing.PROCESS_ALL;
        ChannelControl channel = ChannelControl.CLOSE_AFTER;
        ObservableReader.NotificationMode mode = ObservableReader.NotificationMode.ALWAYS;

        // throw IO
        doThrow(new KeypleIOReaderException("io error when selecting")).when(r)
                .transmitSet(selections, multi, channel);


        // test
        r.setDefaultSelectionRequest(new DefaultSelectionsRequest(selections, multi, channel),
                mode);
        r.processSeInserted();

        // test
        ReaderEvent event = r.processSeInserted();
        Assert.assertEquals(null, event);
    }
}
