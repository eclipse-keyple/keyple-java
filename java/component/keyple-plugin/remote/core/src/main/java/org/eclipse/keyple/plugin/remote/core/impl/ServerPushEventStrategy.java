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
package org.eclipse.keyple.plugin.remote.core.impl;

/**
 * Server Push Event Strategy
 *
 * <p>This internal class indicates the strategy to adopt in a client-server communication to allow
 * the client to receive events from the server.
 *
 * @since 1.0
 */
public class ServerPushEventStrategy {

  private final Type type;
  private int duration;

  /**
   * Create a new instance with a initial duration set to 0.
   *
   * @param type The strategy type to set.
   */
  public ServerPushEventStrategy(Type type) {
    this.type = type;
    this.duration = 0;
  }

  /** The strategy type enum. */
  public enum Type {
    POLLING,
    LONG_POLLING
  }

  /**
   * (package-private)<br>
   * Gets the strategy type.
   *
   * @return a not null value.
   */
  public Type getType() {
    return type;
  }

  /**
   * Sets the duration associated to the strategy.<br>
   * This method must be called by the factory during the initialization process.
   *
   * @param durationInSeconds The duration in seconds (must be {@code >= 0})
   * @return the current instance
   * @since 1.0
   */
  public ServerPushEventStrategy setDuration(int durationInSeconds) {
    this.duration = durationInSeconds;
    return this;
  }

  /**
   * (package-private)<br>
   * Gets the duration (in seconds).
   *
   * @return the duration (in seconds).
   */
  int getDuration() {
    return duration;
  }
}
