package com.bgsoftware.superiorskyblock.api.handlers;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

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

    /**
     * Check whether an admin sub command is considered dangerous.
     * Dangerous commands that are executed by players must be approved by the console.
     * Config-path: dangerous-commands
     *
     * @param command The admin sub command to check.
     */
    boolean isDangerousCommand(SuperiorCommand command);

    /**
     * Submit a request for executing a dangerous command.
     * The command will not be executed until the console approves it.
     * If the player already has a pending request for this command, it will be overridden.
     * Requests expire after 1 minute.
     *
     * @param player  The player that requested to execute the command.
     * @param command The dangerous admin sub command to execute.
     * @param args    The arguments of the command, including the "admin" and sub command labels.
     */
    void submitDangerousRequest(Player player, SuperiorCommand command, String[] args);

    /**
     * Check whether there are pending requests for executing a dangerous command.
     *
     * @param command The dangerous admin sub command to check.
     */
    boolean hasPendingDangerousRequests(SuperiorCommand command);

    /**
     * Approve and execute all pending requests for executing a dangerous command.
     * Requests of players that are offline or no longer have access to the command are skipped.
     *
     * @param sender  The sender that approved the requests.
     * @param command The dangerous admin sub command to approve.
     */
    void approveDangerousRequests(CommandSender sender, SuperiorCommand command);

    /**
     * Cancel all pending requests of a player for executing dangerous commands.
     *
     * @param playerUUID The uuid of the player.
     */
    void cancelDangerousRequests(UUID playerUUID);

    /**
     * Cancel all pending requests for executing dangerous commands.
     */
    void cancelAllDangerousRequests();

}
