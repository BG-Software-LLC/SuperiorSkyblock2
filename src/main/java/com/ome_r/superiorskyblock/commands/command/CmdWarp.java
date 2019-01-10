package com.ome_r.superiorskyblock.commands.command;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.commands.ICommand;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdWarp implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("warp");
    }

    @Override
    public String getPermission() {
        return "superior.island.warp";
    }

    @Override
    public String getUsage() {
        return "island warp [player-name]";
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
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island;

        if(args.length == 1){
            island = wrappedPlayer.getIsland();

            if(island == null){
                Locale.INVALID_ISLAND.send(wrappedPlayer);
                return;
            }
        }
        else{
            WrappedPlayer targetPlayer = WrappedPlayer.of(args[1]);

            if(targetPlayer == null){
                Locale.INVALID_PLAYER.send(wrappedPlayer, args[1]);
                return;
            }

            island = targetPlayer.getIsland();

            if(island == null){
                Locale.INVALID_ISLAND_OTHER.send(wrappedPlayer, targetPlayer.getName());
                return;
            }
        }

        plugin.getPanel().openWarpsPanel(wrappedPlayer, island, 1);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return null;
    }
}
