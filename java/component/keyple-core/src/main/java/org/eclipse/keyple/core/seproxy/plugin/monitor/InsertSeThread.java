package org.eclipse.keyple.core.seproxy.plugin.monitor;

import org.eclipse.keyple.core.seproxy.plugin.AbstractThreadedObservableLocalReader;
import org.eclipse.keyple.core.seproxy.plugin.SmartInsertionReader;

public class InsertSeThread implements Runnable {

    AbstractThreadedObservableLocalReader reader;
    long WAIT_FOR_SE_INSERTION_EXIT_LATENCY;

    InsertSeThread(AbstractThreadedObservableLocalReader reader ,
                   long WAIT_FOR_SE_INSERTION_EXIT_LATENCY){
        this.reader = reader;
        this.WAIT_FOR_SE_INSERTION_EXIT_LATENCY = WAIT_FOR_SE_INSERTION_EXIT_LATENCY;
    }

    @Override
    public void run() {
        while (true) {
            //


        }
    }
}
