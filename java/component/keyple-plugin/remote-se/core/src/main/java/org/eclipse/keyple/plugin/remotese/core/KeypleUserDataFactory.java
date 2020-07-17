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
package org.eclipse.keyple.plugin.remotese.core;


/**
 * <b>Keyple User Data Factory</b> API to be implemented by the user.
 * <p>
 * You must provide to Keyple an implementation of this factory in order to allow him to build a
 * {@link KeypleUserData} object before giving it back to you.
 *
 * @since 1.0
 */
public interface KeypleUserDataFactory<T extends KeypleUserData> {

    /**
     * Gets a new instance of <b>T</b> from a Json representation
     *
     * @param data The data of <b>T</b> as a Json String
     * @return a new instance of <b>T</b>
     * @since 1.0
     */
    T getInstance(String data);
}
