package org.eclipse.keyple.plugin.android.cone2;

import org.eclipse.keyple.core.seproxy.event.ObservableReader;

public interface Cone2ContactlessReader extends ObservableReader {
    String READER_NAME = "AndroidCone2ContactlessReader";
    String PLUGIN_NAME = "Cone2Plugin";

    /**
     * This parameter sets the timeout used in the waitForCardAbsent method
     */
    String CHECK_FOR_ABSENCE_TIMEOUT_KEY = "CHECK_FOR_ABSENCE_TIMEOUT_KEY";
    /**
     * Default value for CHECK_FOR_ABSENCE_TIMEOUT parameter
     */
    String CHECK_FOR_ABSENCE_TIMEOUT_DEFAULT = "500";
    /**
     *  This parameter sets the thread wait timeout
     */
    String THREAD_WAIT_TIMEOUT_KEY = "THREAD_WAIT_TIMEOUT_KEY";
    /**
     * Default value for THREAD_WAIT_TIMEOUT parameter
     */
    String THREAD_WAIT_TIMEOUT_DEFAULT = "2000";
}
