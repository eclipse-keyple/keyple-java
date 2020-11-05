/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.card.command;

import org.eclipse.keyple.core.card.message.ApduRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generic APDU command builder.
 *
 * <p>It provides the generic getters to retrieve:
 *
 * <ul>
 *   <li>the card command reference,
 *   <li>the name of the command,
 *   <li>the built APDURequest,
 *   <li>the corresponding AbstractApduResponseParser class.
 * </ul>
 *
 * @since 0.9
 */
public abstract class AbstractApduCommandBuilder {

  private static final Logger logger = LoggerFactory.getLogger(AbstractApduCommandBuilder.class);

  /**
   * The reference field {@link CardCommand} is used to find the type of command concerned when
   * manipulating a list of abstract builder objects. Unfortunately, the diversity of these objects
   * does not allow the use of simple generic methods.
   *
   * @since 0.9
   */
  protected final CardCommand commandRef;

  private String name;

  /**
   * The byte array APDU request.
   *
   * @since 0.9
   */
  protected ApduRequest request;

  /**
   * (protected)<br>
   * The generic abstract constructor to build an APDU request with a command reference and a byte
   * array.
   *
   * @param commandRef command reference (should not be null)
   * @param request request
   * @since 0.9
   */
  protected AbstractApduCommandBuilder(CardCommand commandRef, ApduRequest request) {
    this.commandRef = commandRef;
    this.request = request;
    // set APDU name for non null request
    if (logger.isDebugEnabled()) {
      this.name = commandRef.getName();
      if (request != null) {
        this.request.setName(this.name);
      }
    }
  }

  /**
   * Appends a string to the current name.
   *
   * <p>The subname completes the name of the current command. This method must therefore only be
   * called conditionally (log level &gt;= debug).
   *
   * @param subName the string to append
   * @since 0.9
   */
  public final void addSubName(String subName) {
    if (subName.length() != 0) {
      this.name = this.name + " - " + subName;
      if (request != null) {
        this.request.setName(this.name);
      }
    }
  }

  /**
   * Gets {@link CardCommand} the current command identification
   *
   * @return A non null reference
   * @since 0.9
   */
  public CardCommand getCommandRef() {
    return commandRef;
  }

  /**
   * Gets the name of this APDU command if it has been allowed by the log level (see constructor).
   *
   * @return A String (may be null).
   * @since 0.9
   */
  public final String getName() {
    return this.name;
  }

  /**
   * Gets {@link ApduRequest} the request
   *
   * @return A nullable reference
   * @since 0.9
   */
  public final ApduRequest getApduRequest() {
    return request;
  }
}
