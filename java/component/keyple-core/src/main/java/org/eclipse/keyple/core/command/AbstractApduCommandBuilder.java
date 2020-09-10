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
package org.eclipse.keyple.core.command;

import org.eclipse.keyple.core.seproxy.message.ApduRequest;

/**
 * Generic APDU command builder.
 *
 * <p>It provides the generic getters to retrieve:
 *
 * <ul>
 *   <li>the SE command reference,
 *   <li>the name of the command,
 *   <li>the built APDURequest,
 *   <li>the corresponding AbstractApduResponseParser class.
 * </ul>
 */
public abstract class AbstractApduCommandBuilder {

  /**
   * The reference field is used to find the type of command concerned when manipulating a list of
   * abstract builder objects. Unfortunately, the diversity of these objects does not allow the use
   * of simple generic methods.
   */
  protected final SeCommand commandRef;

  /** The command name (will appear in logs) */
  private String name;

  /** the byte array APDU request. */
  protected ApduRequest request;

  /**
   * the generic abstract constructor to build an APDU request with a command reference and a byte
   * array.
   *
   * @param commandRef command reference
   * @param request request
   */
  public AbstractApduCommandBuilder(SeCommand commandRef, ApduRequest request) {
    this.commandRef = commandRef;
    this.name = commandRef.getName();
    this.request = request;
    // set APDU name for non null request
    if (request != null) {
      this.request.setName(commandRef.getName());
    }
  }

  /**
   * Append a string to the current name
   *
   * @param subName the string to append
   */
  public final void addSubName(String subName) {
    if (subName.length() != 0) {
      this.name = this.name + " - " + subName;
      if (request != null) {
        this.request.setName(this.name);
      }
    }
  }

  /** @return the current command identification */
  public SeCommand getCommandRef() {
    return commandRef;
  }

  /** @return the name of the APDU command from the CalypsoCommands information. */
  public final String getName() {
    return this.name;
  }

  /** @return the request */
  public final ApduRequest getApduRequest() {
    return request;
  }
}
