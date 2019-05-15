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

/**
 * This class represent a TAG as defined by the Basic Encoding Rules for ASN.1
 * <p>
 * (ITU-T X.690 / ISO 8825)
 */
public class Tag {
    private final int tagNumber;
    private final byte tagClass;
    private final TagType tagType;
    private final int size;

    /* the tag class */
    public final static byte UNIVERSAL = (byte) 0x00;
    public final static byte APPLICATION = (byte) 0x01;
    public final static byte CONTEXT = (byte) 0x02;
    public final static byte PRIVATE = (byte) 0x03;

    /* the tag type */
    public enum TagType {
        PRIMITIVE, CONSTRUCTED
    }

    /**
     * Creates a tag from its attributes.
     * <p>
     * 
     * @param tagNumber the tag value.
     * @param tagClass the tag class.
     * @param tagType constructed or primitive
     */
    public Tag(int tagNumber, byte tagClass, TagType tagType) {
        if (tagType == null) {
            throw new IllegalArgumentException("TLV Tag: type is null.");
        }
        if (tagClass < 0 || tagClass > PRIVATE) {
            throw new IllegalArgumentException("TLV Tag: unknown tag class.");
        }
        this.tagNumber = tagNumber;
        this.tagClass = tagClass;
        this.tagType = tagType;
        if (tagNumber < 0x1F) {
            size = 1;
        } else if (tagNumber < 0x80) {
            size = 2;
        } else if (tagNumber < 0x4000) {
            size = 3;
        } else if (tagNumber < 0x200000) {
            size = 4;
        } else {
            size = 5;
        }
    }

    /**
     * Create a tag from a binary stream.
     * <p>
     * 
     * @param binary the byte array containing the TLV data
     * @param offset the start offset in the byte array
     * @throws IndexOutOfBoundsException if the offset is too large
     */
    public Tag(byte[] binary, int offset) throws IndexOutOfBoundsException {
        /* the 2 first bits (b7b6) of the first byte defines the class */
        tagClass = (byte) ((binary[offset] & 0xC0) >>> 6);

        /* the type bit is the third bit (b5) */
        if ((binary[offset] & (byte) 0x20) == (byte) 0x20) {
            tagType = TagType.CONSTRUCTED;
        } else {
            tagType = TagType.PRIMITIVE;
        }

        /* the tag number is defined in the following bits (b4-b0) and possibly following octets */
        int index = offset;
        int number = 0;
        if ((binary[index] & (byte) 0x1F) == (byte) 0x1F) {
            /* all bits of tag number are set: multi-octet tag */
            do {
                index++;
                number <<= 7;
                number += binary[index] & 0x7F;
                /* loop while the "more bit" (b7) is set */
            } while ((binary[index] & 0x80) == 0x80);
        } else {
            /* single octet tag */
            number = binary[index] & (byte) 0x1F;
        }
        tagNumber = number;
        size = index + 1 - offset;
    }

    public int getTagNumber() {
        return tagNumber;
    }

    public byte getTagClass() {
        return tagClass;
    }

    public TagType getTagType() {
        return tagType;
    }

    public int getSize() {
        return size;
    }

    public boolean equals(Tag tag) {
        return ((this.tagNumber == tag.tagNumber) && (this.tagClass == tag.tagClass)
                && (this.tagType == tag.tagType));
    }

    @Override
    public String toString() {
        String tagClassString;
        switch (tagClass) {
            case Tag.UNIVERSAL:
                tagClassString = "UNIVERSAL";
                break;
            case Tag.APPLICATION:
                tagClassString = "APPLICATION";
                break;
            case Tag.CONTEXT:
                tagClassString = "CONTEXT";
                break;
            case Tag.PRIVATE:
                tagClassString = "PRIVATE";
                break;
            default:
                tagClassString = "UNKWOWN";
                break;
        }
        return String.format("TAG: size=%d Class=%s, Type=%s, Number=%X", size, tagClassString,
                tagType, tagNumber);
    }
}
