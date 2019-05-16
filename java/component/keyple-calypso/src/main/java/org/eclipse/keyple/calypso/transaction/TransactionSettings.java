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
import org.eclipse.keyple.calypso.transaction.exception.KeypleCalypsoSecureSessionUnauthorizedKvcException;

/**
 * A class dedicated to managing the security settings involved in managing secure sessions.
 * <p>
 * The object provides default values when instantiated, they can be modified with the putKeyInfo
 * method.
 * <p>
 * The getKeyInfo method returns the specified setting value.
 */
public class TransactionSettings {
    private final SamResource samResource;
    private String poSerialNumberFilter = null;

    /**
     * List of key information that can be provided when the secure session is created.
     */
    public enum DefaultKeyInfo {
        /** Session Access Level used for personalization purposes. */
        SESSION_LVL_PERSO((byte) 0x21, (byte) 0x00, (byte) 0x00),
        /** Session Access Level used for reloading purposes. */
        SESSION_LVL_LOAD((byte) 0x27, (byte) 0x01, (byte) 0x00),
        /** Session Access Level used for validating and debiting purposes. */
        SESSION_LVL_DEBIT((byte) 0x30, (byte) 0x02, (byte) 0x00);

        private byte defaultKif;
        private byte defaultKvc;
        private byte defaultKeyRecordNumber;

        DefaultKeyInfo(byte defaultKif, byte defaultKvc, byte defaultKeyRecordNumber) {
            this.defaultKif = defaultKif;
            this.defaultKvc = defaultKvc;
            this.defaultKeyRecordNumber = defaultKeyRecordNumber;
        }

        byte getDefaultKif() {
            return defaultKif;
        }

        void setDefaultKif(byte defaultKif) {
            this.defaultKif = defaultKif;
        }

        byte getDefaultKvc() {
            return defaultKvc;
        }

        void setDefaultKvc(byte defaultKvc) {
            this.defaultKvc = defaultKvc;
        }

        void setDefaultKeyRecordNumber(byte defaultKeyRecordNumber) {
            this.defaultKeyRecordNumber = defaultKeyRecordNumber;
        }

        byte getDefaultKeyRecordNumber() {
            return defaultKeyRecordNumber;
        }
    }

    /** List of authorized KIFs */
    private List<Byte> authorizedKifList;

    /** List of authorized KVCs */
    private List<Byte> authorizedKvcList;

    /** Enummap containing the key information */
    private final EnumMap<PoTransaction.SessionAccessLevel, DefaultKeyInfo> keySettings =
            new EnumMap<PoTransaction.SessionAccessLevel, DefaultKeyInfo>(
                    PoTransaction.SessionAccessLevel.class);

    /**
     * Constructor.
     * <p>
     * Initialize default values
     */
    public TransactionSettings() {
        this(null);
    }

    public TransactionSettings(SamResource samResource) {
        /* initialize association map */
        keySettings.put(PoTransaction.SessionAccessLevel.SESSION_LVL_PERSO,
                DefaultKeyInfo.SESSION_LVL_PERSO);
        keySettings.put(PoTransaction.SessionAccessLevel.SESSION_LVL_LOAD,
                DefaultKeyInfo.SESSION_LVL_LOAD);
        keySettings.put(PoTransaction.SessionAccessLevel.SESSION_LVL_DEBIT,
                DefaultKeyInfo.SESSION_LVL_DEBIT);
        this.samResource = samResource;
    }

    /**
     * Associates the provided KIF with the specified key information.
     *
     * @param sessionAccessLevel the {@link PoTransaction.SessionAccessLevel} with which the
     *        specified KIF is to be associated
     * @param kif - the KIF to be associated with the specified key
     * @return the previous value associated with specified key, or null if there was no mapping for
     *         key.
     */
    void setDefaultKif(PoTransaction.SessionAccessLevel sessionAccessLevel, Byte kif) {
        keySettings.get(sessionAccessLevel).setDefaultKif(kif);
    }

    /**
     * Returns the default KIF associated with the provided DefaultKeyInfo.
     * 
     * @param sessionAccessLevel the {@link PoTransaction.SessionAccessLevel} whose associated KIF
     *        is to be returned
     * @return the KIF value
     */
    byte getDefaultKif(PoTransaction.SessionAccessLevel sessionAccessLevel) {
        return keySettings.get(sessionAccessLevel).getDefaultKif();
    }

