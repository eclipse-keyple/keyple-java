/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.seproxy;

import java.util.*;
import java.util.regex.Pattern;
import org.eclipse.keyple.core.seproxy.protocol.SeProtocol;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SeSelector class groups the information and methods used to select a particular secure
 * element
 */
public class SeSelector {
    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(SeSelector.class);

    private final SeProtocol seProtocol;
    private final AidSelector aidSelector;
    private final AtrFilter atrFilter;

    /**
     * - AID’s bytes of the SE application to select. In case the SE application is currently not
     * selected, a logical channel is established and the corresponding SE application is selected
     * by the SE reader, otherwise keep the current channel. - optional {@link FileOccurrence} and
     * {@link FileControlInformation} defines selections modes according to ISO7816-4 - optional
     * successfulSelectionStatusCodes define a list of accepted SW1SW2 codes (in addition to 9000).
     * Allows, for example, to manage the selection of the invalidated cards. - AidSelector could be
     * missing in SeSelector when operating SE which don’t support the Select Application command
     * (as it is the case for SAM).
     */
    public static final class AidSelector {
        public static final int AID_MIN_LENGTH = 5;
        public static final int AID_MAX_LENGTH = 16;

        /**
         * FileOccurrence indicates how to carry out the file occurrence in accordance with
         * ISO7816-4
         * <p>
         * The getIsoBitMask method provides the bit mask to be used to set P2 in the select command
         * (ISO/IEC 7816-4.2)
         */
        public enum FileOccurrence {

            FIRST((byte) 0x00), LAST((byte) 0x01), NEXT((byte) 0x02), PREVIOUS((byte) 0x03);

            private final byte isoBitMask;

            FileOccurrence(byte isoBitMask) {
                this.isoBitMask = isoBitMask;
            }

            public byte getIsoBitMask() {
                return isoBitMask;
            }
        }

        /**
         * FileControlInformation indicates how to which template is expected in accordance with
         * ISO7816-4
         * <p>
         * The getIsoBitMask method provides the bit mask to be used to set P2 in the select command
         * (ISO/IEC 7816-4.2)
         */
        public enum FileControlInformation {
            FCI(((byte) 0x00)), FCP(((byte) 0x04)), FMD(((byte) 0x08)), NO_RESPONSE(((byte) 0x0C));

            private final byte isoBitMask;

            FileControlInformation(byte isoBitMask) {
                this.isoBitMask = isoBitMask;
            }

            public byte getIsoBitMask() {
                return isoBitMask;
            }
        }

        private final FileOccurrence fileOccurrence;
        private final FileControlInformation fileControlInformation;

        private final byte[] aidToSelect;

        /**
         * List of status codes in response to the select application command that should be
         * considered successful although they are different from 9000
         */
        private Set<Integer> successfulSelectionStatusCodes;

        /** Private constructor */
        private AidSelector(AidSelectorBuilder builder) {
            this.aidToSelect = builder.aidToSelect;
            this.fileOccurrence = builder.fileOccurrence;
            this.fileControlInformation = builder.fileControlInformation;
            this.successfulSelectionStatusCodes = null;
        }

        /**
         * (package-private)<br>
         * Builder class to create a AidSelector with additional file occurrence and file control
         * information.
         * <p>
         * The fileOccurrence parameter defines the selection options P2 of the SELECT command
         * message
         * <p>
         * The fileControlInformation parameter defines the expected command output template.
         * <p>
         * Refer to ISO7816-4.2 for detailed information about these parameters
         *
         * @since 0.9
         */
        public static class AidSelectorBuilder {
            private byte[] aidToSelect;
            private FileOccurrence fileOccurrence = FileOccurrence.FIRST;
            private FileControlInformation fileControlInformation = FileControlInformation.FCI;

            /** Private constructor */
            private AidSelectorBuilder() {}

            public AidSelectorBuilder aidToSelect(byte[] aid) {
                if (aid.length < AID_MIN_LENGTH || aid.length > AID_MAX_LENGTH) {
                    aidToSelect = null;
                    throw new IllegalArgumentException("Bad AID length: " + aid.length
                            + ". The AID length should be " + "between 5 and 15.");
                } else {
                    aidToSelect = aid;
                }
                return this;
            }

            public AidSelectorBuilder aidToSelect(String aid) {
                return this.aidToSelect(ByteArrayUtil.fromHex(aid));
            }

            public AidSelectorBuilder fileOccurrence(
                    SeSelector.AidSelector.FileOccurrence fileOccurrence) {
                this.fileOccurrence = fileOccurrence;
                return this;
            }

            public AidSelectorBuilder fileControlInformation(
                    SeSelector.AidSelector.FileControlInformation fileControlInformation) {
                this.fileControlInformation = fileControlInformation;
                return this;
            }

            public SeSelector.AidSelector build() {
                return new SeSelector.AidSelector(this);
            }
        }

        /**
         * Gets a new builder.
         *
         * @return a new builder instance
         */
        public static AidSelectorBuilder builder() {
            return new AidSelectorBuilder();
        }

        /**
         * Getter for the AID provided at construction time
         *
         * @return byte array containing the AID
         */
        public byte[] getAidToSelect() {
            return aidToSelect;
        }

        /**
         * @return the file occurrence parameter
         */
        public FileOccurrence getFileOccurrence() {
            return fileOccurrence;
        }

        /**
         * @return the file control information parameter
         */
        public FileControlInformation getFileControlInformation() {
            return fileControlInformation;
        }

        /**
         * Gets the list of successful selection status codes
         *
         * @return the list of status codes
         */
        public Set<Integer> getSuccessfulSelectionStatusCodes() {
            return successfulSelectionStatusCodes;
        }

