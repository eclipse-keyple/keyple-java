/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.seproxy.exception;

/*
 * Exception thrown when {@link org.eclipse.keyple.seproxy.ProxyReader} is not found
 */
public class KeypleReaderNotFoundException extends KeypleReaderException {

    /**
     * Exception thrown when @{@link org.eclipse.keyple.seproxy.ProxyReader} is not found
     * 
     * @param readerName : readerName that has not been found
     */
    public KeypleReaderNotFoundException(String readerName) {
        super("Reader with name " + readerName + " was not found");
    }

}
