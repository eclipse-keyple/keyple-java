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
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxEngine;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTxPoolEngine;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedSet;


/**
 * Remote SE Plugin Creates a virtual reader when a remote readers connect Manages the dispatch of
 * events received from remote readers
 */
public class RemoteSePluginPool extends RemoteSePlugin implements ReaderPoolPlugin {

    private static final Logger logger = LoggerFactory.getLogger(RemoteSePluginPool.class);


    //Slave Node where the ReaderPluginPool is located
    String slaveNodeId;
    RemoteMethodTxPoolEngine rmTxEngine;

    /**
     * Only {@link MasterAPI} can instanciate a RemoteSePlugin
     */
    RemoteSePluginPool(VirtualReaderSessionFactory sessionManager, DtoSender sender, long rpcTimeout) {
        super(sessionManager, sender,rpcTimeout);

        //allocate a rmTxPoolEngine
        rmTxEngine = new RemoteMethodTxPoolEngine(sender, rpcTimeout);
    }

    void bind(String slaveNodeId){
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
            logger.error("RemoteSePluginPool is not bind to any Slave Node, invoke RemoteSePluginPool#bind()");
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
            logger.error("RemoteSePluginPool is not bind to any Slave Node, invoke RemoteSePluginPool#bind()");
            throw new IllegalStateException("RemoteSePluginPool is not bind to any Slave Node, invoke RemoteSePluginPool#bind()");
        }
    }

    RemoteMethodTxPoolEngine getRmTxEngine() {
        return rmTxEngine;
    }

}
