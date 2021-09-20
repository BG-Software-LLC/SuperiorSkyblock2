package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public final class CmdAdminName implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("name", "setname", "rename");
    }

    @Override
    public String getPermission() {
        return "superior.admin.name";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin name <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_NAME.getMessage(locale);
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
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, Island island, String[] args) {
        String islandName = args[3];

        if(!StringUtils.isValidName(sender, island, islandName))
            return;

        String oldName = island.getName();
        island.setName(islandName);

        String coloredName = plugin.getSettings().getIslandNames().isColorSupport() ?
                StringUtils.translateColors(islandName) : islandName;

        for(Player player : Bukkit.getOnlinePlayers())
            Locale.NAME_ANNOUNCEMENT.send(player, sender.getName(), coloredName);

        if(targetPlayer == null)
            Locale.CHANGED_NAME_OTHER_NAME.send(sender, oldName, coloredName);
        else
            Locale.CHANGED_NAME_OTHER.send(sender, targetPlayer.getName(), coloredName);
    }

}
