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
package org.eclipse.keyple.core.card.selection;

import org.eclipse.keyple.core.CoreBaseTest;
import org.eclipse.keyple.core.card.message.AnswerToReset;
import org.eclipse.keyple.core.card.message.CardResponse;
import org.eclipse.keyple.core.card.message.SelectionStatus;
import org.eclipse.keyple.core.service.Reader;
import org.eclipse.keyple.core.util.ByteArrayUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CardResourceTest extends CoreBaseTest {

  private static final Logger logger = LoggerFactory.getLogger(CardResourceTest.class);

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
    SmartCard smartCard = new SmartCard(new CardResponse(true, true, selectionStatus, null));
    Reader reader = null;
    LocalCardResource localCardResource = new LocalCardResource(reader, smartCard);
    Assert.assertEquals(smartCard, localCardResource.getSmartCard());
    Assert.assertEquals(null, localCardResource.getReader());
  }

  /** Matching card instantiation */
  private final class SmartCard extends AbstractSmartCard {
    SmartCard(CardResponse selectionResponse) {
      super(selectionResponse);
    }
  }

  /** CardResource instantiation */
  private final class LocalCardResource extends CardResource<SmartCard> {

    protected LocalCardResource(Reader reader, SmartCard smartCard) {
      super(reader, smartCard);
    }
  }
}
