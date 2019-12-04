package org.eclipse.keyple.plugin.android.cone2;

import android.util.Log;

import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.junit.Test;

import fr.coppernic.sdk.utils.core.CpcBytes;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * For this test, a SAM must be placed in slot 1, and no SAM in slot 2.
 */
public class Cone2ContactReaderTest extends Cone2ContactReaderTestBase {
    /**
     * Checks that checkSeReader returns true when a SAM is present in the reader.
     */
    @Test
    public void checkSeReaderTest() {
        // A SAM is present in slot 1, checkSePresence must return true
        reader.setParameter(reader.CONTACT_INTERFACE_ID, reader.CONTACT_INTERFACE_ID_SAM_1);
        assertThat(reader.checkSePresence(), is(true));
        // No SAM is present in slot 2, checkSePresence must return false
        reader.setParameter(reader.CONTACT_INTERFACE_ID, reader.CONTACT_INTERFACE_ID_SAM_2);
        assertThat(reader.checkSePresence(), is(false));
    }

    /**
     * Tests that an ATR is returned when a SAM is inserted in the reader
     */
    @Test
    public void getAtrTest() {
        // A SAM is present in slot 1, checkSePresence must return true
        reader.setParameter(reader.CONTACT_INTERFACE_ID, reader.CONTACT_INTERFACE_ID_SAM_1);
        assertThat(reader.checkSePresence(), is(true));
        assertNotNull(reader.getATR());
        assertThat(reader.getATR().length, greaterThan(0));
        // No SAM is present in slot 2, checkSePresence must return false
        reader.setParameter(reader.CONTACT_INTERFACE_ID, reader.CONTACT_INTERFACE_ID_SAM_2);
        assertThat(reader.checkSePresence(), is(false));
        assertNull(reader.getATR());
    }

    /**
     * Tests that transmission mode is contact for SAM reader
     */
    @Test
    public void getTransmissionModeTest() {
        assertThat(reader.getTransmissionMode(), is(TransmissionMode.CONTACTS));
    }

    @Test
    public void transmitApduTest() throws KeypleIOReaderException {
        // A SAM is present in slot 1, checkSePresence must return true
        reader.setParameter(reader.CONTACT_INTERFACE_ID, reader.CONTACT_INTERFACE_ID_SAM_1);
        assertThat(reader.checkSePresence(), is(true));
        byte[] answer = reader.transmitApdu(new byte[] {(byte) 0x94, (byte) 0x84, 0x00, 0x00, 0x08});

        assertThat(answer.length, is(10));
        Log.d("TEST", CpcBytes.byteArrayToString(answer));

        answer = reader.transmitApdu(new byte[] {(byte) 0x94, (byte) 0x84, 0x00, 0x00, 0x04});

        assertThat(answer.length, is(6));
        Log.d("TEST", CpcBytes.byteArrayToString(answer));
    }
}
