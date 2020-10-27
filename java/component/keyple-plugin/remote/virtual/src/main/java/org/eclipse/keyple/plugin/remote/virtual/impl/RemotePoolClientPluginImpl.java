package org.eclipse.keyple.plugin.remote.virtual.impl;

import com.google.gson.JsonObject;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderIOException;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remote.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remote.virtual.RemotePoolClientPlugin;

import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;

final class RemotePoolClientPluginImpl extends AbstractRemotePlugin implements RemotePoolClientPlugin {

    /**
     * (package-private)<br>
     * Constructor.
     * @param name The name of the plugin.
     * @throws KeypleReaderException when an issue is raised with reader
     */
    RemotePoolClientPluginImpl(String name) {
        super(name);
    }

    @Override
    public SortedSet<String> getReaderGroupReferences() {
        String sessionId = generateSessionId();
        try{
            // Open a new session on the node
            node.openSession(sessionId);

            KeypleMessageDto request =new KeypleMessageDto()
                    .setAction(KeypleMessageDto.Action.GET_READER_GROUP_REFERENCES.name())
                    .setSessionId(sessionId)
                    .setBody(null);

            KeypleMessageDto response = getNode().sendRequest(request);

            checkError(response);
            String readerGroupReferencesJson =
                    KeypleJsonParser.getParser()
                            .fromJson(response.getBody(), JsonObject.class)
                            .get("readerGroupReferences")
                            .toString();

            return KeypleJsonParser.getParser().fromJson(readerGroupReferencesJson, SortedSet.class);

        }finally{
            node.closeSessionSilently(sessionId);
        }
    }

    @Override
    public SeReader allocateReader(String groupReference) {
        String sessionId = generateSessionId();
        try{
            // Open a new session on the node
            node.openSession(sessionId);

            JsonObject body = new JsonObject();
            body.addProperty("groupReference", groupReference);
            KeypleMessageDto request =  new KeypleMessageDto()
                    .setAction(KeypleMessageDto.Action.ALLOCATE_READER.name())
                    .setSessionId(sessionId)
                    .setBody(body.toString());

            KeypleMessageDto response = getNode().sendRequest(request);

            checkError(response);
            VirtualReader reader = new VirtualReader(getName(), response.getNativeReaderName(), getNode(),null,response.getClientNodeId());
            readers.put(response.getNativeReaderName(), reader);

            return reader;
        }finally{
            node.closeSessionSilently(sessionId);
        }
    }

    @Override
    public void releaseReader(SeReader reader) {
        Assert.getInstance().notNull(reader,"reader");
        if(!readers.containsKey(reader.getName())){
            throw new IllegalArgumentException("reader is not a virtual reader of this pool plugin");
        }

        String sessionId = generateSessionId();
        try{
            // Open a new session on the node
            node.openSession(sessionId);

            KeypleMessageDto request = new KeypleMessageDto()
                    .setAction(KeypleMessageDto.Action.RELEASE_READER.name())
                    .setSessionId(sessionId)
                    .setVirtualReaderName(reader.getName())
                    .setNativeReaderName(((VirtualReader)reader).getNativeReaderName())
                    .setBody(null);

            KeypleMessageDto response = node.sendRequest(request);
            checkError(response);

            readers.remove(reader.getName());
        }finally{
            node.closeSessionSilently(sessionId);
        }

    }

    @Override
    protected Map<String, SeReader> initNativeReaders() throws KeypleReaderIOException {
        return new ConcurrentHashMap<String, SeReader>();
    }

    @Override
    public SeReader getReader(String name) {
        Assert.getInstance().notNull(name, "reader name");
        SeReader seReader = readers.get(name);
        if (seReader == null) {
            throw new KeypleReaderNotFoundException(name);
        }
        return seReader;
    }

    @Override
    protected void onMessage(KeypleMessageDto msg) {
        //not used
    }
}
