/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */

package org.eclipse.keyple.calypso.command.po.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.command.AbstractApduResponseParser;
import org.eclipse.keyple.seproxy.ApduResponse;

/**
 * This class provides status code properties and the getters to access to the structured fields of
 * data from response of a Get Data response.
 */
public class GetDataFciRespPars extends AbstractApduResponseParser {


    private static final Map<Integer, StatusProperties> STATUS_TABLE;

    static {
        Map<Integer, StatusProperties> m =
                new HashMap<Integer, StatusProperties>(AbstractApduResponseParser.STATUS_TABLE);
        m.put(0x6A88, new StatusProperties(false,
                "Data object not found (optional mode not available)."));
        m.put(0x6B00, new StatusProperties(false,
                "P1 or P2 value not supported (<>004fh, 0062h, 006Fh, 00C0h, 00D0h, 0185h and 5F52h, according to availabl optional modes)."));
        m.put(0x6283, new StatusProperties(true,
                "Successful execution, FCI request and DF is invalidated."));
        STATUS_TABLE = m;
    }

    private static final int[] bufferSizeIndicatorToBufferSize = new int[] {0, 0, 0, 0, 0, 0, 215,
            256, 304, 362, 430, 512, 608, 724, 861, 1024, 1217, 1448, 1722, 2048, 2435, 2896, 3444,
            4096, 4870, 5792, 6888, 8192, 9741, 11585, 13777, 16384, 19483, 23170, 27554, 32768,
            38967, 46340, 55108, 65536, 77935, 92681, 110217, 131072, 155871, 185363, 220435,
            262144, 311743, 370727, 440871, 524288, 623487, 741455, 881743, 1048576};

    @Override
    protected Map<Integer, StatusProperties> getStatusTable() {
        return STATUS_TABLE;
    }

    /** The fci. */
    private final FCI fci;

    /**
     * Instantiates a new PoFciRespPars.
     *
     * @param response the response from Get Data APDU commmand
     */
    public GetDataFciRespPars(ApduResponse response) {
        super(response);
        fci = isSuccessful() ? toFCI(response.getBytes()) : null;
    }

    public byte[] getDfName() {
        return fci != null ? fci.getDfName() : null;
    }

    public byte[] getApplicationSerialNumber() {
        return fci != null ? fci.getApplicationSN() : null;
    }

    public byte getBufferSizeIndicator() {
        return fci != null ? fci.getStartupInformation().getBufferSizeIndicator() : 0x00;
    }

    public int getBufferSizeValue() {
        return fci != null
                ? bufferSizeIndicatorToBufferSize[(int) fci.getStartupInformation()
                        .getBufferSizeIndicator()]
                : 0;
    }

    public byte getPlatformByte() {
        return fci != null ? fci.getStartupInformation().getPlatform() : 0x00;
    }

    public byte getApplicationTypeByte() {
        return fci != null ? fci.getStartupInformation().getApplicationType() : 0x00;
    }

    public boolean isRev3Compliant() {
        return fci != null;
    }

    public boolean isRev3_2ModeAvailable() {
        return fci != null && fci.getStartupInformation().hasCalypsoRev32modeAvailable();
    }

    public boolean isRatificationCommandRequired() {
        return fci != null && fci.getStartupInformation().hasRatificationCommandRequired();
    }

    public boolean hasCalypsoStoredValue() {
        return fci != null && fci.getStartupInformation().hasCalypsoStoreValue();
    }

    public boolean hasCalypsoPin() {
        return fci != null && fci.getStartupInformation().hasCalypsoPin();
    }

    public byte getApplicationSubtypeByte() {
        return fci != null ? fci.getStartupInformation().getApplicationSubtype() : 0x00;
    }

    public byte getSoftwareIssuerByte() {
        return fci != null ? fci.getStartupInformation().getSoftwareIssuer() : 0x00;
    }

