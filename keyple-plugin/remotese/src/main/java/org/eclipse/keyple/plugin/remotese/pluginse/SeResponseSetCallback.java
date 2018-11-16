/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.pluginse;

import org.eclipse.keyple.seproxy.SeResponseSet;

/**
 * Callback function used when seResponseSet is received
 */
interface SeResponseSetCallback {

    /**
     * Callback called when SeResponseSet is received
     * 
     * @param seResponseSet : returns the seResponseSet
     */
    void getResponseSet(SeResponseSet seResponseSet);
}
