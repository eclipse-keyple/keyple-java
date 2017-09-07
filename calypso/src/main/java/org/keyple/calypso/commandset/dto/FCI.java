
package org.keyple.calypso.commandset.dto;

import java.util.Arrays;

import org.keyple.calypso.utils.LogUtils;

/**
 * The Class FCI.
 * FCI: file control information
 */
public class FCI {
    
    /** The aid. */
    private AID aid;
    
    /** The fci proprietary template. */
    private byte[] fciProprietaryTemplate;
    
    /** The fci issuer discretionary data. */
    private byte[] fciIssuerDiscretionaryData;
    
    /** The application SN. */
    private byte[] applicationSN;
    
    /** The startup information. */
    private StartupInformation startupInformation;
    
    /** The msg info. */
    private String msgInfo;

    /**
     * Instantiates a new FCI.
     *
     * @param aid the aid
     * @param fciProprietaryTemplate the fci proprietary template
     * @param fciIssuerDiscretionaryData the fci issuer discretionary data
     * @param applicationSN the application SN
     * @param startupInformation the startup information
     */
    public FCI(AID aid, byte[] fciProprietaryTemplate, byte[] fciIssuerDiscretionaryData, byte[] applicationSN,
            StartupInformation startupInformation) {
        this.aid = aid;
        this.fciProprietaryTemplate = fciProprietaryTemplate;
        this.fciIssuerDiscretionaryData = fciIssuerDiscretionaryData;
        this.applicationSN = applicationSN;
        this.startupInformation = startupInformation;
    }

    /**
     * Gets the aid.
     *
     * @return the aid
     */
    public AID getAid() {
        return aid;
    }

    /**
     * Gets the fci proprietary template.
     *
     * @return the fci proprietary template
     */
    public byte[] getFciProprietaryTemplate() {
        return fciProprietaryTemplate;
    }

    /**
     * Gets the fci issuer discretionary data.
     *
     * @return the fci issuer discretionary data
     */
    public byte[] getFciIssuerDiscretionaryData() {
        return fciIssuerDiscretionaryData;
    }

    /**
     * Gets the application SN.
     *
     * @return the application SN
     */
    public byte[] getApplicationSN() {
        return applicationSN;
    }

    /**
     * Gets the startup information.
     *
     * @return the startup information
     */
    public StartupInformation getStartupInformation() {
        return startupInformation;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((aid == null) ? 0 : aid.hashCode());
        result = prime * result + Arrays.hashCode(applicationSN);
        result = prime * result + Arrays.hashCode(fciIssuerDiscretionaryData);
        result = prime * result + Arrays.hashCode(fciProprietaryTemplate);
        result = prime * result + ((startupInformation == null) ? 0 : startupInformation.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FCI other = (FCI) obj;
        if (aid == null) {
            if (other.aid != null)
                return false;
        } else if (!aid.equals(other.aid))
            return false;
        if (!Arrays.equals(applicationSN, other.applicationSN))
            return false;
        if (!Arrays.equals(fciIssuerDiscretionaryData, other.fciIssuerDiscretionaryData))
            return false;
        if (!Arrays.equals(fciProprietaryTemplate, other.fciProprietaryTemplate))
            return false;
        if (startupInformation == null) {
            if (other.startupInformation != null)
                return false;
        } else if (!startupInformation.equals(other.startupInformation))
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "FCI [aid=" + aid + ", fciProprietaryTemplate=" + LogUtils.hexaToString(fciProprietaryTemplate)
                + ", fciIssuerDiscretionaryData=" + LogUtils.hexaToString(fciIssuerDiscretionaryData)
                + ", applicationSN=" + LogUtils.hexaToString(applicationSN) + ", startupInformation="
                + startupInformation + "]";
    }

	/**
	 * Gets the msg info.
	 *
	 * @return the msg info
	 */
	public String getMsgInfo() {
		return msgInfo;
	}

	/**
	 * Sets the msg info.
	 *
	 * @param msgInfo the new msg info
	 */
	public void setMsgInfo(String msgInfo) {
		this.msgInfo = msgInfo;
	}
    
}