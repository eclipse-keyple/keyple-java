/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.core.seproxy.plugin.state;

import java.util.concurrent.*;
import org.eclipse.keyple.core.seproxy.plugin.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedWaitForSeRemoval extends DefaultWaitForSeRemoval {

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(ThreadedWaitForSeRemoval.class);

    private Future<Boolean> waitForCardAbsentPing;
    private Future<Boolean> waitForCardAbsent;
    private final long timeout;

    public ThreadedWaitForSeRemoval(AbstractObservableLocalReader reader, long timeout) {
        super(reader);
        this.timeout = timeout;
    }

    @Override
    public void activate() {
        logger.debug("[{}] Activate ThreadedWaitForSeRemoval", this.reader.getName());
        ExecutorService executor =
                ((AbstractThreadedObservableLocalReader) reader).getExecutorService();

            if (reader instanceof SmartPresenceReader) {
                logger.trace("[{}] Reader is SmartPresence enabled ", this.reader.getName());
                waitForCardAbsent = executor.submit(waitForCardAbsent());

            } else {
                // reader is not instanceof SmartPresenceReader
                // poll card with isPresentPing
                logger.trace("[{}] Reader is not SmartPresence enabled, use isSePresentPing method", this.reader.getName());
                waitForCardAbsentPing = executor.submit(waitForCardAbsentPing());

            }
    }

    /**
     * Invoke waitForCardAbsent
     * 
     * @return true is the card was removed
     */
    private Callable<Boolean> waitForCardAbsent() {
        logger.debug("[{}] Using method waitForCardAbsentNative", this.reader.getName());

        return new Callable<Boolean>() {
            @Override
            public Boolean call(){
                if (((SmartPresenceReader) reader).waitForCardAbsentNative(timeout)) {// timeout is already managed within the task
                    onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);
                    return true;
                } else {
                    // se was not removed within timeout
                    onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);
                    return false;
                }
            }
        };
    }

    /**
     * Loop on the isSePresentPing method until the SE is removed or timeout is reached
     * 
     * @return true is the card was removed
     */
    private Callable<Boolean> waitForCardAbsentPing() {
        logger.trace("[{}] waitForCardAbsentPing => Timeout : {} ms", timeout);

        return new Callable<Boolean>() {
            long counting = 0;
            long threeshold = 200;
            long retries = 0;

            @Override
            public Boolean call() throws Exception {
                while (true) {
                    logger.debug("[{}] Polling from isSePresentPing", reader.getName());
                    if (!reader.isSePresentPing()) {
                        onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);
                        return true;
                    }
                    retries++;

                    long left = timeout-counting;

                    if(left<0){
                        onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);
                        return false;
                    }
                    if(logger.isTraceEnabled()){
                        logger.trace("[{}] Polling retries :{}, time left {} ms", reader.getName(), retries, left);

                    }
                    // wait a bit
                    Thread.sleep(threeshold);
                    counting = counting + threeshold;

                }
            }
        };
    }


    @Override
    public void deActivate() {
        logger.debug("[{}] deActivate ThreadedWaitForSeRemoval", this.reader.getName());
        if (waitForCardAbsent != null && !waitForCardAbsent.isDone()) {
            waitForCardAbsent.cancel(true);//TODO not tested
        }
        if (waitForCardAbsentPing != null && !waitForCardAbsentPing.isDone()) {
            waitForCardAbsentPing.cancel(true);//TODO not tested
        }
    }

}
