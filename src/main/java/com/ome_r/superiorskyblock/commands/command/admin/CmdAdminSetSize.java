package com.ome_r.superiorskyblock.commands.command.admin;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.commands.ICommand;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetSize implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setsize");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setsize";
    }

    @Override
    public String getUsage() {
        return "island admin setsize <player-name> <size>";
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
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer targetPlayer = WrappedPlayer.of(args[2]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[2]);
            return;
        }

        Island island = targetPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        int size;

        try{
            size = Integer.valueOf(args[3]);
        }catch(IllegalArgumentException ex){
            Locale.INVALID_SIZE.send(sender, args[3]);
            return;
        }

        island.setIslandSize(size);
        Locale.CHANGED_ISLAND_SIZE.send(sender, targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return null;
    }
}
