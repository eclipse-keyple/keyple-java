/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.dto;

/**
 * The Class Ratification. In contact mode, the session is always immediately ratified. In
 * contactless mode, it is possible to force the ratification by setting P1 to the value 80h.
 */
public class Ratification {

    /** The is ratified. */
    private boolean isRatified;

    /**
     * Instantiates a new Ratification.
     *
     * @param isRatified the is ratified
     */
    public Ratification(boolean isRatified) {
        super();
        this.isRatified = isRatified;
    }

    /**
     * Checks if is ratified.
     *
     * @return true, if is ratified
     */
    public boolean isRatified() {
        return isRatified;
    }
}
