package org.eclipse.keyple.plugin.remotese.nativese.method;

import com.google.gson.JsonObject;
import org.eclipse.keyple.core.seproxy.ReaderPoolPlugin;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.seproxy.message.SeResponse;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethod;
import org.eclipse.keyple.plugin.remotese.rm.RemoteMethodExecutor;
import org.eclipse.keyple.plugin.remotese.transport.json.JsonParser;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDto;
import org.eclipse.keyple.plugin.remotese.transport.model.KeypleDtoHelper;
import org.eclipse.keyple.plugin.remotese.transport.model.TransportDto;

public class RmPoolReleaseExecutor implements RemoteMethodExecutor {

    ReaderPoolPlugin poolPlugin;

    public RemoteMethod getMethodName() {
        return RemoteMethod.POOL_ALLOCATE_READER;
    }

    public RmPoolReleaseExecutor(ReaderPoolPlugin poolPlugin){
        this.poolPlugin = poolPlugin;
    }

    @Override
    public TransportDto execute(TransportDto transportDto) {

        KeypleDto keypleDto = transportDto.getKeypleDTO();
        TransportDto out = null;

        // Extract info from keypleDto
        JsonObject body = JsonParser.getGson().fromJson(keypleDto.getBody(), JsonObject.class);
        String nativeReaderName = body.get("nativeReaderName").getAsString();

        //Find reader to release
        SeReader seReader = null;
        try {
            seReader = poolPlugin.getReader(nativeReaderName);

            // Execute Remote Method
            poolPlugin.releaseReader(seReader);

            //Build Response
            JsonObject bodyResp = new JsonObject();
            bodyResp.addProperty("nativeReaderName", seReader.getName());

            out = transportDto.nextTransportDTO(KeypleDtoHelper.buildResponse(
                    getMethodName().getName(),
                    bodyResp.toString(),
                    null,
                    seReader.getName(),
                    null,
                    keypleDto.getTargetNodeId(),
                    keypleDto.getRequesterNodeId(),
                    keypleDto.getId()));

        } catch (KeypleReaderNotFoundException e) {
            // if an exception occurs, send it into a keypleDto to the Master
            out = transportDto.nextTransportDTO(KeypleDtoHelper.ExceptionDTO(
                    getMethodName().getName(),
                    e,
                    null,
                    null,
                    null,
                    keypleDto.getTargetNodeId(),
                    keypleDto.getRequesterNodeId(),
                    keypleDto.getId()));
        }

        return out;
    }
}
