package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.wrappers.WrappedLocation;
import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.island.IslandPermission;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdSetWarp implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setwarp");
    }

    @Override
    public String getPermission() {
        return "superior.island.setwarp";
    }

    @Override
    public String getUsage() {
        return "island setwarp <warp>";
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

        if(!wrappedPlayer.hasPermission(IslandPermission.SET_WARP)){
            Locale.NO_SET_WARP_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.SET_WARP));
            return;
        }

        if(island.getWarpLocation(args[1]) != null){
            Locale.WARP_ALREADY_EXIST.send(wrappedPlayer);
            return;
        }

        island.setWarpLocation(args[1], wrappedPlayer.getLocation());
        Locale.SET_WARP.send(wrappedPlayer, WrappedLocation.of(wrappedPlayer.getLocation()));
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return null;
    }
}