    public byte getSoftwareVersionByte() {
        return fci != null ? fci.getStartupInformation().getSoftwareVersion() : 0x00;
    }

    public byte getSoftwareRevisionByte() {
        return fci != null ? fci.getStartupInformation().getSoftwareRevision() : 0x00;
    }

    public boolean isDfInvalidated() {
        return fci != null;
    }

    /**
     * The Class FCI. FCI: file control information
     */
    public static class FCI {

        /** The DF Name. */
        private final byte[] dfName;

        /** The fci proprietary template. */
        private final byte[] fciProprietaryTemplate;

        /** The fci issuer discretionary data. */
        private final byte[] fciIssuerDiscretionaryData;

        /** The application SN. */
        private final byte[] applicationSN;

        /** The startup information. */
        private final StartupInformation startupInformation;

        /**
         * Instantiates a new FCI.
         *
         * @param dfName the df name
         * @param fciProprietaryTemplate the fci proprietary template
         * @param fciIssuerDiscretionaryData the fci issuer discretionary data
         * @param applicationSN the application SN
         * @param startupInformation the startup information
         */
        public FCI(byte[] dfName, byte[] fciProprietaryTemplate, byte[] fciIssuerDiscretionaryData,
                byte[] applicationSN, StartupInformation startupInformation) {
            this.dfName = dfName;
            this.fciProprietaryTemplate = fciProprietaryTemplate;
            this.fciIssuerDiscretionaryData = fciIssuerDiscretionaryData;
            this.applicationSN = applicationSN;
            this.startupInformation = startupInformation;
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
            return this.startupInformation;
        }

        /**
         * Gets the DF Name.
         *
         * @return the DF name
         */
        public byte[] getDfName() {
            return dfName;
        }

    }

    /**
     * The Class StartupInformation. The Calypso applications return the Startup Information in the
     * answer to the Select Application command. The Startup Information contains several data
     * fields (applicationType,software issuer...)
     */
    public static class StartupInformation {

        /** The buffer size. */
        final byte bufferSizeIndicator;

        /** The platform. */
        final byte platform;

        /** The application type. */
        final byte applicationType;

        /** The application subtype. */
        final byte applicationSubtype;

        /** The software issuer. */
        final byte softwareIssuer;

        /** The software version. */
        final byte softwareVersion;

        /** The software revision. */
        final byte softwareRevision;

        /**
         * Instantiates a new StartupInformation.
         *
         * @param bufferSizeIndicator the buffer size indicator
         * @param platform the platform
         * @param applicationType the application type
         * @param applicationSubtype the application subtype
         * @param softwareIssuer the software issuer
         * @param softwareVersion the software version
         * @param softwareRevision the software revision
         */
        public StartupInformation(byte bufferSizeIndicator, byte platform, byte applicationType,
                byte applicationSubtype, byte softwareIssuer, byte softwareVersion,
                byte softwareRevision) {
            this.bufferSizeIndicator = bufferSizeIndicator;
            this.platform = platform;
            this.applicationType = applicationType;
            this.applicationSubtype = applicationSubtype;
            this.softwareIssuer = softwareIssuer;
            this.softwareVersion = softwareVersion;
            this.softwareRevision = softwareRevision;
        }

        public StartupInformation(byte[] buffer) {
            this.bufferSizeIndicator = buffer[0];
            this.platform = buffer[1];
            this.applicationType = buffer[2];
            this.applicationSubtype = buffer[3];
            this.softwareIssuer = buffer[4];
            this.softwareVersion = buffer[5];
            this.softwareRevision = buffer[6];
        }

