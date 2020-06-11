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
package org.eclipse.keyple.core.seproxy.plugin.mock;

import java.util.Map;
import java.util.SortedSet;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservablePlugin;

/**
 * This mock plugin fails when instantiate
 */
public class BlankFailingPlugin extends AbstractThreadedObservablePlugin {

    public BlankFailingPlugin(String name) throws KeypleReaderException {
        super(name);
    }

    @Override
    protected SortedSet<String> fetchNativeReadersNames() throws KeypleReaderIOException {
        return null;
    }

    @Override
    protected SeReader fetchNativeReader(String name)
            throws KeypleReaderNotFoundException, KeypleReaderIOException {
        return null;
    }

    @Override
    protected SortedSet<SeReader> initNativeReaders() throws KeypleReaderIOException {
        throw new KeypleReaderIOException("");
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws KeypleReaderIOException {

    }
}
