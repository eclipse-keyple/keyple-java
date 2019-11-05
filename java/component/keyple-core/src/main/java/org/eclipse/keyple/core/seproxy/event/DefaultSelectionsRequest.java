package org.eclipse.keyple.core.seproxy.event;

import org.eclipse.keyple.core.seproxy.ChannelControl;
import org.eclipse.keyple.core.seproxy.MultiSeRequestProcessing;
import org.eclipse.keyple.core.seproxy.message.SeRequest;

import java.util.Set;

public interface DefaultSelectionsRequest {
    MultiSeRequestProcessing getMultiSeRequestProcessing();

    ChannelControl getChannelControl();

    Set<SeRequest> getSelectionSeRequestSet();
}
