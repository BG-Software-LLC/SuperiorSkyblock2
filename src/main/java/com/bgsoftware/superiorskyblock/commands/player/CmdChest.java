package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.island.permissions.IslandPrivileges;
import com.bgsoftware.superiorskyblock.lang.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public final class CmdChest implements IPermissibleCommand {

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
        return "chest [" + Message.COMMAND_ARGUMENT_PAGE.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_CHEST.getMessage(locale);
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
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.ISLAND_CHEST;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_ISLAND_CHEST_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        if (args.length == 2) {
            Pair<Integer, Boolean> pageArguments = CommandArguments.getPage(superiorPlayer.asPlayer(), args[1]);

            if (!pageArguments.getValue())
                return;

            int page = pageArguments.getKey() - 1;
            IslandChest[] islandChests = island.getChest();

            if (page < 0 || page >= islandChests.length) {
                Message.INVALID_PAGE.send(superiorPlayer, args[1]);
                return;
            }

            islandChests[page].openChest(superiorPlayer);
        } else {
            plugin.getMenus().openIslandChest(superiorPlayer, null, island);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        IslandChest[] islandChests = island.getChest();
        return args.length == 1 || islandChests.length == 0 ? new ArrayList<>() :
                CommandTabCompletes.getCustomComplete(args[1], IntStream.range(1, islandChests.length + 1).boxed()
                        .map(Object::toString).toArray(String[]::new));
    }

}
