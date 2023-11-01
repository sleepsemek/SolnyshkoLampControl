package com.example.lampcontrol.Models;

public class CommandData {
    private String command;
    private String sourceCharacteristicUUID;

    public CommandData(String command, String sourceCharacteristicUUID) {
        this.command = command;
        this.sourceCharacteristicUUID = sourceCharacteristicUUID;
    }

    public String getCommand() {
        return command;
    }

    public String getSourceCharacteristicUUID() {
        return sourceCharacteristicUUID;
    }
}
