package org.eclipse.keyple.plugin.remote_se.nse;

import org.eclipse.keyple.plugin.remote_se.transport.KeypleDTO;

public abstract class KeypleCommand {


    public abstract KeypleDTO process(KeypleDTO keypleDTO);


}
