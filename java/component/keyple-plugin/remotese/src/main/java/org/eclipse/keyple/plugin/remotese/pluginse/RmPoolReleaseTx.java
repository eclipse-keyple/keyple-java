package org.eclipse.keyple.plugin.remotese.pluginse;

import com.google.gson.JsonObject;
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

public class RmPoolReleaseTx extends RemoteMethodTx<Boolean> {

    private static final Logger logger = LoggerFactory.getLogger(RmPoolReleaseTx.class);

    RemoteSePoolPlugin virtualPoolPlugin;
    DtoSender dtoSender;

    public RmPoolReleaseTx(String nativeReaderName,
                           String virtualReaderName,
                           RemoteSePoolPlugin virtualPoolPlugin,
                           DtoSender dtoSender,
                           String slaveNodeId,
                           String requesterNodeId){
        super(null,
                nativeReaderName,
                virtualReaderName,
                slaveNodeId,
                requesterNodeId);
        this.dtoSender = dtoSender;
        this.virtualPoolPlugin = virtualPoolPlugin;
    }

    @Override
    public RemoteMethod getMethodName() {
        return RemoteMethod.POOL_RELEASE_READER;
    }

    @Override
    protected KeypleDto dto() {
        JsonObject body = new JsonObject();
        body.addProperty("nativeReaderName", nativeReaderName);

        return KeypleDtoHelper.buildRequest(
                getMethodName().getName(),
                body.toString(),
                null,
                nativeReaderName,
                virtualReaderName,
                requesterNodeId,
                targetNodeId,
                id);
    }


    @Override
    protected Boolean parseResponse(KeypleDto keypleDto) throws KeypleRemoteException {
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
            String nativeReaderName = body.get("nativeReaderName").getAsString();

            // create the Virtual Reader related to the Reader Allocation
            try {
                this.virtualPoolPlugin.disconnectRemoteReader(nativeReaderName,keypleDto.getRequesterNodeId());
                return true;
            } catch (KeypleReaderException e) {
                throw new KeypleRemoteException(e.getMessage());
            }


        }
    }
}
