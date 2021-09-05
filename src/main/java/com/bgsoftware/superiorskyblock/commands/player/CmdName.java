package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.Locale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public final class CmdName implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("name", "setname", "rename");
    }

    @Override
    public String getPermission() {
        return "superior.island.name";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "name <" + Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_NAME.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
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
        return IslandPrivileges.CHANGE_NAME;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_NAME_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        String islandName = args[1];

        if(!StringUtils.isValidName(superiorPlayer, island, islandName))
            return;

        island.setName(islandName);

        String coloredName = plugin.getSettings().getIslandNames().isColorSupport() ?
                StringUtils.translateColors(islandName) : islandName;

        for(Player player : Bukkit.getOnlinePlayers())
            Locale.NAME_ANNOUNCEMENT.send(player, superiorPlayer.getName(), coloredName);

        Locale.CHANGED_NAME.send(superiorPlayer, coloredName);
    }

}
