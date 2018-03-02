/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.seproxy.exceptions;

/**
 * The parameter specified are inconsistent with what is expected. TODO: At least I guess so.
 * 
 * @deprecated This class is never thrown
 */
public class InconsistentParameterValueException extends Exception {
    public InconsistentParameterValueException(String message) {
        super(message);
    }
}
