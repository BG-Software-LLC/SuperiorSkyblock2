package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdSetPaypal implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setpaypal");
    }

    @Override
    public String getPermission() {
        return "superior.island.setpaypal";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "setpaypal <" + Locale.COMMAND_ARGUMENT_EMAIL.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_SET_PAYPAL.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPrivileges.SET_PAYPAL)){
            Locale.NO_SET_PAYPAL_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.SET_PAYPAL));
            return;
        }

        island.setPaypal(args[1]);
        Locale.CHANGED_PAYPAL.send(superiorPlayer, args[1]);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
