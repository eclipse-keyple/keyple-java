package org.eclipse.keyple.plugin.android.cone2;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;

import fr.coppernic.sdk.ask.Reader;
import fr.coppernic.sdk.power.impl.cone.ConePeripheral;
import timber.log.Timber;

public class Cone2ContactReaderTestBase extends TestBase {
    private Context context;
    protected Cone2ContactReaderImpl reader;

    @Before
    public void before() throws InterruptedException {
        Timber.plant(new Timber.DebugTree());
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
        // Contact reader can now be instantiated
        reader = new Cone2ContactReaderImpl();
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
