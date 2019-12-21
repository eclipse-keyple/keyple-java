package org.eclipse.keyple.core.util;

import java.util.concurrent.ThreadFactory;

/**
 * factory that allows to create thread with custom name
 */
public class NamedThreadFactory implements ThreadFactory {
    String name;

    public NamedThreadFactory(String name){
        this.name = name;
    }

    public Thread newThread(Runnable r) {
        return new Thread(r, name);
    }
}