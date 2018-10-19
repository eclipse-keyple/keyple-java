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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SeSelector class groups the information and methods used to select a particular secure
 * element
 */
public class SeSelector {
    private static final Logger logger = LoggerFactory.getLogger(SeSelector.class);

    protected List<ApduRequest> seSelectionApduRequestList = new ArrayList<ApduRequest>();
    protected Set<Short> selectApplicationSuccessfulStatusCodes = new HashSet<Short>();
    private Class<? extends MatchingSe> matchingClass = MatchingSe.class;
    private Class<? extends SeSelector> selectorClass = SeSelector.class;
    private final boolean keepChannelOpen;
    private final String atrRegex;
    private final byte[] seAid;
    private final boolean selectionByAid;
    private final SeProtocol protocolFlag;
    private String extraInfo;

    /**
     * ATR based selector
     *
     * @param atrRegex regular expression to be applied to the SE ATR
     * @param keepChannelOpen flag to tell if the logical channel should be left open at the end of
     *        the selection
     * @param protocolFlag flag to be compared with the protocol identified when communicating the
     *        SE
     */
    public SeSelector(String atrRegex, boolean keepChannelOpen, SeProtocol protocolFlag) {
        if (atrRegex == null || atrRegex.length() == 0) {
            throw new IllegalArgumentException("Selector ATR regex can't be null or empty");
        }
        this.atrRegex = atrRegex;
        this.keepChannelOpen = keepChannelOpen;
        seAid = null;
        selectionByAid = false;
        this.protocolFlag = protocolFlag;
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "ATR based selection: ATRREGEX = {}, KEEPCHANNELOPEN = {}, PROTOCOLFLAG = {}",
                    atrRegex, keepChannelOpen, protocolFlag);
        }
    }

    /**
     * ATR based selector with extraInfo
     * 
     * @param atrRegex regular expression to be applied to the SE ATR
     * @param keepChannelOpen flag to tell if the logical channel should be left open at the end of
     *        the selection
     * @param protocolFlag flag to be compared with the protocol identified when communicating the
     *        SE
     * @param extraInfo information string to be printed in logs
     */
    public SeSelector(String atrRegex, boolean keepChannelOpen, SeProtocol protocolFlag,
            String extraInfo) {
        this(atrRegex, keepChannelOpen, protocolFlag);
        if (extraInfo != null) {
            this.extraInfo = extraInfo;
        } else {
            this.extraInfo = "";
        }
    }

    /**
     * AID based selector with extraInfo
     * 
     * @param seAid application identification data
     * @param keepChannelOpen flag to tell if the logical channel should be left open at the end of
     *        the selection
     * @param protocolFlag flag to be compared with the protocol identified when communicating the
     *        SE
     */
    public SeSelector(byte[] seAid, boolean keepChannelOpen, SeProtocol protocolFlag) {
        if (seAid == null) {
            throw new IllegalArgumentException("Selector AID can't be null");
        }
        atrRegex = null;
        this.keepChannelOpen = keepChannelOpen;
        this.seAid = seAid;
        selectionByAid = true;
        this.protocolFlag = protocolFlag;
        if (logger.isTraceEnabled()) {
            logger.trace("AID based selection: AID = {}, KEEPCHANNELOPEN = {}, PROTOCOLFLAG = {}",
                    seAid, keepChannelOpen, protocolFlag);
        }
    }

    /**
     * AID based selector with extraInfo
     * 
     * @param seAid application identification data
     * @param keepChannelOpen flag to tell if the logical channel should be left open at the end of
     *        the selection
     * @param protocolFlag flag to be compared with the protocol identified when communicating the
     *        SE
     * @param extraInfo information string to be printed in logs
     */
    public SeSelector(byte[] seAid, boolean keepChannelOpen, SeProtocol protocolFlag,
            String extraInfo) {
        this(seAid, keepChannelOpen, protocolFlag);
        if (extraInfo != null) {
            this.extraInfo = extraInfo;
        } else {
            this.extraInfo = "";
        }
    }

    /**
     * @return the protocolFlag defined by the constructor
     */
    public SeProtocol getProtocolFlag() {
        return protocolFlag;
    }

    /**
     * Sets the list of ApduRequest to be executed following the selection operation at once
     * 
     * @param seSelectionApduRequestList the list of requests
     */
    public void setSelectionApduRequestList(List<ApduRequest> seSelectionApduRequestList) {
        this.seSelectionApduRequestList = seSelectionApduRequestList;
    }

    /**
     * Returns a selection SeRequest built from the information provided in the constructor and
     * possibly completed with the seSelectionApduRequestList
     *
     * @return the selection SeRequest
     */
    protected SeRequest getSelectorRequest() {
        SeRequest seSelectionRequest;
        if (!selectionByAid) {
            seSelectionRequest = new SeRequest(new SeRequest.AtrSelector(atrRegex),
                    seSelectionApduRequestList, keepChannelOpen, protocolFlag);
        } else {
            seSelectionRequest =
                    new SeRequest(new SeRequest.AidSelector(seAid), seSelectionApduRequestList,
                            keepChannelOpen, protocolFlag, selectApplicationSuccessfulStatusCodes);
        }
        return seSelectionRequest;
    }

    /**
     * Gets the information string
     * 
     * @return a string to be printed in logs
     */
    public String getExtraInfo() {
        return extraInfo;
    }

    /**
     * The matchingClass is the MatchingSe class or one of its extensions
     * <p>
     * It is used in SeSelection to determine what kind of MatchingSe is to be instantiated.
     * 
     * @param matchingClass the expected class for this SeSelector
     */
    public void setMatchingClass(Class<? extends MatchingSe> matchingClass) {
        this.matchingClass = matchingClass;
    }

    /**
     * The selectorClass is the SeSelector class or one of its extensions
     * <p>
     * It is used in SeSelection to determine what kind of SeSelector is to be used as argument to
     * the matchingClass constructor.
     * 
     * @param selectorClass the argument for the constructor of the matchingClass
     */
    public void setSelectorClass(Class<? extends SeSelector> selectorClass) {
        this.selectorClass = selectorClass;
    }

    /**
     * The default value for the matchingClass (unless setMatchingClass is used) is MatchingSe.class
     * 
     * @return the current matchingClass
     */
    public Class<? extends MatchingSe> getMatchingClass() {
        return matchingClass;
    }

    /**
     * The default value for the selectorClass (unless setSelectorClass is used) is SeSelector.class
     * 
     * @return the current selectorClass
     */
    public Class<? extends SeSelector> getSelectorClass() {
        return selectorClass;
    }
}
