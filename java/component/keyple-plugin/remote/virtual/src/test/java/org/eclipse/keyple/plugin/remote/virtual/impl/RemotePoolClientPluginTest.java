package org.eclipse.keyple.plugin.remote.virtual.impl;

import org.eclipse.keyple.core.seproxy.SeProxyService;
import org.eclipse.keyple.core.seproxy.SeReader;
import org.eclipse.keyple.plugin.remote.core.KeypleServerAsync;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class RemotePoolClientPluginTest {

    KeypleServerAsync asyncEndpoint;
    RemotePoolClientPluginImpl remotePoolPlugin;
    String groupReference = "groupReference1";

    @Before
    public void setUp() {
        asyncEndpoint = Mockito.mock(KeypleServerAsync.class);
    }

    @Test
    public void factory_withSyncEndpoint_shouldCreate_PluginWith_SyncNode(){
        SeProxyService.getInstance().registerPlugin(RemotePoolClientPluginFactory.builder().withSyncNode().build());
        assertThat(RemotePoolClientUtils.getSyncPlugin()).isNotNull();
        assertThat(RemotePoolClientUtils.getSyncNode()).isNotNull();

        //unregister plugin
        SeProxyService.getInstance().unregisterPlugin(RemotePoolClientUtils.getSyncPlugin().getName());
    }

    @Test
    public void factory_withAsyncEndpoint_shouldCreate_PluginWith_AsyncNode(){
        SeProxyService.getInstance().registerPlugin(RemotePoolClientPluginFactory.builder().withAsyncNode(asyncEndpoint).build());
        assertThat(RemotePoolClientUtils.getAsyncPlugin()).isNotNull();
        assertThat(RemotePoolClientUtils.getAsyncNode()).isNotNull();

        //unregister plugin
        SeProxyService.getInstance().unregisterPlugin(RemotePoolClientUtils.getAsyncPlugin().getName());
    }

    @Test
    public void allocateReader_onSuccess_shouldCreate_virtualReader(){
        remotePoolPlugin = (RemotePoolClientPluginImpl) RemotePoolClientPluginFactory.builder().withSyncNode().build().getPlugin();
        SeReader virtualReader = remotePoolPlugin.allocateReader(groupReference);
    }

    @Test
    public void allocateReader_onFailure_shouldThrow_exception(){

    }

    @Test
    public void releaseReader_onSuccess_shouldDelete_virtualReader(){

    }

    @Test
    public void releaseReader_onFailure_shouldThrow_exception(){

    }

    @Test
    public void getReferenceGroups_onSuccess_shouldReturn_result(){

    }

    @Test
    public void getReferenceGroups_onFailure_shouldThrow_exception(){

    }


    /*
     *
     */

}
