/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.calypso.transaction;

import java.io.Serializable;
import java.util.HashMap;
import java.util.NoSuchElementException;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.ByteArrayUtil;

/**
 * The class {@code DirectoryHeader} contains all metadata of a Calypso DF.
 *
 * @since 0.9
 */
public class DirectoryHeader implements Serializable {

  private final short lid;
  private final byte[] accessConditions;
  private final byte[] keyIndexes;
  private final byte dfStatus;
  private final HashMap<PoTransaction.SessionSetting.AccessLevel, Byte> kif;
  private final HashMap<PoTransaction.SessionSetting.AccessLevel, Byte> kvc;

  /** Private constructor */
  private DirectoryHeader(DirectoryHeaderBuilder builder) {
    this.lid = builder.lid;
    this.accessConditions = builder.accessConditions;
    this.keyIndexes = builder.keyIndexes;
    this.dfStatus = builder.dfStatus;
    this.kif = builder.kif;
    this.kvc = builder.kvc;
  }

  /**
   * (package-private)<br>
   * Builder pattern
   */
  static final class DirectoryHeaderBuilder {

    private short lid;
    private byte[] accessConditions;
    private byte[] keyIndexes;
    private byte dfStatus;
    private final HashMap<PoTransaction.SessionSetting.AccessLevel, Byte> kif =
        new HashMap<PoTransaction.SessionSetting.AccessLevel, Byte>();
    private final HashMap<PoTransaction.SessionSetting.AccessLevel, Byte> kvc =
        new HashMap<PoTransaction.SessionSetting.AccessLevel, Byte>();

    /** Private constructor */
    private DirectoryHeaderBuilder() {}

    /**
     * (package-private)<br>
     * Sets the LID.
     *
     * @param lid the LID
     * @return the builder instance
     */
    DirectoryHeaderBuilder lid(short lid) {
      this.lid = lid;
      return this;
    }

    /**
     * (package-private)<br>
     * Sets a reference to the provided access conditions byte array.
     *
     * @param accessConditions the access conditions (should be not null and 4 bytes length)
     * @return the builder instance
     */
    DirectoryHeaderBuilder accessConditions(byte[] accessConditions) {
      this.accessConditions = accessConditions;
      return this;
    }

    /**
     * (package-private)<br>
     * Sets a reference to the provided key indexes byte array.
     *
     * @param keyIndexes the key indexes (should be not null and 4 bytes length)
     * @return the builder instance
     */
    DirectoryHeaderBuilder keyIndexes(byte[] keyIndexes) {
      this.keyIndexes = keyIndexes;
      return this;
    }

    /**
     * (package-private)<br>
     * Sets the DF status.
     *
     * @param dfStatus the DF status (byte)
     * @return the builder instance
     */
    DirectoryHeaderBuilder dfStatus(byte dfStatus) {
      this.dfStatus = dfStatus;
      return this;
    }

    /**
     * (package-private)<br>
     * Add a KIF.
     *
     * @param level the KIF session access level (should be not null)
     * @param kif the KIF value
     * @return the builder instance
     */
    DirectoryHeaderBuilder kif(PoTransaction.SessionSetting.AccessLevel level, byte kif) {
      this.kif.put(level, kif);
      return this;
    }

    /**
     * (package-private)<br>
     * Add a KVC.
     *
     * @param level the KVC session access level (should be not null)
     * @param kvc the KVC value
     * @return the builder instance
     */
    DirectoryHeaderBuilder kvc(PoTransaction.SessionSetting.AccessLevel level, byte kvc) {
      this.kvc.put(level, kvc);
      return this;
    }

    /**
     * (package-private)<br>
     * Build a new {@code DirectoryHeader}.
     *
     * @return a new instance
     */
    DirectoryHeader build() {
      return new DirectoryHeader(this);
    }
  }

  /**
   * Gets the associated LID.
   *
   * @return the LID
   * @since 0.9
   */
  public short getLid() {
    return lid;
  }

  /**
   * Gets a reference to access conditions.
   *
   * @return a not empty byte array
   * @since 0.9
   */
  public byte[] getAccessConditions() {
    return accessConditions;
  }

  /**
   * Gets a reference to keys indexes.
   *
   * @return a not empty byte array
   * @since 0.9
   */
  public byte[] getKeyIndexes() {
    return keyIndexes;
  }

  /**
   * Gets the DF status.
   *
   * @return the DF status byte
   * @since 0.9
   */
  public byte getDfStatus() {
    return dfStatus;
  }

  /**
   * Gets the KIF associated to the provided session access level.
   *
   * @param level the session access level (should be not null)
   * @return a not null value
   * @throws IllegalArgumentException if level is null.
   * @throws NoSuchElementException if KIF is not found.
   * @since 0.9
   */
  public byte getKif(PoTransaction.SessionSetting.AccessLevel level) {

    Assert.getInstance().notNull(level, "level");

    Byte result = kif.get(level);
    if (result == null) {
      throw new NoSuchElementException("KIF not found for session access level [" + level + "].");
    }
    return result;
  }

  /**
   * Gets the KVC associated to the provided session access level.
   *
   * @param level the session access level (should be not null)
   * @return a not null value
   * @throws IllegalArgumentException if level is null.
   * @throws NoSuchElementException if KVC is not found.
   * @since 0.9
   */
  public byte getKvc(PoTransaction.SessionSetting.AccessLevel level) {

    Assert.getInstance().notNull(level, "level");

    Byte result = kvc.get(level);
    if (result == null) {
      throw new NoSuchElementException("KVC not found for session access level [" + level + "].");
    }
    return result;
  }

  /**
   * (package-private)<br>
   * Gets a new builder.
   *
   * @return a new builder instance
   */
  static DirectoryHeaderBuilder builder() {
    return new DirectoryHeaderBuilder();
  }

  /**
   * Comparison is based on field "lid".
   *
   * @param o the object to compare
   * @return the comparison evaluation
   * @since 0.9
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DirectoryHeader that = (DirectoryHeader) o;

    return lid == that.lid;
  }

  /**
   * Comparison is based on field "lid".
   *
   * @return the hashcode
   * @since 0.9
   */
  @Override
  public int hashCode() {
    return lid;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("DirectoryHeader{");
    sb.append("lid=0x").append(Integer.toHexString(lid & 0xFFFF));
    sb.append(", accessConditions=").append("0x").append(ByteArrayUtil.toHex(accessConditions));
    sb.append(", keyIndexes=").append("0x").append(ByteArrayUtil.toHex(keyIndexes));
    sb.append(", dfStatus=0x").append(dfStatus);
    sb.append(", kif=").append(kif);
    sb.append(", kvc=").append(kvc);
    sb.append('}');
    return sb.toString();
  }
}
