/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.calypso.commands.po.parser;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.keyple.commands.AbstractApduResponseParser;
import org.keyple.seproxy.ApduResponse;
import org.keyple.seproxy.ByteBufferUtils;

/**
 * This class provides status code properties and the getters to access to the structured fields of
 * data from response of a Get Data response.
 *
 * @author Ixxi
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
        fci = isSuccessful() ? toFCI(response.getBuffer()) : null;
    }

    public ByteBuffer getDfName() {
        return fci != null ? fci.getDfName() : null;
    }

    public ByteBuffer getApplicationSerialNumber() {
        return fci != null ? fci.getApplicationSN() : null;
    }

    public byte getBufferSizeByte() {
        return fci != null ? fci.getStartupInformation().getBufferSize() : 0x00;
    }

    public int getBufferSizeValue() {
        return fci != null ? (int) fci.getStartupInformation().getBufferSize() : 0;
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
        private ByteBuffer dfName;

        /** The fci proprietary template. */
        private ByteBuffer fciProprietaryTemplate;

        /** The fci issuer discretionary data. */
        private ByteBuffer fciIssuerDiscretionaryData;

        /** The application SN. */
        private ByteBuffer applicationSN;

        /** The startup information. */
        private StartupInformation startupInformation;

        /**
         * Instantiates a new FCI.
         *
         * @param dfName the df name
         * @param fciProprietaryTemplate the fci proprietary template
         * @param fciIssuerDiscretionaryData the fci issuer discretionary data
         * @param applicationSN the application SN
         * @param startupInformation the startup information
         */
        public FCI(ByteBuffer dfName, ByteBuffer fciProprietaryTemplate,
                ByteBuffer fciIssuerDiscretionaryData, ByteBuffer applicationSN,
                StartupInformation startupInformation) {
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
        public ByteBuffer getFciProprietaryTemplate() {
            return fciProprietaryTemplate;
        }

        /**
         * Gets the fci issuer discretionary data.
         *
         * @return the fci issuer discretionary data
         */
        public ByteBuffer getFciIssuerDiscretionaryData() {
            return fciIssuerDiscretionaryData;
        }

        /**
         * Gets the application SN.
         *
         * @return the application SN
         */
        public ByteBuffer getApplicationSN() {
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
        public ByteBuffer getDfName() {
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

        public StartupInformation(ByteBuffer buffer) {
            this.bufferSize = buffer.get();
            this.platform = buffer.get();
            this.applicationType = buffer.get();
            this.applicationSubtype = buffer.get();
            this.softwareIssuer = buffer.get();
            this.softwareVersion = buffer.get();
            this.softwareRevision = buffer.get();
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
     * @return the FCI template
     */
    public static FCI toFCI(ByteBuffer apduResponse) {
        StartupInformation startupInformation = StartupInformation.empty();
        byte firstResponseApdubyte = apduResponse.get(0);
        ByteBuffer dfName = null;
        ByteBuffer fciProprietaryTemplate = null;
        ByteBuffer fciIssuerDiscretionaryData = null;
        ByteBuffer applicationSN = null;
        ByteBuffer discretionaryData;

        if ((byte) 0x6F == firstResponseApdubyte) {
            int aidLength = apduResponse.get(3);
            int fciTemplateLength = apduResponse.get(5 + aidLength);
            int fixedPartOfFciTemplate = fciTemplateLength - 22;
            int firstbyteAid = 6 + aidLength + fixedPartOfFciTemplate;
            int fciIssuerDiscretionaryDataLength =
                    apduResponse.get(8 + aidLength + fixedPartOfFciTemplate);
            int firstbyteFciIssuerDiscretionaryData = 9 + aidLength + fixedPartOfFciTemplate;
            int applicationSNLength = apduResponse.get(10 + aidLength + fixedPartOfFciTemplate);
            int firstbyteApplicationSN = 11 + aidLength + fixedPartOfFciTemplate;
            int discretionaryDataLength = apduResponse.get(20 + aidLength + fixedPartOfFciTemplate);
            int firstbyteDiscretionaryData = 21 + aidLength + fixedPartOfFciTemplate;

            if ((byte) 0x84 == apduResponse.get(2)) {
                dfName = ByteBufferUtils.subIndex(apduResponse, 4, 4 + aidLength);
            }

            if ((byte) 0xA5 == apduResponse.get(4 + aidLength)) {
                fciProprietaryTemplate = ByteBufferUtils.subIndex(apduResponse, firstbyteAid,
                        firstbyteAid + fciTemplateLength);
            }

            if ((byte) 0xBF == apduResponse.get(6 + aidLength + fixedPartOfFciTemplate)
                    && ((byte) 0x0C == apduResponse.get(7 + aidLength + fixedPartOfFciTemplate))) {
                fciIssuerDiscretionaryData = ByteBufferUtils.subIndex(apduResponse,
                        firstbyteFciIssuerDiscretionaryData,
                        firstbyteFciIssuerDiscretionaryData + fciIssuerDiscretionaryDataLength);
            }

            if ((byte) 0xC7 == apduResponse.get(9 + aidLength + fixedPartOfFciTemplate)) {
                applicationSN = ByteBufferUtils.subIndex(apduResponse, firstbyteApplicationSN,
                        firstbyteApplicationSN + applicationSNLength);
            }

            if ((byte) 0x53 == apduResponse.get(19 + aidLength + fixedPartOfFciTemplate)) {
                discretionaryData =
                        ByteBufferUtils.subIndex(apduResponse, firstbyteDiscretionaryData,
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
