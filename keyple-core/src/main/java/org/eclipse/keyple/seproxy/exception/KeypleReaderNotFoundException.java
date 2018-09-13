/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.exception;

public class KeypleReaderNotFoundException extends KeypleReaderException {

    /**
     * Exception thrown when Reader is not found
     * 
     * @param readerName : readerName that has not been found
     */
    public KeypleReaderNotFoundException(String readerName) {
        super("Reader with name " + readerName + " was not found");
    }

}
