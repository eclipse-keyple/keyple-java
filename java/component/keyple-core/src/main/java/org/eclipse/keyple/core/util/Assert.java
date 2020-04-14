/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.util;

import java.util.List;

/**
 * The utility class {@code Assert} exposes very useful methods for testing method call parameters
 * and raising a {@link IllegalArgumentException} unchecked exception.
 */
public final class Assert {

    /** Singleton pattern */
    private static final Assert INSTANCE = new Assert();

    /** Private Constructor */
    private Assert() {}

    /**
     * Gets the unique instance.
     *
     * @return the instance
     */
    public static Assert getInstance() {
        return INSTANCE;
    }

    /**
     * Assert that the input {@code Object} is not null.
     *
     * @param o the object to check
     * @param name the object name
     * @return the current instance
     * @throws IllegalArgumentException if {@code o} is null
     */
    public Assert notNull(Object o, String name) {
        if (o == null) {
            throw new IllegalArgumentException("Argument [" + name + "] is null.");
        }
        return INSTANCE;
    }

    /**
     * Assert that the input {@code String} is not null and not empty.
     *
     * @param o the object to check
     * @param name the object name
     * @return the current instance
     * @throws IllegalArgumentException if {@code o} is null or empty
     */
    public Assert notEmpty(String o, String name) {
        if (o == null) {
            throw new IllegalArgumentException("Argument [" + name + "] is null.");
        }
        if (o.isEmpty()) {
            throw new IllegalArgumentException("Argument [" + name + "] is empty.");
        }
        return INSTANCE;
    }

    /**
     * Assert that a list of {@code Objdect} is not null and not empty.
     *
     * @param o the object to check
     * @param name the object name
     * @return the current instance
     * @throws IllegalArgumentException if {@code o} is null or empty
     */
    public Assert notEmpty(final List<? extends Object> o, String name) {
        if (o == null) {
            throw new IllegalArgumentException("Argument [" + name + "] is null.");
        }
        if (o.isEmpty()) {
            throw new IllegalArgumentException("Argument [" + name + "] is empty.");
        }
        return INSTANCE;
    }

    /**
     * Assert that a byte array is not null and not empty.
     *
     * @param o the object to check
     * @param name the object name
     * @return the current instance
     * @throws IllegalArgumentException if {@code o} is null or empty
     */
    public Assert notEmpty(final byte[] o, String name) {
        if (o == null) {
            throw new IllegalArgumentException("Argument [" + name + "] is null.");
        }
        if (o.length == 0) {
            throw new IllegalArgumentException("Argument [" + name + "] is empty.");
        }
        return INSTANCE;
    }

    /**
     * Assert that a condition is true.
     *
     * @param condition the condition to check
     * @param name the object name
     * @return the current instance
     * @throws IllegalArgumentException if {@code condition} is false
     */
    public Assert assertTrue(boolean condition, String name) {
        if (!condition) {
            throw new IllegalArgumentException("Condition [" + name + "] is false.");
        }
        return INSTANCE;
    }
}
