package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarps;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
                plugin.getMenus().openWarpCategories(superiorPlayer, null, island);
                break;
            default:
                plugin.getMenus().openWarpCategories(superiorPlayer, null, island);
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
        List<String> tabCompletes = args.length < 2 ? new ArrayList<>() :
                CommandTabCompletes.getPlayerIslandsExceptSender(plugin, sender, args[1],
                        plugin.getSettings().isTabCompleteHideVanished(), (onlinePlayer, onlineIsland) ->
                                onlineIsland.getIslandWarps().values().stream()
                                        .anyMatch(islandWarp -> !islandWarp.hasPrivateFlag()));

        switch (args.length) {
            case 2: {
                SuperiorPlayer superiorPlayer = sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null;
                Island island = superiorPlayer == null ? null : superiorPlayer.getIsland();
                if (island != null) {
                    tabCompletes.addAll(CommandTabCompletes.getIslandWarps(island, args[1]));
                }
                break;
            }
            case 3: {
                if (!tabCompletes.isEmpty()) {
                    SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[1]);
                    Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[1]) : targetPlayer.getIsland();
                    if(island != null) {
                        tabCompletes = CommandTabCompletes.getIslandWarps(island, args[2]);
                    }
                }
                break;
            }
            default:
                tabCompletes.clear();
                break;
        }

        return tabCompletes;
    }

}