        public static StartupInformation empty() {
            return new StartupInformation((byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                    (byte) 0, (byte) 0);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + applicationSubtype;
            result = prime * result + applicationType;
            result = prime * result + bufferSizeIndicator;
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
                            || (bufferSizeIndicator != other.bufferSizeIndicator)
                            || (platform != other.platform)
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
        public byte getBufferSizeIndicator() {
            return bufferSizeIndicator;
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
            return (this.applicationType & 0x01) != 0;
        }

        public boolean hasCalypsoStoreValue() {
            return (this.applicationType & 0x02) != 0;
        }

        public boolean hasRatificationCommandRequired() {
            return (this.applicationType & 0x04) != 0;
        }

        public boolean hasCalypsoRev32modeAvailable() {
            return (this.applicationType & 0x08) != 0;
        }

    }

    /**
     * Method to get the FCI from the response.
     *
     * @param apduResponse the apdu response
     * @return the FCI template TODO we should check here if the provided FCI data matches an
     *         Calypso PO FCI and return null if not
     */
    public static FCI toFCI(byte[] apduResponse) {
        StartupInformation startupInformation = StartupInformation.empty();
        byte firstResponseApdubyte = apduResponse[0];
        byte[] dfName = null;
        byte[] fciProprietaryTemplate = null;
        byte[] fciIssuerDiscretionaryData = null;
        byte[] applicationSN = null;
        byte[] discretionaryData;

        if ((byte) 0x6F == firstResponseApdubyte) {
            int aidLength = apduResponse[3];
            int fciTemplateLength = apduResponse[5 + aidLength];
            int fixedPartOfFciTemplate = fciTemplateLength - 22;
            int firstbyteAid = 6 + aidLength + fixedPartOfFciTemplate;
            int fciIssuerDiscretionaryDataLength =
                    apduResponse[8 + aidLength + fixedPartOfFciTemplate];
            int firstbyteFciIssuerDiscretionaryData = 9 + aidLength + fixedPartOfFciTemplate;
            int applicationSNLength = apduResponse[10 + aidLength + fixedPartOfFciTemplate];
            int firstbyteApplicationSN = 11 + aidLength + fixedPartOfFciTemplate;
            int discretionaryDataLength = apduResponse[20 + aidLength + fixedPartOfFciTemplate];
            int firstbyteDiscretionaryData = 21 + aidLength + fixedPartOfFciTemplate;

            if ((byte) 0x84 == apduResponse[2]) {
                dfName = Arrays.copyOfRange(apduResponse, 4, 4 + aidLength);
            }

            if ((byte) 0xA5 == apduResponse[4 + aidLength]) {
                fciProprietaryTemplate = Arrays.copyOfRange(apduResponse, firstbyteAid,
                        firstbyteAid + fciTemplateLength);
            }

            if ((byte) 0xBF == apduResponse[6 + aidLength + fixedPartOfFciTemplate]
                    && ((byte) 0x0C == apduResponse[7 + aidLength + fixedPartOfFciTemplate])) {
                fciIssuerDiscretionaryData = Arrays.copyOfRange(apduResponse,
                        firstbyteFciIssuerDiscretionaryData,
                        firstbyteFciIssuerDiscretionaryData + fciIssuerDiscretionaryDataLength);
            }

            if ((byte) 0xC7 == apduResponse[9 + aidLength + fixedPartOfFciTemplate]) {
                applicationSN = Arrays.copyOfRange(apduResponse, firstbyteApplicationSN,
                        firstbyteApplicationSN + applicationSNLength);
            }

            if ((byte) 0x53 == apduResponse[19 + aidLength + fixedPartOfFciTemplate]) {
                discretionaryData = Arrays.copyOfRange(apduResponse, firstbyteDiscretionaryData,
                        firstbyteDiscretionaryData + discretionaryDataLength);
                startupInformation = new StartupInformation(discretionaryData);
                /*
                 * startupInformation = new StartupInformation(discretionaryData.get(0),
                 * discretionaryData.get(1), discretionaryData.get(2), discretionaryData.get(3),
                 * discretionaryData.get(4), discretionaryData.get(5), discretionaryData.get(6));
                 */
            }
        }

        return new FCI(dfName, fciProprietaryTemplate, fciIssuerDiscretionaryData, applicationSN,
                startupInformation);
    }
}
