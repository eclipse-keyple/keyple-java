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
package org.eclipse.keyple.plugin.remote.nativ;

import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.util.Assert;

/**
 * This POJO class contains parameters of the method {@link
 * NativeClientService#executeRemoteService(RemoteServiceParameters, Class)} :
 *
 * <ul>
 *   <li><b>serviceId</b> : The ticketing service id. It will permit to indicate to the server which
 *       ticketing service to execute (Materialization, Validation, Control, ...). This field is
 *       free and is for the user's use only.
 *   <li><b>nativeReader</b> : The native reader to manage by the server.
 *   <li><b>userInputData</b> (optional) : An object with the user input data if you want to
 *       transmit data during the call to the remote ticketing service.
 *   <li><b>initialCardContent</b> (optional) : An initial Card content inside an {@link
 *       AbstractMatchingSe} to send to the server ticketing service. For Calypso ticketing
 *       application, this object will be a <b>CalypsoPo</b> or a <b>CalypsoSam</b>, depending on
 *       the context.
 * </ul>
 *
 * @since 1.0
 */
public class RemoteServiceParameters {

  private final String serviceId;
  private final SeReader nativeReader;
  private final Object userInputData;
  private final AbstractMatchingSe initialCardContent;

  private RemoteServiceParameters(Builder builder) {

    Assert.getInstance() //
        .notEmpty(builder.serviceId, "serviceId") //
        .notNull(builder.nativeReader, "nativeReader");

    serviceId = builder.serviceId;
    nativeReader = builder.nativeReader;
    userInputData = builder.userInputData;
    initialCardContent = builder.initialCardContent;
  }

  /**
   * Gets a new builder.
   *
   * @param serviceId The ticketing service id. It will permit to indicate to the server which
   *     ticketing service to execute (Materialization, Validation, Control,...). This field is free
   *     and is for the user's use only.
   * @param nativeReader The native reader to manage by the server.
   * @return a new builder instance.
   * @since 1.0
   */
  public static Builder builder(String serviceId, SeReader nativeReader) {
    return new Builder(serviceId, nativeReader);
  }

  /** The builder pattern */
  public static class Builder {

    private final String serviceId;
    private final SeReader nativeReader;
    private Object userInputData;
    private AbstractMatchingSe initialCardContent;

    private Builder(String serviceId, SeReader nativeReader) {
      this.serviceId = serviceId;
      this.nativeReader = nativeReader;
    }

    /**
     * Add user input data to send to the server ticketing service.
     *
     * @param userInputData The object containing the user input data.
     * @return the builder instance
     * @since 1.0
     */
    public Builder withUserInputData(Object userInputData) {
      this.userInputData = userInputData;
      return this;
    }

    /**
     * Add an initial Card content inside an AbstractMatchingSe to send to the server ticketing
     * service.<br>
     * For Calypso ticketing application, this object will be a <b>CalypsoPo</b> or a
     * <b>CalypsoSam</b>, depending on the context.
     *
     * @param initialCardContent The initial card content.
     * @return the builder instance
     * @since 1.0
     */
    public Builder withInitialSeContext(AbstractMatchingSe initialCardContent) {
      this.initialCardContent = initialCardContent;
      return this;
    }

    /**
     * Build a new RemoteServiceParameters.
     *
     * @return a new instance
     * @since 1.0
     */
    public RemoteServiceParameters build() {
      return new RemoteServiceParameters(this);
    }
  }

  /**
   * Gets the ticketing service id.
   *
   * @return a not empty string.
   * @since 1.0
   */
  public String getServiceId() {
    return serviceId;
  }

  /**
   * Gets the native reader.
   *
   * @return a not null reference.
   * @since 1.0
   */
  public SeReader getNativeReader() {
    return nativeReader;
  }

  /**
   * Gets the user input data.
   *
   * @return a nullable reference.
   * @since 1.0
   */
  public Object getUserInputData() {
    return userInputData;
  }

  /**
   * Gets the initial content.
   *
   * @return a nullable reference.
   * @since 1.0
   */
  public AbstractMatchingSe getInitialCardContent() {
    return initialCardContent;
  }
}
