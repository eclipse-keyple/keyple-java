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
package org.eclipse.keyple.seproxy.plugin;


/**
 * Abstract Observable Reader class dedicated to static reader configurations.
 * <p>
 * A static reader doesn't offer card insertion/removal mechanism (e.g. AndroidOmapiReader)
 */
public abstract class AbstractStaticReader extends AbstractLocalReader {

    protected AbstractStaticReader(String pluginName, String readerName) {
        super(pluginName, readerName);
    }

    /**
     * Empty start and stopObservation implementations to avoid implementation at the plugin level
     * and remain in compliance with {@link AbstractLocalReader}.
     */

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    protected final void startObservation() {}

    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    protected final void stopObservation() {}

    /** Prevents the use of observers that are not available in a static reader context */

    public final void addObserver(Observer observer) {
        throw new IllegalAccessError(
                "Abstract Static Reader does not support Observers, do not use this function");
    }

    public final void removeObserver(Observer observer) {
        throw new IllegalAccessError(
                "Abstract Static Reader does not support Observers, do not use this function");
    }
}