        /**
         * Add as status code to be accepted to the list of successful selection status codes
         * 
         * @param statusCode the status code to be accepted
         */
        public void addSuccessfulStatusCode(int statusCode) {
            // the list is kept null until a code is added
            if (this.successfulSelectionStatusCodes == null) {
                this.successfulSelectionStatusCodes = new LinkedHashSet<Integer>();
            }
            this.successfulSelectionStatusCodes.add(statusCode);
        }

        /**
         * Print out the AID in hex
         *
         * @return a string
         */
        @Override
        public String toString() {
            return "AidSelector{" + "aidToSelect=" + ByteArrayUtil.toHex(aidToSelect)
                    + ", fileOccurrence=" + fileOccurrence + ", fileControlInformation="
                    + fileControlInformation + ", successfulSelectionStatusCodes="
                    + successfulSelectionStatusCodes + '}';
        }
    }

    /**
     * Static nested class to hold the data elements used to perform an ATR based filtering
     */
    public static final class AtrFilter {
        /**
         * Regular expression dedicated to handle SE logical channel opening based on ATR pattern
         */
        private String atrRegex;

        /**
         * Regular expression based filter
         *
         * @param atrRegex String hex regular expression
         */
        public AtrFilter(String atrRegex) {
            this.atrRegex = atrRegex;
        }

        /**
         * Setter for the regular expression provided at construction time
         *
         * @param atrRegex expression string
         */
        public void setAtrRegex(String atrRegex) {
            this.atrRegex = atrRegex;
        }

        /**
         * Getter for the regular expression provided at construction time
         *
         * @return Regular expression string
         */
        public String getAtrRegex() {
            return atrRegex;
        }

        /**
         * Tells if the provided ATR matches the registered regular expression
         *
         * If the registered regular expression is empty, the ATR is always matching.
         *
         * @param atr a buffer containing the ATR to be checked
         * @return a boolean true the ATR matches the current regex
         */
        public boolean atrMatches(byte[] atr) {
            boolean m;
            if (atrRegex.length() != 0) {
                Pattern p = Pattern.compile(atrRegex);
                String atrString = ByteArrayUtil.toHex(atr);
                m = p.matcher(atrString).matches();
            } else {
                m = true;
            }
            return m;
        }

        /**
         * Print out the ATR regex
         *
         * @return a string
         */
        @Override
        public String toString() {
            return "AtrFilter{" + "atrRegex='" + atrRegex + '\'' + '}';
        }
    }

    /**
     * Private constructor
     * 
     * @param builder the SeSelector builder
     */
    protected SeSelector(SeSelectorBuilder builder) {
        this.seProtocol = builder.seProtocol;
        this.aidSelector = builder.aidSelector;
        this.atrFilter = builder.atrFilter;
        if (logger.isTraceEnabled()) {
            logger.trace("Selection data: AID = {}, ATRREGEX = {}",
                    (this.aidSelector == null || this.aidSelector.getAidToSelect() == null) ? "null"
                            : ByteArrayUtil.toHex(this.aidSelector.getAidToSelect()),
                    this.atrFilter == null ? "null" : this.atrFilter.getAtrRegex());
        }
    }

    /**
     * Create a SeSelector to perform the SE selection<br>
     * Builder pattern with inheritance inspired from https://stackoverflow.com/a/52294689
     * <p>
     * if seProtocol is null, all protocols will match and the selection process will continue
     *
     * <p>
     * if seProtocol is not null, the current SE protocol will checked and the selection process
     * will continue only if the protocol matches.
     *
     * <p>
     * if aidSelector is null, no 'select application' command is generated. In this case the SE
     * must have a default application selected. (e.g. SAM or Rev1 Calypso cards)
     * <p>
     * if aidSelector is not null, a 'select application' command is generated and performed.
     * Furthermore, the status code is checked against the list of successful status codes in the
     * {@link AidSelector} to determine if the SE matched or not the selection data.
     * <p>
     * if atrFilter is null, no check of the ATR is performed. All SE will match.
     * <p>
     * if atrFilter is not null, the ATR of the SE is compared with the regular expression provided
     * in the {@link AtrFilter} in order to determine if the SE match or not the expected ATR.
     *
     * @since 0.9
     */
    public static class SeSelectorBuilder {
        SeProtocol seProtocol;
        SeSelector.AtrFilter atrFilter;
        SeSelector.AidSelector aidSelector;


        /** Private constructor */
        protected SeSelectorBuilder() {}

        public SeSelectorBuilder seProtocol(SeProtocol seProtocol) {
            this.seProtocol = seProtocol;
            return this;
        }

        public SeSelectorBuilder atrFilter(SeSelector.AtrFilter atrFilter) {
            this.atrFilter = atrFilter;
            return this;
        }

        public SeSelectorBuilder aidSelector(SeSelector.AidSelector aidSelector) {
            this.aidSelector = aidSelector;
            return this;
        }

        public SeSelector build() {
            return new SeSelector(this);
        }
    }

    public static SeSelectorBuilder builder() {
        return new SeSelectorBuilder();
    }

    /**
     * Getter
     *
     * @return the {@link SeProtocol} provided at construction time
     */
    public SeProtocol getSeProtocol() {
        return seProtocol;
    }

    /**
     * Getter
     * 
     * @return the {@link AtrFilter} provided at construction time
     */
    public AtrFilter getAtrFilter() {
        return atrFilter;
    }

    /**
     * Getter
     *
     * @return the {@link AidSelector} provided at construction time
     */
    public AidSelector getAidSelector() {
        return aidSelector;
    }

    @Override
    public String toString() {
        return "SeSelector{" + "seProtocol=" + seProtocol + ", aidSelector=" + aidSelector
                + ", atrFilter=" + atrFilter + '}';
    }
}
