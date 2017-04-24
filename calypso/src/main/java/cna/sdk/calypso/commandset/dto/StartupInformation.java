package cna.sdk.calypso.commandset.dto;

import cna.sdk.calypso.utils.LogUtils;

/**
 * The Class StartupInformation.
 * The Calypso applications return the Startup Information in the answer to the Select Application command. The Startup Information contains several data fields
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
    public StartupInformation(byte bufferSize, byte platform, byte applicationType, byte applicationSubtype,
            byte softwareIssuer, byte softwareVersion, byte softwareRevision) {
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StartupInformation other = (StartupInformation) obj;
        if (applicationSubtype != other.applicationSubtype)
            return false;
        if (applicationType != other.applicationType)
            return false;
        if (bufferSize != other.bufferSize)
            return false;
        if (platform != other.platform)
            return false;
        if (softwareIssuer != other.softwareIssuer)
            return false;
        if (softwareRevision != other.softwareRevision)
            return false;
        if (softwareVersion != other.softwareVersion)
            return false;
        return true;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "StartupInformation [bufferSize=" + LogUtils.hexaToString(bufferSize) + ", platform="
                + LogUtils.hexaToString(platform) + ", applicationType=" + LogUtils.hexaToString(applicationType)
                + ", applicationSubtype=" + LogUtils.hexaToString(applicationSubtype) + ", softwareIssuer="
                + LogUtils.hexaToString(softwareIssuer) + ", softwareVersion=" + LogUtils.hexaToString(softwareVersion)
                + ", softwareRevision=" + LogUtils.hexaToString(softwareRevision) + "]";
    }


}
