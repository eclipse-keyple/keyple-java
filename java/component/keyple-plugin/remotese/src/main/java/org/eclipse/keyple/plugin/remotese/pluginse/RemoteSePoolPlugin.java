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
package org.eclipse.keyple.plugin.remotese.pluginse;

import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxPoolEngine;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;


/**
 * Remote SE Plugin Creates a virtual reader when a remote readers connect Manages the dispatch of
 * events received from remote readers
 */
public class RemoteSePoolPlugin extends RemoteSePlugin implements ReaderPoolPlugin {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSePoolPlugin.class);


    //Slave Node where the ReaderPluginPool is located
    String slaveNodeId;
    RemoteMethodTxPoolEngine rmTxEngine;

    /**
     * Only {@link MasterAPI} can instanciate a RemoteSePlugin
     */
    RemoteSePoolPlugin(VirtualReaderSessionFactory sessionManager, DtoSender sender, long rpcTimeout) {
        super(sessionManager, sender,rpcTimeout);

        //allocate a rmTxPoolEngine
        rmTxEngine = new RemoteMethodTxPoolEngine(sender, rpcTimeout);
    }

    public void bind(String slaveNodeId){
        this.slaveNodeId = slaveNodeId;
    }

    @Override
    public SortedSet<String> getReaderGroupReferences() {
        /*
         Not implemented
         */
        return null;
    }

    @Override
    public SeReader allocateReader(String groupReference) {

        if(slaveNodeId==null){
            throw new IllegalStateException("RemoteSePluginPool is not bind to any Slave Node, invoke RemoteSePluginPool#bind()");
        }

        //call remote method for allocateReader
        RmPoolAllocateTx allocate =
                new RmPoolAllocateTx(groupReference,this,this.dtoSender, slaveNodeId, dtoSender.getNodeId());
        this.rmTxEngine.add(allocate);
        try {
            return allocate.getResponse();
        } catch (KeypleRemoteException e) {
            return null;//todo throw exception here
        }

    }

    @Override
    public void releaseReader(SeReader seReader) {
        //call remote method for releaseReader
        if(slaveNodeId==null){
            throw new IllegalStateException("RemoteSePluginPool is not bind to any Slave Node, invoke RemoteSePluginPool#bind() first");
        }

        if(!(seReader instanceof VirtualReader)){
            throw new IllegalStateException("RemoteSePluginPool can release only VirtualReader, seReader is type of "+ seReader.getClass().getSimpleName());
        }

        VirtualReader virtualReader = (VirtualReader) seReader;

        //call remote method for allocateReader
        RmPoolReleaseTx releaseTx =
                new RmPoolReleaseTx(virtualReader.getNativeReaderName(),virtualReader.getName(),this,this.dtoSender, slaveNodeId, dtoSender.getNodeId());
        this.rmTxEngine.add(releaseTx);

        try {
            releaseTx.getResponse();
        } catch (KeypleRemoteException e) {
            logger.error("Impossible to release reader {} {}", virtualReader.getName(), virtualReader.getNativeReaderName());
        }
    }

    RemoteMethodTxPoolEngine getRmTxEngine() {
        return rmTxEngine;
    }



}
