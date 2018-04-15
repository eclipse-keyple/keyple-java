package org.keyple.seproxy;

public class ErrorPluginEvent extends PluginEvent {
    private final Exception exception;

    public ErrorPluginEvent(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
