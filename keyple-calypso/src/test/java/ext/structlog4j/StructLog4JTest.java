/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package ext.structlog4j;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.keyple.calypso.commands.po.PoRevision;
import org.keyple.calypso.commands.po.builder.ReadRecordsCmdBuild;
import org.keyple.calypso.commands.po.builder.UpdateRecordCmdBuild;
import org.keyple.seproxy.ApduRequest;
import org.keyple.seproxy.ByteBufferUtils;
import com.github.structlog4j.ILogger;
import com.github.structlog4j.SLoggerFactory;

public class StructLog4JTest {
    private static final ILogger logger = SLoggerFactory.getLogger(StructLog4JTest.class);

    @Test
    public void basic() {
        logger.info("My info", "name", "Florent Clairambault", "class", getClass(), "ex",
                new RuntimeException());
    }

    @Test
    public void apduList() {
        List<ApduRequest> list = Arrays.asList(
                new UpdateRecordCmdBuild(PoRevision.REV3_2, (byte) 0x01, (byte) 0x02,
                        ByteBufferUtils.fromHex("01020304")).getApduRequest(),
                new ReadRecordsCmdBuild(PoRevision.REV3_2, (byte) 0x01, true, (byte) 0x02, (byte) 4)
                        .getApduRequest());
        logger.info("Apdu list", "action", "demo.listing_apdus", "apduList", list);
    }
}
