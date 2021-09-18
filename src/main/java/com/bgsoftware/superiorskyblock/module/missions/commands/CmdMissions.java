package com.bgsoftware.superiorskyblock.module.missions.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandMissions;
import com.bgsoftware.superiorskyblock.menu.impl.MenuPlayerMissions;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdMissions implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("missions", "challenges");
    }

    @Override
    public String getPermission() {
        return "superior.island.missions";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "missions [" + Locale.COMMAND_ARGUMENT_ISLAND.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_PLAYER.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_MISSIONS.getMessage(locale);
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
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        if(args.length == 2){
            if(args[1].equalsIgnoreCase(Locale.COMMAND_ARGUMENT_ISLAND.getMessage(superiorPlayer.getUserLocale()))){
                MenuIslandMissions.openInventory(superiorPlayer, null);
                return;
            }
            else if(args[1].equalsIgnoreCase(Locale.COMMAND_ARGUMENT_PLAYER.getMessage(superiorPlayer.getUserLocale()))){
                MenuPlayerMissions.openInventory(superiorPlayer, null);
                return;
            }
        }

        plugin.getMenus().openMissions(superiorPlayer, null);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getCustomComplete(args[1], "members", "visitors", "toggle") : new ArrayList<>();
    }

}
