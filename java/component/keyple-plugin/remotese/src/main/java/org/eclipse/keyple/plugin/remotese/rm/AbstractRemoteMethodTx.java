/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.remotese.rm;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to implement blocking and non blocking call to a Remote Method that sends a
 * response
 * 
 * @param <T> : type of the response
 */
public abstract class AbstractRemoteMethodTx<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRemoteMethodTx.class);
    protected final String sessionId;
    protected final String nativeReaderName;
    protected final String virtualReaderName;
    protected final String targetNodeId;
    protected final String requesterNodeId;
    protected final String id;
    protected Boolean isRegistered;

    // response
    private T response;

    // exception thrown if any
    private KeypleRemoteException remoteException;

    // blocking mechanism
    private CountDownLatch lock;
    private IRemoteMethodTxCallback<T> callback;
    private long timeout;

    private DtoSender sender;

    protected AbstractRemoteMethodTx(String sessionId, String nativeReaderName,
            String virtualReaderName, String targetNodeId, String requesterNodeId) {
        this.sessionId = sessionId;
        this.nativeReaderName = nativeReaderName;
        this.virtualReaderName = virtualReaderName;
        this.targetNodeId = targetNodeId;
        this.requesterNodeId = requesterNodeId;

        // generate id
        this.id = UUID.randomUUID().toString();

    }

    void setDtoSender(DtoSender sender) {
        this.sender = sender;
    }

    void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Internal method to set manually the keypleDto response To be called by the tx manager
     *
     * @param keypleDto incoming message to parse response from
     * @throws KeypleRemoteException if a problem occurs while sending the keypleDto
     * @return response
     */
    protected abstract T parseResponse(KeypleDto keypleDto) throws KeypleRemoteException;

    /**
     * Return name of the Remote Method
     * 
     * @return : name of the remote method
     */
    public abstract RemoteMethodName getMethodName();


    /**
     * Non blocking method to execute results from the remote method call
     * 
     * @param callback get Result from this callback
     * @throws KeypleRemoteException if a problem occurs while sending
     */
    public void send(IRemoteMethodTxCallback<T> callback) throws KeypleRemoteException {
        this.callback = callback;
        sender.sendDTO(this.dto());
    }


    /**
     * Blocking method to execute results from the remote method call. To be called by the client
     * (used internally by rmCommands, do not use)
     *
     * @param rmTxEngine : local RemoteMethodTxEngine to execute the command into
     * @return T : result of the command
     * @throws KeypleRemoteException : if an
     */
    final public T execute(IRemoteMethodTxEngine rmTxEngine) throws KeypleRemoteException {

        // register this method to receive response
        rmTxEngine.register(this);

        if (!isRegistered) {
            throw new IllegalStateException(
                    "RemoteMethodTx#execute() can not be used until RemoteMethod is isRegistered in a RemoteMethodEngine, please call RemoteMethodEngine#register");
        }
        // logger.debug("Blocking Get {}", this.getClass().getCanonicalName());
        final AbstractRemoteMethodTx thisInstance = this;

        Thread asyncSend = new Thread() {
            @Override
            public void run() {
                try {
                    send(new IRemoteMethodTxCallback<T>() {
                        @Override
                        public void get(T response, KeypleRemoteException exception) {
                            logger.trace("Release lock of rm {} {}", thisInstance.getMethodName(),
                                    thisInstance.id);
                            lock.countDown();
                        }
                    });
                } catch (KeypleRemoteException e) {
                    logger.error("Exception while sending Dto", e);
                    thisInstance.remoteException = e;
                    lock.countDown();
                }
            }
        };

        try {
            lock = new CountDownLatch(1);
            logger.trace("" + "" + "Set callback on RemoteMethodTx {} {}",
                    this.getClass().getCanonicalName(), this.hashCode());
            asyncSend.start();
            logger.trace("Lock name:{}, id{}", thisInstance.getMethodName(), this.id);

            // lock until response is received
            boolean responseReceived = lock.await(timeout, TimeUnit.MILLISECONDS);

            if (responseReceived) {
                logger.trace("Unlock name:{}, id{}", thisInstance.getMethodName(), this.id);
                if (this.remoteException != null) {
                    throw remoteException;
                } else {
                    return response;
                }
            } else {
                /*
                 * timeout, no answer has been received
                 */
                throw new KeypleRemoteException(
                        "Waiting time elapsed, no answer received from the other node for method "
                                + this.getClass().getCanonicalName());
            }


        } catch (InterruptedException e) {
            throw new IllegalStateException(
                    "Thread locking in blocking transmitSet has encountered an exception", e);
        }
    }

    /**
     * Set the response contained in the keypleDto Response Call the callback of the RmMethod
     *
     * @param keypleDto
     */
    void setResponse(KeypleDto keypleDto) {
        try {
            this.response = parseResponse(keypleDto);
            this.callback.get(response, null);
        } catch (KeypleRemoteException e) {
            this.remoteException = e;
            this.callback.get(null, e);
        }
    }

    /**
     * Mark or Unmark as registered
     * 
     * @param registered true to register, false to unregister
     */
    public void setRegistered(Boolean registered) {
        isRegistered = registered;
    }

    /**
     * Generates a Request Dto for this Rm Method call
     * 
     * @return keypleDto
     */
    protected abstract KeypleDto dto();



}
