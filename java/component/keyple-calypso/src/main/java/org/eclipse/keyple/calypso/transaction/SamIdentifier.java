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
package org.eclipse.keyple.calypso.transaction;

import static org.eclipse.keyple.calypso.command.sam.SamRevision.AUTO;

import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.command.sam.SamRevision;

/**
 * Holds the needed data to proceed a SAM selection.
 *
 * <p>SAM Revision (see {@link SamRevision})
 *
 * <p>Serial Number (may be a regular expression)
 *
 * <p>Group reference (key group reference)
 */
public class SamIdentifier {
  SamRevision samRevision;
  String serialNumber;
  String groupReference;

  /** Private constructor */
  private SamIdentifier(SamIdentifierBuilder builder) {
    this.samRevision = builder.samRevision;
    this.serialNumber = builder.serialNumber;
    this.groupReference = builder.groupReference;
  }

  /**
   * Builder for a {@link SamIdentifier}
   *
   * @since 0.9
   */
  public static class SamIdentifierBuilder {
    private SamRevision samRevision;
    private String serialNumber = ""; // default: any S/N matches
    private String groupReference = "";

    /**
     * Sets the targeted SAM revision
     *
     * @param samRevision the {@link SamRevision} of the targeted SAM
     * @return the builder instance
     */
    public SamIdentifierBuilder samRevision(SamRevision samRevision) {
      this.samRevision = samRevision;
      return this;
    }

    /**
     * Sets the targeted SAM serial number
     *
     * @param serialNumber the serial number of the targeted SAM as regex
     * @return the builder instance
     */
    public SamIdentifierBuilder serialNumber(String serialNumber) {
      this.serialNumber = serialNumber;
      return this;
    }

    /**
     * Sets the targeted SAM group reference
     *
     * @param groupReference the group reference of the targeted SAM as a string
     * @return the builder instance
     */
    public SamIdentifierBuilder groupReference(String groupReference) {
      this.groupReference = groupReference;
      return this;
    }

    /**
     * Build a new {@code SamIdentifier}.
     *
     * @return a new instance
     */
    public SamIdentifier build() {
      return new SamIdentifier(this);
    }
  }

  /**
   * Gets a new builder.
   *
   * @return a new builder instance
   */
  public static SamIdentifierBuilder builder() {
    return new SamIdentifierBuilder();
  }

  /** @return the SAM revision */
  public SamRevision getSamRevision() {
    return samRevision;
  }

  /** @return the SAM serial number */
  public String getSerialNumber() {
    return serialNumber;
  }

  /** @return the group reference */
  public String getGroupReference() {
    return groupReference;
  }

  /**
   * Compare two SamIdentifiers with the following rules:
   *
   * <ul>
   *   <li>when the provided {@link SamIdentifier} is null the result is true
   *   <li>when the provided {@link SamIdentifier} is not null
   *       <ul>
   *         <li>the AUTO revision matches any revision
   *         <li>if not null, the serial number is used as a regular expression to check the current
   *             serial number
   *         <li>if not null the group reference is compared as a string
   *       </ul>
   * </ul>
   *
   * @param samIdentifier the {@link SamIdentifier} object to be compared to the current object
   * @return true if the identifier provided matches the current identifier
   */
  public boolean matches(SamIdentifier samIdentifier) {
    if (samIdentifier == null) {
      return true;
    }
    if (samIdentifier.getSamRevision() != AUTO && samIdentifier.getSamRevision() != samRevision) {
      return false;
    }
    if (samIdentifier.getSerialNumber() != null && !samIdentifier.getSerialNumber().isEmpty()) {
      Pattern p = Pattern.compile(samIdentifier.getSerialNumber());
      if (!p.matcher(serialNumber).matches()) {
        return false;
      }
    }
    if (samIdentifier.getGroupReference() != null
        && !samIdentifier.getGroupReference().equals(groupReference)) {
      return false;
    }
    return true;
  }
}
