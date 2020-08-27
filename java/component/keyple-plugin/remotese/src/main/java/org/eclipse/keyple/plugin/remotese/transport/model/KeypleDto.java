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
package org.eclipse.keyple.plugin.remotese.transport.model;

/**
 * Immutable Message used in the RPC protocol.
 *
 * <p>It contains the name of the method {@link
 * org.eclipse.keyple.plugin.remotese.rm.RemoteMethodName}, the parameters
 */
public class KeypleDto {

  /*
   * API call
   */

  // API method to be called (required)
  private final String action;

  // Params of the API (optional)
  private final String body;

  // Is a request or a response (required)
  private final Boolean isRequest;

  // Id of the request (optional)
  private final String id;

  // Error (optional)
  private final String error;

  /*
   * Metadata
   */

  // Requester Node Id, either slave or master (required)
  private final String requesterNodeId;

  // Target Node Id, either slave or master (required)
  private final String targetNodeId;

  // Master reader session (optional)
  private final String sessionId;

  // Slave reader name (required)
  private final String nativeReaderName;

  // Master reader name (optional)
  private final String virtualReaderName;

  /**
   * Constructor of a KeypleDto
   *
   * @param action : API method to be called (required)
   * @param body : Arguments of the API - json (optional)
   * @param isRequest : Is a request or a response (required)
   * @param sessionId : Session Id of current Virtual Reader Session Id (optional)
   * @param nativeReaderName : readerName of the native reader (required)
   * @param virtualReaderName : readerName of the virtual reader (optional)
   * @param requesterNodeId : node the request is sent from (required)
   * @param targetNodeId : node the request is sent to (required)
   * @param id : unique id of this request (optional)
   * @param error : error - java exception (optional)
   */
  public KeypleDto(
      String action,
      String body,
      Boolean isRequest,
      String sessionId,
      String nativeReaderName,
      String virtualReaderName,
      String requesterNodeId,
      String targetNodeId,
      String id,
      String error) {

    this.sessionId = sessionId;
    this.action = action;
    this.body = body;
    this.isRequest = isRequest;
    this.nativeReaderName = nativeReaderName;
    this.virtualReaderName = virtualReaderName;
    this.requesterNodeId = requesterNodeId;
    this.targetNodeId = targetNodeId;
    this.id = id;
    this.error = error;
  }

  /*
   * Getters and Setters
   */

  public Boolean isRequest() {
    return isRequest;
  }

  public String getAction() {
    return action;
  }

  public String getBody() {
    return body;
  }

  public String getSessionId() {
    return sessionId;
  }

  public String getRequesterNodeId() {
    return requesterNodeId;
  }

  public String getNativeReaderName() {
    return nativeReaderName;
  }

  public String getVirtualReaderName() {
    return virtualReaderName;
  }

  public String getTargetNodeId() {
    return targetNodeId;
  }

  public String getId() {
    return id;
  }

  public String getError() {
    return error;
  }

  @Override
  public String toString() {
    return String.format(
        "KeypleDto : %s - isRequest : %s - native : %s - virtual : %s - requesterNodeId : %s - targetNodeId : %s - sessionId : %s - body : %s",
        this.getAction(),
        this.isRequest(),
        this.getNativeReaderName(),
        this.getVirtualReaderName(),
        this.getRequesterNodeId(),
        this.getTargetNodeId(),
        this.getSessionId(),
        this.getBody());
  }
}
