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

import static org.junit.Assert.*;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;


public class TagTest {

    @Test
    public void getTagNumber() {
        Tag tag;
        tag = new Tag(0x55, Tag.APPLICATION, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(0x55, tag.getTagNumber());
        tag = new Tag(ByteArrayUtil.fromHex("BF550100"), 0);
        Assert.assertEquals(0x55, tag.getTagNumber());
    }

    @Test
    public void getTagClass() {
        Tag tag;
        tag = new Tag(0x55, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(Tag.UNIVERSAL, tag.getTagClass());
        tag = new Tag(0x55, Tag.APPLICATION, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(Tag.APPLICATION, tag.getTagClass());
        tag = new Tag(0x55, Tag.CONTEXT, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(Tag.CONTEXT, tag.getTagClass());
        tag = new Tag(0x55, Tag.PRIVATE, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(Tag.PRIVATE, tag.getTagClass());

        tag = new Tag(ByteArrayUtil.fromHex("1F550100"), 0);
        Assert.assertEquals(Tag.UNIVERSAL, tag.getTagClass());
        tag = new Tag(ByteArrayUtil.fromHex("5F550100"), 0);
        Assert.assertEquals(Tag.APPLICATION, tag.getTagClass());
        tag = new Tag(ByteArrayUtil.fromHex("BF550100"), 0);
        Assert.assertEquals(Tag.CONTEXT, tag.getTagClass());
        tag = new Tag(ByteArrayUtil.fromHex("DF550100"), 0);
        Assert.assertEquals(Tag.PRIVATE, tag.getTagClass());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTagClass_Bad1() {
        Tag tag;
        tag = new Tag(0x55, (byte) 10, Tag.TagType.PRIMITIVE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getTagClass_Bad2() {
        Tag tag;
        tag = new Tag(0x55, (byte) -10, Tag.TagType.PRIMITIVE);
    }

    @Test
    public void getTagType() {
        Tag tag;
        tag = new Tag(0x55, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(Tag.TagType.PRIMITIVE, tag.getTagType());
        tag = new Tag(0x55, Tag.APPLICATION, Tag.TagType.CONSTRUCTED);
        Assert.assertEquals(Tag.TagType.CONSTRUCTED, tag.getTagType());

        tag = new Tag(ByteArrayUtil.fromHex("1F550100"), 0);
        Assert.assertEquals(Tag.TagType.PRIMITIVE, tag.getTagType());
        tag = new Tag(ByteArrayUtil.fromHex("3F550100"), 0);
        Assert.assertEquals(Tag.TagType.CONSTRUCTED, tag.getTagType());
    }

    @Test
    public void getSize() {
        Tag tag;
        tag = new Tag(0x15, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(1, tag.getSize());
        tag = new Tag(0x1E, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(1, tag.getSize());
        tag = new Tag(0x1F, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(2, tag.getSize());
        tag = new Tag(0x7F, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(2, tag.getSize());
        tag = new Tag(0x80, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(3, tag.getSize());
        tag = new Tag(0x3FFF, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(3, tag.getSize());
        tag = new Tag(0x4000, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(4, tag.getSize());
        tag = new Tag(0x1FFFFF, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(4, tag.getSize());
        tag = new Tag(0x200000, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE);
        Assert.assertEquals(5, tag.getSize());
    }

    @Test
    public void equals1() {
        Tag tag1, tag2, tag3;
        tag1 = new Tag(0x55, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE);
        tag2 = new Tag(ByteArrayUtil.fromHex("1F550100"), 0);
        tag3 = new Tag(ByteArrayUtil.fromHex("6F550100"), 0);
        Assert.assertTrue(tag1.equals(tag2));
        Assert.assertFalse(tag1.equals(tag3));
        Assert.assertFalse(tag2.equals(tag3));
    }
}
