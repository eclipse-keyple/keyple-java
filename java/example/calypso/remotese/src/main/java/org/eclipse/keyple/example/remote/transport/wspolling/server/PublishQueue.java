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
package org.eclipse.keyple.example.remote.transport.wspolling.server;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishQueue<T> {

    private final BlockingQueue<T> q;
    private final String webClientId;
    private static final Logger logger = LoggerFactory.getLogger(PublishQueue.class);


    public PublishQueue(String webClientId) {
        this.webClientId = webClientId;
        q = new LinkedBlockingQueue<T>(1);
    }

    public String getWebClientId() {
        return this.webClientId;
    }

    public void init() {
        if (!q.isEmpty()) {
            try {
                T state = q.take();
                logger.error("Remove un-consumed element : " + state);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            logger.debug("Queue is empty");

        }
    }

    public void publish(T state) {
        logger.debug("Publish new state : " + state);
        try {
            if (q.size() > 0) {
                logger.warn("Warning call init() before publishing");
            }
            q.put(state);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public T get(long timeout) throws InterruptedException {

        T element = q.poll(timeout, TimeUnit.MILLISECONDS);
        return element;

    }


}
