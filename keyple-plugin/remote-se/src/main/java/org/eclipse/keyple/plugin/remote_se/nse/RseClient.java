/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.nse;

import java.util.Map;

import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.event.ObservableReader;
import org.eclipse.keyple.seproxy.exception.IOReaderException;

public interface RseClient extends ObservableReader.ReaderObserver {


    /**
     * Connect Physical Local Reader to Remote SE Creates a Session to exchange data with this
     * Reader with an option to duplex connection
     */
    String connectReader(ProxyReader localReader, Map<String,Object> options) throws IOReaderException;


    void disconnectReader(ProxyReader localReader) throws IOReaderException;




}
