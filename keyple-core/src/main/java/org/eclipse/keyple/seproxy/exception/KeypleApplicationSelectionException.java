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
package org.eclipse.keyple.seproxy.exception;


import org.eclipse.keyple.seproxy.ProxyReader;

/**
 * Application selection failure in {@link ProxyReader} by AID or ATR
 */
public class KeypleApplicationSelectionException extends KeypleReaderException {


    public KeypleApplicationSelectionException(String message) {
        super(message);
    }

    public KeypleApplicationSelectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
