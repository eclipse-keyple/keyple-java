/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclise.keyple.example.remote.server.transport.sync;

import org.eclipse.keyple.seproxy.event.ReaderEvent;

public interface SyncServerListener {

    String onReaderConnect(String readerName);

    void onRemoteReaderEvent(ReaderEvent event);

}
