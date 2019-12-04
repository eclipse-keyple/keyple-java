package org.eclipse.keyple.plugin.android.cone2;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import fr.coppernic.sdk.ask.Reader;
import fr.coppernic.sdk.power.impl.cone.ConePeripheral;

@RunWith(AndroidJUnit4.class)
public class Cone2ContactlessReaderImplTestBase extends TestBase {
    protected static final int SEARCH_TIMEOUT = 3000;

    private Context context;
    protected Cone2ContactlessReaderImpl reader;

    @Before
    public void before() throws InterruptedException {
        // Context of the app under test.
        context = InstrumentationRegistry.getTargetContext();
        // Powers on contactless reader
        ConePeripheral.RFID_ASK_UCM108_GPIO.getDescriptor().power(context, true)
                .blockingGet();
        // Initializes the Cone2AskReader unique instance
        Cone2AskReader.getInstance(context, new Cone2AskReader.ReaderListener() {
            @Override
            public void onInstanceAvailable(Reader reader) {
                // Reader has been initialized
                unblock();
            }

            @Override
            public void onError(int error) {

            }
        });
        //Waits for ASK reader object be instantiated and initialized
        block();
        // Reader can now be instantiated
        reader = new Cone2ContactlessReaderImpl();
    }

    @After
    public void after() {
        // Switches RFID reader off
        ConePeripheral.RFID_ASK_UCM108_GPIO.getDescriptor().power(context, false)
                .blockingGet();
        // Clears instance to be able to initialize reader again
        Cone2AskReader.clearInstance();
    }
}
