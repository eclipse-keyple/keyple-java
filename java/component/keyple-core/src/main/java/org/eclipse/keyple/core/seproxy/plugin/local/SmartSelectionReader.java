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
package org.eclipse.keyple.core.seproxy.plugin.local;

import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.message.ApduResponse;

/**
 * Interface implemented by readers able to handle natively the SE selection process (e.g. Android
 * OMAPI readers).
 */
public interface SmartSelectionReader extends SeReader {

    /**
     * Opens a logical channel for the provided AID
     * 
     * @param aidSelector the selection data
     * @return an ApduResponse containing the SE answer to selection
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     */
    ApduResponse openChannelForAid(SeSelector.AidSelector aidSelector)
            throws KeypleReaderIOException;
}
