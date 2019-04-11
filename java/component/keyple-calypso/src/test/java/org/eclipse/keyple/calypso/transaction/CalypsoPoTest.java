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
package org.eclipse.keyple.calypso.transaction;


import org.eclipse.keyple.calypso.command.po.PoRevision;
import org.eclipse.keyple.seproxy.ChannelState;
import org.eclipse.keyple.seproxy.SeSelector;
import org.eclipse.keyple.seproxy.message.AnswerToReset;
import org.eclipse.keyple.seproxy.message.ApduResponse;
import org.eclipse.keyple.seproxy.message.SeResponse;
import org.eclipse.keyple.seproxy.message.SelectionStatus;
import org.eclipse.keyple.seproxy.protocol.ContactlessProtocols;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CalypsoPoTest {
    /* Building FCI data with the application byte as a variant and initialize PO */
    public static CalypsoPo getPoApplicationByte(byte applicationByte) {
        AnswerToReset atr = new AnswerToReset(ByteArrayUtils.fromHex("3B8880010000000000718100F9"));
        ApduResponse fciData =
                new ApduResponse(ByteArrayUtils.fromHex(String.format("6F 22 84 08 315449432E494341"
                        + "A5 16 BF0C 13 C7 08 0000000011223344" + "53 07 060A %02X 02200311 9000",
                        applicationByte)), null);
        SeResponse selectionData =
                new SeResponse(true, false, new SelectionStatus(atr, fciData, true), null);
        PoSelectionRequest poSelectionRequest =
                new PoSelectionRequest(
                        new SeSelector(
                                new SeSelector.AidSelector(
                                        ByteArrayUtils.fromHex("315449432E494341"), null),
                                null, null),
                        ChannelState.KEEP_OPEN, ContactlessProtocols.PROTOCOL_ISO14443_4);
        CalypsoPo calypsoPo = new CalypsoPo(selectionData, null);
        return calypsoPo;
    }

    @Test
    public void computePoRevision() {
        Assert.assertEquals(getPoApplicationByte((byte) 0x01).getRevision(), PoRevision.REV2_4);

        Assert.assertEquals(getPoApplicationByte((byte) 0x04).getRevision(), PoRevision.REV2_4);

        Assert.assertEquals(getPoApplicationByte((byte) 0x06).getRevision(), PoRevision.REV2_4);

        Assert.assertEquals(getPoApplicationByte((byte) 0x1F).getRevision(), PoRevision.REV2_4);

        Assert.assertEquals(getPoApplicationByte((byte) 0x20).getRevision(), PoRevision.REV3_1);

        Assert.assertEquals(getPoApplicationByte((byte) 0x27).getRevision(), PoRevision.REV3_1);

        Assert.assertEquals(getPoApplicationByte((byte) 0x28).getRevision(), PoRevision.REV3_2);

        Assert.assertEquals(getPoApplicationByte((byte) 0x2F).getRevision(), PoRevision.REV3_2);
    }
}
