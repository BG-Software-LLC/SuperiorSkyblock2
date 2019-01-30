package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.island.IslandPermission;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CmdSetPaypal implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setpaypal");
    }

    @Override
    public String getPermission() {
        return "superior.island.setpaypal";
    }

    @Override
    public String getUsage() {
        return "island setpaypal <email>";
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
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(wrappedPlayer);
            return;
        }

        if(!wrappedPlayer.hasPermission(IslandPermission.SET_PAYPAL)){
            Locale.NO_SET_PAYPAL_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.SET_PAYPAL));
            return;
        }

        island.setPaypal(args[1]);
        Locale.CHANGED_PAYPAL.send(wrappedPlayer, args[1]);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
