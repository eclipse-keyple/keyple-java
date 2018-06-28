/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.event;

import org.eclipse.keyple.seproxy.local.AbstractLocalReader;

/**
 * Abstract Observable Reader class dedicated to static reader configurations
 */
public abstract class AbstractStaticReader extends AbstractLocalReader {
    protected AbstractStaticReader(String name) {
        super(name);
    }

    public final void addObserver(Observer observer) {
        throw new RuntimeException("Abstract Static Reader does not support Observers, do not use this function");
    }

    public final void removeObserver(Observer observer) {
        throw new RuntimeException("Abstract Static Reader does not support Observers, do not use this function");
    }
}
