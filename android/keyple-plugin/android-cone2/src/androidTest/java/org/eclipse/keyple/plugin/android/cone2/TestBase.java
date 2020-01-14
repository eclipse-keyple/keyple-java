package org.eclipse.keyple.plugin.android.cone2;

public class TestBase {
    private Object syncObject = new Object();

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

    protected void block(Object object) throws InterruptedException {
        synchronized(object) {
            object.wait();
        }
    }

    protected void unblock(Object object) {
        synchronized(object) {
            object.notify();
        }
    }
}
