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
package org.eclipse.keyple.core.selection;

import java.util.*;
import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.command.AbstractApduResponseParser;
import org.eclipse.keyple.core.command.AbstractApduResponseParserTest;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeSelectionTest extends CoreBaseTest {

    private static final Logger logger = LoggerFactory.getLogger(SeSelectionTest.class);

    @Before
    public void setUp() {
        logger.info("------------------------------");
        logger.info("Test {}", name.getMethodName() + "");
        logger.info("------------------------------");
    }

    @Test
    public void prepareSelection() {
        SeSelection seSelection = createSeSelection();

        // let's check if the result is as expected
        // (see createSelectionSelectionSelection to have a look at the expected values)

        // get the selection operation
        AbstractDefaultSelectionsRequest selectionOperation = seSelection.getSelectionOperation();

        // check common flags
        Assert.assertEquals(MultiSeRequestProcessing.FIRST_MATCH,
                ((DefaultSelectionsRequest) selectionOperation).getMultiSeRequestProcessing());
        Assert.assertEquals(ChannelControl.KEEP_OPEN,
                ((DefaultSelectionsRequest) selectionOperation).getChannelControl());

        // get the serequest set
        Set<SeRequest> selectionSeRequestSet =
                ((DefaultSelectionsRequest) selectionOperation).getSelectionSeRequestSet();
        Assert.assertEquals(2, selectionSeRequestSet.size());

        // get the two se requests
        Iterator<SeRequest> iterator = selectionSeRequestSet.iterator();
        SeRequest seRequest1 = iterator.next();
        SeRequest seRequest2 = iterator.next();

        // check selectors
        Assert.assertEquals("AABBCCDDEE", ByteArrayUtil
                .toHex(seRequest1.getSeSelector().getAidSelector().getAidToSelect().getValue()));
        Assert.assertEquals("1122334455", ByteArrayUtil
                .toHex(seRequest2.getSeSelector().getAidSelector().getAidToSelect().getValue()));

        Assert.assertEquals(SeSelector.AidSelector.FileOccurrence.FIRST,
                seRequest1.getSeSelector().getAidSelector().getFileOccurrence());
        Assert.assertEquals(SeSelector.AidSelector.FileOccurrence.NEXT,
                seRequest2.getSeSelector().getAidSelector().getFileOccurrence());

        Assert.assertEquals(SeSelector.AidSelector.FileControlInformation.FCI,
                seRequest1.getSeSelector().getAidSelector().getFileControlInformation());
        Assert.assertEquals(SeSelector.AidSelector.FileControlInformation.FCP,
                seRequest2.getSeSelector().getAidSelector().getFileControlInformation());

        Assert.assertNull(
                seRequest1.getSeSelector().getAidSelector().getSuccessfulSelectionStatusCodes());

        Assert.assertEquals(1, seRequest2.getSeSelector().getAidSelector()
                .getSuccessfulSelectionStatusCodes().size());
        Assert.assertEquals(0x6283, seRequest2.getSeSelector().getAidSelector()
                .getSuccessfulSelectionStatusCodes().toArray()[0]);

        Assert.assertNull(seRequest1.getSeSelector().getAtrFilter());
        Assert.assertEquals(".*", seRequest2.getSeSelector().getAtrFilter().getAtrRegex());

        Assert.assertEquals(2, seRequest1.getApduRequests().size());
        Assert.assertEquals(0, seRequest2.getApduRequests().size());

        List<ApduRequest> apduRequests = seRequest1.getApduRequests();

        Assert.assertArrayEquals(apduRequests.get(0).getBytes(),
                ByteArrayUtil.fromHex("001122334455"));
        Assert.assertArrayEquals(apduRequests.get(1).getBytes(),
                ByteArrayUtil.fromHex("66778899AABB"));

        Assert.assertEquals(apduRequests.get(0).isCase4(), false);
        Assert.assertEquals(apduRequests.get(1).isCase4(), true);

        // that's all!
    }

    @Test
    public void processDefaultSelectionNull() {
        SeSelection seSelection = Mockito.mock(SeSelection.class);

        Assert.assertNull(seSelection.processDefaultSelection(null));
    }

    @Test
    public void processDefaultSelectionEmpty() {
        SeSelection seSelection = createSeSelection();

        AbstractDefaultSelectionsResponse defaultSelectionsResponse;
        List<SeResponse> seResponseList = new ArrayList<SeResponse>();

        defaultSelectionsResponse = new DefaultSelectionsResponse(seResponseList);

        SelectionsResult selectionsResult =
                seSelection.processDefaultSelection(defaultSelectionsResponse);

        Assert.assertFalse(selectionsResult.hasActiveSelection());
        Assert.assertEquals(0, selectionsResult.getMatchingSelections().size());
    }

    @Test
    public void processDefaultSelectionNotMatching() {
        // create a SeSelection
        SeSelection seSelection = createSeSelection();

        // create a selection response
        AbstractDefaultSelectionsResponse defaultSelectionsResponse;
        List<SeResponse> seResponseList = new ArrayList<SeResponse>();

        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex(
                "CC 11223344 9999 00112233445566778899AABBCCDDEEFF 00112233445566778899AABBCC 9000"),
                null);

        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();

        apduResponseList.add(apduResponse);

        SelectionStatus selectionStatus = new SelectionStatus(null,
                new ApduResponse(ByteArrayUtil.fromHex("001122334455669000"), null), false);

        SeResponse seResponse = new SeResponse(true, true, selectionStatus, apduResponseList);

        seResponseList.add(seResponse);

        defaultSelectionsResponse = new DefaultSelectionsResponse(seResponseList);

        // process the selection response with the SeSelection
        SelectionsResult selectionsResult =
                seSelection.processDefaultSelection(defaultSelectionsResponse);

        Assert.assertFalse(selectionsResult.hasActiveSelection());
        Assert.assertNull(selectionsResult.getActiveSelection());
    }

    @Test
    public void processDefaultSelectionMatching() {
        // create a SeSelection
        SeSelection seSelection = createSeSelection();

        // create a selection response
        AbstractDefaultSelectionsResponse defaultSelectionsResponse;
        List<SeResponse> seResponseList = new ArrayList<SeResponse>();

        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex(
                "CC 11223344 9999 00112233445566778899AABBCCDDEEFF 00112233445566778899AABBCC 9000"),
                null);

        List<ApduResponse> apduResponseList = new ArrayList<ApduResponse>();

        apduResponseList.add(apduResponse);

        SelectionStatus selectionStatus = new SelectionStatus(null,
                new ApduResponse(ByteArrayUtil.fromHex("001122334455669000"), null), true);

        SeResponse seResponse = new SeResponse(true, true, selectionStatus, apduResponseList);

        seResponseList.add(seResponse);

        defaultSelectionsResponse = new DefaultSelectionsResponse(seResponseList);

        // process the selection response with the SeSelection
        SelectionsResult selectionsResult =
                seSelection.processDefaultSelection(defaultSelectionsResponse);

        Assert.assertTrue(selectionsResult.hasActiveSelection());
        Assert.assertNotNull(selectionsResult.getActiveSelection());
        MatchingSelection matchingSelection = selectionsResult.getActiveSelection();
        MatchingSe matchingSe = (MatchingSe) matchingSelection.getMatchingSe();
        Assert.assertTrue(matchingSe.isSelected());
        Assert.assertEquals(true, matchingSe.getSelectionStatus().hasMatched());
        Assert.assertEquals(TransmissionMode.CONTACTLESS, matchingSe.getTransmissionMode());
        Assert.assertEquals("Se Selector #1", matchingSe.getSelectionExtraInfo());
        Assert.assertEquals(0, matchingSelection.getSelectionIndex());
        AbstractApduResponseParser responseParser = matchingSelection.getResponseParser(0);
        Assert.assertTrue(responseParser.isSuccessful());
        Assert.assertEquals("Se Selector #1", matchingSelection.getExtraInfo());
    }

    /*
     * @Test public void processExplicitSelection() { // create a SeSelection SeSelection
     * seSelection = createSeSelection();
     *
     * AbstractLocalReader r = Mockito.spy(new Reader("SeSelectionP", "SeSelectionR"));
     *
     * // success apdu doReturn(ByteArrayUtil.fromHex(
     * "001122334455669000")).when(r).transmitApdu(ByteArrayUtil.fromHex( "001122334455669000"));
     *
     * // aid selection doReturn(ByteArrayUtil.fromHex(
     * "6F25840BA000000291A00000019102A516BF0C13C70800000000C0E11FA653070A3C230C1410019000"))
     * .when(r).transmitApdu(ByteArrayUtil
     * .fromHex("00 A4 04 00 0A A0 00 00 02 91 A0 00 00 01 91 00"));
     *
     * // physical channel is open doReturn(true).when(r).isPhysicalChannelOpen(); }
     */

    /**
     * Create a SeSelection object
     */
    private SeSelection createSeSelection() {
        SeSelection seSelection = new SeSelection();

        // create and add two selection cases
        SeSelector seSelector1 = new SeSelector(SeCommonProtocols.PROTOCOL_ISO14443_4, null,
                new SeSelector.AidSelector(new SeSelector.AidSelector.IsoAid("AABBCCDDEE"), null,
                        SeSelector.AidSelector.FileOccurrence.FIRST,
                        SeSelector.AidSelector.FileControlInformation.FCI),
                "Se Selector #1");

        // APDU requests
        List<ApduRequest> apduRequestList = new ArrayList<ApduRequest>();
        apduRequestList.add(
                new ApduRequest("Apdu 001122334455", ByteArrayUtil.fromHex("001122334455"), false));
        apduRequestList.add(
                new ApduRequest("Apdu 66778899AABB", ByteArrayUtil.fromHex("66778899AABB"), true));

        seSelection.prepareSelection(new SeSelectionRequest(seSelector1, apduRequestList));

        Set<Integer> successfulSelectionStatusCodes = new HashSet<Integer>() {
            {
                add(0x6283);
            }
        };

        SeSelector seSelector2 = new SeSelector(SeCommonProtocols.PROTOCOL_B_PRIME,
                new SeSelector.AtrFilter(".*"),
                new SeSelector.AidSelector(new SeSelector.AidSelector.IsoAid("1122334455"),
                        successfulSelectionStatusCodes, SeSelector.AidSelector.FileOccurrence.NEXT,
                        SeSelector.AidSelector.FileControlInformation.FCP),
                "Se Selector #1");

        seSelection.prepareSelection(new SeSelectionRequest(seSelector2, null));

        return seSelection;
    }

    /**
     * Selection Request instantiation
     */
    private final class SeSelectionRequest extends AbstractSeSelectionRequest {

        public SeSelectionRequest(SeSelector seSelector, List<ApduRequest> apduRequestList) {
            super(seSelector);

            if (apduRequestList != null) {
                for (ApduRequest apduRequest : apduRequestList) {
                    super.addApduRequest(apduRequest);
                }
            }
        }

        @Override
        protected AbstractMatchingSe parse(SeResponse seResponse) {
            return new MatchingSe(seResponse, seSelector.getSeProtocol().getTransmissionMode(),
                    seSelector.getExtraInfo());
        }

        @Override
        public AbstractApduResponseParser getCommandParser(SeResponse seResponse,
                int commandIndex) {
            return AbstractApduResponseParserTest
                    .getApduResponseParser(seResponse.getApduResponses().get(commandIndex));
        }
    }

    /**
     * Matching Se instantiation
     */
    private final class MatchingSe extends AbstractMatchingSe {
        MatchingSe(SeResponse selectionResponse, TransmissionMode transmissionMode,
                String extraInfo) {
            super(selectionResponse, transmissionMode, extraInfo);
        }
    }


}
