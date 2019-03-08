/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.seproxy.exception;

import org.eclipse.keyple.seproxy.message.ProxyReader;

/*
 * Exception thrown when {@link org.eclipse.keyple.seproxy.message.ProxyReader} is not found
 */
public class KeypleReaderNotFoundException extends KeypleReaderException {

    /**
     * Exception thrown when @{@link ProxyReader} is not found
     * 
     * @param readerName : readerName that has not been found
     */
    public KeypleReaderNotFoundException(String readerName) {
        super("Reader with name " + readerName + " was not found");
    }

}
