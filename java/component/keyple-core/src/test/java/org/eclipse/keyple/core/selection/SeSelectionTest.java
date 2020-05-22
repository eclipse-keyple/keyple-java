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
import org.eclipse.keyple.core.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsResponse;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.message.*;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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

    @Ignore // TODO Restore this test
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
        List<SeRequest> selectionSeRequests =
                ((DefaultSelectionsRequest) selectionOperation).getSelectionSeRequests();
        Assert.assertEquals(2, selectionSeRequests.size());

        // get the two se requests
        Iterator<SeRequest> iterator = selectionSeRequests.iterator();
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

        try {
            Assert.assertNull(seSelection.processDefaultSelection(null));
        } catch (KeypleException e) {
            Assert.fail("Exception raised: " + e.getMessage());
        }
    }

    @Test
    public void processDefaultSelectionEmpty() {
        SeSelection seSelection = createSeSelection();

        AbstractDefaultSelectionsResponse defaultSelectionsResponse;
        List<SeResponse> seResponses = new ArrayList<SeResponse>();

        defaultSelectionsResponse = new DefaultSelectionsResponse(seResponses);

        SelectionsResult selectionsResult = null;
        try {
            selectionsResult = seSelection.processDefaultSelection(defaultSelectionsResponse);
        } catch (KeypleException e) {
            Assert.fail("Exception raised: " + e.getMessage());
        }

        Assert.assertFalse(selectionsResult.hasActiveSelection());
        Assert.assertEquals(0, selectionsResult.getMatchingSelections().size());
    }

    @Test
    public void processDefaultSelectionNotMatching() {
        // create a SeSelection
        SeSelection seSelection = createSeSelection();

        // create a selection response
        AbstractDefaultSelectionsResponse defaultSelectionsResponse;
        List<SeResponse> seResponses = new ArrayList<SeResponse>();

        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex(
                "CC 11223344 9999 00112233445566778899AABBCCDDEEFF 00112233445566778899AABBCC 9000"),
                null);

        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();

        apduResponses.add(apduResponse);

        SelectionStatus selectionStatus = new SelectionStatus(null,
                new ApduResponse(ByteArrayUtil.fromHex("001122334455669000"), null), false);

        SeResponse seResponse = new SeResponse(true, true, selectionStatus, apduResponses);

        seResponses.add(seResponse);

        defaultSelectionsResponse = new DefaultSelectionsResponse(seResponses);

        // process the selection response with the SeSelection
        SelectionsResult selectionsResult = null;
        try {
            selectionsResult = seSelection.processDefaultSelection(defaultSelectionsResponse);
        } catch (KeypleException e) {
            Assert.fail("Exception raised: " + e.getMessage());
        }

        Assert.assertFalse(selectionsResult.hasActiveSelection());
        try {
            selectionsResult.getActiveMatchingSe();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("No active Matching SE is available"));
        }
    }

    @Test
    public void processDefaultSelectionMatching() {
        // create a SeSelection
        SeSelection seSelection = createSeSelection();

        // create a selection response
        AbstractDefaultSelectionsResponse defaultSelectionsResponse;
        List<SeResponse> seResponses = new ArrayList<SeResponse>();

        ApduResponse apduResponse = new ApduResponse(ByteArrayUtil.fromHex(
                "CC 11223344 9999 00112233445566778899AABBCCDDEEFF 00112233445566778899AABBCC 9000"),
                null);

        List<ApduResponse> apduResponses = new ArrayList<ApduResponse>();

        apduResponses.add(apduResponse);

        SelectionStatus selectionStatus = new SelectionStatus(null,
                new ApduResponse(ByteArrayUtil.fromHex("001122334455669000"), null), true);

        SeResponse seResponse = new SeResponse(true, true, selectionStatus, apduResponses);

        seResponses.add(seResponse);

        defaultSelectionsResponse = new DefaultSelectionsResponse(seResponses);

        // process the selection response with the SeSelection
        SelectionsResult selectionsResult = null;
        try {
            selectionsResult = seSelection.processDefaultSelection(defaultSelectionsResponse);
        } catch (KeypleException e) {
            Assert.fail("Exception raised: " + e.getMessage());
        }

        Assert.assertTrue(selectionsResult.hasActiveSelection());
        Assert.assertNotNull(selectionsResult.getActiveMatchingSe());
        MatchingSe matchingSe = (MatchingSe) selectionsResult.getActiveMatchingSe();
        Assert.assertEquals(TransmissionMode.CONTACTLESS, matchingSe.getTransmissionMode());
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
        SeSelector.AidSelector aidSelector =
                new SeSelector.AidSelector(new SeSelector.AidSelector.IsoAid("AABBCCDDEE"),
                        SeSelector.AidSelector.FileOccurrence.FIRST,
                        SeSelector.AidSelector.FileControlInformation.FCI);
        SeSelector seSelector1 =
                new SeSelector(SeCommonProtocols.PROTOCOL_ISO14443_4, null, aidSelector);

        // TODO add an implementation of AbstractApduCommandBuilder/Parser
        // // APDU requests
        // List<ApduRequest> apduRequests= new ArrayList<ApduRequest>();
        // apduRequestList.add(
        // new ApduRequest("Apdu 001122334455", ByteArrayUtil.fromHex("001122334455"), false));
        // apduRequestList.add(
        // new ApduRequest("Apdu 66778899AABB", ByteArrayUtil.fromHex("66778899AABB"), true));
        //
        // seSelection.prepareSelection(new SeSelectionRequest(seSelector1, apduRequestList));

        aidSelector = new SeSelector.AidSelector(new SeSelector.AidSelector.IsoAid("1122334455"),
                SeSelector.AidSelector.FileOccurrence.NEXT,
                SeSelector.AidSelector.FileControlInformation.FCP);
        aidSelector.addSuccessfulStatusCode(0x6283);

        SeSelector seSelector2 = new SeSelector(SeCommonProtocols.PROTOCOL_B_PRIME,
                new SeSelector.AtrFilter(".*"), aidSelector);

        seSelection.prepareSelection(new SeSelectionRequest(seSelector2, null));

        return seSelection;
    }

    /**
     * Selection Request instantiation
     */
    private final class SeSelectionRequest extends AbstractSeSelectionRequest {

        public SeSelectionRequest(SeSelector seSelector,
                List<AbstractApduCommandBuilder> commandBuilders) {
            super(seSelector);

            if (commandBuilders != null) {
                for (AbstractApduCommandBuilder commandBuilder : commandBuilders) {
                    super.addCommandBuilder(commandBuilder);
                }
            }
        }

        @Override
        protected AbstractMatchingSe parse(SeResponse seResponse) {
            return new MatchingSe(seResponse, seSelector.getSeProtocol().getTransmissionMode());
        }
    }

    /**
     * Matching Se instantiation
     */
    private final class MatchingSe extends AbstractMatchingSe {
        MatchingSe(SeResponse selectionResponse, TransmissionMode transmissionMode) {
            super(selectionResponse, transmissionMode);
        }
    }
}
