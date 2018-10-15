/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.seproxy;

import java.util.SortedSet;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.util.NameableConfigurable;


/**
 * Card readers plugin interface.
 */
public interface ReaderPlugin extends NameableConfigurable, Comparable<ReaderPlugin> {

    /**
     * Gets the name.
     *
     * @return the ‘unique’ name of the readers’ plugin.
     */
    String getName();

    /**
     * Gets the readers.
     *
     * @return the ‘unique’ name of the readers’ plugin.
     * @throws KeypleReaderException if the list of readers has not been initialized
     */
    SortedSet<? extends ProxyReader> getReaders() throws KeypleReaderException;

    /**
     * Gets the reader whose name is provided as an argument
     * 
     * @param name of the reader
     * @return the ProxyReader object.
     * @throws KeypleReaderNotFoundException if the wanted reader is not found
     */
    ProxyReader getReader(String name) throws KeypleReaderNotFoundException;
}
