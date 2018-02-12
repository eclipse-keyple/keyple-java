/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

import java.util.List;
import org.keyple.seproxy.exceptions.IOReaderException;

/**
 * The Interface ReadersPlugin. This interface has to be implemented by each plugins of readers’
 * drivers.
 *
 * @author Ixxi
 */
public interface ReadersPlugin {

    // TODO - possibility to force implementatiosn to be singleton?
    // TODO - add ObservablePlugin interface

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
     * @throws IOReaderException Exception of type IO Reader
     */
    List<ProxyReader> getReaders() throws IOReaderException;

}
