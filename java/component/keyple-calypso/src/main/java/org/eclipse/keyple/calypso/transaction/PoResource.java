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
package org.eclipse.keyple.calypso.transaction;

import org.eclipse.keyple.seproxy.SeReader;
import org.eclipse.keyple.transaction.SeResource;

public class PoResource extends SeResource<CalypsoPo> {
    /**
     * Constructor
     *
     * @param seReader the {@link SeReader} with which the SE is communicating
     * @param calypsoPo the {@link CalypsoPo} information structure
     */
    public PoResource(SeReader seReader, CalypsoPo calypsoPo) {
        super(seReader, calypsoPo);
    }
}
