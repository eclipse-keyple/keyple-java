/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import org.keyple.seproxy.exceptions.IOReaderException;

/**
 * Interface each {@link ReadersPlugin} should implement
 */
public interface ProxyReader {

    /**
     * Gets the name.
     *
     * @return returns the ‘unique’ name of the SE reader for the selected plugin.
     */
    String getName();

    /**
     * Transmits a request to a SE application and get back the corresponding SE response o the
     * usage of this method is conditioned to the presence of a SE in the selected reader, this
     * method could also fail in case of IO error or wrong card state → some reader’s exceptions (SE
     * missing, IO error, wrong card state, timeout) have to be caught during the processing of the
     * SE request transmission.
     *
     * @param seApplicationRequest the se application request
     * @return the SE response
     * @throws IOReaderException Exception of type IO Reader
     */
    SeResponseSet transmit(SeRequestSet seApplicationRequest) throws IOReaderException;

    /**
     * Checks if is SE present.
     *
     * @return true if a Secure Element is present in the reader
     * @throws IOReaderException Exception of type IO Reader
     */
    boolean isSEPresent() throws IOReaderException;
}
