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

import java.util.SortedSet;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;



/**
 * Card readers plugin interface.
 */
public interface ReaderPlugin extends ProxyElement, Comparable<ReaderPlugin> {

    /**
     * Gets the list of names of all readers
     *
     * @return a list of String
     */
    SortedSet<String> getReaderNames();

    /**
     * Gets the readers.
     *
     * @return list of connected readers in this plugin, can be an empty list, can not be null;
     */
    SortedSet<SeReader> getReaders();

    /**
     * Gets the reader whose name is provided as an argument
     * 
     * @param name of the reader
     * @return the SeReader object.
     * @throws KeypleReaderNotFoundException if the wanted reader is not found
     */
    SeReader getReader(String name);
}
