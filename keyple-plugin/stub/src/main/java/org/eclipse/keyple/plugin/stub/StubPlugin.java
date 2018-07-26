/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.seproxy.plugin.AbstractThreadedObservablePlugin;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public final class StubPlugin extends AbstractThreadedObservablePlugin {

    private static final StubPlugin uniqueInstance = new StubPlugin();

    private static final ILogger logger = SLoggerFactory.getLogger(StubPlugin.class);

    private final Map<String, String> parameters = new HashMap<String, String>();

    private StubPlugin() {
        super("StubPlugin");
    }

    /**
     * Gets the single instance of StubPlugin.
     *
     * @return single instance of StubPlugin
     */
    public static StubPlugin getInstance() {
        return uniqueInstance;
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }

    @Override
    public void setParameter(String key, String value) {
        parameters.put(key, value);
    }

    @Override
    protected SortedSet<AbstractObservableReader> getNativeReaders() throws IOReaderException {
        SortedSet<AbstractObservableReader> nativeReaders =
                new ConcurrentSkipListSet<AbstractObservableReader>();
        // add native readers
        nativeReaders.add(new StubReader());
        return nativeReaders;
    }

    @Override
    protected AbstractObservableReader getNativeReader(String name) throws IOReaderException {
        for (AbstractObservableReader reader : readers) {
            if (reader.getName().equals(name)) {
                return reader;
            }
        }
        throw new IOReaderException("Reader with name " + name + " was not found");
    }

    @Override
    protected SortedSet<String> getNativeReadersNames() throws IOReaderException {
        return nativeReadersNames;
    }
}
