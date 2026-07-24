package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.api.enums.DimensionSelectionMode;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.World;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdBiome implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("biome", "setbiome");
    }

    @Override
    public String getPermission() {
        return "superior.island.biome";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        if (isMode(DimensionSelectionMode.ARGUMENT) || isMode(DimensionSelectionMode.AUTO))
            return "biome [" + Message.COMMAND_ARGUMENT_DIMENSION.getMessage(locale) + "]";
        else
            return "biome";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_BIOME.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return (isMode(DimensionSelectionMode.ARGUMENT) || isMode(DimensionSelectionMode.AUTO)) ? 2 : 1;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.SET_BIOME;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_SET_BIOME_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        Dimension dimension;

        // There is no need to check if dimension selection mode is ARGUMENT or BOTH,
        // because if it is not, length will never be 2
        if (args.length == 2) {
            dimension = CommandArguments.getDimension(superiorPlayer.asPlayer(), args[1]);
        } else {
            World world = superiorPlayer.asPlayer().getWorld();

            if ((isMode(DimensionSelectionMode.LOCATION) || isMode(DimensionSelectionMode.AUTO)) &&
                    plugin.getProviders().getWorldsProvider().isIslandsWorld(world)) {
                dimension = plugin.getProviders().getWorldsProvider().getIslandsWorldDimension(world);
            } else {
                dimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();
            }
        }

        if (dimension == null)
            return;

        if (!plugin.getProviders().getWorldsProvider().isDimensionEnabled(dimension)) {
            Message.WORLD_NOT_ENABLED.send(superiorPlayer, Formatters.CAPITALIZED_FORMATTER.format(dimension.getName()));
            return;
        }

        if (!island.isDimensionEnabled(dimension)) {
            Message.WORLD_NOT_UNLOCKED.send(superiorPlayer, Formatters.CAPITALIZED_FORMATTER.format(dimension.getName()));
            return;
        }

        if (!island.wasSchematicGenerated(dimension)) {
            Message.WORLD_NOT_GENERATED.send(superiorPlayer, Formatters.CAPITALIZED_FORMATTER.format(dimension.getName()));
            return;
        }

        plugin.getMenus().openBiomes(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), superiorPlayer.getIsland(), dimension);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        if (args.length == 2 && (isMode(DimensionSelectionMode.ARGUMENT) || isMode(DimensionSelectionMode.AUTO)))
            return CommandTabCompletes.getDimensions(plugin, args[1]);
        else
            return Collections.emptyList();
    }

    private boolean isMode(DimensionSelectionMode dimensionSelectionMode) {
        return Menus.MENU_BIOMES.getDimensionSelectionMode() == dimensionSelectionMode;
    }

}
