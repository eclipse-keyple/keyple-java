/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.dto;

/**
 * The Class StartupInformation. The Calypso applications return the Startup Information in the
 * answer to the Select Application command. The Startup Information contains several data fields
 * (applicationType,software issuer...)
 */
public class StartupInformation {

    /** The buffer size. */
    byte bufferSize;

    /** The platform. */
    byte platform;

    /** The application type. */
    byte applicationType;

    /** The application subtype. */
    byte applicationSubtype;

    /** The software issuer. */
    byte softwareIssuer;

    /** The software version. */
    byte softwareVersion;

    /** The software revision. */
    byte softwareRevision;

    /**
     * Instantiates a new StartupInformation.
     *
     * @param bufferSize the buffer size
     * @param platform the platform
     * @param applicationType the application type
     * @param applicationSubtype the application subtype
     * @param softwareIssuer the software issuer
     * @param softwareVersion the software version
     * @param softwareRevision the software revision
     */
    public StartupInformation(byte bufferSize, byte platform, byte applicationType,
            byte applicationSubtype, byte softwareIssuer, byte softwareVersion,
            byte softwareRevision) {
        this.bufferSize = bufferSize;
        this.platform = platform;
        this.applicationType = applicationType;
        this.applicationSubtype = applicationSubtype;
        this.softwareIssuer = softwareIssuer;
        this.softwareVersion = softwareVersion;
        this.softwareRevision = softwareRevision;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + applicationSubtype;
        result = prime * result + applicationType;
        result = prime * result + bufferSize;
        result = prime * result + platform;
        result = prime * result + softwareIssuer;
        result = prime * result + softwareRevision;
        result = prime * result + softwareVersion;
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        boolean isEquals = false;

        if (this == obj) {
            isEquals = true;
        }

        if (obj != null) {
            StartupInformation other;
            if (getClass() == obj.getClass()) {
                other = (StartupInformation) obj;
                if ((applicationSubtype != other.applicationSubtype)
                        || (applicationType != other.applicationType)
                        || (bufferSize != other.bufferSize) || (platform != other.platform)
                        || (softwareIssuer != other.softwareIssuer)
                        || (softwareRevision != other.softwareRevision)
                        || (softwareVersion != other.softwareVersion)) {
                    isEquals = false;
                }
            } else {
                isEquals = false;
            }

        } else {
            isEquals = false;
        }

        return isEquals;
    }

    /**
     * Gets the buffer size.
     *
     * @return the buffer size
     */
    public byte getBufferSize() {
        return bufferSize;
    }

    /**
     * Gets the platform.
     *
     * @return the platform
     */
    public byte getPlatform() {
        return platform;
    }

    /**
     * Gets the application type.
     *
     * @return the application type
     */
    public byte getApplicationType() {
        return applicationType;
    }

    /**
     * Gets the application subtype.
     *
     * @return the application subtype
     */
    public byte getApplicationSubtype() {
        return applicationSubtype;
    }

    /**
     * Gets the software issuer.
     *
     * @return the software issuer
     */
    public byte getSoftwareIssuer() {
        return softwareIssuer;
    }

    /**
     * Gets the software version.
     *
     * @return the software version
     */
    public byte getSoftwareVersion() {
        return softwareVersion;
    }

    /**
     * Gets the software revision.
     *
     * @return the software revision
     */
    public byte getSoftwareRevision() {
        return softwareRevision;
    }

    public boolean hasCalypsoPin() {
        byte mask = 0x01;
        return (this.applicationType & mask) == mask;
    }

    public boolean hasCalypsoStoreValue() {
        byte mask = 0x02;
        return (this.applicationType & mask) == mask;
    }

    public boolean hasRatificationCommandRequired() {
        byte mask = 0x04;
        return (this.applicationType & mask) == mask;
    }

    public boolean hasCalypsoRev32modeAvailable() {
        byte mask = 0x08;
        return (this.applicationType & mask) == mask;
    }

}
