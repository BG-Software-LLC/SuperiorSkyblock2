package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdAdminResetWorld implements IAdminIslandCommand {

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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        World.Environment environment = CommandArguments.getEnvironment(sender, args[3]);

        if (environment == null)
            return;

        if (environment == plugin.getSettings().getWorlds().getDefaultWorld()) {
            Message.INVALID_ENVIRONMENT.send(sender, args[3]);
            return;
        }

        islands.forEach(island -> {
            World world;

            try {
                world = island.getCenter(environment).getWorld();
            } catch (NullPointerException ex) {
                PluginDebugger.debug(ex);
                return;
            }

            // Sending the players that are in that world to the main island.
            // If the world that will be reset is the normal world, they will be teleported to spawn.
            for (SuperiorPlayer superiorPlayer : island.getAllPlayersInside()) {
                assert superiorPlayer.getWorld() != null;
                if (superiorPlayer.getWorld().equals(world))
                    superiorPlayer.teleport(island);
            }

            // Resetting the chunks
            List<ChunkPosition> chunkPositions = IslandUtils.getChunkCoords(island, world, true, true);
            IslandUtils.deleteChunks(island, chunkPositions, () -> island.calcIslandWorth(null));

            island.setSchematicGenerate(environment, false);
        });

        if (islands.size() > 1)
            Message.RESET_WORLD_SUCCEED_ALL.send(sender, StringUtils.format(args[3]));
        else if (targetPlayer == null)
            Message.RESET_WORLD_SUCCEED_NAME.send(sender, StringUtils.format(args[3]), islands.get(0).getName());
        else
            Message.RESET_WORLD_SUCCEED.send(sender, StringUtils.format(args[3]), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        if (args.length != 4)
            return new ArrayList<>();

        List<String> environments = new ArrayList<>();

        for (World.Environment environment : World.Environment.values()) {
            if (environment != plugin.getSettings().getWorlds().getDefaultWorld()) {
                switch (environment) {
                    case NORMAL:
                        if (plugin.getProviders().getWorldsProvider().isNormalEnabled())
                            environments.add(environment.name().toLowerCase());
                        break;
                    case NETHER:
                        if (plugin.getProviders().getWorldsProvider().isNetherEnabled())
                            environments.add(environment.name().toLowerCase());
                        break;
                    case THE_END:
                        if (plugin.getProviders().getWorldsProvider().isEndEnabled())
                            environments.add(environment.name().toLowerCase());
                        break;
                }
            }
        }

        return CommandTabCompletes.getCustomComplete(args[3], environments.toArray(new String[0]));
    }

}
