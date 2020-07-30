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
package org.eclipse.keyple.plugin.remotese.core;


import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.plugin.remotese.core.exception.KeypleDoNotPropagateEventException;

public interface KeypleClientReaderEventFilter<T> {
    /**
     * Configure the Type of the output
     *
     * @return non nullable instance of the factory
     */
    Class<T> getUserOutputDataClass();

    /**
     * Execute any process before the event is sent to the server
     *
     * @param event that will be propagated
     * @return nullable data that will be sent to the server.
     * @throws KeypleDoNotPropagateEventException if event should not be propagated to server
     */
    Object beforePropagation(ReaderEvent event) throws KeypleDoNotPropagateEventException;

    /**
     * Retrieve the output from the event global processing
     *
     * @param userOutputData nullable instance of the
     */
    void afterPropagation(T userOutputData);

}
