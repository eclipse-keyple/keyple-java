package org.eclipse.keyple.plugin.android.cone2;

import android.os.SystemClock;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.event.PluginEvent;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.Assert.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class Cone2PluginImplTest extends TestBase {
    private Cone2PluginImpl plugin = new Cone2PluginImpl();
    private ObservablePlugin.PluginObserver observer;
    private Object syncObject1 = new Object();
    private Object syncObject2 = new Object();

    /**
     * This test checks that the reader stops waiting for card when the plugin powers it off.
     *
     * Important: it is mandatory that no card is present during this test.
     *
     * 1 - A PluginObserver is created and added to the observers of the plugin
     * 2 - Reader is powered on
     * 3 - When the reader is connected, we start the card detection
     * 4 - When card detection is started, isWaitingForCard() must return true
     * 5 - We power off the reader
     * 6 - When the reader is powered off, we check that isWaitingForCard returns false
     */
    @Test
    public void waitForCardStoppedAfterPowerOff() {
        // 1 - A PluginObserver is created and added to the observers of the plugin
        observer = new ObservablePlugin.PluginObserver() {
            @Override
            public void update(PluginEvent event) {
                switch(event.getEventType()) {

                    case READER_CONNECTED:
                        checkReaderIsWaitingForCard();
                        break;
                    case READER_DISCONNECTED:
                        checkReaderIsNotWaitingForCardAnymore();
                        unblock(syncObject1);
                        break;
                }
            }
        };
        plugin.addObserver(observer);
        // 2 - Reader is powered on
        plugin.power(InstrumentationRegistry.getTargetContext(), true);

        try {
            block(syncObject1);
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail();
        }
    }

    private AtomicBoolean waitForCardPresentRunning = new AtomicBoolean(true);

    private void checkReaderIsWaitingForCard() {
        try {
            // 3 - When the reader is connected, we start the card detection
            final Cone2ContactlessReaderImpl reader =
                    (Cone2ContactlessReaderImpl)(plugin.
                            fetchNativeReader(Cone2ContactlessReader.READER_NAME));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    reader.waitForCardPresent();
                    waitForCardPresentRunning.set(false);
                    unblock(syncObject2);
                }
            }).start();
            SystemClock.sleep(500);
            // 4 - When card detection is started, isWaitingForCard() must return
            // true
            assertTrue(reader.isWaitingForCard());
            // 5 - We power off the reader
            plugin.power(InstrumentationRegistry.getTargetContext(), false);
        } catch (KeypleReaderException e) {
            e.printStackTrace();
            fail();
        }
    }

    private void checkReaderIsNotWaitingForCardAnymore() {
        try {
            // 6 - When the reader is powered off, we check that isWaitingForCard
            // returns false
            Cone2ContactlessReaderImpl reader =
                    (Cone2ContactlessReaderImpl)(plugin.
                            fetchNativeReader(Cone2ContactlessReader.READER_NAME));
            assertFalse(reader.isWaitingForCard());
            try {
                block(syncObject2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertFalse(waitForCardPresentRunning.get());
            plugin.removeObserver(observer);
        } catch (KeypleReaderException e) {
            e.printStackTrace();
            plugin.removeObserver(observer);
            fail();
        }
    }
}
