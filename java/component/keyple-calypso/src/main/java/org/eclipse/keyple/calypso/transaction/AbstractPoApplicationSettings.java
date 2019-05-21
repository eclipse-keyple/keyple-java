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

public abstract class AbstractPoApplicationSettings {
    private List<Byte> authorizedKifList;

    private List<Byte> authorizedKvcList;

    private String aid;

    private String poSerialNumberFilter;

    private class ApplicationKeysDescriptors {
        private EnumMap<PoTransaction.SessionAccessLevel, KeyDescriptor> applicationKeysDescriptors =
                new EnumMap<PoTransaction.SessionAccessLevel, KeyDescriptor>(
                        PoTransaction.SessionAccessLevel.class);

        /**
         * Default constructor
         */
        ApplicationKeysDescriptors() {}

        void setApplicationKeyDescriptor(PoTransaction.SessionAccessLevel sessionAccessLevel,
                KeyDescriptor applicationKeyDescriptor) {
            applicationKeysDescriptors.put(sessionAccessLevel, applicationKeyDescriptor);
        }

        KeyDescriptor getApplicationKeyDescriptor(
                PoTransaction.SessionAccessLevel sessionAccessLevel) {
            return applicationKeysDescriptors.get(sessionAccessLevel);
        }
    }

    private EnumMap<ApplicationType, ApplicationKeysDescriptors> keysDescriptors =
            new EnumMap<ApplicationType, ApplicationKeysDescriptors>(ApplicationType.class);;

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getAid() {
        return aid;
    }

    public void setPoSerialNumberFilter(String poSerialNumberFilter) {
        this.poSerialNumberFilter = poSerialNumberFilter;
    }

    public String getPoSerialNumberFilter() {
        return poSerialNumberFilter;
    }

    public void setKeyDescriptor(ApplicationType applicationType,
            PoTransaction.SessionAccessLevel sessionAccessLevel, KeyDescriptor keyDescriptor) {
        ApplicationKeysDescriptors applicationKeysDescriptors =
                keysDescriptors.get(applicationType);
        applicationKeysDescriptors.setApplicationKeyDescriptor(sessionAccessLevel, keyDescriptor);
        keysDescriptors.put(applicationType, applicationKeysDescriptors);
    }

    public KeyDescriptor getKeyDescriptor(ApplicationType applicationType,
            PoTransaction.SessionAccessLevel sessionAccessLevel) {
        return keysDescriptors.get(applicationType).getApplicationKeyDescriptor(sessionAccessLevel);
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
    public final void setAuthorizedKifList(List<Byte> authorizedKifList) {
        this.authorizedKifList = authorizedKifList;
    }

    /**
     * Check if the provided KIF value is authorized or not.
     * <p>
     * If no list of authorized KIF is defined (authorizedKifList null), all KIF are authorized.
     *
     * @param kif to be tested
     * @return true if the kif is authorized
     */
    public final boolean isAuthorizedKif(byte kif) {
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
    public final void setAuthorizedKvcList(List<Byte> authorizedKvcList) {
        this.authorizedKvcList = authorizedKvcList;
    }

    /**
     * Check if the provided defaultKvc value is authorized or not.
     * <p>
     * If no list of authorized defaultKvc is defined (authorizedKvcList null), all defaultKvc are
     * authorized.
     *
     * @param kvc to be tested
     * @return true if the kvc is authorized
     */
    public final boolean isAuthorizedKvc(byte kvc) {
        return authorizedKvcList == null || authorizedKvcList.contains(kvc);
    }
}
