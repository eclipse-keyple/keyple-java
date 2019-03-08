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
package org.eclipse.keyple.plugin.remotese.nativese;


import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.seproxy.message.ProxyReader;

public interface NativeReaderService {


    /**
     * Connect Physical Local Reader to Remote SE Creates a Session to exchange data with this
     * Reader with an option to duplex connection
     */
    String connectReader(ProxyReader localReader, String clientNodeId) throws KeypleReaderException;

    /**
     * Disconnect Physical Local Reader from Remote Se Master Server
     */
    void disconnectReader(String sessionId, String nativeReaderName, String clientNodeId)
            throws KeypleReaderException;

    /**
     * Find a local reader accross plugins
     * 
     * @param nativeReaderName
     * @return
     * @throws KeypleReaderNotFoundException
     */
    SeReader findLocalReader(String nativeReaderName) throws KeypleReaderNotFoundException;

}
