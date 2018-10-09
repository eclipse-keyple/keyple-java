/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.eclipse.keyple.plugin.remote_se.rse;

import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.keyple.plugin.remote_se.transport.*;
import org.eclipse.keyple.plugin.remote_se.transport.json.JsonParser;
import org.eclipse.keyple.seproxy.ProxyReader;
import org.eclipse.keyple.seproxy.ReaderPlugin;
import org.eclipse.keyple.seproxy.SeResponseSet;
import org.eclipse.keyple.seproxy.event.ObservablePlugin;
import org.eclipse.keyple.seproxy.event.PluginEvent;
import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.util.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class RsePlugin extends Observable implements ObservablePlugin, DtoDispatcher {

    private static final Logger logger = LoggerFactory.getLogger(RsePlugin.class);

    private final RseSessionManager sessionManager;

    // virtal readers
    private final SortedSet<RseReader> rseReaders = new TreeSet<RseReader>();

    public RsePlugin() {
        sessionManager = new RseSessionManager();
        logger.info("RemoteSePlugin");
    }

    @Override
    public String getName() {
        return "RemoteSePlugin";
    }

    @Override
    public Map<String, String> getParameters() {
        return null;
    }

    @Override
    public void setParameter(String key, String value) throws IllegalArgumentException {}

    @Override
    public void setParameters(Map<String, String> parameters) throws IllegalArgumentException {

    }

    @Override
    public SortedSet<? extends ProxyReader> getReaders() {
        return rseReaders;
    }

    @Override
    public ProxyReader getReader(String name) throws KeypleReaderNotFoundException {
        for (RseReader RseReader : rseReaders) {
            if (RseReader.getName().equals(name)) {
                return RseReader;
            }
        }
        throw new KeypleReaderNotFoundException("reader with name not found : " + name);
    }

    public ProxyReader getReaderByRemoteName(String remoteName)
            throws KeypleReaderNotFoundException {
        for (RseReader RseReader : rseReaders) {
            if (RseReader.getRemoteName().equals(remoteName)) {
                return RseReader;
            }
        }
        throw new KeypleReaderNotFoundException(
                "reader with Remote Name not found : " + remoteName);
    }

    /**
     * Create a virtual reader
     * 
     * @param name
     * @param session
     * @return
     */
    private void connectRemoteReader(String name, IReaderSession session) {
        logger.debug("connectRemoteReader {}", name);

        // check if reader is not already connected (by name)
        if (!isReaderConnected(name)) {
            logger.info("Connecting a new RemoteSeReader with name {} with session {}", name,
                    session.getSessionId());

            RseReader rseReader = new RseReader(session, name);
            rseReaders.add(rseReader);
            notifyObservers(new PluginEvent(getName(), rseReader.getName(),
                    PluginEvent.EventType.READER_CONNECTED));
            logger.info("*****************************");
            logger.info(" CONNECTED {} ", rseReader.getName());
            logger.info("*****************************");

        } else {
            logger.warn("RemoteSeReader with name {} is already connected", name);
        }
        // todo errors
    }

    private void onReaderEvent(ReaderEvent event, String sessionId) {
        logger.debug("OnReaderEvent {}", event);
        logger.debug("Dispatch ReaderEvent to the appropriate Reader {} {}", event.getReaderName(),
                sessionId);
        try {
            // todo dispatch is managed by name, should take sessionId also
            RseReader rseReader = (RseReader) getReaderByRemoteName(event.getReaderName());
            rseReader.onRemoteReaderEvent(event);

        } catch (KeypleReaderNotFoundException e) {
            e.printStackTrace();
        }

    }



    /**
     * Add an observer. This will allow to be notified about all readers or plugins events.
     *
     * @param observer Observer to notify
     */

    public void addObserver(ObservablePlugin.PluginObserver observer) {
        logger.trace("[{}][{}] addObserver => Adding an observer.", this.getClass(),
                this.getName());
        super.addObserver(observer);
    }

    /**
     * Remove an observer.
     *
     * @param observer Observer to stop notifying
     */

    public void removeObserver(ObservablePlugin.PluginObserver observer) {
        logger.trace("[{}] removeObserver => Deleting a reader observer", this.getName());
        super.removeObserver(observer);
    }



    /**
     * This method shall be called only from a SE Proxy plugin or reader implementing
     * AbstractObservableReader or AbstractObservablePlugin. Push a ReaderEvent / PluginEvent of the
     * selected AbstractObservableReader / AbstractObservablePlugin to its registered Observer.
     *
     * @param event the event
     */

    public final void notifyObservers(PluginEvent event) {
        logger.trace("[{}] AbstractObservableReader => Notifying a plugin event: ", this.getName(),
                event);
        setChanged();
        super.notifyObservers(event);

    }

    private Boolean isReaderConnected(String name) {
        for (RseReader RseReader : rseReaders) {
            if (RseReader.getRemoteName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    // todo
    @Override
    public int compareTo(ReaderPlugin o) {
        return 0;
    }


    // called by node transport
    @Override
    public TransportDto onDTO(TransportDto transportDto) {

        KeypleDto keypleDTO = transportDto.getKeypleDTO();
        TransportDto out = null;
        logger.debug("onDTO {}", KeypleDtoHelper.toJson(transportDto.getKeypleDTO()));


        // if (msg.getHash()!=null && !KeypleDtoHelper.verifyHash(msg, msg.getHash())) {
        // return exception, msg is signed but has is invalid
        // }

        // READER EVENT : SE_INSERTED, SE_REMOVED etc..
        if (keypleDTO.getAction().equals(KeypleDtoHelper.READER_EVENT)) {
            logger.info("**** ACTION - READER_EVENT ****");

            ReaderEvent event =
                    JsonParser.getGson().fromJson(keypleDTO.getBody(), ReaderEvent.class);

            // dispatch reader event
            this.onReaderEvent(event, keypleDTO.getSessionId());

            // chain response with a seRequest if needed
            out = isSeRequestToSendBack(transportDto);

        } else if (keypleDTO.getAction().equals(KeypleDtoHelper.READER_CONNECT)) {
            logger.info("**** ACTION - READER_CONNECT ****");

            // parse msg
            JsonObject body = JsonParser.getGson().fromJson(keypleDTO.getBody(), JsonObject.class);
            String readerName = body.get("nativeReaderName").getAsString();
            Boolean isAsync = body.get("isAsync").getAsBoolean();
            String nodeId = body.get("nodeId").getAsString();


            String sessionId = sessionManager.generateSessionId(readerName, nodeId);
            IReaderSession rseSession;// reader session

            if (!isAsync) {
                // todo
                logger.error("Rse Plugin supports only Async session");
                throw new IllegalArgumentException("Rse Plugin needs a Async Session to work");
            } else {
                // rseSession = new ReaderAsyncSessionImpl(sessionId, message.getDtoSender());
                rseSession = new ReaderAsyncSessionImpl(sessionId);
                // add the web socket node as an observer for the session as the session will send
                // KeypleDto
                ((ReaderAsyncSessionImpl) rseSession).addObserver(transportDto.getDtoSender());

                this.connectRemoteReader(readerName, rseSession);
            }


            // response
            JsonObject respBody = new JsonObject();
            respBody.add("statusCode", new JsonPrimitive(0));
            respBody.add("nativeReaderName", new JsonPrimitive(readerName));
            out = transportDto.nextTransportDTO(new KeypleDto(KeypleDtoHelper.READER_CONNECT,
                    respBody.toString(), false, sessionId));

        } else if (keypleDTO.getAction().equals(KeypleDtoHelper.READER_DISCONNECT)) {
            logger.info("**** ACTION - READER_DISCONNECT ****");

            // not implemented yet
            out = transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());

        } else if (keypleDTO.getAction().equals(KeypleDtoHelper.READER_TRANSMIT)
                && !keypleDTO.isRequest()) {
            logger.info("**** RESPONSE - READER_TRANSMIT ****");

            // parse msg
            SeResponseSet seResponseSet =
                    JsonParser.getGson().fromJson(keypleDTO.getBody(), SeResponseSet.class);
            logger.debug("Receive responseSet from transmit {}", seResponseSet);
            RseReader reader = null;
            try {
                reader = this.getReaderBySessionId(keypleDTO.getSessionId());
                ((IReaderAsyncSession) reader.getSession()).asyncSetSeResponseSet(seResponseSet);

                // chain response with a seRequest if needed
                out = isSeRequestToSendBack(transportDto);

            } catch (KeypleReaderNotFoundException e) {
                e.printStackTrace();
                out = transportDto.nextTransportDTO(KeypleDtoHelper.ErrorDTO());
            }
        } else {
            logger.info("**** ERROR - UNRECOGNIZED ****");
            logger.error("Receive unrecognized message action : {} {} {} {}", keypleDTO.getAction(),
                    keypleDTO.getSessionId(), keypleDTO.getBody(), keypleDTO.isRequest());
            out = transportDto.nextTransportDTO(KeypleDtoHelper.NoResponse());
        }

        logger.debug("onDTO response {}", KeypleDtoHelper.toJson(out.getKeypleDTO()));
        return out;


    }



    private TransportDto isSeRequestToSendBack(TransportDto tdto) {
        TransportDto out = null;
        try {
            // retrieve reader by session
            RseReader rseReader = this.getReaderBySessionId(tdto.getKeypleDTO().getSessionId());

            if (rseReader.getSession().isAsync()
                    && ((IReaderAsyncSession) rseReader.getSession()).hasSeRequestSet()) {

                // send back seRequestSet
                out = tdto.nextTransportDTO(new KeypleDto(KeypleDtoHelper.READER_TRANSMIT,
                        JsonParser.getGson().toJson(
                                ((IReaderAsyncSession) rseReader.getSession()).getSeRequestSet()),
                        true, rseReader.getSession().getSessionId()));
            } else {
                // no response
                out = tdto.nextTransportDTO(KeypleDtoHelper.NoResponse());
            }

        } catch (KeypleReaderNotFoundException e) {
            logger.debug("Reader was not found by session", e);
            out = tdto.nextTransportDTO(KeypleDtoHelper.ErrorDTO());
        }

        return out;
    }


    private RseReader getReaderBySessionId(String sessionId) throws KeypleReaderNotFoundException {
        for (RseReader reader : rseReaders) {
            if (reader.getSession().getSessionId().equals(sessionId)) {
                return reader;
            }
        }
        throw new KeypleReaderNotFoundException(
                "Reader sesssion was not found for session : " + sessionId);
    }
}
