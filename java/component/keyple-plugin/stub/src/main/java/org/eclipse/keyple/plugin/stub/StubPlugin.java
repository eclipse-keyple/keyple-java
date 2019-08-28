package org.eclipse.keyple.plugin.stub;

import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

import java.util.Set;

public interface StubPlugin {

    String PLUGIN_NAME = "StubPlugin";

    void plugStubReader(String name, Boolean synchronous);

    void plugStubReader(String name, TransmissionMode transmissionMode,
                               Boolean synchronous);

    void unplugStubReader(String name, Boolean synchronous) throws KeypleReaderException;

    void unplugStubReaders(Set<String> names, Boolean synchronous);
}
