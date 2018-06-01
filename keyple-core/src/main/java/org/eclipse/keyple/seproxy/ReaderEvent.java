/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy;


/**
 */
public enum ReaderEvent {

    /** An io error occurred. */
    IO_ERROR("IO Error"),

    /** A SE has been inserted. */
    SE_INSERTED("insertion"),

    /** The SE has been inserted. */
    SE_REMOVAL("removal");

    /** The event name. */
    private String name;

    ReaderEvent(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
