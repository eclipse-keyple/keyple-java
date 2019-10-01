/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.seproxy.plugin;

import java.util.SortedSet;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;

/**
 * Interface to be implemented by the plugins requiring a thread handled by Keyple core to monitor
 * the reader insertion/removal
 */
public interface ThreadedMonitoringPlugin {
    /**
     * Fetch the list of connected native reader (usually from third party library) and returns
     * their names (or id)
     *
     * @return connected readers' name list
     * @throws KeypleReaderException if a reader error occurs
     */
    SortedSet<String> fetchNativeReadersNames() throws KeypleReaderException;
}
