/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License version 2.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 */
package org.eclipse.keyple.transaction;

import java.util.*;
import org.eclipse.keyple.seproxy.ApduRequest;
import org.eclipse.keyple.seproxy.SeProtocol;
import org.eclipse.keyple.seproxy.SeRequest;
import org.eclipse.keyple.util.ByteArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SeSelector class groups the information and methods used to select a particular secure
 * element
 */
public class SeSelector {
    private static final Logger logger = LoggerFactory.getLogger(SeSelector.class);

    protected List<ApduRequest> seSelectionApduRequestList = new ArrayList<ApduRequest>();
    protected Set<Integer> selectApplicationSuccessfulStatusCodes = new HashSet<Integer>();
    private Class<? extends MatchingSe> matchingClass = MatchingSe.class;
    private Class<? extends SeSelector> selectorClass = SeSelector.class;
    private final boolean keepChannelOpen;
    private final SelectionParameters selectionParameters;
    private final SeProtocol protocolFlag;
    private String extraInfo;

    /**
     * Inner class to gather the parameters of the selection.
     */
    public static class SelectionParameters {
        private final String atrRegex;
        private final byte[] aid;
        private final Short dfLid;
        private final boolean selectNext;
        private final boolean selectionByAid;

        /**
         * Constructor dedicated to select a SE that does not support the select application command
         * 
         * @param atrRegex a regular expression to compare with the ATR of the targeted SE
         * @param dfLid the LID of the DF to be selected if necessary (can be null if no DF is to be
         *        selected)
         */
        public SelectionParameters(String atrRegex, Short dfLid) {
            if (atrRegex == null || atrRegex.length() == 0) {
                throw new IllegalArgumentException("Selector ATR regex can't be null or empty");
            }
            this.atrRegex = atrRegex;
            this.dfLid = dfLid;
            this.aid = null;
            this.selectNext = false;
            selectionByAid = false;
        }

        /**
         * Constructor dedicated to select a SE by its AID
         * 
         * @param aid the target AID (end bytes can be truncated)
         * @param selectNext a flag to indicate if the first or the next occurrence is requested
         *        (see ISO7816-4 for a complete description of the select next mechanism)
         */
        public SelectionParameters(byte[] aid, boolean selectNext) {
            if (aid == null) {
                throw new IllegalArgumentException("Selector AID can't be null");
            }
            atrRegex = null;
            dfLid = null;
            this.aid = aid;
            this.selectNext = selectNext;
            selectionByAid = true;
        }

        public String getAtrRegex() {
            return atrRegex;
        }

        public short getDfLid() {
            return dfLid;
        }

        public byte[] getAid() {
            return aid;
        }

        public boolean isSelectNext() {
            return selectNext;
        }

        public boolean isSelectionByAid() {
            return selectionByAid;
        }
    }

    /**
     * Instantiate a SeSelector object with the selection data, describing the selection method, the
     * channel management after the selection and the protocol flag to possibly target a specific
     * protocol
     *
     * @param selectionParameters the information data to select the SE
     * @param keepChannelOpen flag to tell if the logical channel should be left open at the end of
     *        the selection
     * @param protocolFlag flag to be compared with the protocol identified when communicating the
     *        SE
     */
    public SeSelector(SelectionParameters selectionParameters, boolean keepChannelOpen,
            SeProtocol protocolFlag) {
        this.selectionParameters = selectionParameters;
        this.keepChannelOpen = keepChannelOpen;
        this.protocolFlag = protocolFlag;
        if (logger.isTraceEnabled()) {
            if (selectionParameters.isSelectionByAid()) {
                logger.trace(
                        "AID based selection: AID = {}, KEEPCHANNELOPEN = {}, PROTOCOLFLAG = {}",
                        ByteArrayUtils.toHex(selectionParameters.getAid()), keepChannelOpen,
                        protocolFlag);
            } else {
                logger.trace(
                        "ATR based selection: ATRREGEX = {}, KEEPCHANNELOPEN = {}, PROTOCOLFLAG = {}",
                        selectionParameters.getAtrRegex(), keepChannelOpen, protocolFlag);
            }
        }
    }

