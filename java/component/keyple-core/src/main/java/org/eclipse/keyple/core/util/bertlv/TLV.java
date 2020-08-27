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

/**
 * This class helps to parse a byte array as a TLV structure
 *
 * <p>(ITU-T X.690 / ISO 8825)
 */
public class TLV {
  private Tag tag;
  private int length;
  private final byte[] binary;
  private int position;

  /**
   * Create a TLV object initialized with a byte array
   *
   * <p>
   *
   * @param binary the byte array containing the TLV structure
   */
  public TLV(byte[] binary) {
    tag = new Tag(0, (byte) 0, Tag.TagType.PRIMITIVE, 1); // This is a primitive TLV
    this.binary = binary;
    length = 0;
    position = 0;
  }

  /**
   * Parse the byte array to find the expected TLV.
   *
   * <p>The method returns true if the tag is found.
   *
   * <p>The analysis result is available with getValue and getPosition
   *
   * @param tag the tag to search in the byte array
   * @param offset the position to start in the byte array
   * @return true or false according to the presence of the provided tag
   */
  public boolean parse(Tag tag, int offset) {
    if (tag == null) {
      throw new IllegalArgumentException("TLV parsing: tag can't be null.");
    }
    try {
      this.tag = new Tag(binary, offset);
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("TLV parsing: index is too large.");
    }
    length = 0;
    if (tag.equals(this.tag)) {
      offset += this.tag.getTagSize();
      position += this.tag.getTagSize();
      if ((binary[offset] & (byte) 0x80) == (byte) 0x00) {
        /* short form: single octet length */
        length += binary[offset];
        position++;
      } else {
        /* long form: first octet (b6-b0)) gives the number of following length octets */
        int following = (binary[offset] & (byte) 0x7F);
        position++;
        while (following > 0) {
          offset++;
          position++;
          length += (binary[offset] & 0xFF);
          if (following > 1) {
            length <<= 8;
          }
          following--;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Return a byte array copied from the main array corresponding to value part of the last TLV
   * parsing.
   *
   * <p>This method modifies the global position in the main array. Thus, it must be called once
   * only.
   *
   * @return a byte array
   */
  public byte[] getValue() {
    byte[] value = new byte[length];
    System.arraycopy(binary, position, value, 0, length);
    position += length;
    return value;
  }

  /** @return the current position in the main array */
  public int getPosition() {
    return position;
  }

  @Override
  public String toString() {
    return String.format(
        "TAG: %s, LENGTH: %d, VALUE: %s", tag.toString(), length, ByteArrayUtil.toHex(getValue()));
  }
}
