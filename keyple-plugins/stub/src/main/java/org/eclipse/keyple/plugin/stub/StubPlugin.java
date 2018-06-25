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
import org.eclipse.keyple.seproxy.event.AbstractStaticPlugin;
import org.eclipse.keyple.seproxy.exception.IOReaderException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public final class StubPlugin extends AbstractStaticPlugin {

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
        logger.info("Create Stub plugin native reader");
        StubReader reader = new StubReader();
        readers.put(reader.getName(), reader);
        return new ConcurrentSkipListSet(readers.values());
    }

    @Override
    protected AbstractObservableReader getNativeReader(String name) throws IOReaderException {
        // TODO check what to do here
        return null;
    }
}
