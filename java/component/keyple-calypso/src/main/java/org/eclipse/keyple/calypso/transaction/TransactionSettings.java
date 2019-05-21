/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.transaction;



/**
 * A class dedicated to managing the security settings involved in managing secure sessions.
 * <p>
 * The object provides default values when instantiated, they can be modified with the putKeyInfo
 * method.
 * <p>
 * The getKeyInfo method returns the specified setting value.
 */
public class TransactionSettings {
    private final AbstractPoApplicationSettings poApplicationSettings;
    private final SamResource samResource;

    /**
     * Create a {@link TransactionSettings} object from a {@link AbstractPoApplicationSettings} and
     * a {@link SamResource}
     * 
     * @param poApplicationSettings object containing parameters about the current PO (aid, security
     *        keys, serial number filter)
     * @param samResource the {@link SamResource} to be used to process the cryptographic
     *        computations
     */
    public TransactionSettings(AbstractPoApplicationSettings poApplicationSettings,
            SamResource samResource) {
        this.poApplicationSettings = poApplicationSettings;
        this.samResource = samResource;
    }

    public AbstractPoApplicationSettings getPoApplicationSettings() {
        return poApplicationSettings;
    }

    /**
     * Get the {@link SamResource} associated with this {@link TransactionSettings} object.
     * 
     * @return the current {@link SamResource}
     */
    SamResource getSamResource() {
        return samResource;
    }
}
