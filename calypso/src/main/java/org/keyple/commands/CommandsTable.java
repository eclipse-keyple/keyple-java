package org.keyple.commands;

public interface CommandsTable {

    public String getName();

    public byte getInstructionByte();

    public Class<?> getCommandBuilderClass();

    public Class<?> getResponseParserClass();

}
