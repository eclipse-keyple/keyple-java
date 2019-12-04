package org.eclipse.keyple.plugin.android.cone2;

import org.eclipse.keyple.core.seproxy.SeReader;

public interface Cone2ContactReader extends SeReader {
    String READER_NAME = "AndroidCone2ContactReader";
    String PLUGIN_NAME = "Cone2Plugin";

    /**
     * THis parameter allows SAM reader selection
     */
    String CONTACT_INTERFACE_ID = "CONTACT_INTERFACE_ID";
    /**
     * SAM 1
     */
    String CONTACT_INTERFACE_ID_SAM_1 = "1";
    /**
     * SAM 2
     */
    String CONTACT_INTERFACE_ID_SAM_2 = "2";
}
