/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.util.bertlv;

import static org.eclipse.keyple.core.util.bertlv.Tag.TagType.CONSTRUCTED;
import static org.eclipse.keyple.core.util.bertlv.Tag.TagType.PRIMITIVE;
import static org.junit.Assert.*;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;

public class TLVTest {

    @Test
    public void parse() {
        Tag tag1 = new Tag(0x04, Tag.CONTEXT, PRIMITIVE);
        Tag tag2 = new Tag(0x04, Tag.CONTEXT, CONSTRUCTED);
        TLV tlv = new TLV(ByteArrayUtil.fromHex("84050011223344"));
        // 1st parsing
        Assert.assertTrue(tlv.parse(tag1, 0));
        // 2nd same parsing
        Assert.assertTrue(tlv.parse(tag1, 0));
        // search another tag
        Assert.assertFalse(tlv.parse(tag2, 0));
    }

    @Test
    public void getValue() {
        Tag tag1 = new Tag(0x04, Tag.CONTEXT, PRIMITIVE);
        TLV tlv = new TLV(ByteArrayUtil.fromHex("84050011223344"));
        Assert.assertTrue(tlv.parse(tag1, 0));
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("0011223344"), tlv.getValue());

        // length octets variant
        tlv = new TLV(ByteArrayUtil.fromHex("8481050011223344"));
        Assert.assertTrue(tlv.parse(tag1, 0));
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("0011223344"), tlv.getValue());
    }

    @Test
    public void getPosition() {
        Tag tag1 = new Tag(0x04, Tag.CONTEXT, PRIMITIVE);
        // two TLV
        TLV tlv = new TLV(ByteArrayUtil.fromHex("8405001122334484055566778899"));
        Assert.assertTrue(tlv.parse(tag1, 0));
        // test position before getValue
        Assert.assertEquals(tlv.getPosition(), 2);
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("0011223344"), tlv.getValue());
        // test position after getValue
        Assert.assertEquals(tlv.getPosition(), 7);
        Assert.assertTrue(tlv.parse(tag1, tlv.getPosition()));
        // test position before getValue
        Assert.assertEquals(tlv.getPosition(), 9);
        Assert.assertArrayEquals(ByteArrayUtil.fromHex("5566778899"), tlv.getValue());
        // test position after getValue
        Assert.assertEquals(tlv.getPosition(), 14);
    }
}
