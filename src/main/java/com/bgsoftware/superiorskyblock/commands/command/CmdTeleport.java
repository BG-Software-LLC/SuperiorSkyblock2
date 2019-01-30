package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.Island;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CmdTeleport implements ICommand {

    @Override
    public List<String> getAliases(){
        return Arrays.asList("tp", "teleport", "go");
    }

    @Override
    public String getPermission() {
        return "superior.island.teleport";
    }

    @Override
    public String getUsage() {
        return "island teleport";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
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

        wrappedPlayer.asPlayer().teleport(island.getCenter());
        Locale.TELEPORTED_SUCCESS.send(wrappedPlayer);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
