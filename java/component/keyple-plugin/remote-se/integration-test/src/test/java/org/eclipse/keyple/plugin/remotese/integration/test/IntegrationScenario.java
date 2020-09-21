package org.eclipse.keyple.plugin.remotese.integration.test;

public interface IntegrationScenario {

    /**
     * A successful aid selection is executed locally on the terminal followed by a remoteService call
     * to launch the remote Calypso session. The SE content is sent during this first called along
     * with custom data. All this information is received by the server to select and execute the
     * corresponding ticketing scenario.
     *
     * <p>At the end of a successful calypso session, custom data is sent back to the client as a
     * final result.
     *
     * <p>This scenario can be executed on Sync node and Async node.
     */
     void execute1_localselection_remoteTransaction_successful();


    /**
     * The client application invokes the remoteService with enabling observability capabilities. As a
     * result the server creates a Observable Virtual Reader that receives native reader events such
     * as SE insertions and removals.
     *
     * <p>A SE Insertion is simulated locally followed by a SE removal 1 second later.
     *
     * <p>The SE Insertion event is sent to the Virtual Reader whose observer starts a remote Calypso
     * session. At the end of a successful calypso session, custom data is sent back to the client as
     * a final result.
     *
     * <p>The operation is executed twice with two different users.
     *
     * <p>After the second SE insertion, Virtual Reader observers are cleared to purge the server
     * virtual reader.
     */
     void execute2_defaultSelection_onMatched_transaction_successful();


    /**
     * Similar to scenario 1 without the local aid selection. In this case, the server application is
     * responsible for ordering the aid selection.
     */
     void execute3_remoteselection_remoteTransaction_successful();

    /** Similar to scenario 3 with two concurrent clients. */
    void execute4_multiclient_remoteselection_remoteTransaction_successful();
}
