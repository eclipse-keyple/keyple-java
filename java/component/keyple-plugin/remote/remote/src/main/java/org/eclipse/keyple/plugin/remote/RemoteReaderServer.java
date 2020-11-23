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

/**
 * API of the <b>Remote Reader Server</b> provided by the <b>Remote Plugin Server</b>.
 *
 * <p>This reader behaves like a {@link Reader} but exposes additional services.
 *
 * @since 1.0
 */
public interface RemoteReaderServer extends Reader {

  /**
   * Gets the id of the remote service to execute on the server side.
   *
   * @return a not empty string.
   * @since 1.0
   */
  String getServiceId();

  /**
   * Gets the user input data if it is set.
   *
   * @param classOfT The expected user input data type.
   * @param <T> The expected user input data type.
   * @return <b>null</b> if there is no user input data.
   * @since 1.0
   */
  <T> T getUserInputData(Class<T> classOfT);

  /**
   * Gets the initial content of the smart card if it is set.
   *
   * @param classOfT The expected smart card type.
   * @param <T> The expected smart card type.
   * @return <b>null</b> if there is no initial card content.
   * @since 1.0
   */
  <T extends AbstractSmartCard> T getInitialCardContent(Class<T> classOfT);
}
