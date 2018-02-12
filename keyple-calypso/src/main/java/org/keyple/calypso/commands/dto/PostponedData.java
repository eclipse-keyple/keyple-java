/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.dto;

/**
 * The Class PostponedData. The postponed data is returned at the beginning of the Close Secure
 * Session response data.(in case of Store Value operation)
 */
public class PostponedData {

    /** The has postponed data. */
    private boolean hasPostponedData;

    /** The postponed data. */
    private byte[] postponedData;

    /**
     * Instantiates a new PostponedData.
     *
     * @param hasPostponedData the has postponed data
     * @param postponedData the postponed data
     */
    public PostponedData(boolean hasPostponedData, byte[] postponedData) {
        super();
        this.hasPostponedData = hasPostponedData;
        this.postponedData = (postponedData == null ? null : postponedData.clone());
    }

    /**
     * Gets the checks for postponed data.
     *
     * @return the checks for postponed data
     */
    public boolean getHasPostponedData() {
        return hasPostponedData;
    }

    /**
     * Gets the postponed data.
     *
     * @return the postponed data
     */
    public byte[] getPostponedData() {
        return (postponedData == null ? null : postponedData.clone());
    }

}