    /**
     * Alternate constructor to give the possibility to provide additional textual information for
     * logging purpose
     *
     * @param selectionParameters the information data to select the SE
     * @param keepChannelOpen flag to tell if the logical channel should be left open at the end of
     *        the selection
     * @param protocolFlag flag to be compared with the protocol identified when communicating the
     *        SE
     * @param extraInfo information string (to be printed in logs)
     */
    public SeSelector(SelectionParameters selectionParameters, boolean keepChannelOpen,
            SeProtocol protocolFlag, String extraInfo) {
        this(selectionParameters, keepChannelOpen, protocolFlag);
        if (extraInfo != null) {
            this.extraInfo = extraInfo;
        } else {
            this.extraInfo = "";
        }
    }

    public SelectionParameters getSelectionParameters() {
        return selectionParameters;
    }

    /**
     * @return the protocolFlag defined by the constructor
     */
    public final SeProtocol getProtocolFlag() {
        return protocolFlag;
    }

    /**
     * Sets the list of ApduRequest to be executed following the selection operation at once
     * 
     * @param seSelectionApduRequestList the list of requests
     */
    public final void setSelectionApduRequestList(List<ApduRequest> seSelectionApduRequestList) {
        this.seSelectionApduRequestList = seSelectionApduRequestList;
    }

    /**
     * Returns a selection SeRequest built from the information provided in the constructor and
     * possibly completed with the seSelectionApduRequestList
     *
     * @return the selection SeRequest
     */
    protected final SeRequest getSelectorRequest() {
        SeRequest seSelectionRequest;
        if (!selectionParameters.isSelectionByAid()) {
            seSelectionRequest =
                    new SeRequest(new SeRequest.AtrSelector(selectionParameters.getAtrRegex()),
                            seSelectionApduRequestList, keepChannelOpen, protocolFlag);
        } else {
            seSelectionRequest = new SeRequest(
                    new SeRequest.AidSelector(selectionParameters.getAid(),
                            selectionParameters.isSelectNext()),
                    seSelectionApduRequestList, keepChannelOpen, protocolFlag,
                    selectApplicationSuccessfulStatusCodes);
        }
        return seSelectionRequest;
    }

    /**
     * Gets the information string
     * 
     * @return a string to be printed in logs
     */
    public final String getExtraInfo() {
        return extraInfo;
    }

    /**
     * The matchingClass is the MatchingSe class or one of its extensions
     * <p>
     * It is used in SeSelection to determine what kind of MatchingSe is to be instantiated.
     *
     * This method must be called in the classes that extend SeSelector in order to specify the
     * expected class derived from MatchingSe in return to the selection process.
     * 
     * @param matchingClass the expected class for this SeSelector
     */
    protected final void setMatchingClass(Class<? extends MatchingSe> matchingClass) {
        this.matchingClass = matchingClass;
    }

    /**
     * The selectorClass is the SeSelector class or one of its extensions
     * <p>
     * It is used in SeSelection to determine what kind of SeSelector is to be used as argument to
     * the matchingClass constructor.
     *
     * This method must be called in the classes that extend SeSelector in order to specify the
     * expected class derived from SeSelector used as an argument to derived form of MatchingSe.
     * 
     * @param selectorClass the argument for the constructor of the matchingClass
     */
    protected final void setSelectorClass(Class<? extends SeSelector> selectorClass) {
        this.selectorClass = selectorClass;
    }

    /**
     * The default value for the matchingClass (unless setMatchingClass is used) is MatchingSe.class
     * 
     * @return the current matchingClass
     */
    protected final Class<? extends MatchingSe> getMatchingClass() {
        return matchingClass;
    }

    /**
     * The default value for the selectorClass (unless setSelectorClass is used) is SeSelector.class
     * 
     * @return the current selectorClass
     */
    protected final Class<? extends SeSelector> getSelectorClass() {
        return selectorClass;
    }
}
