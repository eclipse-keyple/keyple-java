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
package org.eclipse.keyple.core.plugin.reader;

import java.util.concurrent.ExecutorService;
import org.eclipse.keyple.core.reader.event.ObservableReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wait for card Processing State
 *
 * <p>The state during which the card is being processed by the application.
 *
 * <ul>
 *   <li>Upon SE_PROCESSED event, the machine changes state for WAIT_FOR_SE_REMOVAL or
 *       WAIT_FOR_SE_DETECTION according to the {@link ObservableReader.PollingMode} setting.
 *   <li>Upon CARD_REMOVED event, the machine changes state for WAIT_FOR_SE_INSERTION or
 *       WAIT_FOR_SE_DETECTION according to the {@link ObservableReader.PollingMode} setting.
 *   <li>Upon STOP_DETECT event, the machine changes state for WAIT_FOR_SE_DETECTION.
 * </ul>
 */
class WaitForCardProcessingState extends AbstractObservableState {

  /** logger */
  private static final Logger logger = LoggerFactory.getLogger(WaitForCardProcessingState.class);

  WaitForCardProcessingState(AbstractObservableLocalReader reader) {
    super(MonitoringState.WAIT_FOR_SE_PROCESSING, reader);
  }

  WaitForCardProcessingState(
      AbstractObservableLocalReader reader,
      AbstractMonitoringJob monitoringJob,
      ExecutorService executorService) {
    super(MonitoringState.WAIT_FOR_SE_PROCESSING, reader, monitoringJob, executorService);
  }

  @Override
  void onEvent(AbstractObservableLocalReader.InternalEvent event) {
    if (logger.isTraceEnabled()) {
      logger.trace(
          "[{}] onEvent => Event {} received in currentState {}", reader.getName(), event, state);
    }
    /*
     * Process InternalEvent
     */
    switch (event) {
      case SE_PROCESSED:
        if (this.reader.getPollingMode() == ObservableReader.PollingMode.REPEATING) {
          switchState(MonitoringState.WAIT_FOR_SE_REMOVAL);
        } else {
          // We close the channels now and notify the application of
          // the CARD_REMOVED event.
          this.reader.processCardRemoved();
          switchState(MonitoringState.WAIT_FOR_START_DETECTION);
        }
        break;

      case CARD_REMOVED:
        // the card has been removed, we close all channels and return to
        // the currentState of waiting
        // for insertion
        // We notify the application of the CARD_REMOVED event.
        reader.processCardRemoved();
        if (reader.getPollingMode() == ObservableReader.PollingMode.REPEATING) {
          switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
        } else {
          switchState(MonitoringState.WAIT_FOR_START_DETECTION);
        }
        break;

      case STOP_DETECT:
        reader.processCardRemoved();
        switchState(MonitoringState.WAIT_FOR_START_DETECTION);
        break;

      default:
        logger.warn(
            "[{}] Ignore =>  Event {} received in currentState {}", reader.getName(), event, state);
        break;
    }
  }
}
