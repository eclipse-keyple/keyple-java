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

import static org.eclipse.keyple.calypso.transaction.PoTransaction.SessionSetting.*;
import java.util.EnumMap;
import java.util.List;
import org.eclipse.keyple.core.selection.SeResource;


/**
 * A class dedicated to managing the security settings involved in managing secure sessions.
 * <p>
 * The object provides default values when instantiated, they can be modified with the putKeyInfo
 * method.
 * <p>
 * The getKeyInfo method returns the specified setting value.
 */
public class PoSecuritySettings {
    private final SeResource<CalypsoSam> samResource;
    /** List of authorized KVCs */
    private final List<Byte> authorizedKvcList;

    /** EnumMap associating session levels and corresponding KIFs */
    private final EnumMap<AccessLevel, Byte> defaultKif;
    private final EnumMap<AccessLevel, Byte> defaultKvc;
    private final EnumMap<AccessLevel, Byte> defaultKeyRecordNumber;

    private final ModificationMode sessionModificationMode;
    private final RatificationMode ratificationMode;

    public static final ModificationMode defaultSessionModificationMode = ModificationMode.ATOMIC;
    public static final RatificationMode defaultRatificationMode = RatificationMode.CLOSE_RATIFIED;

    /** Private constructor */
    private PoSecuritySettings(PoSecuritySettingsBuilder builder) {
        this.samResource = builder.samResource;
        this.authorizedKvcList = builder.authorizedKvcList;
        this.defaultKif = builder.defaultKif;
        this.defaultKvc = builder.defaultKvc;
        this.defaultKeyRecordNumber = builder.defaultKeyRecordNumber;
        this.sessionModificationMode = builder.sessionModificationMode;
        this.ratificationMode = builder.ratificationMode;
    }

    /**
     * Builder pattern
     */
    public static final class PoSecuritySettingsBuilder {
        private final SeResource<CalypsoSam> samResource;
        /** List of authorized KVCs */
        private List<Byte> authorizedKvcList;

        /** EnumMap associating session levels and corresponding KIFs */
        private final EnumMap<AccessLevel, Byte> defaultKif =
                new EnumMap<AccessLevel, Byte>(AccessLevel.class);
        private final EnumMap<AccessLevel, Byte> defaultKvc =
                new EnumMap<AccessLevel, Byte>(AccessLevel.class);
        private final EnumMap<AccessLevel, Byte> defaultKeyRecordNumber =
                new EnumMap<AccessLevel, Byte>(AccessLevel.class);

        ModificationMode sessionModificationMode = defaultSessionModificationMode;
        RatificationMode ratificationMode = defaultRatificationMode;

        /**
         * Constructor
         * 
         * @param samResource the SAM resource we'll be working with<br>
         *        Needed in any cases.
         */
        public PoSecuritySettingsBuilder(SeResource<CalypsoSam> samResource) {
            if (samResource == null) {
                throw new IllegalStateException("ManagedSamResource cannot be null.");
            }
            this.samResource = samResource;
        }

        /**
         * Set the Session Modification Mode<br>
         * The default value is ATOMIC
         * 
         * @param sessionModificationMode the desired Session Modification Mode
         * @return the builder instance
         * @since 0.9
         */
        public PoSecuritySettingsBuilder sessionModificationMode(
                ModificationMode sessionModificationMode) {
            this.sessionModificationMode = sessionModificationMode;
            return this;
        }

        /**
         * Set the Ratification Mode<br>
         * The default value is CLOSE_RATIFIED
         * 
         * @param ratificationMode the desired Ratification Mode
         * @return the builder instance
         * @since 0.9
         */
        public PoSecuritySettingsBuilder ratificationMode(RatificationMode ratificationMode) {
            this.ratificationMode = ratificationMode;
            return this;
        }

        /**
         * Set the default KIF<br>
         *
         * @param sessionAccessLevel the session level
         * @param kif the desired default KIF
         * @return the builder instance
         * @since 0.9
         */
        public PoSecuritySettingsBuilder sessionDefaultKif(AccessLevel sessionAccessLevel,
                byte kif) {
            defaultKif.put(sessionAccessLevel, kif);
            return this;
        }

        /**
         * Set the default KVC<br>
         *
         * @param sessionAccessLevel the session level
         * @param kvc the desired default KVC
         * @return the builder instance
         * @since 0.9
         */
        public PoSecuritySettingsBuilder sessionDefaultKvc(AccessLevel sessionAccessLevel,
                byte kvc) {
            defaultKvc.put(sessionAccessLevel, kvc);
            return this;
        }

        /**
         * Set the default key record number<br>
         *
         * @param sessionAccessLevel the session level
         * @param keyRecordNumber the desired default key record number
         * @return the builder instance
         * @since 0.9
         */
        public PoSecuritySettingsBuilder sessionDefaultKeyRecordNumber(
                AccessLevel sessionAccessLevel, byte keyRecordNumber) {
            defaultKeyRecordNumber.put(sessionAccessLevel, keyRecordNumber);
            return this;
        }

        /**
         * Provides a list of authorized KVC
         *
         * If this method is not called, the list will remain empty and all KVCs will be accepted.
         *
         * @param authorizedKvcList the list of authorized KVCs
         * @return the builder instance
         */
        public PoSecuritySettingsBuilder sessionAuthorizedKvcList(List<Byte> authorizedKvcList) {
            this.authorizedKvcList = authorizedKvcList;
            return this;
        }

        /**
         * Build a new {@code PoSecuritySettings}.
         *
         * @return a new instance
         */
        public PoSecuritySettings build() {
            return new PoSecuritySettings(this);
        }
    }

    /**
     * (package-private)<br>
     *
     * @return the Sam resource
     * @since 0.9
     */
    SeResource<CalypsoSam> getSamResource() {
        return samResource;
    }

    /**
     * (package-private)<br>
     * 
     * @return the Session Modification Mode
     * @since 0.9
     */
    ModificationMode getSessionModificationMode() {
        return sessionModificationMode;
    }

    /**
     * (package-private)<br>
     * 
     * @return the Ratification Mode
     * @since 0.9
     */
    RatificationMode getRatificationMode() {
        return ratificationMode;
    }

    /**
     * (package-private)<br>
     * 
     * @return the default session KIF
     * @since 0.9
     */
    Byte getSessionDefaultKif(AccessLevel sessionAccessLevel) {
        return defaultKif.get(sessionAccessLevel);
    }

    /**
     * (package-private)<br>
     * 
     * @return the default session KVC
     * @since 0.9
     */
    Byte getSessionDefaultKvc(AccessLevel sessionAccessLevel) {
        return defaultKvc.get(sessionAccessLevel);
    }

    /**
     * (package-private)<br>
     *
     * @return the default session key record number
     * @since 0.9
     */
    Byte getSessionDefaultKeyRecordNumber(AccessLevel sessionAccessLevel) {
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
