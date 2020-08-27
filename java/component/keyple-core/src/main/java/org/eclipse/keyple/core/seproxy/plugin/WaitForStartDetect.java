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
package org.eclipse.keyple.core.seproxy.plugin;

import java.util.concurrent.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wait for Start SE Detection
 *
 * <p>The state during which the reader does not wait for a SE to be inserted but for a signal from
 * the application to do so (switch to the WAIT_FOR_SE_INSERTION state).
 *
 * <ul>
 *   <li>Upon START_DETECT event, the machine changes state for WAIT_FOR_SE_INSERTION.
 * </ul>
 */
public class WaitForStartDetect extends AbstractObservableState {

  /** logger */
  private static final Logger logger = LoggerFactory.getLogger(WaitForStartDetect.class);

  public WaitForStartDetect(AbstractObservableLocalReader reader) {
    super(MonitoringState.WAIT_FOR_START_DETECTION, reader);
  }

  public WaitForStartDetect(
      AbstractObservableLocalReader reader,
      AbstractMonitoringJob monitoringJob,
      ExecutorService executorService) {
    super(MonitoringState.WAIT_FOR_START_DETECTION, reader, monitoringJob, executorService);
  }

  @Override
  public void onEvent(AbstractObservableLocalReader.InternalEvent event) {
    if (logger.isTraceEnabled()) {
      logger.trace(
          "[{}] onEvent => Event {} received in currentState {}", reader.getName(), event, state);
    }
    /*
     * Process InternalEvent
     */
    switch (event) {
      case START_DETECT:
        switchState(MonitoringState.WAIT_FOR_SE_INSERTION);
        break;

      default:
        logger.warn(
            "[{}] Ignore =>  Event {} received in currentState {}", reader.getName(), event, state);
        break;
    }
  }
}
