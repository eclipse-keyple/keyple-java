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

/**
 * This POJO contains the description of a Calypso EF.
 *
 * @since 0.9
 */
public class ElementaryFile implements Serializable {

  private final byte sfi;
  private FileHeader header;
  private final FileData data;

  /**
   * (package-private)<br>
   * Constructor
   *
   * @param sfi the associated SFI
   * @since 0.9
   */
  ElementaryFile(byte sfi) {
    this.sfi = sfi;
    this.data = new FileData();
  }

  /**
   * (package-private)<br>
   * Constructor used to create a clone of the provided EF.
   *
   * @param source the EF to be cloned
   */
  ElementaryFile(ElementaryFile source) {
    this.sfi = source.getSfi();
    if (source.getHeader() != null) {
      this.header = new FileHeader(source.getHeader());
    }
    this.data = new FileData(source.getData());
  }

  /**
   * Gets the associated SFI.
   *
   * @return the SFI
   * @since 0.9
   */
  public byte getSfi() {
    return sfi;
  }

  /**
   * Gets the file header.
   *
   * @return a header reference or null if header is not yet set.
   * @since 0.9
   */
  public FileHeader getHeader() {
    return header;
  }

  /**
   * (package-private)<br>
   * Sets the file header.
   *
   * @param header the file header (should be not null)
   * @return the current instance.
   */
  ElementaryFile setHeader(FileHeader header) {
    this.header = header;
    return this;
  }

  /**
   * Gets the file data.
   *
   * @return a not null data reference.
   * @since 0.9
   */
  public FileData getData() {
    return data;
  }

  /**
   * Comparison is based on field "sfi".
   *
   * @param o the object to compare
   * @return the comparison evaluation
   * @since 0.9
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ElementaryFile that = (ElementaryFile) o;

    return sfi == that.sfi;
  }

  /**
   * Comparison is based on field "sfi".
   *
   * @return the hashcode
   * @since 0.9
   */
  @Override
  public int hashCode() {
    return sfi;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ElementaryFile{");
    sb.append("sfi=").append(sfi);
    sb.append(", header=").append(header);
    sb.append(", data=").append(data);
    sb.append('}');
    return sb.toString();
  }
}
