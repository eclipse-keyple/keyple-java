/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import java.nio.ByteBuffer;
import org.junit.Test;
import org.keyple.seproxy.AbstractApduWrapper;
import org.keyple.seproxy.ApduResponse;

public class PoGetChallengeRespParsTest {

    // TODO: Do the same with all other classes
    @Test
    public void getPoChallenge() {
        final ApduResponse apdu = new ApduResponse("01 02 03 04 9000");

        PoGetChallengeRespPars resp = new PoGetChallengeRespPars(apdu);

        // Here we compare that the data fetched is only the part that is before the execution
        // status
        // Note: Until here, zero buffer allocation/copy has been made
        assertEquals(apdu.getDataBeforeStatus(), resp.getPoChallengeV2());
        assertEquals("01020304", AbstractApduWrapper.toHex(resp.getPoChallengeV2()));

        // Now, just to be clear: All of the zero allocation/copy logic means we're always using the
        // same array,
        // here is the proof:
        assertEquals(apdu.getBuffer().array(), resp.getPoChallengeV2().array());

        // Let's still do things the old way
        // Here we do a buffer allocation/copy (BAD)
        final byte[] payload = apdu.getBytesBeforeStatus(); // 01 02 03 04

        // We do an another one here (BAD)
        assertArrayEquals(payload, resp.getPoChallenge());

        // We compare the wrapped one we created with the ByteBuffer we fetched
        assertEquals(ByteBuffer.wrap(payload), resp.getPoChallengeV2());
    }
}
