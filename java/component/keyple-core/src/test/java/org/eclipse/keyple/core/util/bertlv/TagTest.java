/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.util.bertlv;

import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Test;

public class TagTest {

  @Test
  public void getTagNumber() {
    Tag tag;
    tag = new Tag(0x55, Tag.APPLICATION, Tag.TagType.PRIMITIVE, 2);
    Assert.assertEquals(0x55, tag.getTagNumber());
    tag = new Tag(ByteArrayUtil.fromHex("BF550100"), 0);
    Assert.assertEquals(0x55, tag.getTagNumber());
  }

  @Test
  public void getTagClass() {
    Tag tag;
    tag = new Tag(0x55, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE, 2);
    // TODO change these incomprehensible comparisons!
    Assert.assertEquals(Tag.UNIVERSAL, tag.getTagClass(), 2);
    tag = new Tag(0x55, Tag.APPLICATION, Tag.TagType.PRIMITIVE, 2);
    Assert.assertEquals(Tag.APPLICATION, tag.getTagClass(), 2);
    tag = new Tag(0x55, Tag.CONTEXT, Tag.TagType.PRIMITIVE, 2);
    Assert.assertEquals(Tag.CONTEXT, tag.getTagClass(), 2);
    tag = new Tag(0x55, Tag.PRIVATE, Tag.TagType.PRIMITIVE, 2);
    Assert.assertEquals(Tag.PRIVATE, tag.getTagClass(), 2);

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
    tag = new Tag(0x55, (byte) 10, Tag.TagType.PRIMITIVE, 2);
  }

  @Test(expected = IllegalArgumentException.class)
  public void getTagClass_Bad2() {
    Tag tag;
    tag = new Tag(0x55, (byte) -10, Tag.TagType.PRIMITIVE, 2);
  }

  @Test
  public void getTagType() {
    Tag tag;
    tag = new Tag(0x55, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE, 2);
    Assert.assertEquals(Tag.TagType.PRIMITIVE, tag.getTagType());
    tag = new Tag(0x55, Tag.APPLICATION, Tag.TagType.CONSTRUCTED, 2);
    Assert.assertEquals(Tag.TagType.CONSTRUCTED, tag.getTagType());

    tag = new Tag(ByteArrayUtil.fromHex("1F550100"), 0);
    Assert.assertEquals(Tag.TagType.PRIMITIVE, tag.getTagType());
    tag = new Tag(ByteArrayUtil.fromHex("3F550100"), 0);
    Assert.assertEquals(Tag.TagType.CONSTRUCTED, tag.getTagType());
  }

  @Test
  public void getSize() {
    Tag tag;
    tag = new Tag(0x15, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE, 1);
    Assert.assertEquals(1, tag.getTagSize());
    tag = new Tag(0x1E, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE, 2);
    Assert.assertEquals(2, tag.getTagSize());
  }

  @Test
  public void equals1() {
    Tag tag1 = new Tag(0x55, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE, 2);
    Tag tag2 = new Tag(ByteArrayUtil.fromHex("1F550100"), 0);
    Tag tag3 = new Tag(ByteArrayUtil.fromHex("6F550100"), 0);
    System.out.println("Tag1: " + tag1.toString());
    System.out.println("Tag2: " + tag2.toString());
    System.out.println("Tag3: " + tag3.toString());
    Assert.assertTrue(tag1.equals(tag2));
    Assert.assertFalse(tag1.equals(tag3));
    Assert.assertFalse(tag2.equals(tag3));

    Tag tag4 = new Tag(0x05, Tag.UNIVERSAL, Tag.TagType.CONSTRUCTED, 1);
    Tag tag5 = new Tag(ByteArrayUtil.fromHex("250100"), 0);
    System.out.println("Tag4: " + tag4.toString());
    System.out.println("Tag5: " + tag5.toString());
    Assert.assertTrue(tag4.equals(tag5));

    Tag tag6 = new Tag(0x07, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE, 1);
    Tag tag7 = new Tag(ByteArrayUtil.fromHex("070100"), 0);
    System.out.println("Tag6: " + tag6.toString());
    System.out.println("Tag7: " + tag7.toString());
    Assert.assertTrue(tag6.equals(tag7));

    Tag tag8 = new Tag(0x12, Tag.UNIVERSAL, Tag.TagType.PRIMITIVE, 2);
    Tag tag9 = new Tag(ByteArrayUtil.fromHex("1F120100"), 0);
    System.out.println("Tag8: " + tag8.toString());
    System.out.println("Tag9: " + tag9.toString());
    Assert.assertTrue(tag8.equals(tag9));
  }
}
