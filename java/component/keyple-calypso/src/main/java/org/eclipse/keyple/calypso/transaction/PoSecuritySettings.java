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

import java.util.EnumMap;
import java.util.List;


/**
 * A class dedicated to managing the security settings involved in managing secure sessions.
 * <p>
 * The object provides default values when instantiated, they can be modified with the putKeyInfo
 * method.
 * <p>
 * The getKeyInfo method returns the specified setting value.
 */
public class PoSecuritySettings {
    /** List of authorized KVCs */
    private List<Byte> authorizedKvcList;

    /** EnumMap associating session levels and corresponding KIFs */
    private static EnumMap<PoTransaction.SessionSetting.AccessLevel, Byte> defaultKif =
            new EnumMap<PoTransaction.SessionSetting.AccessLevel, Byte>(
                    PoTransaction.SessionSetting.AccessLevel.class);
    private static EnumMap<PoTransaction.SessionSetting.AccessLevel, Byte> defaultKvc =
            new EnumMap<PoTransaction.SessionSetting.AccessLevel, Byte>(
                    PoTransaction.SessionSetting.AccessLevel.class);
    private static EnumMap<PoTransaction.SessionSetting.AccessLevel, Byte> defaultKeyRecordNumber =
            new EnumMap<PoTransaction.SessionSetting.AccessLevel, Byte>(
                    PoTransaction.SessionSetting.AccessLevel.class);

    PoTransaction.SessionSetting.ModificationMode sessionModificationMode =
            PoTransaction.SessionSetting.ModificationMode.ATOMIC;
    PoTransaction.SessionSetting.RatificationMode ratificationMode =
            PoTransaction.SessionSetting.RatificationMode.CLOSE_RATIFIED;

    /**
     * Constructor.
     */
    public PoSecuritySettings() {}

    /**
     * Set the Session Modification Mode<br>
     * The default value is ATOMIC
     * 
     * @param sessionModificationMode the desired Session Modification Mode
     * @since 0.9
     */
    public void setSessionModificationMode(
            PoTransaction.SessionSetting.ModificationMode sessionModificationMode) {
        this.sessionModificationMode = sessionModificationMode;
    }

    /**
     * Set the Ratification Mode<br>
     * The default value is CLOSE_RATIFIED
     * 
     * @param ratificationMode the desired Ratification Mode
     * @since 0.9
     */
    public void setRatificationMode(
            PoTransaction.SessionSetting.RatificationMode ratificationMode) {
        this.ratificationMode = ratificationMode;
    }

    /**
     * Set the default KIF<br>
     *
     * @param sessionAccessLevel the session level
     * @param kif the desired default KIF
     * @since 0.9
     */
    public void setSessionDefaultKif(PoTransaction.SessionSetting.AccessLevel sessionAccessLevel,
            byte kif) {
        defaultKif.put(sessionAccessLevel, kif);
    }

    /**
     * Set the default KVC<br>
     *
     * @param sessionAccessLevel the session level
     * @param kvc the desired default KVC
     * @since 0.9
     */
    public void setSessionDefaultKvc(PoTransaction.SessionSetting.AccessLevel sessionAccessLevel,
            byte kvc) {
        defaultKvc.put(sessionAccessLevel, kvc);
    }

    /**
     * Set the default key record number<br>
     *
     * @param sessionAccessLevel the session level
     * @param keyRecordNumber the desired default key record number
     * @since 0.9
     */
    public void setSessionDefaultKeyRecordNumber(
            PoTransaction.SessionSetting.AccessLevel sessionAccessLevel, byte keyRecordNumber) {
        defaultKeyRecordNumber.put(sessionAccessLevel, keyRecordNumber);
    }

    /**
     * Provides a list of authorized KVC
     *
     * If this method is not called, the list will remain empty and all KVCs will be accepted.
     *
     * @param authorizedKvcList the list of authorized KVCs
     */
    public void setSessionAuthorizedKvcList(List<Byte> authorizedKvcList) {
        this.authorizedKvcList = authorizedKvcList;
    }

    /**
     * (package-private)<br>
     * 
     * @return the Session Modification Mode
     * @since 0.9
     */
    PoTransaction.SessionSetting.ModificationMode getSessionModificationMode() {
        return sessionModificationMode;
    }

    /**
     * (package-private)<br>
     * 
     * @return the Ratification Mode
     * @since 0.9
     */
    PoTransaction.SessionSetting.RatificationMode getRatificationMode() {
        return ratificationMode;
    }

    /**
     * (package-private)<br>
     * 
     * @return the default session KIF
     * @since 0.9
     */
    Byte getSessionDefaultKif(PoTransaction.SessionSetting.AccessLevel sessionAccessLevel) {
        return defaultKif.get(sessionAccessLevel);
    }

    /**
     * (package-private)<br>
     * 
     * @return the default session KVC
     * @since 0.9
     */
    Byte getSessionDefaultKvc(PoTransaction.SessionSetting.AccessLevel sessionAccessLevel) {
        return defaultKvc.get(sessionAccessLevel);
    }

    /**
     * (package-private)<br>
     *
     * @return the default session key record number
     * @since 0.9
     */
    Byte getSessionDefaultKeyRecordNumber(
            PoTransaction.SessionSetting.AccessLevel sessionAccessLevel) {
        return defaultKeyRecordNumber.get(sessionAccessLevel);
    }

    /**
     * (package-private)<br>
     * Check if the provided kvc value is authorized or not.
     * <p>
     * If no list of authorized kvc is defined (authorizedKvcList null), all kvc are authorized.
     *
     * @param kvc to be tested
     * @return true if the kvc is authorized
     */
    public boolean isSessionKvcAuthorized(byte kvc) {
        return authorizedKvcList == null || authorizedKvcList.contains(kvc);
    }
}
