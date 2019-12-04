package org.eclipse.keyple.plugin.android.cone2;

import android.support.test.runner.AndroidJUnit4;

import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.junit.Test;
import org.junit.runner.RunWith;

import fr.coppernic.sdk.utils.core.CpcBytes;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * These tests must be executed with a card present in front of the antenna of the contactless
 * reader
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class Cone2ContactlessReaderImplTestWithCard extends Cone2ContactlessReaderImplTestBase {

    @Test
    public void waitForCardPresentTest() {
        // Waits for the card to be detected
        assertThat(reader.waitForCardPresent(SEARCH_TIMEOUT), is(true));
    }

    @Test
    public void checkSePresenceTest() {
        // Waits for the card to be detected
        assertThat(reader.waitForCardPresent(SEARCH_TIMEOUT), is(true));
        // Checks that checkSePresent returns true
        assertThat(reader.checkSePresence(), is(true));
    }

    @Test
    public void getAtrTest() {
        // Polls for card
        waitForCardPresentTest();
        // Checks ATR of the card
        byte[] atr = reader.getATR();
        assertThat(atr, is(new byte[] {0x00, 0x00, 0x00, 0x00, 0x00, 0x26, 0x12, 0x11, 0x55, 0x00,
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00}));
    }

    @Test
    public void transmitApduTest() throws KeypleIOReaderException {
        // Polls for card
        waitForCardPresentTest();
        // Creates APDU byte array
        String apduString = "00A404000AA0000004040125090101";
        byte[] apdu = CpcBytes.parseHexStringToArray(apduString);
        // Sends APDU to card
        byte[] answer = reader.transmitApdu(apdu);
        // Checks answer
        String answerString = "6F2A8410A0000004040125090101000000000000A516BF0C13C70800000000261" +
                "2115553070628114210122B9000";
        byte[] expectedAnswer = CpcBytes.parseHexStringToArray(answerString);
        assertThat(answer, is(expectedAnswer));
    }
}
