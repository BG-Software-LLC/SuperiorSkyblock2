package com.bgsoftware.superiorskyblock.api.commands;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Locale;

public interface SuperiorCommand2 extends SuperiorCommand {


    List<CommandArgument<?>> getArguments();

    void execute(SuperiorSkyblock plugin, CommandContext context) throws CommandSyntaxException;

    /**
     * Get the usage of the sub command.
     *
     * @param locale The locale of the player.
     */
    @Deprecated
    default String getUsage(Locale locale) {
        throw new UnsupportedOperationException("Not supported in SuperiorCommand2");
    }

    /**
     * Get the minimum arguments required for the command.
     * For example, the command /is example PLAYER_NAME has 2 arguments.
     */
    @Deprecated
    default int getMinArgs() {
        throw new UnsupportedOperationException("Not supported in SuperiorCommand2");
    }

    /**
     * Get the maximum arguments required for the command.
     * For example, the command /is example PLAYER_NAME has 2 arguments.
     */
    @Deprecated
    default int getMaxArgs() {
        throw new UnsupportedOperationException("Not supported in SuperiorCommand2");
    }

    /**
     * The method to be executed when the command is running.
     *
     * @param plugin The instance of the plugin.
     * @param sender The sender who ran the command.
     * @param args   The arguments of the command.
     */
    @Deprecated
    default void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        throw new UnsupportedOperationException("Not supported in SuperiorCommand2");
    }

    /**
     * Get the tab-complete arguments of the command.
     *
     * @param plugin The instance of the plugin.
     * @param sender The sender who ran the command.
     * @param args   The arguments of the command.
     */
    @Deprecated
    default List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        throw new UnsupportedOperationException("Not supported in SuperiorCommand2");
    }

}
