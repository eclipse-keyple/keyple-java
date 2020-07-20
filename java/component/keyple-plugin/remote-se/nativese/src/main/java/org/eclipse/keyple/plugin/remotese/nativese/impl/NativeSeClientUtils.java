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
package org.eclipse.keyple.plugin.remotese.nativese.impl;

import org.eclipse.keyple.plugin.remotese.nativese.NativeSeClientService;

/**
 * Utils class to retrieve the client NativeSeService
 */
public class NativeSeClientUtils {

    /**
     * Retrieve the NativeSeClient if initiated
     * 
     * @return the singleton instance of the service if instanciated, null instead
     */
    static public NativeSeClientService getService() {
        return NativeSeClientServiceImpl.getInstance();
    }


    // TODO: get asyncNode

}
