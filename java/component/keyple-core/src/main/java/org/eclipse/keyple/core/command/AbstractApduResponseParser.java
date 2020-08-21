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

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.core.command.exception.KeypleSeCommandException;
import org.eclipse.keyple.core.command.exception.KeypleSeCommandUnknownStatusException;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/** Base class for parsing APDU */
public abstract class AbstractApduResponseParser {

  protected static final Map<Integer, StatusProperties> STATUS_TABLE;

  static {
    HashMap<Integer, StatusProperties> m = new HashMap<Integer, StatusProperties>();
    m.put(0x9000, new StatusProperties("Success"));
    STATUS_TABLE = m;
  }

  /** the byte array APDU response. */
  protected final ApduResponse response;

  /**
   * Parsers are usually created by their associated builder. The CalypsoSam field maintains a link
   * between the builder and the parser in order to allow the parser to access the builder
   * parameters that were used to create the command (e.g. SFI, registration number, etc.).
   */
  protected final AbstractApduCommandBuilder builder;

  /**
   * the generic abstract constructor to build a parser of the APDU response.
   *
   * @param response response to parse
   * @param builder the reference of the builder that created the parser
   */
  public AbstractApduResponseParser(ApduResponse response, AbstractApduCommandBuilder builder) {
    this.response = response;
    this.builder = builder;
  }

  /** @return the internal status table */
  protected Map<Integer, StatusProperties> getStatusTable() {
    return STATUS_TABLE;
  }

  /**
   * Build a command exception.<br>
   * This method should be override in subclasses in order to create specific exceptions.
   *
   * @param exceptionClass the exception class
   * @param message the message
   * @param commandRef the command reference
   * @param statusCode the status code
   * @return a new instance not null
   */
  protected KeypleSeCommandException buildCommandException(
      Class<? extends KeypleSeCommandException> exceptionClass,
      String message,
      SeCommand commandRef,
      Integer statusCode) {
    return new KeypleSeCommandUnknownStatusException(message, commandRef, statusCode);
  }

  /** @return the APDU response */
  public final ApduResponse getApduResponse() {
    return response;
  }

  /** @return the associated builder reference */
  public AbstractApduCommandBuilder getBuilder() {
    return builder;
  }

  /** @return the properties associated to the response status code */
  private StatusProperties getStatusCodeProperties() {
    return getStatusTable().get(response.getStatusCode());
  }

  /**
   * @return true if the status is successful from the statusTable according to the current status
   *     code.
   */
  public boolean isSuccessful() {
    StatusProperties props = getStatusCodeProperties();
    return props != null && props.isSuccessful();
  }

  /**
   * This method check the status code.<br>
   * If status code is not referenced, then status is considered unsuccessful.
   *
   * @throws KeypleSeCommandException if status is not successful.
   */
  public void checkStatus() {

    StatusProperties props = getStatusCodeProperties();
    if (props != null && props.isSuccessful()) {
      return;
    }
    // Status code is not referenced, or not successful.

    // exception class
    Class<? extends KeypleSeCommandException> exceptionClass =
        props != null ? props.getExceptionClass() : null;

    // message
    String message = props != null ? props.getInformation() : "Unknown status";

    // command reference
    SeCommand commandRef = getCommandRef();

    // status code
    Integer statusCode = response.getStatusCode();

    // Throw the exception
    throw buildCommandException(exceptionClass, message, commandRef, statusCode);
  }

  /**
   * Gets the associated command reference.<br>
   * By default, the command reference is retrieved from the associated builder.
   *
   * @return a nullable command reference
   */
  protected SeCommand getCommandRef() {
    return builder != null ? builder.getCommandRef() : null;
  }

  /** @return the ASCII message from the statusTable for the current status code. */
  public final String getStatusInformation() {
    StatusProperties props = getStatusCodeProperties();
    return props != null ? props.getInformation() : null;
  }

  /** Status code properties */
  protected static class StatusProperties {

    /** The status information */
    private final String information;

    /** The successful indicator */
    private final boolean successful;

    /** The associated exception class in case of error status */
    private final Class<? extends KeypleSeCommandException> exceptionClass;

    /**
     * Create a successful status.
     *
     * @param information the status information
     */
    public StatusProperties(String information) {
      this.information = information;
      this.successful = true;
      this.exceptionClass = null;
    }

    /**
     * Create an error status.<br>
     * If {@code exceptionClass} is null, then a successful status is created.
     *
     * @param information the status information
     * @param exceptionClass the associated exception class
     */
    public StatusProperties(
        String information, Class<? extends KeypleSeCommandException> exceptionClass) {
      this.information = information;
      this.successful = exceptionClass == null;
      this.exceptionClass = exceptionClass;
    }

    /** @return the status information */
    public String getInformation() {
      return information;
    }

    /** @return the successful indicator */
    public boolean isSuccessful() {
      return successful;
    }

    /** @return the nullable exception class */
    public Class<? extends KeypleSeCommandException> getExceptionClass() {
      return exceptionClass;
    }
  }
}
