package org.eclipse.keyple.plugin.remotese.pluginse;

import org.eclipse.keyple.core.seproxy.ReaderPlugin;
import org.eclipse.keyple.core.seproxy.exception.KeypleReaderNotFoundException;

public interface RemoteSePlugin extends ReaderPlugin {

     VirtualReaderImpl getReaderByRemoteName(String remoteName, String slaveNodeId) throws KeypleReaderNotFoundException;
}
