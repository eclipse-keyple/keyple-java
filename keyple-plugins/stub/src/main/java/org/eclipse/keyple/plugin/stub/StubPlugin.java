/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import org.eclipse.keyple.seproxy.event.AbstractObservableReader;
import org.eclipse.keyple.seproxy.event.AbstractThreadedObservablePlugin;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public final class StubPlugin extends AbstractThreadedObservablePlugin {

    private static final StubPlugin uniqueInstance = new StubPlugin();

    private final Map<String, AbstractObservableReader> readers =
            new HashMap<String, AbstractObservableReader>();

    private static final ILogger logger = SLoggerFactory.getLogger(StubPlugin.class);


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
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IOException {

    }

    @Override
    protected SortedSet<AbstractObservableReader> getNativeReaders() throws IOReaderException {
        // logger may not be initialized when getNativeReaders is called from
        // AbstractObservablePlugin logger.info("Create Stub plugin native reader");
        SortedSet<AbstractObservableReader> nativeReaders =
                new ConcurrentSkipListSet<AbstractObservableReader>();
        nativeReaders.add(new StubReader());
        return nativeReaders;
    }

    @Override
    protected AbstractObservableReader getNativeReader(String name) throws IOReaderException {
        // TODO check what to do here
        return null;
    }

    @Override
    protected SortedSet<String> getNativeReadersNames() throws IOReaderException {
        return null;
    }
}
