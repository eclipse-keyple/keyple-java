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
 * This class represent a TAG as defined by the Basic Encoding Rules for ASN.1 <br>
 * This implementation limits the tag size to 2.<br>
 * (ITU-T X.690 / ISO 8825)
 */
public class Tag {
    private final int tagNumber;
    private final byte tagClass;
    private final TagType tagType;
    private final int tagSize;

    /* the tag class */
    public static final byte UNIVERSAL = (byte) 0x00;
    public static final byte APPLICATION = (byte) 0x01;
    public static final byte CONTEXT = (byte) 0x02;
    public static final byte PRIVATE = (byte) 0x03;

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
     * @param tagSize the tag size (1 or 2)
     */
    public Tag(int tagNumber, byte tagClass, TagType tagType, int tagSize) {
        if (tagType == null) {
            throw new IllegalArgumentException("TLV Tag: type is null.");
        }
        if (tagClass < 0 || tagClass > PRIVATE) {
            throw new IllegalArgumentException("TLV Tag: unknown tag class.");
        }
        this.tagNumber = tagNumber;
        this.tagClass = tagClass;
        this.tagType = tagType;
        this.tagSize = tagSize;
    }

    /**
     * Create a tag from a binary stream.
     * <p>
     * 
     * @param binary the byte array containing the TLV data
     * @param offset the start offset in the byte array
     * @throws IndexOutOfBoundsException if the offset is too large
     *
     */
    public Tag(byte[] binary, int offset) {
        /* the 2 first bits (b7b6) of the first byte defines the class */
        tagClass = (byte) ((binary[offset] & 0xC0) >>> 6);

        /* the type bit is the third bit (b5) */
        if ((binary[offset] & (byte) 0x20) == (byte) 0x20) {
            tagType = TagType.CONSTRUCTED;
        } else {
            tagType = TagType.PRIMITIVE;
        }

        /* */
        int number = binary[offset] & (byte) 0x1F;
        if (number == (byte) 0x1F) {
            /* two-byte tag */
            number = binary[offset + 1];
            tagSize = 2;
        } else {
            /* one-byte tag */
            tagSize = 1;
        }
        tagNumber = number;
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

    public int getTagSize() {
        return tagSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Tag tag = (Tag) o;

        if (tagNumber != tag.tagNumber)
            return false;
        if (tagClass != tag.tagClass)
            return false;
        if (tagSize != tag.tagSize)
            return false;
        return tagType == tag.tagType;
    }

    @Override
    public int hashCode() {
        int result = tagNumber;
        result = 31 * result + (int) tagClass;
        result = 31 * result + (tagType != null ? tagType.hashCode() : 0);
        result = 31 * result + tagSize;
        return result;
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
        return String.format("TAG: size=%d Class=%s, Type=%s, Number=%X", tagSize, tagClassString,
                tagType, tagNumber);
    }
}
