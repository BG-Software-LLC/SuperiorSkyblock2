package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
        World.Environment environment = CommandArguments.getEnvironment(sender, args[3]);

        if (environment == null)
            return;

        if (environment == plugin.getSettings().getWorlds().getDefaultWorld()) {
            Message.INVALID_ENVIRONMENT.send(sender, args[3]);
            return;
        }

        boolean enable = Boolean.parseBoolean(args[4]);

        boolean anyWorldsChanged = false;

        for (Island island : islands) {
            if (enable ? !plugin.getEventsBus().callIslandUnlockWorldEvent(island, environment) :
                    !plugin.getEventsBus().callIslandLockWorldEvent(island, environment))
                continue;

            anyWorldsChanged = true;

            switch (environment) {
                case NORMAL:
                    island.setNormalEnabled(enable);
                    break;
                case NETHER:
                    island.setNetherEnabled(enable);
                    break;
                case THE_END:
                    island.setEndEnabled(enable);
                    break;
            }
        }

        if (anyWorldsChanged)
            Message.UNLOCK_WORLD_ANNOUNCEMENT.send(sender, Formatters.CAPITALIZED_FORMATTER.format(args[3]));
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        if (args.length == 5)
            return CommandTabCompletes.getCustomComplete(args[3], "true", "false");

        if (args.length != 4)
            return Collections.emptyList();

        List<String> environments = new ArrayList<>();
        for (World.Environment environment : World.Environment.values()) {
            environments.add(environment.name().toLowerCase(Locale.ENGLISH));
        }

        return CommandTabCompletes.getCustomComplete(args[3], environments.toArray(new String[0]));
    }

}
