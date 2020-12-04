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
package org.eclipse.keyple.plugin.remote;

import org.eclipse.keyple.core.card.selection.AbstractSmartCard;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.util.Assert;

/**
 * This POJO contains the <b>parameters</b> of the method {@link
 * LocalServiceClient#executeRemoteService(RemoteServiceParameters, Class)}.
 *
 * <ul>
 *   <li><b>serviceId</b> : The ticketing service id. It will permit to indicate to the server which
 *       ticketing service to execute (Materialization, Validation, Control, etc...). This field is
 *       free and is for the user's use only.
 *   <li><b>localReader</b> : The local reader to manage remotely from the server.
 *   <li><b>userInputData</b> (optional) : A DTO containing the user input data if you want to
 *       transmit data to the remote ticketing service.
 *   <li><b>initialCardContent</b> (optional) : An {@link AbstractSmartCard} containing the initial
 *       smart card content to transmit to the remote ticketing service. For example, for Calypso
 *       ticketing application, this object will be a <b>CalypsoPo</b> or a <b>CalypsoSam</b>,
 *       depending on the context.
 * </ul>
 *
 * @since 1.0
 */
public class RemoteServiceParameters {

  private final String serviceId;
  private final Reader localReader;
  private final Object userInputData;
  private final AbstractSmartCard initialCardContent;

  private RemoteServiceParameters(Builder builder) {

    Assert.getInstance()
        .notEmpty(builder.serviceId, "serviceId")
        .notNull(builder.localReader, "localReader");

    serviceId = builder.serviceId;
    localReader = builder.localReader;
    userInputData = builder.userInputData;
    initialCardContent = builder.initialCardContent;
  }

  /**
   * Gets a new builder.
   *
   * @param serviceId The ticketing service id. It will permit to indicate to the server which
   *     ticketing service to execute (Materialization, Validation, Control, etc...). This field is
   *     free and is for the user's use only.
   * @param localReader The local reader to manage remotely from the server.
   * @return a new builder instance.
   * @since 1.0
   */
  public static Builder builder(String serviceId, Reader localReader) {
    return new Builder(serviceId, localReader);
  }

  /** The builder pattern */
  public static class Builder {

    private final String serviceId;
    private final Reader localReader;
    private Object userInputData;
    private AbstractSmartCard initialCardContent;

    private Builder(String serviceId, Reader localReader) {
      this.serviceId = serviceId;
      this.localReader = localReader;
    }

    /**
     * Add a DTO containing user input data to transmit to the remote ticketing service.
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
     * Add an {@link AbstractSmartCard} containing the initial smart card content to transmit to the
     * remote ticketing service.
     *
     * <p>For example, for Calypso ticketing application, this object will be a <b>CalypsoPo</b> or
     * a <b>CalypsoSam</b>, depending on the context.
     *
     * @param initialCardContent The initial smart card content.
     * @return the builder instance
     * @since 1.0
     */
    public Builder withInitialCardContent(AbstractSmartCard initialCardContent) {
      this.initialCardContent = initialCardContent;
      return this;
    }

    /**
     * Builds the object.
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
   * Gets the local reader.
   *
   * @return a not null reference.
   * @since 1.0
   */
  public Reader getLocalReader() {
    return localReader;
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
   * Gets the initial smart card content.
   *
   * @return a nullable reference.
   * @since 1.0
   */
  public AbstractSmartCard getInitialCardContent() {
    return initialCardContent;
  }
}
