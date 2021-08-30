package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;

import javax.annotation.Nullable;
import java.util.List;

public interface CommandsManager {

    /**
     * Register a sub-command.
     * @param superiorCommand The sub command to register.
     */
    void registerCommand(SuperiorCommand superiorCommand);

    /**
     * Unregister a sub-command.
     * @param superiorCommand The sub command to register.
     */
    void unregisterCommand(SuperiorCommand superiorCommand);

    /**
     * Register a sub-command to the admin command.
     * @param superiorCommand The sub command to unregister.
     */
    void registerAdminCommand(SuperiorCommand superiorCommand);

    /**
     * Unregister a sub-command from the admin command.
     * @param superiorCommand The sub command to unregister.
     */
    void unregisterAdminCommand(SuperiorCommand superiorCommand);

    /**
     * Get all the registered sub-commands.
     */
    List<SuperiorCommand> getSubCommands();

    /**
     * Get a sub command by its label.
     * @param commandLabel The label of the sub command.
     * @return The sub command if exists or null.
     */
    @Nullable
    SuperiorCommand getCommand(String commandLabel);

    /**
     * Get all the registered admin sub-commands.
     */
    List<SuperiorCommand> getAdminSubCommands();

    /**
     * Get an admin sub command by its label.
     * @param commandLabel The label of the sub command.
     * @return The sub command if exists or null.
     */
    @Nullable
    SuperiorCommand getAdminCommand(String commandLabel);

}
