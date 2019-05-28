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

import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;

public abstract class AbstractObservableReaderPoolPlugin extends AbstractObservablePlugin
        implements ReaderPoolPlugin {
    /**
     * Instanciates a new ReaderPoolPlugin. Retrieve the current readers list.
     * <p>
     * Gets the list for the native method the first time (null)
     *
     * @param name name of the plugin
     */
    protected AbstractObservableReaderPoolPlugin(String name) {
        super(name);
    }
}
