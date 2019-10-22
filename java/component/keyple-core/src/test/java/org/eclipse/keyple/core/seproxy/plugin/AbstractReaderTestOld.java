/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.seproxy.plugin;


import static org.mockito.Mockito.doAnswer;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.exception.*;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test methods linked to observability
 *
 * TODO Review this test because the thread start control is no longer relevant.
 */
@Deprecated
public class AbstractReaderTestOld extends CoreBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(AbstractReaderTestOld.class);


    final String PLUGIN_NAME = "abstractPluginTest";
    final String READER_NAME = "AbstractReaderTest";

    final ObservableReader.ReaderObserver obs1 = getObserver();
    final ObservableReader.ReaderObserver obs2 = getObserver();

    MockThreadedObservableLocalReader spyReader;

    CountDownLatch addObserverCall;
    CountDownLatch removeObserverCall;

    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
        spyReader = Mockito.spy(getBlankAbstractReader(PLUGIN_NAME, READER_NAME));
        initSpyReader();
    }

    /*
     * TESTS
     */

    // @Test
    public void testAddObserver() {
        addObserverCall = new CountDownLatch(5);
        removeObserverCall = new CountDownLatch(5);
        spyReader.addObserver(obs1);
        Assert.assertEquals(1, spyReader.countObservers());
        Assert.assertEquals(4, addObserverCall.getCount());// should be called once
        Assert.assertEquals(5, removeObserverCall.getCount());// should not be called

    }

    // @Test
    public void testRemoveObserver() {
        addObserverCall = new CountDownLatch(5);
        removeObserverCall = new CountDownLatch(5);
        spyReader.addObserver(obs1);
        spyReader.removeObserver(obs1);
        Assert.assertEquals(0, spyReader.countObservers());
        Assert.assertEquals(4, addObserverCall.getCount());// should be called once
        Assert.assertEquals(4, removeObserverCall.getCount());// should be called once

    }

    // @Test
    public void testAddRemoveObserver() {
        addObserverCall = new CountDownLatch(5);
        removeObserverCall = new CountDownLatch(5);
        spyReader.addObserver(obs1);
        spyReader.addObserver(obs2);
        spyReader.removeObserver(obs2);
        Assert.assertEquals(1, spyReader.countObservers());
        Assert.assertEquals(3, addObserverCall.getCount());// should be called twice
        Assert.assertEquals(4, removeObserverCall.getCount());// should be once
    }



    /*
     * HELPERS
     */

    /**
     * Class extending {@link AbstractThreadedObservableLocalReader} to enable thread monitoring
     */
    abstract class MockThreadedObservableLocalReader extends AbstractThreadedObservableLocalReader
            implements SmartInsertionReader {
        /**
         * Constructor
         *
         * @param pluginName the name of the plugin that instantiated the reader
         * @param readerName the name of the reader
         */
        public MockThreadedObservableLocalReader(String pluginName, String readerName) {
            super(pluginName, readerName);
        }
    }

    MockThreadedObservableLocalReader getBlankAbstractReader(String pluginName, String readerName) {
        /* anonymous subclass of ThreadedTestReader */
        return new MockThreadedObservableLocalReader(pluginName, readerName) {

            @Override
            public boolean waitForCardPresent(long timeout) {
                return false;
            }

            @Override
            protected void closePhysicalChannel() throws KeypleChannelControlException {

            }

            @Override
            protected boolean isPhysicalChannelOpen() {
                return false;
            }

            @Override
            protected byte[] transmitApdu(byte[] apduIn) throws KeypleIOReaderException {
                return new byte[0];
            }

            @Override
            protected boolean protocolFlagMatches(SeProtocol protocolFlag)
                    throws KeypleReaderException {
                return false;
            }

            @Override
            protected boolean checkSePresence() throws NoStackTraceThrowable {
                return false;
            }

            @Override
            protected byte[] getATR() {
                return new byte[0];
            }

            @Override
            protected void openPhysicalChannel() throws KeypleChannelControlException {

            }

            @Override
            public TransmissionMode getTransmissionMode() {
                return null;
            }

            @Override
            public Map<String, String> getParameters() {
                return null;
            }

            @Override
            public void setParameter(String key, String value)
                    throws IllegalArgumentException, KeypleBaseException {

            }
        };
    }



    ObservableReader.ReaderObserver getObserver() {
        return new ObservableReader.ReaderObserver() {
            @Override
            public void update(ReaderEvent readerEvent) {}
        };
    }


    void initSpyReader() {

        // track when addObserver with obs1 is called
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                addObserverCall.countDown();
                invocation.callRealMethod();
                return null;
            }
        }).when(spyReader).addObserver(obs1);

        // track when when removeObserver with obs1 is called
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                removeObserverCall.countDown();
                invocation.callRealMethod();
                return null;
            }
        }).when(spyReader).removeObserver(obs1);

        // track when addObserver with obs2 is called
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                addObserverCall.countDown();
                invocation.callRealMethod();
                return null;
            }
        }).when(spyReader).addObserver(obs2);

        // track when when removeObserver with obs2 is called
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                removeObserverCall.countDown();
                invocation.callRealMethod();
                return null;
            }
        }).when(spyReader).removeObserver(obs2);

    }


}
