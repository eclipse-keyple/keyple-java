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
package org.eclipse.keyple.calypso.command.sam.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.Gson;
import org.eclipse.keyple.calypso.command.sam.CalypsoSamCommand;
import org.eclipse.keyple.core.command.exception.KeypleSeCommandException;
import org.eclipse.keyple.core.util.json.BodyError;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.junit.Before;
import org.junit.Test;

public class SamCommandExceptionTest {

  Gson parser;

  @Before
  public void setUp() {
    parser = KeypleJsonParser.getParser();
  }

  @Test
  public void serializeException() {
    CalypsoSamAccessForbiddenException source =
        new CalypsoSamAccessForbiddenException("message", CalypsoSamCommand.CARD_CIPHER_PIN, 2);
    String json = parser.toJson(new BodyError(source));
    assertThat(json).doesNotContain("stackTrace");
    KeypleSeCommandException target =
        (KeypleSeCommandException) parser.fromJson(json, BodyError.class).getException();
    assertThat(target).isEqualToComparingFieldByFieldRecursively(source);
  }
}