    /**
     * Associates the provided KVC with the specified key information.
     *
     * @param sessionAccessLevel the {@link PoTransaction.SessionAccessLevel} with which the
     *        specified KIF is to be associated
     * @param kvc - the KVC to be associated with the specified key
     * @return the previous value associated with specified key, or null if there was no mapping for
     *         key.
     */
    void setDefaultKvc(PoTransaction.SessionAccessLevel sessionAccessLevel, Byte kvc) {
        keySettings.get(sessionAccessLevel).setDefaultKvc(kvc);
    }

    /**
     * Returns the KVC associated with the provided DefaultKeyInfo.
     *
     * @param sessionAccessLevel the {@link PoTransaction.SessionAccessLevel} whose associated KIF
     *        is to be returned
     * @return the KVC value
     */
    byte getDefaultKvc(PoTransaction.SessionAccessLevel sessionAccessLevel) {
        return keySettings.get(sessionAccessLevel).getDefaultKvc();
    }

    /**
     * Associates the provided key record number with the specified key information.
     *
     * @param sessionAccessLevel the {@link PoTransaction.SessionAccessLevel} with which the
     *        specified key record number is to be associated
     * @param keyRecordNumber the key record number
     */
    void setDefaultKeyRecordNumber(PoTransaction.SessionAccessLevel sessionAccessLevel,
            Byte keyRecordNumber) {
        keySettings.get(sessionAccessLevel).setDefaultKeyRecordNumber(keyRecordNumber);
    }

    /**
     * Returns the Key record number associated with the provided DefaultKeyInfo.
     *
     * @param sessionAccessLevel the {@link PoTransaction.SessionAccessLevel} whose associated KIF
     *        is to be returned
     * @return the KVC value
     */
    byte getDefaultKeyRecordNumber(PoTransaction.SessionAccessLevel sessionAccessLevel) {
        return keySettings.get(sessionAccessLevel).getDefaultKeyRecordNumber();
    }

    /**
     * Provides a list of authorized KIF
     *
     * If this method is not called, the list will remain empty and all KIFs will be accepted.
     *
     * If a list is provided and a PO with a KIF not belonging to this list is presented, a
     * {@link KeypleCalypsoSecureSessionUnauthorizedKvcException} will be raised.
     *
     * @param authorizedKifList the list of authorized KIFs
     */
    public void setAuthorizedKifList(List<Byte> authorizedKifList) {
        this.authorizedKifList = authorizedKifList;
    }

    /**
     * CHeck if the provided KIF value is authorized or not.
     * <p>
     * If no list of authorized KIF is defined (authorizedKifList null), all KIF are authorized.
     *
     * @param kif to be tested
     * @return true if the kif is authorized
     */
    public boolean isAuthorizedKif(byte kif) {
        return authorizedKifList == null || authorizedKifList.contains(kif);
    }

    /**
     * Provides a list of authorized KVC
     *
     * If this method is not called, the list will remain empty and all KVCs will be accepted.
     *
     * If a list is provided and a PO with a KVC not belonging to this list is presented, a
     * {@link KeypleCalypsoSecureSessionUnauthorizedKvcException} will be raised.
     *
     * @param authorizedKvcList the list of authorized KVCs
     */
    public void setAuthorizedKvcList(List<Byte> authorizedKvcList) {
        this.authorizedKvcList = authorizedKvcList;
    }

    /**
     * CHeck if the provided defaultKvc value is authorized or not.
     * <p>
     * If no list of authorized defaultKvc is defined (authorizedKvcList null), all defaultKvc are
     * authorized.
     * 
     * @param kvc to be tested
     * @return true if the kvc is authorized
     */
    public boolean isAuthorizedKvc(byte kvc) {
        return authorizedKvcList == null || authorizedKvcList.contains(kvc);
    }

    /**
     * Set the PO filter.
     * 
     * @param poSerialNumberFilter regular expression used to check the PO serial number during a
     *        PoTransaction
     */
    public void setPoSerialNumberFilter(String poSerialNumberFilter) {
        this.poSerialNumberFilter = poSerialNumberFilter;
    }

    /**
     * Get the PO filter.
     * <p>
     * 
     * @return String regular expression used to check the PO serial number during a PoTransaction.
     */
    public String getPoSerialNumberFilter() {
        return poSerialNumberFilter;
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
