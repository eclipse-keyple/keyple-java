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
package org.eclipse.keyple.plugin.remote.integration.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.eclipse.keyple.core.service.exception.KeypleRuntimeException;
import org.eclipse.keyple.plugin.remote.KeypleMessageDto;

public class JacksonParser {

  private static ObjectMapper parser = new ObjectMapper();

  public static String toJson(KeypleMessageDto message) {
    try {
      return parser.writeValueAsString(message);
    } catch (JsonProcessingException e) {
      throw new KeypleRuntimeException("Error while serializing dto", e);
    }
  }

  public static KeypleMessageDto fromJson(String data) {
    try {
      return parser.readValue(data, KeypleMessageDto.class);
    } catch (JsonProcessingException e) {
      throw new KeypleRuntimeException("Error while deserializing dto", e);
    }
  }

  public static String toJson(List<KeypleMessageDto> messages) {
    try {
      return parser.writeValueAsString(messages);
    } catch (JsonProcessingException e) {
      throw new KeypleRuntimeException("Error while serializing dto", e);
    }
  }

  public static List<KeypleMessageDto> fromJsonList(String data) {
    try {
      return parser.readValue(data, new TypeReference<List<KeypleMessageDto>>() {});
    } catch (JsonProcessingException e) {
      throw new KeypleRuntimeException("Error while deserializing dto", e);
    }
  }
}
