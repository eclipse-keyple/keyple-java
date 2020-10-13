/* **************************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.core.selection;

import static org.junit.Assert.*;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.message.AnswerToReset;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.core.seproxy.message.SelectionStatus;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeResourceTest extends CoreBaseTest {

  private static final Logger logger = LoggerFactory.getLogger(SeResourceTest.class);

  @Before
  public void setUp() throws Exception {
    logger.info("------------------------------");
    logger.info("Test {}", name.getMethodName() + "");
    logger.info("------------------------------");
  }

  @Test
  public void testConstructor() {
    SelectionStatus selectionStatus =
        new SelectionStatus(
            new AnswerToReset(ByteArrayUtil.fromHex("3B00000000000000")), null, false);
    MatchingSe matchingSe = new MatchingSe(new SeResponse(true, true, selectionStatus, null));
    SeReader seReader = null;
    LocalSeResource localSeResource = new LocalSeResource(seReader, matchingSe);
    Assert.assertEquals(matchingSe, localSeResource.getMatchingSe());
    Assert.assertEquals(null, localSeResource.getSeReader());
  }

  /** Matching card instantiation */
  private final class MatchingSe extends AbstractMatchingSe {
    MatchingSe(SeResponse selectionResponse) {
      super(selectionResponse);
    }
  }

  /** SeResource instantiation */
  private final class LocalSeResource extends SeResource<MatchingSe> {

    protected LocalSeResource(SeReader seReader, MatchingSe matchingSe) {
      super(seReader, matchingSe);
    }
  }
}
