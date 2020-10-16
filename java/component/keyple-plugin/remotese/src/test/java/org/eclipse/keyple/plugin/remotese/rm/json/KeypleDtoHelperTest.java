/* **************************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remotese.rm.json;

import java.io.IOException;
import org.eclipse.keyple.core.reader.exception.KeypleReaderIOException;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PMD")
@RunWith(MockitoJUnitRunner.class)
public class KeypleDtoHelperTest {

  private static final Logger logger = LoggerFactory.getLogger(KeypleDtoHelperTest.class);

  @Test
  public void testRequestDto() {
    String id = "anyId";

    KeypleDto dtoRequest =
        KeypleDtoHelper.buildRequest(
            "anyAction",
            "anyBody",
            "sessionId",
            "anyReaderNameId",
            "anyVirtualId",
            "anyRequesterId",
            "anyTargetId",
            id);

    KeypleDto dtoRequestParsed = KeypleDtoHelper.fromJson(KeypleDtoHelper.toJson(dtoRequest));

    logger.trace(KeypleDtoHelper.toJson(dtoRequestParsed));

    Assert.assertFalse(KeypleDtoHelper.containsException(dtoRequestParsed));
    Assert.assertFalse(KeypleDtoHelper.isNoResponse(dtoRequestParsed));
    Assert.assertTrue(dtoRequestParsed.isRequest());
    Assert.assertEquals(id, dtoRequestParsed.getId());
  }

  @Test
  public void testResponseDto() {
    String id = "anyId";

    KeypleDto dtoResponse =
        KeypleDtoHelper.buildResponse(
            "anyAction",
            "anyBody",
            "sessionId",
            "anyReaderNameId",
            "anyVirtualId",
            "anyRequesterId",
            "anyTargetId",
            id);

    KeypleDto dtoResponseParsed = KeypleDtoHelper.fromJson(KeypleDtoHelper.toJson(dtoResponse));

    logger.trace(KeypleDtoHelper.toJson(dtoResponseParsed));

    Assert.assertFalse(dtoResponseParsed.isRequest());
    Assert.assertEquals(id, dtoResponseParsed.getId());
    Assert.assertFalse(KeypleDtoHelper.containsException(dtoResponseParsed));
    Assert.assertFalse(KeypleDtoHelper.isNoResponse(dtoResponseParsed));
  }

  @Test
  public void testNotificationDto() {

    KeypleDto dtoNotification =
        KeypleDtoHelper.buildNotification(
            "anyAction",
            "anyBody",
            "sessionId",
            "anyReaderNameId",
            "anyVirtualId",
            "anyRequesterId",
            "anyTargetId");

    KeypleDto dtoNotificationParsed =
        KeypleDtoHelper.fromJson(KeypleDtoHelper.toJson(dtoNotification));
    logger.trace(KeypleDtoHelper.toJson(dtoNotificationParsed));

    Assert.assertTrue(dtoNotificationParsed.isRequest());
    Assert.assertNull(dtoNotificationParsed.getId());
    Assert.assertFalse(KeypleDtoHelper.containsException(dtoNotificationParsed));
    Assert.assertFalse(KeypleDtoHelper.isNoResponse(dtoNotificationParsed));
  }

  @Test
  public void testNoResponseDto() {

    String id = "anyId";

    KeypleDto dtoNoResponse = KeypleDtoHelper.NoResponse(id);

    logger.trace(KeypleDtoHelper.toJson(dtoNoResponse));

    Assert.assertEquals(id, dtoNoResponse.getId());
    Assert.assertFalse(KeypleDtoHelper.containsException(dtoNoResponse));
    Assert.assertTrue(KeypleDtoHelper.isNoResponse(dtoNoResponse));
  }

  @Test
  public void testContainsError() {
    Exception ex =
        new KeypleReaderIOException("keyple Reader Exception message", new IOException("error io"));

    Exception t = new IllegalStateException("illegal state  message", new IOException("error io"));

    Exception npe = new NullPointerException("NPE  message");

    KeypleDto dtoWithException =
        KeypleDtoHelper.ExceptionDTO("any", ex, "any", "any", "any", "any", "any", "any");
    KeypleDto dtoWithThrowable =
        KeypleDtoHelper.ExceptionDTO("any", t, "any", "any", "any", "any", "any", "any");
    KeypleDto dtoWithNPE =
        KeypleDtoHelper.ExceptionDTO("any", npe, "any", "any", "any", "any", "any", "any");

    KeypleDto dtoWithExceptionParsed =
        KeypleDtoHelper.fromJson(KeypleDtoHelper.toJson(dtoWithException));
    KeypleDto dtoWithThrowableParsed =
        KeypleDtoHelper.fromJson(KeypleDtoHelper.toJson(dtoWithThrowable));
    KeypleDto dtoWithNPEParsed = KeypleDtoHelper.fromJson(KeypleDtoHelper.toJson(dtoWithNPE));

    logger.trace(KeypleDtoHelper.toJson(dtoWithExceptionParsed));
    logger.trace(KeypleDtoHelper.toJson(dtoWithNPEParsed));
    logger.trace(KeypleDtoHelper.toJson(dtoWithThrowableParsed));

    Assert.assertTrue(KeypleDtoHelper.containsException(dtoWithException));
    Assert.assertTrue(KeypleDtoHelper.containsException(dtoWithThrowable));
    Assert.assertTrue(KeypleDtoHelper.containsException(dtoWithNPE));

    Assert.assertTrue(KeypleDtoHelper.containsException(dtoWithExceptionParsed));
    Assert.assertTrue(KeypleDtoHelper.containsException(dtoWithThrowableParsed));
    Assert.assertTrue(KeypleDtoHelper.containsException(dtoWithNPEParsed));
  }
}
