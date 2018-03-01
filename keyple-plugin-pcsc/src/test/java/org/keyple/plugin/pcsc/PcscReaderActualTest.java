/*
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * All rights reserved. This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License version 2.0 which accompanies this distribution, and is
 * available at https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.html
 */

package org.keyple.plugin.pcsc;

import org.junit.Test;
import org.keyple.seproxy.ObservableReader;
import org.keyple.seproxy.ReaderEvent;
import org.keyple.seproxy.ReaderObserver;
import org.keyple.seproxy.exceptions.IOReaderException;

public class PcscReaderActualTest {

    public class MyReaderObserver implements ReaderObserver {

        private Thread lastThread;

        @Override
        public void notify(ReaderEvent event) {
            lastThread = Thread.currentThread();
            System.out.println("Observer: " + event + " / " + Thread.currentThread().getName());
            synchronized (this) {
                notify(); // It's the standard java notify, nothing to do with *our* notify
            }
        }
    }

    @Test
    public void testActual() throws IOReaderException, InterruptedException {
        PcscPlugin plugin = PcscPlugin.getInstance().setLogging(true);

        final MyReaderObserver observer = new MyReaderObserver();
        for (ObservableReader reader : plugin.getReaders()) {
            reader.addObserver(observer);
        }

        System.out.println("Waiting for card insertion/removal...");
        synchronized (observer) {
            observer.wait();
        }
        System.out.println("OK");


        System.out.println("Waiting for card insertion/removal (AGAIN)...");
        synchronized (observer) {
            observer.wait();
        }
        System.out.println("");

        Thread firstThread = observer.lastThread;
        System.out.println("First thread: " + firstThread);

        for (ObservableReader reader : plugin.getReaders()) {
            reader.deleteObserver(observer);
        }


        while (firstThread.getState() != Thread.State.TERMINATED) {
            System.out.println(
                    "Thread " + firstThread.getName() + " is still in " + firstThread.getState());
            Thread.sleep(1000);
        }

        for (ObservableReader reader : plugin.getReaders()) {
            reader.addObserver(observer);
        }

        synchronized (observer) {
            observer.wait();
        }

        Thread secondThread = observer.lastThread;

        if (secondThread.getState() != Thread.State.WAITING) {
            System.out.println("Second thread is waiting");
        }
    }
}
