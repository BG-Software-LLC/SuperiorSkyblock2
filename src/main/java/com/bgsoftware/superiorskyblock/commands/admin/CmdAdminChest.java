package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.menu.MenuIslandChest;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdAdminChest implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("chest");
    }

    @Override
    public String getPermission() {
        return "superior.admin.chest";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin chest <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_CHEST.getMessage(locale);
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
        return false;
    }

    @Override
    public boolean supportMultipleIslands() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, Island island, String[] args) {
        MenuIslandChest.openInventory(plugin.getPlayers().getSuperiorPlayer(sender), null, island);
    }

}
