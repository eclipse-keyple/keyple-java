package org.eclipse.keyple.plugin.android.cone2;

import android.content.Context;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import fr.coppernic.sdk.ask.Defines;
import fr.coppernic.sdk.ask.Reader;
import fr.coppernic.sdk.utils.io.InstanceListener;

/**
 * This class provides the one and only Reader instance.
 * The purpose of providing only one instance is to share the uniqueAskReaderInstance between
 * contactless and contact interfaces.
 */
public class Cone2AskReader {
    // The unique reader instance for whole API
    private static WeakReference<Reader> uniqueAskReaderInstance = new WeakReference<Reader>(null);

    private static ExecutorService threadPool = Executors.newFixedThreadPool(1);

    // This variable assures that we are not checking for card when it is transmitting.
    // The reason is that the ASK reader is working synchronously and the reader logic is
    // asynchronous. If a command has been sent to the reader using transmitApdu, and then before
    // the answer a checkSePresence call is made, the reader will fall in timeout causing the API to
    // interpret this as a card removed event.
    private static ReentrantLock isTransmitting = new ReentrantLock();

    // Interface needed because the instantiation of the Reader instance is asynchronous.
    public interface ReaderListener {
        void onInstanceAvailable(Reader reader);
        void onError(int error);
    }

    /**
     * Provides the one and only instance of ASK reader, this must be called before any call to
     * getInstance()
     * @param context A context
     * @param listener ReaderListener, needed because the instantiation is asynchronous
     */
    public static void getInstance(Context context, final ReaderListener listener) {
        // If uniqueAskReaderInstance is null, instantiates it
        if (uniqueAskReaderInstance.get() == null) {
            Reader.getInstance(context, new InstanceListener<Reader>() {
                @Override
                public void onCreated(final Reader reader) {

                    // Opens reader
                    int ret = reader.cscOpen(
                        fr.coppernic.sdk.core.Defines.SerialDefines.ASK_READER_PORT,
                        115200,
                        false);

                    if (ret != Defines.RCSC_Ok) {
                        listener.onError(ret);
                    }

                    threadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            // Initializes reader
                            StringBuilder sb = new StringBuilder();
                            int ret = reader.cscVersionCsc(sb);

                            // Stores the instance
                            Cone2AskReader.uniqueAskReaderInstance = new WeakReference<Reader>(reader);

                            if (ret != Defines.RCSC_Ok) {
                                listener.onError(ret);
                            } else {
                                listener.onInstanceAvailable(Cone2AskReader
                                        .uniqueAskReaderInstance
                                        .get());
                            }
                        }
                    });
                }

                @Override
                public void onDisposed(Reader reader) {

                }
            });
        } else {
            // Or provides the current instance
            listener.onInstanceAvailable(uniqueAskReaderInstance.get());
        }
    }


    /**
     * Returns the unique instance of ASK reader. This should not be called as long as
     * getInstance(Context context, final ReaderListener listener) has not successfully executed.
     * @return Unique Reader instance
     */
    @Nullable
    public static Reader getInstance() {
        return uniqueAskReaderInstance.get();
    }

    /**
     * Resets the instance, this is needed when the reader is powered off for instance.
     */
    static void clearInstance () {
        if (uniqueAskReaderInstance.get() != null) {
            uniqueAskReaderInstance.get().destroy();
            uniqueAskReaderInstance = new WeakReference<Reader>(null);
        }
    }

    /**
     * Acquires the lock to synchronize the communication with the reader
     */
    public static void acquireLock() {
        isTransmitting.lock();
    }

    /**
     * Releases the lock
     */
    public static void releaseLock() {
        isTransmitting.unlock();
    }
}
