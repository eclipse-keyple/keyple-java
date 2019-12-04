package org.eclipse.keyple.plugin.android.cone2;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.coppernic.sdk.ask.Reader;
import fr.coppernic.sdk.power.impl.cone.ConePeripheral;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class Cone2AskReaderTest extends TestBase {
    private Context context;
    private Reader reader;

    @Before
    public void before() {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Powers on contactless reader
        ConePeripheral.RFID_ASK_UCM108_GPIO.getDescriptor().power(context, true)
                .blockingGet();
    }

    @After
    public void after() {
        // Powers off contactless reader
        ConePeripheral.RFID_ASK_UCM108_GPIO.getDescriptor().power(context, false)
                .blockingGet();

        Cone2AskReader.clearInstance();
    }

    /**
     * Tests the parameterized getInstance.
     */
    @Test
    public void getInstanceTest() throws InterruptedException {
        Cone2AskReader.getInstance(context, new Cone2AskReader.ReaderListener() {
            @Override
            public void onInstanceAvailable(Reader reader) {
                Cone2AskReaderTest.this.reader = reader;
                unblock();
            }

            @Override
            public void onError(int error) {
                unblock();
                fail();
            }
        });

        block();

        // When getInstance succeeds, the reader must be opened.
        assertThat(reader.isOpened(), is(true));
    }

    /**
     * This test checks the behaviour of getInstance() when getInstance(Context, Listener) has not
     * been called previously.
     */
    @Test
    public void getInstanceWithNoParametersNotInitializedTest() {
        // Direct call to getInstance
        reader = Cone2AskReader.getInstance();
        // reader must be null
        assertNull(reader);
    }

    /**
     * This test checks the behaviour of getInstance() when getInstance(Context, Listener) has been
     * called previously.
     */
    @Test
    public void getInstanceWithNoParametersInitializedTest() throws InterruptedException {
        getInstanceTest();
        // Direct call to getInstance
        reader = Cone2AskReader.getInstance();
        // When getInstance succeeds, the reader must be opened.
        assertThat(reader.isOpened(), is(true));
    }
}
