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
package org.eclipse.keyple.core.util.json;

/**
 * POJO used to transports the content of a {@link RuntimeException} into a String
 *
 * @since 1.0
 */
public class BodyError {

  private final String code;
  private final RuntimeException exception;

  /**
   * Constructor.
   *
   * @param exception The runtime exception.
   * @since 1.0
   */
  public BodyError(RuntimeException exception) {
    this.exception = exception;
    this.code = exception.getClass().getName();
  }

  /**
   * Gets the exception class name.
   *
   * @return a not null value.
   * @since 1.0
   */
  public String getCode() {
    return code;
  }

  /**
   * Gets the associated runtime exception.
   *
   * @return a not null reference.
   * @since 1.0
   */
  public RuntimeException getException() {
    return exception;
  }
}
