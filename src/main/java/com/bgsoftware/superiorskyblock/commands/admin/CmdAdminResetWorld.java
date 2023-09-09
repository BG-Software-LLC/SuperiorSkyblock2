package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdAdminResetWorld implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("resetworld", "rworld");
    }

    @Override
    public String getPermission() {
        return "superior.admin.resetworld";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin resetworld <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <normal/nether/the_end>";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_RESET_WORLD.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return 4;
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

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            World world;

            try {
                world = island.getCenter(environment).getWorld();
            } catch (NullPointerException error) {
                Log.entering("ENTER", island.getOwner().getName(), environment);
                Log.error(error, "An unexpected error occurred while resetting world:");
                return;
            }

            if (!plugin.getEventsBus().callIslandWorldResetEvent(sender, island, environment))
                continue;

            anyIslandChanged = true;

            // Sending the players that are in that world to the main island.
            // If the world that will be reset is the normal world, they will be teleported to spawn.
            for (SuperiorPlayer superiorPlayer : island.getAllPlayersInside()) {
                assert superiorPlayer.getWorld() != null;
                if (superiorPlayer.getWorld().equals(world))
                    superiorPlayer.teleport(island);
            }

            // Resetting the chunks
            island.resetChunks(environment, IslandChunkFlags.ONLY_PROTECTED, () -> island.calcIslandWorth(null));

            island.setSchematicGenerate(environment, false);
        }

        if (!anyIslandChanged)
            return;

        if (islands.size() > 1)
            Message.RESET_WORLD_SUCCEED_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(args[3]));
        else if (targetPlayer == null)
            Message.RESET_WORLD_SUCCEED_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(args[3]), islands.get(0).getName());
        else
            Message.RESET_WORLD_SUCCEED.send(sender, Formatters.CAPITALIZED_FORMATTER.format(args[3]), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        if (args.length != 4)
            return Collections.emptyList();

        List<String> environments = new ArrayList<>();

        for (World.Environment environment : World.Environment.values()) {
            if (environment != plugin.getSettings().getWorlds().getDefaultWorld()) {
                boolean addEnvironment = false;
                switch (environment) {
                    case NORMAL:
                        addEnvironment = plugin.getProviders().getWorldsProvider().isNormalEnabled();
                        break;
                    case NETHER:
                        addEnvironment = plugin.getProviders().getWorldsProvider().isNetherEnabled();
                        break;
                    case THE_END:
                        addEnvironment = plugin.getProviders().getWorldsProvider().isEndEnabled();
                        break;
                }
                if (addEnvironment)
                    environments.add(environment.name().toLowerCase(Locale.ENGLISH));
            }
        }

        return CommandTabCompletes.getCustomComplete(args[3], environments.toArray(new String[0]));
    }

}
