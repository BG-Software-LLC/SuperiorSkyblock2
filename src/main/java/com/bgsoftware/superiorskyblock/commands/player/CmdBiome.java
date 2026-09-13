package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuDimensionSelection;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

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
        if (Menus.MENU_BIOMES.isOnlyDefaultDimension()) {
            return "biome";
        } else {
            return "biome [" + Message.COMMAND_ARGUMENT_DIMENSION.getMessage(locale) + "]";
        }
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
        return Menus.MENU_BIOMES.isOnlyDefaultDimension() ? 1 : 2;
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
        if (args.length == 1) {
            if (Menus.MENU_BIOMES.isOnlyDefaultDimension()) {
                Dimension dimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();
                openBiomesMenu(plugin, superiorPlayer, island, dimension);
            } else {
                plugin.getMenus().openDimensionSelection(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()),
                        selectedDimension -> openBiomesMenu(plugin, superiorPlayer, island, selectedDimension)
                );
            }
        } else {
            Dimension dimension = CommandArguments.getEnabledDimension(plugin, superiorPlayer.asPlayer(), args[1]);
            openBiomesMenu(plugin, superiorPlayer, island, dimension);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        if (args.length == 2 && !Menus.MENU_BIOMES.isOnlyDefaultDimension()) {
            return CommandTabCompletes.getDimensions(plugin, args[1]);
        } else {
            return Collections.emptyList();
        }
    }

    private void openBiomesMenu(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, Dimension dimension) {
        if (!island.isDimensionEnabled(dimension)) {
            Message.WORLD_NOT_UNLOCKED.send(superiorPlayer, Formatters.CAPITALIZED_FORMATTER.format(dimension.getName()));
            closeMenu(superiorPlayer);
            return;
        }

        if (!island.wasSchematicGenerated(dimension)) {
            Message.WORLD_NOT_GENERATED.send(superiorPlayer, Formatters.CAPITALIZED_FORMATTER.format(dimension.getName()));
            closeMenu(superiorPlayer);
            return;
        }

        plugin.getMenus().openBiomes(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island, dimension);
    }

    private void closeMenu(SuperiorPlayer superiorPlayer) {
        MenuView<?, ?> menuView = superiorPlayer.getOpenedView();

        if (menuView != null && menuView.getMenu() instanceof MenuDimensionSelection) {
            menuView.setPreviousMove(false);
            BukkitExecutor.sync(menuView::closeView, 1L);
        }
    }

}
