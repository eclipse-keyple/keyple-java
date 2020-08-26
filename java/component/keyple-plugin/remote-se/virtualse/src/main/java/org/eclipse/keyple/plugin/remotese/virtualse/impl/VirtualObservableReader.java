package org.eclipse.keyple.plugin.remotese.virtualse.impl;

import com.google.gson.JsonObject;
import org.eclipse.keyple.core.seproxy.event.AbstractDefaultSelectionsRequest;
import org.eclipse.keyple.core.seproxy.event.ObservableReader;
import org.eclipse.keyple.core.seproxy.event.ReaderEvent;
import org.eclipse.keyple.core.seproxy.plugin.ObservableReaderNotifier;
import org.eclipse.keyple.core.util.Assert;
import org.eclipse.keyple.core.util.json.KeypleJsonParser;
import org.eclipse.keyple.plugin.remotese.core.KeypleMessageDto;
import org.eclipse.keyple.plugin.remotese.core.impl.AbstractKeypleNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

final class VirtualObservableReader extends AbstractVirtualReader implements ObservableReaderNotifier {

    private static final Logger logger = LoggerFactory.getLogger(VirtualObservableReader.class);

    /* The observers of this object */
    private final List<ReaderObserver> observers;

    /**
     * (package-private)<br>
     * Constructor
     *
     * @param pluginName       The name of the plugin (must be not null).
     * @param nativeReaderName The name of the native reader (must be not null).
     * @param node             The associated node (must be not null).
     */
    VirtualObservableReader(String pluginName, String nativeReaderName, AbstractKeypleNode node) {
        super(pluginName, nativeReaderName, node);
        observers = new ArrayList<ReaderObserver>(1);
    }

    @Override
    public void notifyObservers(ReaderEvent event) {

        logger.trace("[{}] Notifying a reader event to {} observers. EVENTNAME = {}",
                this.getName(), this.countObservers(), event.getEventType().getName());

        List<ReaderObserver> observersCopy = new ArrayList<ReaderObserver>(observers);

        for (ObservableReader.ReaderObserver observer : observersCopy) {
            observer.update(event);//todo send each message in another thread?
        }
    }

    @Override
    public void addObserver(ReaderObserver observer) {
        Assert.getInstance().notNull(observer,"Reader Observer");

        if(observers.add(observer)){
            logger.trace("[{}] Added reader observer '{}'", getName(),observer.getClass().getSimpleName());
        };

    }

    @Override
    public void removeObserver(ReaderObserver observer) {
        Assert.getInstance().notNull(observer,"Reader Observer");
        if(observers.remove(observer)){
            logger.trace("[{}] Deleted reader observer '{}'", this.getName(),observer.getClass().getSimpleName());
        }
    }

    @Override
    public void clearObservers() {
        observers.clear();
        logger.trace("[{}] Clear reader observers", this.getName());
    }

    @Override
    public int countObservers() {
        return observers.size();
    }

    @Override
    public void startSeDetection(PollingMode pollingMode) {

    }

    @Override
    public void stopSeDetection() {

    }

    @Override
    public void setDefaultSelectionRequest(AbstractDefaultSelectionsRequest defaultSelectionsRequest, NotificationMode notificationMode) {
        setDefaultSelectionRequest(defaultSelectionsRequest,notificationMode,null);
    }

    @Override
    public void setDefaultSelectionRequest(AbstractDefaultSelectionsRequest defaultSelectionsRequest, NotificationMode notificationMode, PollingMode pollingMode) {
        sendRequest(KeypleMessageDto.Action.SET_DEFAULT_SELECTION, setDefaultSelectionBody(defaultSelectionsRequest, notificationMode, pollingMode));
    }

    @Override
    public void finalizeSeProcessing() {

    }

    private JsonObject setDefaultSelectionBody(AbstractDefaultSelectionsRequest defaultSelectionsRequest, NotificationMode notificationMode, PollingMode pollingMode) {
        // Extract info from the message
        JsonObject body =
                new JsonObject();

        body.addProperty("defaultSelectionsRequest",
                        KeypleJsonParser.getParser().toJson(defaultSelectionsRequest));

        body.addProperty("notificationMode", KeypleJsonParser.getParser().toJson(notificationMode));

        if(pollingMode!=null){
            body.addProperty("pollingMode", KeypleJsonParser.getParser().toJson(pollingMode));
        }

        return body;
    }

}
