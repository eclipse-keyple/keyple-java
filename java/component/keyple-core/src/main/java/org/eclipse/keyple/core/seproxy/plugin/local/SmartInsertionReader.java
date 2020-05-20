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

import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;

public interface SmartInsertionReader extends ObservableReader {
    /**
     * Waits for a SE. Returns true if a SE is detected before the end of the provided timeout.
     * <p>
     * This method must be implemented by the plugin's reader class when it implements the
     * {@link SmartInsertionReader} interface.
     * <p>
     * Returns false if no SE is detected.
     *
     * @return presence status
     * @throws KeypleReaderIOException if the communication with the reader or the SE has failed
     *         (disconnection)
     */
    boolean waitForCardPresent() throws KeypleReaderIOException;


    /**
     * Interrupts the waiting of a SE
     */
    void stopWaitForCard();
}
