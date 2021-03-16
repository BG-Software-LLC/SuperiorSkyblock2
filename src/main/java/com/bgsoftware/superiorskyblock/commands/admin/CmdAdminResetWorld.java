package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
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
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <normal/nether/the_end>";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_RESET_WORLD.getMessage(locale);
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

        if(environment == null)
            return;

        if(environment == World.Environment.NORMAL){
            Locale.INVALID_ENVIRONMENT.send(sender, args[3]);
            return;
        }

        islands.forEach(island -> {
            World world = island.getCenter(environment).getWorld();

            // Sending the players that are in that world to the main island.
            // If the world that will be reset is the normal world, they will be teleported to spawn.
            for(SuperiorPlayer superiorPlayer : island.getAllPlayersInside()){
                assert superiorPlayer.getWorld() != null;
                if(superiorPlayer.getWorld().equals(world))
                    superiorPlayer.teleport(island);
            }

            // Resetting the chunks
            List<ChunkPosition> chunkPositions = IslandUtils.getChunkCoords(island, world, true, true);
            
            if(!chunkPositions.isEmpty()) {
                for (int i = 0; i < chunkPositions.size() - 1; i++)
                    IslandUtils.deleteChunk(island, chunkPositions.get(i), null);

                IslandUtils.deleteChunk(island, chunkPositions.get(chunkPositions.size() - 1),
                        () -> island.calcIslandWorth(null));
            }

            island.setSchematicGenerate(environment, false);
        });

        if(islands.size() > 1)
            Locale.RESET_WORLD_SUCCEED_ALL.send(sender, StringUtils.format(args[3]));
        else if(targetPlayer == null)
            Locale.RESET_WORLD_SUCCEED_NAME.send(sender, StringUtils.format(args[3]), islands.get(0).getName());
        else
            Locale.RESET_WORLD_SUCCEED.send(sender, StringUtils.format(args[3]), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getCustomComplete(args[3], "nether", "the_end") : new ArrayList<>();
    }

}
