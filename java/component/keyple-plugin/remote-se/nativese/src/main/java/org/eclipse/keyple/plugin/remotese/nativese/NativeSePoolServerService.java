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
package org.eclipse.keyple.plugin.remotese.nativese;

/**
 * <b>Native SE Pool Server Service</b> API.
 *
 * <p>This service must be used in the use case of the <b>Remote SE Pool Client Plugin</b>.
 *
 * <p>It must be started by a <b>server</b> application that is installed on a terminal with native
 * access to the SE pool cards reader :
 *
 * <ul>
 *   <li>To start the service, use the factory {@link NativeSePoolServerServiceFactory}.
 *   <li>To access the service, there is no method because this service is only used internally by
 *       Keyple.
 *   <li>To stop the service, there is nothing special to do because the service is a standard java
 *       singleton instance.
 * </ul>
 *
 * @since 1.0
 */
public interface NativeSePoolServerService {}
