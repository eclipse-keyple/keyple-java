/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.stub;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.keyple.seproxy.AbstractObservableReader;
import org.eclipse.keyple.seproxy.ReadersPlugin;
import org.eclipse.keyple.seproxy.exceptions.IOReaderException;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public final class StubPlugin implements ReadersPlugin {

    private static final StubPlugin uniqueInstance = new StubPlugin();

    private final Map<String, AbstractObservableReader> readers = new HashMap<String, AbstractObservableReader>();

    private static final ILogger logger = SLoggerFactory.getLogger(StubPlugin.class);


    private StubPlugin() {}

    /**
     * Gets the single instance of StubPlugin.
     *
     * @return single instance of StubPlugin
     */
    public static StubPlugin getInstance() {
        return uniqueInstance;
    }

    @Override
    public String getName() {
        return "StubPlugin";
    }

    @Override
    public List<AbstractObservableReader> getReaders() throws IOReaderException {
        if (readers.size() == 0) {
            logger.info("Stub Reader list is empty, adding one reader");
            StubReader reader = new StubReader();
            readers.put(reader.getName(), reader);
        }
        return new ArrayList(readers.values());
    }


}
