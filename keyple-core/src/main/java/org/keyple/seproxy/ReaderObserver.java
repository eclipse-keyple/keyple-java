/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy;

/**
 * An asynchronous update interface for receiving notifications about Reader information as the
 * Reader is constructed.
 *
 * @author Ixxi
 */
public interface ReaderObserver {

    /**
     * This method is called when information about an Reader which was previously requested using
     * an asynchronous interface becomes available.
     *
     * @param event the event
     */
    void notify(ReaderEvent event);

}
