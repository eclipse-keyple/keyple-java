/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.nativese;


import org.eclipse.keyple.plugin.remotese.nativese.impl.NativeSeClientServiceFactory;
import org.eclipse.keyple.plugin.remotese.nativese.impl.NativeSeClientUtils;

/**
 * <b>Native SE Client Service</b> API.
 * <p>
 * This service must be used in the use case of the <b>Remote SE Server Plugin</b>.
 * <p>
 * It must be started by a <b>client</b> application that is installed on a terminal with native
 * access to the SE card reader :
 * <ul>
 * <li>To <b>start</b> the service, use the factory {@link NativeSeClientServiceFactory}.</li>
 * <li>To <b>access</b> the service, use the utility method
 * {@link NativeSeClientUtils#getService()}.</li>
 * <li>To <b>stop</b> the service, there is nothing special to do because the service is a standard
 * java singleton instance.</li>
 * </ul>
 *
 * @since 1.0
 */
public interface NativeSeClientService {

    /**
     * This method allows you to connect a native SE reader to a remote server and execute a
     * specific ticketing service from the server.<br>
     * The service is identify by the <b>serviceId</b> parameter.
     *
     * @param parameters The service parameters (serviceId, ...) (see
     *        {@link RemoteServiceParameters} documentation for all possible parameters)
     * @param classOfT The actual class of the expected user output data.
     * @param <T> The generic type of the expected user output data.
     * @return a new instance of <b>T</b>.
     * @throws RuntimeException if an unexpected error occurs.
     * @since 1.0
     */
    <T> T executeRemoteService(RemoteServiceParameters parameters, Class<T> classOfT);
}
