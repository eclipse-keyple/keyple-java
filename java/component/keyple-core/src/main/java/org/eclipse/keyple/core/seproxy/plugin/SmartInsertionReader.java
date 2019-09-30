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
package org.eclipse.keyple.core.seproxy.plugin;

import org.eclipse.keyple.core.seproxy.event.ObservableReader;

public interface SmartInsertionReader extends ObservableReader {
    /**
     * TODO Check if we really need a timeout parameter Waits for a card. Returns true if a card is
     * detected before the end of the provided timeout.
     * <p>
     * This method must be implemented by the plugin's reader class.
     * <p>
     * Returns false if no card detected within the delay.
     *
     * @param timeout the delay in millisecond we wait for a card insertion, a value of zero means
     *        wait for ever.
     * @return presence status
     */
    boolean waitForCardPresent(long timeout);
}
