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
package org.eclipse.keyple.transaction;

import org.eclipse.keyple.seproxy.SeReader;

/**
 * The SeResource class groups a MatchingSe and its associated SeReader
 */
public class SeResource<T> {
    private final SeReader seReader;
    private final T matchingSe;

    /**
     * Constructor
     * 
     * @param seReader the {@link SeReader} with which the SE is communicating
     * @param matchingSe the {@link MatchingSe} information structure
     */
    public SeResource(SeReader seReader, T matchingSe) {
        this.seReader = seReader;
        this.matchingSe = matchingSe;
    }

    /**
     * @return the current {@link SeReader} for this SE
     */
    public SeReader getSeReader() {
        return seReader;
    }

    /**
     * @return the {@link MatchingSe}
     */
    public T getMatchingSe() {
        return matchingSe;
    }
}
