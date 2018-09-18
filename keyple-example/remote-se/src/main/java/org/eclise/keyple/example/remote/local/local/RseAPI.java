/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.local.local;

import org.eclipse.keyple.seproxy.event.ReaderEvent;

import java.util.Map;

public interface RseAPI {

    String onReaderConnect(String readerName, Map<String, Object> options);

    String onReaderDisconnect(String readerName, String sessionId);

    void onRemoteReaderEvent(ReaderEvent event, String sessionId);

}
