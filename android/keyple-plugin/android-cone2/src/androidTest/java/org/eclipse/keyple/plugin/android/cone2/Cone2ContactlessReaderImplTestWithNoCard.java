package org.eclipse.keyple.plugin.android.cone2;

import android.os.SystemClock;
import android.support.test.runner.AndroidJUnit4;

import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

@RunWith(AndroidJUnit4.class)
public class Cone2ContactlessReaderImplTestWithNoCard extends Cone2ContactlessReaderImplTestBase {


    @Test
    public void waitForCardPresentTest() {
        // Waits for the card to be detected
        Assert.assertThat(reader.waitForCardPresent(SEARCH_TIMEOUT), is(false));
    }

    @Test
    public void waitForCardPresentCheckTimeoutTest() {
        // Gets start time
        long start = SystemClock.uptimeMillis();
        // Waits for the card to be detected
        Assert.assertThat(reader.waitForCardPresent(SEARCH_TIMEOUT), is(false));
        // Gets end time
        long end = SystemClock.uptimeMillis();
        // Checks that timeout is around 2s
        int duration = (int)(end - start);
        assertThat(duration, greaterThan(SEARCH_TIMEOUT));
        assertThat(duration, lessThan(SEARCH_TIMEOUT + 500));
    }

    @Test
    public void getTransmissionModeTest() {
        // Checks that getTransmissionMode returns CONTACTLESS
        assertThat(reader.getTransmissionMode(), is(TransmissionMode.CONTACTLESS));
    }

    @Test
    public void checkSePresenceTest() {
        // Waits for the card to be detected
        Assert.assertThat(reader.waitForCardPresent(SEARCH_TIMEOUT), is(false));
        // Checks that checkSePresence returns false
        assertThat(reader.checkSePresence(), is(false));
    }

    @Test
    public void getAtrTest() {
        // Waits for the card to be detected
        Assert.assertThat(reader.waitForCardPresent(SEARCH_TIMEOUT), is(false));
        // Checks that ATR is null
        assertNull(reader.getATR());
    }
}
