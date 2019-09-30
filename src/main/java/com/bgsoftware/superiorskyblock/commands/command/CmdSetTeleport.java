package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdSetTeleport implements ICommand {

    @Override
    public List<String> getAliases(){
        return Arrays.asList("settp", "setteleport", "setgo", "sethome");
    }

    @Override
    public String getPermission() {
        return "superior.island.setteleport";
    }

    @Override
    public String getUsage() {
        return "island setteleport";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_SET_TELEPORT.getMessage();
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPermission.SET_HOME)){
            Locale.NO_SET_HOME_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPermission.SET_HOME));
            return;
        }

        if (!island.isInsideRange(superiorPlayer.getLocation())) {
            Locale.TELEPORT_LOCAITON_OUTSIDE_ISLAND.send(superiorPlayer);
            return;
        }

        island.setTeleportLocation(superiorPlayer.getLocation());
        Locale.CHANGED_TELEPORT_LOCATION.send(superiorPlayer);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
