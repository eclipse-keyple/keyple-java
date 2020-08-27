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

/** Helper POJO used to serialize and deserialize a {@link RuntimeException} into a String */
public class BodyError {

  private final String code;
  private final RuntimeException exception;

  public BodyError(RuntimeException exception) {
    this.exception = exception;
    this.code = exception.getClass().getName();
  }

  public String getCode() {
    return code;
  }

  public RuntimeException getException() {
    return exception;
  }
}
