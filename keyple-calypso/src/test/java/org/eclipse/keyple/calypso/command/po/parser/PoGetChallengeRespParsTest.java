/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.calypso.command.po.parser;

import static org.junit.Assert.assertEquals;
import java.nio.ByteBuffer;
import org.eclipse.keyple.calypso.command.po.parser.session.PoGetChallengeRespPars;
import org.eclipse.keyple.seproxy.ApduResponse;
import org.eclipse.keyple.util.ByteBufferUtils;
import org.junit.Test;

public class PoGetChallengeRespParsTest {

    // TODO: Do the same with all other classes
    @Test
    public void getPoChallenge() {
        final ApduResponse apdu =
                new ApduResponse(ByteBufferUtils.fromHex("01 02 03 04 9000"), null);

        PoGetChallengeRespPars resp = new PoGetChallengeRespPars(apdu);

        // Here we compare that the data fetched is only the part that is before the execution
        // status
        // Note: Until here, zero buffer allocation/copy has been made
        assertEquals(apdu.getDataOut(), resp.getPoChallenge());
        assertEquals("01020304", ByteBufferUtils.toHex(resp.getPoChallenge()));

        // Now, just to be clear: All of the zero allocation/copy logic means we're always using the
        // same array,
        // here is the proof:
        assertEquals(apdu.getBytes().array(), resp.getPoChallenge().array());

        // Let's still do things the old way
        final ByteBuffer payload = apdu.getDataOut(); // 01 02 03 04

        assertEquals(payload, resp.getPoChallenge());
    }
}
