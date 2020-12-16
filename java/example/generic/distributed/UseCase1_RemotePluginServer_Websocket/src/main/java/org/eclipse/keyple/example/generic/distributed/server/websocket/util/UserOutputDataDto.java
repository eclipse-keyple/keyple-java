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
package org.eclipse.keyple.example.generic.distributed.server.websocket.util;

/**
 * Example of POJO which contains the <b>user output data</b> associated to the
 * <b>RemotePluginServer</b> API.
 */
public class UserOutputDataDto {

  private Boolean isSuccessful;
  private String userId;

  public UserOutputDataDto setSuccessful(Boolean successful) {
    isSuccessful = successful;
    return this;
  }

  public Boolean isSuccessful() {
    return isSuccessful;
  }

  public String getUserId() {
    return userId;
  }

  public UserOutputDataDto setUserId(String userId) {
    this.userId = userId;
    return this;
  }
}
