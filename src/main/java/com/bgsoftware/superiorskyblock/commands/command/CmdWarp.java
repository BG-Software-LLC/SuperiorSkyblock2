package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.menus.types.warps.IslandWarpsMenu;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdWarp implements ICommand {

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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island;

        if(args.length == 1){
            island = superiorPlayer.getIsland();

            if(island == null){
                Locale.INVALID_ISLAND.send(superiorPlayer);
                return;
            }
        }
        else{
            SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

            if(targetPlayer == null){
                Locale.INVALID_PLAYER.send(superiorPlayer, args[1]);
                return;
            }

            island = targetPlayer.getIsland();

            if(island == null){
                Locale.INVALID_ISLAND_OTHER.send(superiorPlayer, targetPlayer.getName());
                return;
            }
        }

//        plugin.getPanel().openWarpsPanel(superiorPlayer, island, 1);
        new IslandWarpsMenu(superiorPlayer.asPlayer(), island);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
