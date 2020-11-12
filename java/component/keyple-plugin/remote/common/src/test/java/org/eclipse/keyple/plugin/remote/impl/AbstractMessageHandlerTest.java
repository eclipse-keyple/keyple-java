/* **************************************************************************************
 * Copyright (c) 2020 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information
 * regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ************************************************************************************** */
package org.eclipse.keyple.plugin.remote.impl;

import com.google.gson.Gson;
import org.eclipse.keyple.core.service.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.service.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.MessageDto;
import org.junit.Before;
import org.junit.Test;

public class AbstractMessageHandlerTest {

  AbstractMessageHandler handler;
  Gson parser = KeypleJsonParser.getParser();

  MessageDto response = new MessageDto().setAction(MessageDto.Action.SET_DEFAULT_SELECTION.name());

  MessageDto responseWithKRIoExceptionException =
      new MessageDto()
          .setAction(MessageDto.Action.ERROR.name())
          .setBody(parser.toJson(new BodyError(new KeypleReaderIOException("keyple io reader"))));

  MessageDto responseWithKRNotFounExceptionException =
      new MessageDto()
          .setAction(MessageDto.Action.ERROR.name())
          .setBody(
              parser.toJson(
                  new BodyError(new KeypleReaderNotFoundException("keyple reader not found"))));

  @Before
  public void setUp() {
    handler =
        new AbstractMessageHandler() {
          @Override
          protected void onMessage(MessageDto msg) {}
        };
  }

  @Test
  public void checkError_noError_doNothing() {
    handler.checkError(response);
  }

  @Test(expected = KeypleReaderNotFoundException.class)
  public void checkError_readerNotFound() {
    handler.checkError(responseWithKRNotFounExceptionException);
  }

  @Test(expected = KeypleReaderIOException.class)
  public void checkError_knownError_throwException() {
    handler.checkError(responseWithKRIoExceptionException);
  }
}
