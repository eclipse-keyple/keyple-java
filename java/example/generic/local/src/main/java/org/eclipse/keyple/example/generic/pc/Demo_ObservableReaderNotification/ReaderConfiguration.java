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
package org.eclipse.keyple.example.generic.pc.Demo_ObservableReaderNotification;

import org.eclipse.keyple.core.service.event.ObservableReader;
import org.eclipse.keyple.core.service.event.ReaderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReaderConfiguration {

  private static final Logger logger = LoggerFactory.getLogger(ReaderConfiguration.class);

  static ObservableReader.ReaderObserver getObserver() {
    return new ObservableReader.ReaderObserver() {
      public void update(ReaderEvent event) {
        /* just log the event */
        logger.info(
            "Event: PLUGINNAME = {}, READERNAME = {}, EVENT = {}",
            event.getPluginName(),
            event.getReaderName(),
            event.getEventType().name());

        switch (event.getEventType()) {
          case CARD_MATCHED:
            /*
             * Informs the underlying layer of the end of the card processing, in order to
             * manage the removal sequence.
             */
            ((ObservableReader) (event.getReader())).finalizeCardProcessing();
            break;

          case CARD_INSERTED:
            /*
             * end of the card processing is automatically done
             */
            break;
        }
      }
    };
  };
}
