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
import org.eclipse.keyple.core.seproxy.exception.NoStackTraceThrowable;

/**
 * Interface implemented by the readers able to handle natively the card removal process.
 * <p>
 */
public interface SmartReader extends ObservableReader {
    /**
     * Wait until the card disappears.
     * <p>
     * This method must be implemented by the plugin's reader class when it implements the
     * {@link SmartReader} interface. The reader implementation must manage the card withdrawal
     * process itself. (for example by using the analogous waitForCardAbsent method in the case of a
     * plugin based on smartcard.io [PC/SC]).
     * <p>
     * In the case where the reader plugin is not able to handle the card removal process itself
     * (not implementing the {@link SmartReader} interface, then it is managed by the
     * waitForCardAbsentPing method defined in this class.
     * <p>
     * Returns true if a card has disappeared before the end of the provided timeout. Returns false
     * if thecd .. is still present within the delay.
     *
     * @param timeout the delay in millisecond we wait for a card to be withdrawn, a value of zero
     *        means wait for ever.
     * @return presence status
     */
    boolean waitForCardAbsentNative(long timeout) throws NoStackTraceThrowable;
}
