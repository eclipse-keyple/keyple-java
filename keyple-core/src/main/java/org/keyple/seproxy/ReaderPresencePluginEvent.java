package org.keyple.seproxy;

public class ReaderPresencePluginEvent extends PluginEvent {
    private final boolean added;
    private final ProxyReader reader;

    public ReaderPresencePluginEvent(boolean added, ProxyReader reader) {
        this.added = added;
        this.reader = reader;
    }

    /**
     * Define if the reader was added or removed
     * @return true for added
     */
    public boolean isAdded() {
        return added;
    }

    /**
     * Reader that was added or removed
     * @return Reader
     */
    public ProxyReader getReader() {
        return reader;
    }
}
