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
 * 
 * @since 0.9
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
     * Assert that the input object is not null.
     *
     * @param obj the object to check
     * @param name the object name
     * @return the current instance
     * @throws IllegalArgumentException if object is null
     * @since 0.9
     */
    public Assert notNull(Object obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException("Argument [" + name + "] is null.");
        }
        return INSTANCE;
    }

    /**
     * Assert that the input string is not null and not empty.
     *
     * @param obj the object to check
     * @param name the object name
     * @return the current instance
     * @throws IllegalArgumentException if object is null or empty
     * @since 0.9
     */
    public Assert notEmpty(String obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException("Argument [" + name + "] is null.");
        }
        if (obj.isEmpty()) {
            throw new IllegalArgumentException("Argument [" + name + "] is empty.");
        }
        return INSTANCE;
    }

    /**
     * Assert that a list of objects is not null and not empty.
     *
     * @param obj the object to check
     * @param name the object name
     * @return the current instance
     * @throws IllegalArgumentException if object is null or empty
     * @since 0.9
     */
    public Assert notEmpty(List<? extends Object> obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException("Argument [" + name + "] is null.");
        }
        if (obj.isEmpty()) {
            throw new IllegalArgumentException("Argument [" + name + "] is empty.");
        }
        return INSTANCE;
    }

    /**
     * Assert that a byte array is not null and not empty.
     *
     * @param obj the object to check
     * @param name the object name
     * @return the current instance
     * @throws IllegalArgumentException if object is null or empty
     * @since 0.9
     */
    public Assert notEmpty(byte[] obj, String name) {
        if (obj == null) {
            throw new IllegalArgumentException("Argument [" + name + "] is null.");
        }
        if (obj.length == 0) {
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
     * @throws IllegalArgumentException if condition is null or false
     * @since 0.9
     */
    public Assert isTrue(Boolean condition, String name) {
        if (condition == null) {
            throw new IllegalArgumentException("Condition [" + name + "] is null.");
        }
        if (!condition) {
            throw new IllegalArgumentException("Condition [" + name + "] is false.");
        }
        return INSTANCE;
    }

    /**
     * Assert that an integer is not null and is greater than or equal to minValue.
     *
     * @param number the number to check
     * @param minValue the min accepted value
     * @param name the object name
     * @return the current instance
     * @throws IllegalArgumentException if number is null or has a value less than minValue.
     * @since 0.9
     */
    public Assert greaterOrEqual(Integer number, int minValue, String name) {
        if (number == null) {
            throw new IllegalArgumentException("Argument [" + name + "] is null.");
        }
        if (number < minValue) {
            throw new IllegalArgumentException("Argument [" + name + "] has a value [" + number
                    + "] less than [" + minValue + "].");
        }
        return INSTANCE;
    }

    /**
     * Assert that an integer is equal to value.
     *
     * @param number the number to check
     * @param value the expected value
     * @param name the object name
     * @return the current instance
     * @throws IllegalArgumentException if number is null or has a value less than minValue.
     * @since 0.9
     */
    public Assert isEqual(Integer number, int value, String name) {
        if (number == null) {
            throw new IllegalArgumentException("Argument [" + name + "] is null.");
        }
        if (number != value) {
            throw new IllegalArgumentException("Argument [" + name + "] has a value [" + number
                    + "] not equal to [" + value + "].");
        }
        return INSTANCE;
    }

    /**
     * Assert that an integer is not null and is in the range minValue, maxValue.
     *
     * @param number the number to check
     * @param minValue the min accepted value
     * @param maxValue the max accepted value
     * @param name the object name
     * @return the current instance
     * @throws IllegalArgumentException if number is null or is out of range.
     * @since 0.9
     */
    public Assert isInRange(Integer number, int minValue, int maxValue, String name) {
        if (number == null) {
            throw new IllegalArgumentException("Argument [" + name + "] is null.");
        }
        if (number < minValue) {
            throw new IllegalArgumentException("Argument [" + name + "] has a value [" + number
                    + "] less than [" + minValue + "].");
        }
        if (number > maxValue) {
            throw new IllegalArgumentException("Argument [" + name + "] has a value [" + number
                    + "] more than [" + maxValue + "].");
        }
        return INSTANCE;
    }
}
