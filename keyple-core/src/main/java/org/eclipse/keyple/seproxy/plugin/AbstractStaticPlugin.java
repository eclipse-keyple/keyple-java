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
 * Abstract Observable Plugin class dedicated to static reader configurations
 */
public abstract class AbstractStaticPlugin extends AbstractObservablePlugin {
    protected AbstractStaticPlugin(String name) {
        super(name);
    }

    public final void addObserver(Observer observer) {
        super.addObserver(observer);
    }

    public final void removeObserver(Observer observer) {
        super.removeObserver(observer);
    }
}
