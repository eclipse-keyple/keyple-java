/********************************************************************************
 * Copyright (c) 2019 Calypso Networks Association https://www.calypsonet-asso.org/
 *
 * See the NOTICE file(s) distributed with this work for additional information regarding copyright
 * ownership.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.keyple.calypso.transaction;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.keyple.calypso.command.po.AbstractPoCommandBuilder;
import org.eclipse.keyple.calypso.command.po.AbstractPoResponseParser;
import org.eclipse.keyple.calypso.command.po.CalypsoPoCommand;
import org.eclipse.keyple.calypso.transaction.exception.CalypsoPoTransactionIllegalStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The PO command manager handles the AbstractPoCommandBuilder list updated by the "prepare" methods
 * of PoTransaction. It is used to keep builders between the time the commands are created and the
 * time their responses are parsed.
 * <p>
 * A flag (preparedCommandsProcessed) is used to manage the reset of the command list. It allows the
 * builders to be kept until the application creates a new list of commands.
 * <p>
 * This flag is set by calling the method notifyCommandsProcessed and reset when a new
 * AbstractPoCommandBuilder is added or when a attempt
 */
class PoCommandManager {
    private static final Logger logger = LoggerFactory.getLogger(PoCommandManager.class);

    /** The list to contain the prepared commands and their parsers */
    private final List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> poCommands =
            new ArrayList<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>>();
    /** The command index, incremented each time a command is added */
    private boolean preparedCommandsProcessed;
    private CalypsoPoCommand svLastCommand;
    private PoTransaction.SvSettings.Operation svOperation;
    private boolean svOperationPending = false;

    PoCommandManager() {
        preparedCommandsProcessed = true;
    }

    /**
     * Add a regular command to the builders and parsers list.
     * <p>
     * Handle the clearing of the list if needed.
     *
     * @param commandBuilder the command builder
     */
    void addRegularCommand(
            AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder) {
        /*
         * Reset the list if the preparation of the command is done after a previous processing
         * notified by notifyCommandsProcessed.
         */
        if (preparedCommandsProcessed) {
            poCommands.clear();
            preparedCommandsProcessed = false;
        }
        poCommands.add(commandBuilder);
    }

    /**
     * Add a StoredValue command to the builders and parsers list.
     * <p>
     * Handle the clearing of the list if needed.
     * <p>
     * Set up a mini state machine to manage the scheduling of Stored Value commands keeping the
     * position of the last SvGet/Operation in the command list.
     * <p>
     * The {@link PoTransaction.SvSettings.Operation} and {@link PoTransaction.SvSettings.Action}
     * are also used to check the consistency of the SV process.
     * <p>
     * The svOperationPending flag is set when an SV operation (Reload/Debit/Undebit) command is
     * added.
     *
     * @param commandBuilder the StoredValue command builder
     * @param svOperation the type of the current SV operation (Realod/Debit/Undebit)
     * @throws IllegalStateException if the provided command is not an SV command
     * @throws CalypsoPoTransactionIllegalStateException if the SV API is not properly used.
     */
    void addStoredValueCommand(
            AbstractPoCommandBuilder<? extends AbstractPoResponseParser> commandBuilder,
            PoTransaction.SvSettings.Operation svOperation) {
        if (preparedCommandsProcessed) {
            poCommands.clear();
            preparedCommandsProcessed = false;
        }

        // Check the logic of the SV command sequencing
        switch (commandBuilder.getCommandRef()) {
            case SV_GET:
                this.svOperation = svOperation;
                break;
            case SV_RELOAD:
            case SV_DEBIT:
            case SV_UNDEBIT:
                if (!poCommands.isEmpty()) {
                    throw new CalypsoPoTransactionIllegalStateException(
                            "This SV command can only be placed in the first position in the list of prepared commands");
                }

                if (svLastCommand != CalypsoPoCommand.SV_GET) {
                    // @see Calypso Layer ID 8.07/8.08 (200108)
                    throw new IllegalStateException(
                            "This SV command must follow an SV Get command");
                }

                // here, we expect the builder and the SV operation to be consistent
                if (svOperation != this.svOperation) {
                    logger.error("Sv operation = {}, current command = {}", this.svOperation,
                            svOperation);
                    throw new CalypsoPoTransactionIllegalStateException(
                            "Inconsistent SV operation.");
                }
                this.svOperation = svOperation;
                svOperationPending = true;
                break;
            default:
                throw new IllegalStateException("An SV command is expected.");
        }
        svLastCommand = commandBuilder.getCommandRef();

        poCommands.add(commandBuilder);
    }

    /**
     * Informs that the commands have been processed.
     * <p>
     * Just record the information. The initialization of the list of commands will be done only the
     * next time a command is added, this allows access to the parsers contained in the list..
     */
    void notifyCommandsProcessed() {
        preparedCommandsProcessed = true;
    }

    /**
     * @return the current AbstractPoCommandBuilder list
     */
    List<AbstractPoCommandBuilder<? extends AbstractPoResponseParser>> getPoCommandBuilders() {
        /* Clear the list if no command has been added since the last call to a process method. */
        if (preparedCommandsProcessed) {
            poCommands.clear();
            preparedCommandsProcessed = false;
        }
        return poCommands;
    }

    /**
     * @return true if the {@link PoCommandManager} has commands
     */
    boolean hasCommands() {
        return !poCommands.isEmpty();
    }

    /**
     * Indicates whether an SV (Reload/Debit) operation has been requested
     *
     * @return true if a reload or debit command has been requested
     */
    public boolean isSvOperationPending() {
        return svOperationPending;
    }
}
