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
package org.eclipse.keyple.plugin.stub;

import java.util.Set;
import org.eclipse.keyple.core.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;

public interface StubPlugin extends ObservablePlugin {

    String PLUGIN_NAME = "STUB_PLUGIN";

    void plugStubReader(String name, Boolean synchronous);

    void plugStubReader(String name, TransmissionMode transmissionMode, Boolean synchronous);

    void unplugStubReader(String name, Boolean synchronous) throws KeypleReaderException;

    void unplugStubReaders(Set<String> names, Boolean synchronous);
}
