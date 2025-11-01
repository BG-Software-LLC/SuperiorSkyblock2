package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdAdminUnlockWorld implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("unlockworld", "world", "uworld");
    }

    @Override
    public String getPermission() {
        return "superior.admin.world";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin unlockworld <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <nether/the_end/normal> <true/false>";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_UNLOCK_WORLD.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        Dimension dimension = CommandArguments.getDimension(sender, args[3]);

        if (dimension == null)
            return;

        if (dimension == plugin.getSettings().getWorlds().getDefaultWorldDimension()) {
            Message.INVALID_ENVIRONMENT.send(sender, args[3]);
            return;
        }

        boolean unlockWorld = Boolean.parseBoolean(args[4]);

        if (unlockWorld) {
            handleWorldUnlock(sender, islands, dimension, targetPlayer, args[3]);
        } else {
            handleWorldLock(sender, islands, dimension, targetPlayer, args[3]);
        }
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        if (args.length == 5)
            return CommandTabCompletes.getCustomComplete(args[4], "true", "false");

        if (args.length != 4)
            return Collections.emptyList();

        return CommandTabCompletes.getDimensions(args[3]);
    }

    private void handleWorldUnlock(CommandSender sender, List<Island> islands, Dimension dimension,
                                   @Nullable SuperiorPlayer targetPlayer, String worldType) {
        int islandsChangedCount = 0;

        for (Island island : islands) {
            if (!PluginEventsFactory.callIslandUnlockWorldEvent(island, sender, dimension))
                continue;

            island.setDimensionEnabled(dimension, true);
            ++islandsChangedCount;
        }

        if (islandsChangedCount > 1)
            Message.UNLOCK_WORLD_ANNOUNCEMENT_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(worldType));
        else if (targetPlayer == null)
            Message.UNLOCK_WORLD_ANNOUNCEMENT_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(worldType), islands.get(0).getName());
        else
            Message.UNLOCK_WORLD_ANNOUNCEMENT.send(sender, Formatters.CAPITALIZED_FORMATTER.format(worldType), targetPlayer.getName());

    }

    private void handleWorldLock(CommandSender sender, List<Island> islands, Dimension dimension,
                                 @Nullable SuperiorPlayer targetPlayer, String worldType) {
        int islandsChangedCount = 0;

        for (Island island : islands) {
            if (!PluginEventsFactory.callIslandLockWorldEvent(island, sender, dimension))
                continue;

            island.setDimensionEnabled(dimension, false);
            ++islandsChangedCount;
        }

        if (islandsChangedCount > 1)
            Message.LOCK_WORLD_ANNOUNCEMENT_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(worldType));
        else if (targetPlayer == null)
            Message.LOCK_WORLD_ANNOUNCEMENT_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(worldType), islands.get(0).getName());
        else
            Message.LOCK_WORLD_ANNOUNCEMENT.send(sender, Formatters.CAPITALIZED_FORMATTER.format(worldType), targetPlayer.getName());

    }

}
