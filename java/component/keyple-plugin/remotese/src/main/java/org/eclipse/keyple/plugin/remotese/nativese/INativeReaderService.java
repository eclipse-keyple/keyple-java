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


import java.util.Map;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;

public interface INativeReaderService {


    /**
     * Connect Physical Local Reader to Remote SE Creates a Session to exchange data with this
     * Reader with an option to duplex connection
     * 
     * @param localReader nativeReader to be connected to Master
     * @return sessionId id of the session of the virtual reader
     * @throws KeypleReaderException if reader is already connected
     */
    String connectReader(SeReader localReader) throws KeypleReaderException;

    /**
     * Connect Physical Local Reader to Remote SE Creates a Session to exchange data with this
     * Reader with an option to duplex connection
     * 
     * @param localReader nativeReader to be connected to Master
     * @param options map of parameters to set into the virtual reader see
     *        {@link SeReader#getParameters()}
     * @return sessionId id of the session of the virtual reader
     * @throws KeypleReaderException if reader is already connected
     */
    String connectReader(SeReader localReader, Map<String, String> options)
            throws KeypleReaderException;


    /**
     * Disconnect Physical Local Reader from RemoteSe Master, nativeReaderName must be used as the
     * identifier of the nativeReader
     *
     * @param sessionId (optional)
     * @param nativeReaderName local name of the reader, will be used coupled with the nodeId to
     *        identify the virtualReader
     * @throws KeypleReaderException if reader is not already connected
     */
    void disconnectReader(String sessionId, String nativeReaderName) throws KeypleReaderException;

    /**
     * Find a local reader across plugins
     * 
     * @param nativeReaderName : native name of the reader to find
     * @return SeReader : Se Reader found
     * @throws KeypleReaderNotFoundException : if none reader was found
     */
    SeReader findLocalReader(String nativeReaderName) throws KeypleReaderNotFoundException;

}
