package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandArgument;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CmdWarp implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("warp");
    }

    @Override
    public String getPermission() {
        return "superior.island.warp";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "warp [" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "] [" +
                Message.COMMAND_ARGUMENT_WARP_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_WARP.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Island targetIsland = null;
        String targetWarpName = null;

        switch (args.length) {
            case 1: {
                IslandArgument arguments = CommandArguments.getSenderIsland(plugin, sender);
                targetIsland = arguments.getIsland();
                break;
            }
            case 2: {
                IslandArgument arguments = CommandArguments.getSenderIsland(plugin, sender);
                targetIsland = arguments.getIsland();
                targetWarpName = args[1];
                break;
            }
            case 3: {
                IslandArgument arguments = CommandArguments.getIsland(plugin, sender, args[1]);
                targetIsland = arguments.getIsland();
                targetWarpName = args[2];
                break;
            }
        }

        if (targetIsland == null)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        IslandWarp islandWarp = targetWarpName == null ? null : targetIsland.getWarp(targetWarpName);

        if (islandWarp == null) {
            switch (args.length) {
                case 1:
                    Menus.MENU_WARP_CATEGORIES.openMenu(superiorPlayer, superiorPlayer.getOpenedView(), targetIsland);
                    break;
                case 2:
                    IslandArgument arguments = CommandArguments.getIsland(plugin, sender, args[1]);
                    targetIsland = arguments.getIsland();
                    if (targetIsland != null) {
                        Menus.MENU_WARP_CATEGORIES.openMenu(superiorPlayer, superiorPlayer.getOpenedView(), targetIsland);
                    }
                    break;
                case 3:
                    Message.INVALID_WARP.send(superiorPlayer, targetWarpName);
                    break;
            }

            return;
        }

        if (!targetIsland.isMember(superiorPlayer) && islandWarp.hasPrivateFlag()) {
            Message.INVALID_WARP.send(superiorPlayer, targetWarpName);
            return;
        }

        targetIsland.warpPlayer(superiorPlayer, targetWarpName);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island playerIsland = superiorPlayer.getIsland();

        switch (args.length) {
            case 2: {
                List<String> tabCompletes = new LinkedList<>(CommandTabCompletes.getPlayerIslandsExceptSender(plugin, sender, args[1], true,
                        (player, island) -> island.getIslandWarps().values().stream().anyMatch(islandWarp ->
                                island.isMember(superiorPlayer) || !islandWarp.hasPrivateFlag())));

                if (playerIsland != null) {
                    for (String warpName : playerIsland.getIslandWarps().keySet()) {
                        if (warpName.startsWith(args[1]))
                            tabCompletes.add(warpName);
                    }
                }

                return tabCompletes.isEmpty() ? Collections.emptyList() : tabCompletes;
            }
            case 3: {
                Island targetIsland = plugin.getGrid().getIsland(args[1]);
                if (targetIsland != null) {
                    return new SequentialListBuilder<Map.Entry<String, IslandWarp>>()
                            .filter(islandWarpEntry -> targetIsland.isMember(superiorPlayer) || !islandWarpEntry.getValue().hasPrivateFlag())
                            .map(targetIsland.getIslandWarps().entrySet(), Map.Entry::getKey);
                }
                break;
            }
        }

        return Collections.emptyList();
    }

}
