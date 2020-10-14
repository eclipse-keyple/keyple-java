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
package org.eclipse.keyple.example.generic.pc.Demo_ObservableReaderNotification;

import org.eclipse.keyple.core.seproxy.SmartCardService;
import org.eclipse.keyple.plugin.pcsc.PcscPluginFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo_ObservableReaderNotification_Pcsc {
  private static final Logger logger =
      LoggerFactory.getLogger(Demo_ObservableReaderNotification_Pcsc.class);
  public static final Object waitBeforeEnd = new Object();

  public static void main(String[] args) throws Exception {
    ObservableReaderNotificationEngine demoEngine = new ObservableReaderNotificationEngine();

    // Get the instance of the SmartCardService (Singleton pattern)
    SmartCardService smartCardService = SmartCardService.getInstance();

    // Assign PcscPlugin to the SmartCardService
    smartCardService.registerPlugin(new PcscPluginFactory());

    // /* Set observers *//**/
    demoEngine.setPluginObserver();

    logger.info("Wait for reader or card insertion/removal");

    // Wait indefinitely. CTRL-C to exit.
    synchronized (waitBeforeEnd) {
      waitBeforeEnd.wait();
    }
  }
}
