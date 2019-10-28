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

import org.eclipse.keyple.core.selection.SeResource;
import org.eclipse.keyple.core.seproxy.SeReader;

public class SamResource extends SeResource<CalypsoSam> {
    /** the free/busy enum status */
    public enum SamResourceStatus {
        FREE, BUSY;
    }

    /** the free/busy status of the resource */
    private SamResourceStatus samResourceStatus;

    /** the sam identifier */
    private SamIdentifier samIdentifier;

    /**
     * Constructor
     *
     * @param seReader the {@link SeReader} with which the SE is communicating
     * @param calypsoSam the {@link CalypsoSam} information structure
     */
    public SamResource(SeReader seReader, CalypsoSam calypsoSam) {
        super(seReader, calypsoSam);

        samResourceStatus = SamResourceStatus.FREE;
        samIdentifier = null;
    }

    /**
     * Indicates whether the SamResource is FREE or BUSY
     *
     * @return the busy status
     */
    public boolean isSamResourceFree() {
        return samResourceStatus.equals(SamResourceStatus.FREE);
    }

    /**
     * Defines the {@link SamIdentifier} of the current {@link SamResource}
     * 
     * @param samIdentifier the SAM identifier
     */
    public void setSamIdentifier(SamIdentifier samIdentifier) {
        this.samIdentifier = samIdentifier;
    }

    /**
     * Indicates whether the SamResource matches the provided SAM identifier.
     * <p>
     * The test includes the {@link org.eclipse.keyple.calypso.command.sam.SamRevision}, serial
     * number and group reference provided by the {@link SamIdentifier}.
     * <p>
     * The SAM serial number can be null or empty, in this case all serial numbers are accepted. It
     * can also be a regular expression target one or more specific serial numbers.
     * <p>
     * The groupe reference can be null or empty to let all group references match but not empty the
     * group reference must match the {@link SamIdentifier} to have the method returning true.
     *
     * @param samIdentifier the SAM identifier
     * @return true or false according to the result of the correspondence test
     */
    public boolean isSamMatching(SamIdentifier samIdentifier) {
        return samIdentifier.matches(this.samIdentifier);
    }

    /**
     * Sets the free/busy status of the SamResource
     *
     * @param samResourceStatus FREE/BUSY enum value
     */
    public void setSamResourceStatus(SamResourceStatus samResourceStatus) {
        this.samResourceStatus = samResourceStatus;
    }
}
