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
package org.eclipse.keyple.core.seproxy;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;



/**
 * Card readers plugin interface.
 */
public interface ReaderPlugin extends ProxyElement {

    /**
     * Gets the list of names of all readers
     *
     * @return a list of String
     */
    Set<String> getReaderNames();

    /**
     * Gets the readers.
     *
     * @return the map of this plugin's connected reader's name and instance, can be an empty list,
     *         can not be null;
     */
    ConcurrentMap<String, SeReader> getReaders();

    /**
     * Gets the reader whose name is provided as an argument
     * 
     * @param name of the reader
     * @return the SeReader object.
     * @throws KeypleReaderNotFoundException if the wanted reader is not found
     */
    SeReader getReader(String name);
}
