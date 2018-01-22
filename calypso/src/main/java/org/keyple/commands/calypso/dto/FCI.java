
package org.keyple.commands.calypso.dto;

/**
 * The Class FCI. FCI: file control information
 */
public class FCI {

    /** The DF Name. */
    private byte[] dfName;

    /** The fci proprietary template. */
    private byte[] fciProprietaryTemplate;

    /** The fci issuer discretionary data. */
    private byte[] fciIssuerDiscretionaryData;

    /** The application SN. */
    private byte[] applicationSN;

    /** The startup information. */
    private StartupInformation startupInformation;

    /**
     * Instantiates a new FCI.
     *
     * @param dfName
     *            the df name
     * @param fciProprietaryTemplate
     *            the fci proprietary template
     * @param fciIssuerDiscretionaryData
     *            the fci issuer discretionary data
     * @param applicationSN
     *            the application SN
     * @param startupInformation
     *            the startup information
     */
    public FCI(byte[] dfName, byte[] fciProprietaryTemplate, byte[] fciIssuerDiscretionaryData, byte[] applicationSN,
            StartupInformation startupInformation) {
        if (dfName != null) {
            this.dfName = dfName.clone();
        }

        this.fciProprietaryTemplate = (fciProprietaryTemplate == null ? null : fciProprietaryTemplate.clone());
        this.fciIssuerDiscretionaryData = (fciIssuerDiscretionaryData == null ? null
                : fciIssuerDiscretionaryData.clone());
        this.applicationSN = (applicationSN == null ? null : applicationSN.clone());
        this.startupInformation = startupInformation;
    }

    /**
     * Gets the fci proprietary template.
     *
     * @return the fci proprietary template
     */
    public byte[] getFciProprietaryTemplate() {
        return (this.fciProprietaryTemplate == null ? null : this.fciProprietaryTemplate.clone());
    }

    /**
     * Gets the fci issuer discretionary data.
     *
     * @return the fci issuer discretionary data
     */
    public byte[] getFciIssuerDiscretionaryData() {
        return (this.fciIssuerDiscretionaryData == null ? null : this.fciIssuerDiscretionaryData.clone());
    }

    /**
     * Gets the application SN.
     *
     * @return the application SN
     */
    public byte[] getApplicationSN() {
        return (this.applicationSN == null ? null : this.applicationSN.clone());
    }

    /**
     * Gets the startup information.
     *
     * @return the startup information
     */
    public StartupInformation getStartupInformation() {
        return this.startupInformation;
    }

    /**
     * Gets the DF Name.
     *
     * @return the DF name
     */
    public byte[] getDfName() {
        if (dfName != null) {
            return dfName.clone();
        } else {
            return new byte[0];
        }
    }

}