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
        logger.debug("Activate ThreadedWaitForSeRemoval {} ", this.state);
        ExecutorService executor =
                ((AbstractThreadedObservableLocalReader) reader).getExecutorService();

            if (reader instanceof SmartPresenceReader) {
                logger.trace("Reader is SmartPresence enabled");
                waitForCardAbsent = executor.submit(waitForCardAbsent(timeout));

            } else {
                // reader is not instanceof SmartPresenceReader
                // poll card with isPresentPing
                logger.trace("Reader is not SmartPresence enabled");
                waitForCardAbsentPing = executor.submit(waitForCardAbsentPing(timeout));

            }
    }

    /**
     * Invoke waitForCardAbsent
     * 
     * @return true is the card was removed
     */
    private Callable<Boolean> waitForCardAbsent(final long timeout) {
        logger.trace("Using method waitForCardAbsentNative");
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
    private Callable<Boolean> waitForCardAbsentPing(final long timeout) {
        return new Callable<Boolean>() {
            long counting = 0;
            long threeshold = 10;

            @Override
            public Boolean call() throws Exception {
                while (true) {
                    //logger.trace("Polling method isSePresentPing");
                    if (!reader.isSePresentPing()) {
                        onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);
                        return true;
                    }

                    if(counting>timeout){
                        onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);
                        return false;
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
        if (waitForCardAbsent != null && !waitForCardAbsent.isDone()) {
            waitForCardAbsent.cancel(true);
        }
        if (waitForCardAbsentPing != null && !waitForCardAbsentPing.isDone()) {
            waitForCardAbsentPing.cancel(true);
        }
    }

}
