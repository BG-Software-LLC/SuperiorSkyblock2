package com.bgsoftware.superiorskyblock.api.commands;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;

public interface SuperiorCommand {

    /**
     * Get the aliases of the sub command.
     */
    List<String> getAliases();

    /**
     * Get the required permission to use the sub command.
     * If no permission is required, can be empty.
     */
    String getPermission();

    /**
     * Get the usage of the sub command.
     *
     * @param locale The locale of the player.
     */
    String getUsage(Locale locale);

    /**
     * Get the description of the sub command.
     *
     * @param locale The locale of the player.
     */
    String getDescription(Locale locale);

    /**
     * Get the minimum arguments required for the command.
     * For example, the command /is example PLAYER_NAME has 2 arguments.
     */
    int getMinArgs();

    /**
     * Get the maximum arguments required for the command.
     * For example, the command /is example PLAYER_NAME has 2 arguments.
     */
    int getMaxArgs();

    /**
     * Can the command be executed from console?
     * If true, sender cannot be casted directly into a player. Otherwise, it can be.
     */
    boolean canBeExecutedByConsole();


    /**
     * Should the command be displayed in /is help (or /is admin for admin commands)?
     */
    boolean displayCommand();

    /**
     * The method to be executed when the command is running.
     *
     * @param plugin The instance of the plugin.
     * @param sender The sender who ran the command.
     * @param args   The arguments of the command.
     */
    void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args);

    /**
     * Get the tab-complete arguments of the command.
     *
     * @param plugin The instance of the plugin.
     * @param sender The sender who ran the command.
     * @param args   The arguments of the command.
     */
    List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args);

}
