package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;

import java.util.List;

public interface CommandsManager {

    /**
     * Register a sub-command.
     * @param superiorCommand The sub command to register.
     */
    void registerCommand(SuperiorCommand superiorCommand);

    /**
     * Register a sub-command to the admin command.
     * @param superiorCommand The sub command to register.
     */
    void registerAdminCommand(SuperiorCommand superiorCommand);

    /**
     * Get all the registered sub-commands.
     */
    List<SuperiorCommand> getSubCommands();

    /**
     * Get all the registered admin sub-commands.
     */
    List<SuperiorCommand> getAdminSubCommands();

}
