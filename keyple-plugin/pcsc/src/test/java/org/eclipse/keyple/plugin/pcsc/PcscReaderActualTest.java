/********************************************************************************
 * Copyright (c) 2018 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.plugin.pcsc;

import org.eclipse.keyple.seproxy.event.ReaderEvent;
import org.eclipse.keyple.seproxy.exception.KeypleReaderException;
import org.eclipse.keyple.seproxy.plugin.AbstractObservableReader;
import org.eclipse.keyple.util.Observable;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class PcscReaderActualTest {

    public class MyReaderObserver implements Observable.Observer<ReaderEvent> {

        private Thread lastThread;

        /*
         * @Override public void notify(ReaderEvent event) { lastThread = Thread.currentThread();
         * System.out.println("Observer: " + event + " (from thread" +
         * Thread.currentThread().getName() + ")"); if (event == ReaderEventSE_INSERTED) {
         * synchronized (this) { notify(); // It's the standard java notify, nothing to do with
         * *our* notify } } }
         */

        @Override
        public void update(ReaderEvent event) {
            lastThread = Thread.currentThread();
            System.out.println("Observer: " + event + " (from thread"
                    + Thread.currentThread().getName() + ")");
            if (event.getEventType() == ReaderEvent.EventType.SE_INSERTED) {
                synchronized (this) {
                    notify(); // It's the standard java notify, nothing to do with *our* notify
                }
            }
        }
    }

    /**
     * This test registers/deregisters on an {@link AbstractObservableReader} twice. This allows to
     * verify we create and dispose threads correctly.
     * 
     * @throws KeypleReaderException
     * @throws InterruptedException
     */
    @Ignore // <-- This test works but can only be executed with an actual card present
    @Test
    public void testActual() throws KeypleReaderException, InterruptedException {
        PcscPlugin plugin = PcscPlugin.getInstance().setLogging(true);

        final MyReaderObserver observer = new MyReaderObserver();
        for (AbstractObservableReader reader : plugin.getReaders()) {
            reader.addObserver(observer);
        }

        // We wait to see if the thread management works correctly (thread is created here)
        System.out.println("Waiting for card insertion (1/3)... ");
        synchronized (observer) {
            observer.wait();
        }
        System.out.println("OK");


        // And then one more time to make sure we can do it twice
        System.out.println("Waiting for card insertion (2/3)...");
        synchronized (observer) {
            observer.wait();
        }
        System.out.println("OK");

        // We look at the thread that was used
        Thread firstThread = observer.lastThread;
        System.out.println("First thread: " + firstThread);

        // Remove the observer from the observable (thread disappears)
        for (AbstractObservableReader reader : plugin.getReaders()) {
            reader.removeObserver(observer);
        }

        // Re-add it (thread is created)
        for (AbstractObservableReader reader : plugin.getReaders()) {
            reader.addObserver(observer);
        }

        // Wait for the card event
        System.out.println("Waiting for card insertion (3/3)...");
        synchronized (observer) {
            observer.wait();
        }

        // We look at the second thread that was used
        Thread secondThread = observer.lastThread;

        // Making sure it's not the same
        Assert.assertNotEquals(firstThread, secondThread);

        Assert.assertEquals(secondThread.getState(), Thread.State.RUNNABLE);

        // Now if things went fast enough the first thread (which consumes the same PCSC resources)
        // isn't dead yet.
        System.out.println("Waiting for first thread...");
        firstThread.join();
        System.out.println("Done !");
        System.out.println(
                "Thread " + firstThread.getName() + " is now " + firstThread.getState() + " !");

        // Remove the observer from the observable (thread disappears)
        for (AbstractObservableReader reader : plugin.getReaders()) {
            reader.removeObserver(observer);
        }
        System.out.println("Waiting for last thread...");
        secondThread.join();
        System.out.println("Done !");
    }
}
