package org.keyple.examples.pc;

import org.keyple.plugin.pcsc.PcscPlugin;
import org.keyple.seproxy.*;
import org.keyple.seproxy.exceptions.IOReaderException;
import org.keyple.util.event.Observable;

public class ObservableEventTest {
    public static void main(String[] args) throws Exception {
        final Object waitBeforeEnd = new Object();

        PcscPlugin.getInstance().addObserver(new Observable.Observer<PluginEvent>() {
            @Override
            public void update(Observable<? extends PluginEvent> observable, PluginEvent event) {
                if (event instanceof ReaderPresencePluginEvent) {
                    ReaderPresencePluginEvent presence = (ReaderPresencePluginEvent) event;
                    if (presence.isAdded()) {
                        System.out.println("New reader: " + presence.getReader().getName());
                        ProxyReader reader = presence.getReader();
                        if (reader instanceof ObservableReader) {
                            ((ObservableReader) reader).addObserver(new Observable.Observer<ReaderEvent>() {
                                @Override
                                public void update(Observable<? extends ReaderEvent> observable, ReaderEvent event) {
                                    if (event.getEventType().equals(ReaderEvent.EventType.SE_INSERTED)) {
                                        System.out.println("Card inserted on: " + event.getReader().getName());
                                        analyseCard(event.getReader());
                                    }
                                }

                                private void analyseCard(ObservableReader reader) {
                                    try {
                                        System.out.println("Card present = " + reader.isSEPresent());
                                    } catch (IOReaderException ex) {
                                        ex.printStackTrace(System.err);
                                    }
                                }
                            });
                        }
                    } else {
                        System.out.println("Removed reader: " + presence.getReader().getName());
                        synchronized (waitBeforeEnd) {
                            waitBeforeEnd.notify();
                        }
                    }
                }
            }
        });

        synchronized (waitBeforeEnd) {
            waitBeforeEnd.wait();
        }
    }
}
