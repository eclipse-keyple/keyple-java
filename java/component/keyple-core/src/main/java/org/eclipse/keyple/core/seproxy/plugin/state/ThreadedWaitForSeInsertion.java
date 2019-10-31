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
import org.eclipse.keyple.core.seproxy.plugin.SmartInsertionReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class ThreadedWaitForSeInsertion extends DefaultWaitForSeInsertion {

    private Future<Boolean> waitForCarPresent;
    private final ExecutorService executor;

    /** logger */
    private static final Logger logger = LoggerFactory.getLogger(ThreadedWaitForSeInsertion.class);

    public ThreadedWaitForSeInsertion(AbstractObservableLocalReader reader,
            ExecutorService executor) {
        super(reader);
        this.executor = executor;

    }

    @Override
    public void onActivate() {
        logger.trace("[{}] Activate => ThreadedWaitForSeInsertion", reader.getName());
        waitForCarPresent = executor.submit(waitForCardPresent());
        // logger.debug("End of onActivate currentState {} ",this.currentState);

    }

    private Callable<Boolean> waitForCardPresent() {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() {
                logger.trace("[{}] Invoke waitForCardPresent asynchronously", reader.getName());
                try {
                    if (((SmartInsertionReader) reader).waitForCardPresent()) {
                        onEvent(AbstractObservableLocalReader.InternalEvent.SE_INSERTED);
                        return true;
                    } else {
                        logger.trace("[{}] waitForCardPresent => return false, task interrupted",
                                reader.getName());
                        return false;
                    }
                } catch (KeypleIOReaderException e) {
                    logger.trace(
                            "[{}] waitForCardPresent => Error while polling card with waitForCardPresent",
                            reader.getName());
                    onEvent(AbstractObservableLocalReader.InternalEvent.STOP_DETECT);
                    return false;
                }
                // onEvent(AbstractObservableLocalReader.InternalEvent.TIME_OUT);
            }
        };
    }


    @Override
    public void onDeactivate() {
        logger.trace("[{}] onDeactivate => ThreadedWaitForSeInsertion", reader.getName());
        if (waitForCarPresent != null && !waitForCarPresent.isDone()) {
            boolean canceled = waitForCarPresent.cancel(true);
            logger.trace(
                    "[{}] onDeactivate => cancel runnable waitForCarPresent by thead interruption {}",
                    reader.getName(), canceled);
        }
    }


}
