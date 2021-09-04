package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuWarpCategories;
import com.bgsoftware.superiorskyblock.menu.MenuWarps;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdWarp implements ISuperiorCommand {

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
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "] [" +
                Locale.COMMAND_ARGUMENT_WARP_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_WARP.getMessage(locale);
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
        Pair<Island, SuperiorPlayer> arguments = args.length == 1 ? CommandArguments.getSenderIsland(plugin, sender) :
                CommandArguments.getIsland(plugin, sender, args[1]);

        Island island = arguments.getKey();

        if (island == null)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        switch (args.length) {
            case 3:
                IslandWarp warp = island.getWarp(args[2]);
                if (warp == null) {
                    Locale.INVALID_WARP.send(superiorPlayer, args[2]);
                    return;
                }
                warpPlayer(warp, superiorPlayer, island);
                break;
            case 2:
                warp = island.getWarp(args[1]);
                if (warp != null) {
                    warpPlayer(warp, superiorPlayer, island);
                    return;
                }
                MenuWarpCategories.openInventory(superiorPlayer, null, island);
                break;
            default:
                MenuWarpCategories.openInventory(superiorPlayer, null, island);
                break;
        }

    }

    private void warpPlayer(IslandWarp warp, SuperiorPlayer superiorPlayer, Island island) {
        if (!warp.getIsland().isMember(superiorPlayer) && warp.hasPrivateFlag()) {
            Locale.INVALID_WARP.send(superiorPlayer, warp.getName());
        } else {
            MenuWarps.simulateClick(superiorPlayer, island, warp.getName());
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        switch (args.length) {
            case 2:
                return CommandTabCompletes.getPlayerIslandsExceptSender(plugin, sender, args[1],
                        plugin.getSettings().tabCompleteHideVanished);
            case 3:
                return CommandTabCompletes.getIslandWarps(CommandArguments.getIsland(plugin, sender, args[1]).getKey(), args[2]);
            default:
                return new ArrayList<>();
        }
    }

}