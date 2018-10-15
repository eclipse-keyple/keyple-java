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

package org.eclipse.keyple.seproxy.plugin;


/**
 * Abstract Observable Reader class dedicated to static reader configurations
 */
public abstract class AbstractStaticReader extends AbstractLocalReader {

    protected AbstractStaticReader(String pluginName, String readerName) {
        super(pluginName, readerName);
    }

    public final void addObserver(Observer observer) {
        throw new RuntimeException(
                "Abstract Static Reader does not support Observers, do not use this function");
    }

    public final void removeObserver(Observer observer) {
        throw new RuntimeException(
                "Abstract Static Reader does not support Observers, do not use this function");
    }
}
