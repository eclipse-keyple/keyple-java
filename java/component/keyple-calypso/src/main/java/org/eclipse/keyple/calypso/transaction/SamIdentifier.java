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

import static org.eclipse.keyple.calypso.command.sam.SamRevision.AUTO;
import java.util.regex.Pattern;
import org.eclipse.keyple.calypso.command.sam.SamRevision;

/**
 * Holds the needed data to proceed a SAM selection.
 * <p>
 * SAM Revision (see {@link SamRevision})
 * <p>
 * Serial Number (may be a regular expression)
 * <p>
 * Group reference (key group reference)
 */
public class SamIdentifier {
    SamRevision samRevision;
    String serialNumber;
    String groupReference;

    /**
     * Constructor for a SamIdentifier
     * 
     * @param samRevision the SAM revision
     * @param serialNumber the SAM serial number as an hex string or a regular expression
     * @param groupReference the group reference string
     */
    public SamIdentifier(SamRevision samRevision, String serialNumber, String groupReference) {
        this.samRevision = samRevision;
        this.serialNumber = serialNumber;
        this.groupReference = groupReference;
    }

    /**
     * @return the SAM revision
     */
    public SamRevision getSamRevision() {
        return samRevision;
    }

    /**
     * @return the SAM serial number
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * @return the group reference
     */
    public String getGroupReference() {
        return groupReference;
    }

    /**
     * Compare two SamIdentifiers with the following rules:
     * <ul>
     * <li>when the provided {@link SamIdentifier} is null the result is true</li>
     * <li>when the provided {@link SamIdentifier} is not null
     * <ul>
     * <li>the AUTO revision matches any revision</li>
     * <li>if not null, the serial number is used as a regular expression to check the current
     * serial number</li>
     * <li>if not null the group reference is compared as a string</li>
     * </ul>
     * </li>
     * </ul>
     *
     * @param samIdentifier the {@link SamIdentifier} object to be compared to the current object
     * @return true if the identifier provided matches the current identifier
     */
    public boolean matches(SamIdentifier samIdentifier) {
        if (samIdentifier == null) {
            return true;
        }
        if (samIdentifier.getSamRevision() != AUTO
                && samIdentifier.getSamRevision() != samRevision) {
            return false;
        }
        if (samIdentifier.getSerialNumber() != null && !samIdentifier.getSerialNumber().isEmpty()) {
            Pattern p = Pattern.compile(samIdentifier.getSerialNumber());
            if (!p.matcher(serialNumber).matches()) {
                return false;
            }
        }
        if (samIdentifier.getGroupReference() != null
                && !samIdentifier.getGroupReference().equals(groupReference)) {
            return false;
        }
        return true;
    }
}
