/********************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
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

import static org.assertj.core.api.Java6Assertions.assertThat;
import java.util.List;
import org.eclipse.keyple.core.command.AbstractApduCommandBuilder;
import org.eclipse.keyple.core.command.SeCommand;
import org.eclipse.keyple.core.seproxy.SeSelector;
import org.eclipse.keyple.core.seproxy.exception.KeypleException;
import org.eclipse.keyple.core.seproxy.message.ApduRequest;
import org.eclipse.keyple.core.seproxy.message.SeRequest;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.protocol.SeCommonProtocols;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Test;

public class AbstractSeSelectionRequestTest {
    private static final String AID = "112233445566";
    private static final String APDU1 = "00 11 2233 01 11";
    private static final String APDU2 = "00 11 2233 01 22";


    @Test
    public void testGetSelectionRequest() {}

    @Test
    public void testGetSeSelector() {
        SeSelector seSelector = SeSelector.builder()
                .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)
                .aidSelector(SeSelector.AidSelector.builder().aidToSelect(AID).build()).build();
        TestSeSelectionRequest testSeSelectionRequest = new TestSeSelectionRequest(seSelector);
        assertThat(testSeSelectionRequest.getSeSelector()).isEqualTo(seSelector);
    }

    @Test
    public void testAddCommandBuilder_GetCommandBuilders_getSelectionRequest() {
        SeSelector seSelector = SeSelector.builder()
                .seProtocol(SeCommonProtocols.PROTOCOL_ISO14443_4)
                .aidSelector(SeSelector.AidSelector.builder().aidToSelect(AID).build()).build();
        TestSeSelectionRequest testSeSelectionRequest = new TestSeSelectionRequest(seSelector);
        ApduRequest apduRequest1 = new ApduRequest(ByteArrayUtil.fromHex(APDU1), true);
        ApduRequest apduRequest2 = new ApduRequest(ByteArrayUtil.fromHex(APDU2), true);
        TestCommandBuilder builder1 = new TestCommandBuilder(TestCommands.COMMAND1, apduRequest1);
        TestCommandBuilder builder2 = new TestCommandBuilder(TestCommands.COMMAND1, apduRequest2);
        testSeSelectionRequest.addCommandBuilder(builder1);
        testSeSelectionRequest.addCommandBuilder(builder2);
        List<AbstractApduCommandBuilder> builders = testSeSelectionRequest.getCommandBuilders();
        assertThat(builders.get(0)).isEqualTo(builder1);
        assertThat(builders.get(1)).isEqualTo(builder2);
        SeRequest selectionRequest = testSeSelectionRequest.getSelectionRequest();
        List<ApduRequest> apduRequests = selectionRequest.getApduRequests();
        assertThat(apduRequests.get(0)).isEqualTo(apduRequest1);
        assertThat(apduRequests.get(1)).isEqualTo(apduRequest2);
    }


    private static class TestSeSelectionRequest
            extends AbstractSeSelectionRequest<AbstractApduCommandBuilder> {
        public TestSeSelectionRequest(SeSelector seSelector) {
            super(seSelector);
        }

        @Override
        protected AbstractMatchingSe parse(SeResponse seResponse) throws KeypleException {
            return null;
        }
    }

    private static class TestCommandBuilder extends AbstractApduCommandBuilder {
        public TestCommandBuilder(SeCommand commandRef, ApduRequest request) {
            super(commandRef, request);
        }
    }

    private enum TestCommands implements SeCommand {
        COMMAND1("Command 1", (byte) 0x01), COMMAND2("Command 2", (byte) 0x02);
        private final String name;
        private final byte instructionByte;

        @Override
        public String getName() {
            return name;
        }

        @Override
        public byte getInstructionByte() {
            return instructionByte;
        }

        TestCommands(String name, byte instructionByte) {
            this.name = name;
            this.instructionByte = instructionByte;
        }
    }
}
