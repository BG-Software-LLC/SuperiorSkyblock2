package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdAdminSyncUpgrades implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("syncupgrades");
    }

    @Override
    public String getPermission() {
        return "superior.admin.syncupgrades";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin syncupgrades <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + ">";

    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SYNC_UPGRADES.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
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
        islands.forEach(Island::syncUpgrades);

        if(islands.size() > 1)
            Locale.SYNC_UPGRADES_ALL.send(sender);
        else if(targetPlayer == null)
            Locale.SYNC_UPGRADES_NAME.send(sender, islands.get(0).getName());
        else
            Locale.SYNC_UPGRADES.send(sender, targetPlayer.getName());
    }

}
