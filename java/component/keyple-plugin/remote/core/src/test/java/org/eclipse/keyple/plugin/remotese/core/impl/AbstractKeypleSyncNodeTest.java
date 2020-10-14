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
package org.eclipse.keyple.plugin.remotese.core.impl;

import com.google.gson.JsonObject;

public abstract class AbstractKeypleSyncNodeTest extends AbstractKeypleNodeTest {

  String bodyPolling;
  String bodyLongPolling;
  String bodyLongPollingLongTimeout;

  {
    JsonObject body = new JsonObject();
    body.addProperty("strategy", ServerPushEventStrategy.Type.POLLING.name());
    bodyPolling = body.toString();

    body = new JsonObject();
    body.addProperty("strategy", ServerPushEventStrategy.Type.LONG_POLLING.name());
    body.addProperty("duration", 1);
    bodyLongPolling = body.toString();

    body = new JsonObject();
    body.addProperty("strategy", ServerPushEventStrategy.Type.LONG_POLLING.name());
    body.addProperty("duration", 5);
    bodyLongPollingLongTimeout = body.toString();
  }

  void setUp() {
    super.setUp();
  }
}
