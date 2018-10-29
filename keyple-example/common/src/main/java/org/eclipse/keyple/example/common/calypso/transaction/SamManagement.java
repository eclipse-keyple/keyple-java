/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.example.common.calypso.transaction;

import org.eclipse.keyple.example.common.calypso.postructure.CalypsoClassicInfo;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.transaction.SeSelection;
import org.eclipse.keyple.transaction.SeSelector;

public class SamManagement {
    /**
     * Check SAM presence and consistency
     *
     * Throw an exception if the expected SAM is not available
     *
     * @param samReader the SAM reader
     */
    public static void checkSamAndOpenChannel(ProxyReader samReader) {
        /*
         * check the availability of the SAM doing a ATR based selection, open its physical and
         * logical channels and keep it open
         */
        SeSelection samSelection = new SeSelection(samReader);

        SeSelector samSelector = new SeSelector(
                new SeSelector.SelectionParameters(CalypsoClassicInfo.SAM_C1_ATR_REGEX, (short) 0),
                true, null);

        /* Prepare selector, ignore MatchingSe here */
        samSelection.prepareSelector(samSelector);

        try {
            if (!samSelection.processSelection()) {
                throw new IllegalStateException("Unable to open a logical channel for SAM!");
            } else {
            }
        } catch (KeypleReaderException e) {
            throw new IllegalStateException("Reader exception: " + e.getMessage());

        }
    }
}
