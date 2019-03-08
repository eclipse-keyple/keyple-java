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

import java.util.HashMap;
import java.util.Map;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the KeypleDto polling publish queue
 */
public class PublishQueueManager {

    private final Logger logger = LoggerFactory.getLogger(PublishQueueManager.class);

    // clientId
    final Map<String, PublishQueue<KeypleDto>> queues;

    public PublishQueueManager() {

        logger.info("Initialize PublishQueueManager");
        queues = new HashMap<String, PublishQueue<KeypleDto>>();
    }

    public PublishQueue create(String webClientId) {
        logger.debug("Create a PublishQueue for webClientId {}", webClientId);
        if (webClientId == null) {
            throw new IllegalArgumentException("webClientId must not be null");
        }
        PublishQueue<KeypleDto> queue = new PublishQueue<KeypleDto>(webClientId);
        queues.put(webClientId, queue);
        return queues.get(webClientId);
    }

    public PublishQueue get(String webClientId) {
        if (webClientId == null) {
            throw new IllegalArgumentException("webClientId must not be null");
        }
        return queues.get(webClientId);
    }

    public void delete(String webClientId) {
        if (webClientId == null) {
            throw new IllegalArgumentException("webClientId must not be null");
        }
        queues.remove(webClientId);
    }

    public Boolean exists(String webClientId) {
        if (webClientId == null) {
            throw new IllegalArgumentException("webClientId must not be null");
        }
        return queues.containsKey(webClientId);
    }

}
