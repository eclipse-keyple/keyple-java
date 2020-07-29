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
package org.eclipse.keyple.plugin.remotese.virtualse;


import org.eclipse.keyple.core.selection.AbstractMatchingSe;
import org.eclipse.keyple.core.seproxy.SeReader;

/**
 * <b>Remote SE Server Reader</b> API.
 * <p>
 * This reader must be used in the use case of the <b>Remote SE Server Plugin</b>.
 * <p>
 * This reader behaves like an {@link SeReader} but exposes additional services.
 *
 * @since 1.0
 */
public interface RemoteSeServerReader extends SeReader {

    /**
     * Gets the id of the remote service to execute on the server's side.
     *
     * @return a not empty string.
     * @since 1.0
     */
    String getServiceId();

    /**
     * Gets the user input data if they are set.
     *
     * @param classOfT The type expected of user input data.
     * @return a nullable reference if there is no user input data.
     * @since 1.0
     */
    <T> T getUserInputData(Class<T> classOfT);

    /**
     * Gets the initial SE content if is set.
     *
     * @return a nullable reference if there is no initial SE content.
     * @since 1.0
     */
    <T extends AbstractMatchingSe> T getInitialSeContent();
}
