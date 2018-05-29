/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.seproxy.exceptions;

/**
 * The parameter specified are inconsistent with what is expected.
 */
public class InconsistentParameterValueException extends IOReaderException {
    private final String name, value;

    /**
     * Constructor
     * 
     * @param message Message
     * @param name Name
     * @param value Value
     */
    public InconsistentParameterValueException(String message, String name, String value) {
        super(String.format("%s / %s=%s", message, name, value));
        this.name = name;
        this.value = value;
    }

    /**
     * Constructor
     * 
     * @param name Name
     * @param value Value
     */
    public InconsistentParameterValueException(String name, String value) {
        this("Invalid parameter", name, value);
    }

    /**
     * Get the parameter name
     * 
     * @return Name of the parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Get the parameter value
     * 
     * @return Value of the parameter
     */
    public String getValue() {
        return value;
    }
}
