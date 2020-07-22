package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CmdChest implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("chest", "vault");
    }

    @Override
    public String getPermission() {
        return "superior.island.chest";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "chest [page]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_CHEST.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPrivileges.ISLAND_CHEST)){
            Locale.NO_ISLAND_CHEST_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.ISLAND_CHEST));
            return;
        }

        int page = 1;

        if(args.length == 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (Exception ignored) {
                Locale.INVALID_PAGE.send(sender, args[1]);
                return;
            }
        }

        IslandChest[] islandChests = island.getChest();


        if(page > islandChests.length){
            if(islandChests.length == 0)
                Locale.EMPTY_ISLAND_CHEST.send(sender);
            else
                Locale.NO_PAGE_ACCESS.send(sender);
            return;
        }

        islandChests[page - 1].openChest(superiorPlayer);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPrivileges.ISLAND_CHEST)){
            return IntStream.range(1, island.getChestSize() + 1).boxed().map(i -> i + "")
                    .filter(i -> i.startsWith(args[1])).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

}
