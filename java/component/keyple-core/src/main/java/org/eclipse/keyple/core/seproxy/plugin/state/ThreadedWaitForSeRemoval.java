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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.eclipse.keyple.core.seproxy.exception.KeypleIOReaderException;
import org.eclipse.keyple.core.seproxy.plugin.AbstractObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.SmartRemovalReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedWaitForSeRemoval extends DefaultWaitForSeRemoval {

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(ThreadedWaitForSeRemoval.class);

    private Future<Boolean> waitForCardAbsentPing;
    private Future<Boolean> waitForCardAbsent;
    private final ExecutorService executor;


    public ThreadedWaitForSeRemoval(AbstractObservableLocalReader reader,
            ExecutorService executor) {
        super(reader);
        this.executor = executor;
    }

    @Override
    public void onActivate() {
        logger.debug("[{}] Activate ThreadedWaitForSeRemoval", this.reader.getName());

        if (reader instanceof SmartRemovalReader) {
            logger.trace("[{}] Reader is SmartRemoval enabled ", this.reader.getName());
            waitForCardAbsent = executor.submit(waitForCardAbsentNative());

        } else {
            // reader is not instanceof SmartRemovalReader
            // poll card with isPresentPing
            logger.trace("[{}] Reader is not SmartRemoval enabled, use isSePresentPing method",
                    this.reader.getName());
            waitForCardAbsentPing = executor.submit(waitForCardAbsentPing());

        }
    }

    /**
     * Invoke waitForCardAbsentNative
     * 
     * @return true is the card was removed
     */
    private Callable<Boolean> waitForCardAbsentNative() {
        logger.debug("[{}] Using method waitForCardAbsentNative", this.reader.getName());

        return new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    if (((SmartRemovalReader) reader).waitForCardAbsentNative()) {
                        // timeout is already managed within the task
                        onEvent(AbstractObservableLocalReader.InternalEvent.SE_REMOVED);
                        return true;
                    } else {
                        // se was not removed within timeout
                        // onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);
                        logger.trace(
                                "[{}] waitForCardAbsentNative => return false, task interrupted",
                                reader.getName());
                        return false;
                    }
                } catch (KeypleIOReaderException e) {
                    logger.trace(
                            "[{}] waitForCardAbsentNative => Error while polling card with waitForCardAbsentNative",
                            reader.getName());
                    onEvent(AbstractObservableLocalReader.InternalEvent.STOP_DETECT);
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


                    if (logger.isTraceEnabled()) {
                        logger.trace("[{}] Polling retries :{}, time left {} ms", reader.getName(),
                                retries);
                    }
                    // wait a bit
                    Thread.sleep(threeshold);
                    counting = counting + threeshold;

                }
            }
        };
    }


    @Override
    public void onDeactivate() {
        logger.debug("[{}] onDeactivate ThreadedWaitForSeRemoval", this.reader.getName());
        if (waitForCardAbsent != null && !waitForCardAbsent.isDone()) {
            waitForCardAbsent.cancel(true);// TODO not tested
        }
        if (waitForCardAbsentPing != null && !waitForCardAbsentPing.isDone()) {
            waitForCardAbsentPing.cancel(true);// TODO not tested
        }
    }

}
