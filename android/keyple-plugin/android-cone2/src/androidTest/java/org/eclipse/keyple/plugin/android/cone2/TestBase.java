package org.eclipse.keyple.plugin.android.cone2;

public class TestBase {

    private final Object syncObject = new Object();

    protected void block() throws InterruptedException {
        synchronized(syncObject) {
            syncObject.wait();
        }
    }

    protected void unblock() {
        synchronized(syncObject) {
            syncObject.notify();
        }
    }
}
