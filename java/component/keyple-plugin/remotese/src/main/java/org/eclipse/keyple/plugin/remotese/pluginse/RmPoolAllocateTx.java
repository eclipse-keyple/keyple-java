package org.eclipse.keyple.plugin.remotese.pluginse;

import com.google.gson.JsonObject;
import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.protocol.TransmissionMode;
import org.eclipse.keyple.plugin.remotese.exception.KeypleRemoteException;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodTx;
import org.eclipse.keyple.plugin.remotese.transport.DtoSender;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RmPoolAllocateTx extends RemoteMethodTx<SeReader> {

    private static final Logger logger = LoggerFactory.getLogger(RmPoolAllocateTx.class);


    String groupReference;
    RemoteSePluginPool virtualPoolPlugin;
    DtoSender dtoSender;

    public RmPoolAllocateTx(String groupReference,
                            RemoteSePluginPool virtualPoolPlugin,
                            DtoSender dtoSender,
                            String slaveNodeId,
                            String requesterNodeId){
        super(null,
                null,
                null,
                slaveNodeId,
                requesterNodeId);
        this.groupReference = groupReference;
        this.dtoSender = dtoSender;
        this.virtualPoolPlugin = virtualPoolPlugin;
    }

    @Override
    public RemoteMethod getMethodName() {
        return RemoteMethod.POOL_ALLOCATE_READER;
    }

    @Override
    protected KeypleDto dto() {
        JsonObject body = new JsonObject();
        body.addProperty("groupReference", groupReference);

        return KeypleDtoHelper.buildRequest(
                getMethodName().getName(),
                body.toString(),
                null,
                null,
                null,
                requesterNodeId,
                targetNodeId,
                id);
    }


    @Override
    protected SeReader parseResponse(KeypleDto keypleDto) throws KeypleRemoteException {
        logger.trace("KeypleDto : {}", keypleDto);
        if (KeypleDtoHelper.containsException(keypleDto)) {
            logger.trace("KeypleDto contains an exception: {}", keypleDto);
            KeypleReaderException ex =
                    JsonParser.getGson().fromJson(keypleDto.getBody(), KeypleReaderException.class);
            throw new KeypleRemoteException(
                    "An exception occurs while calling the remote method transmitSet", ex);
        } else {
            logger.trace("KeypleDto contains a response: {}", keypleDto);

            JsonObject body = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);
            String transmissionMode = body.get("transmissionMode").getAsString();
            String slaveNodeId = keypleDto.getRequesterNodeId();

            // create the Virtual Reader related to the Reader Allocation
            try {
                VirtualReader virtualReader = (VirtualReader) this.virtualPoolPlugin.createVirtualReader(slaveNodeId,
                        nativeReaderName, this.dtoSender, TransmissionMode.valueOf(transmissionMode));

                return virtualReader;

            } catch (KeypleReaderException e) {
                throw new KeypleRemoteException(e.getMessage());
            }


        }
    }
}
