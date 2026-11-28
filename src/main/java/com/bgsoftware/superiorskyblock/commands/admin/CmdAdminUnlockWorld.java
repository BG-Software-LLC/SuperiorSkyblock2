package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BulkChange;
import com.bgsoftware.superiorskyblock.commands.ChangeResult;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
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
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_DIMENSION.getMessage(locale) + "> <true/false>";
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

        BulkChange<Island> change = BulkChange.ofIslands(islands, island -> {
            boolean allowed = unlockWorld ? PluginEventsFactory.callIslandUnlockWorldEvent(island, sender, dimension) :
                    PluginEventsFactory.callIslandLockWorldEvent(island, sender, dimension);
            return ChangeResult.ifAllowed(allowed, island, dimension, unlockWorld, Island::setDimensionEnabled);
        });

        change.sendSummary(sender, args);
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getDimensions(plugin, args[3]) : args.length == 5 ?
                CommandTabCompletes.getCustomComplete(args[4], "true", "false") : Collections.emptyList();
    }

}
