package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.service.dragon.DragonBattleService;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.IslandWorlds;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdAdminResetWorld implements IAdminIslandCommand {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final LazyReference<DragonBattleService> dragonBattleService = new LazyReference<DragonBattleService>() {
        @Override
        protected DragonBattleService create() {
            return plugin.getServices().getService(DragonBattleService.class);
        }
    };

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
        Dimension dimension = CommandArguments.getDimension(sender, args[3]);

        if (dimension == null)
            return;

        int islandsChangedCount = 0;

        for (Island island : islands) {
            if (!PluginEventsFactory.callIslandWorldResetEvent(island, sender, dimension))
                continue;

            ++islandsChangedCount;

            IslandWorlds.accessIslandWorldAsync(island, dimension, true, islandWorldResult -> {
                islandWorldResult.ifLeft(world -> resetChunksInternal(island, world, dimension));
            });
        }

        if (islandsChangedCount <= 0)
            return;

        if (islandsChangedCount > 1)
            Message.RESET_WORLD_SUCCEED_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(args[3]));
        else if (targetPlayer == null)
            Message.RESET_WORLD_SUCCEED_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(args[3]), islands.get(0).getName());
        else
            Message.RESET_WORLD_SUCCEED.send(sender, Formatters.CAPITALIZED_FORMATTER.format(args[3]), targetPlayer.getName());
    }

    private static void resetChunksInternal(Island island, World world, Dimension dimension) {
        boolean isDefaultDimension = dimension == plugin.getSettings().getWorlds().getDefaultWorldDimension();

        // Sending the players that are in that world to the main island.
        // If the world that will be reset is the normal world, they will be teleported to spawn.
        for (SuperiorPlayer superiorPlayer : island.getAllPlayersInside()) {
            assert superiorPlayer.getWorld() != null;
            if (superiorPlayer.getWorld().equals(world)) {
                if (isDefaultDimension) {
                    superiorPlayer.teleport(plugin.getGrid().getSpawnIsland());
                } else {
                    superiorPlayer.teleport(island);
                }
            }
        }

        // Resetting the chunks
        island.resetChunks(dimension, IslandChunkFlags.ONLY_PROTECTED,
                isDefaultDimension ? null : () -> island.calcIslandWorth(null));

        if (isDefaultDimension) {
            String islandSchematic = island.getSchematicName();
            Schematic schematic = plugin.getSchematics().getSchematic(islandSchematic);
            if (schematic != null) {
                regenerateSchematicInternal(island, dimension, schematic);
                return;
            }
        }

        island.setSchematicGenerate(dimension, false);
    }

    private static void regenerateSchematicInternal(Island island, Dimension dimension, Schematic schematic) {
        Location centerLocation = island.getCenter(dimension);
        Location schematicPlacementLocation = centerLocation.getBlock().getRelative(BlockFace.DOWN).getLocation();

        schematic.pasteSchematic(island, schematicPlacementLocation, () -> {
            island.setSchematicGenerate(dimension);

            Location homeLocation = schematic.adjustRotation(centerLocation);
            island.setIslandHome(dimension, homeLocation);

            if (dimension.getEnvironment() == World.Environment.THE_END) {
                dragonBattleService.get().resetEnderDragonBattle(island, dimension);
            }

            island.setIslandHome(dimension, homeLocation);

            island.calcIslandWorth(null);
        }, Throwable::printStackTrace);
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        if (args.length != 4)
            return Collections.emptyList();

        List<String> environments = new ArrayList<>();

        for (Dimension dimension : Dimension.values()) {
            if (plugin.getProviders().getWorldsProvider().isDimensionEnabled(dimension)) {
                environments.add(dimension.getName().toLowerCase(Locale.ENGLISH));
            }
        }

        return CommandTabCompletes.getCustomComplete(args[3], environments.toArray(new String[0]));
    }

}
