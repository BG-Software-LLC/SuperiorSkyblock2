package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public interface CommandsManager {

    /**
     * Register a sub-command.
     *
     * @param superiorCommand The sub command to register.
     */
    void registerCommand(SuperiorCommand superiorCommand);

    /**
     * Unregister a sub-command.
     *
     * @param superiorCommand The sub command to register.
     */
    void unregisterCommand(SuperiorCommand superiorCommand);

    /**
     * Register a sub-command to the admin command.
     *
     * @param superiorCommand The sub command to unregister.
     */
    void registerAdminCommand(SuperiorCommand superiorCommand);

    /**
     * Unregister a sub-command from the admin command.
     *
     * @param superiorCommand The sub command to unregister.
     */
    void unregisterAdminCommand(SuperiorCommand superiorCommand);

    /**
     * Get all the registered sub-commands.
     */
    List<SuperiorCommand> getSubCommands();

    /**
     * Get all the registered sub-commands.
     *
     * @param includeDisabled Whether to include disabled commands.
     */
    List<SuperiorCommand> getSubCommands(boolean includeDisabled);

    /**
     * Get a sub command by its label.
     *
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
     *
     * @param commandLabel The label of the sub command.
     * @return The sub command if exists or null.
     */
    @Nullable
    SuperiorCommand getAdminCommand(String commandLabel);

    /**
     * Dispatch a sub command.
     * If the sub command does not exist, Bukkit#dispatchCommand is executed.
     *
     * @param sender     The sender to dispatch the command.
     * @param subCommand The sub-command to dispatch.
     */
    void dispatchSubCommand(CommandSender sender, String subCommand);

    /**
     * Dispatch a sub command.
     * If the sub command does not exist, Bukkit#dispatchCommand is executed.
     *
     * @param sender     The sender to dispatch the command.
     * @param subCommand The sub-command to dispatch.
     * @param args       The argument to use for the command.
     */
    void dispatchSubCommand(CommandSender sender, String subCommand, @Nullable String args);

}
