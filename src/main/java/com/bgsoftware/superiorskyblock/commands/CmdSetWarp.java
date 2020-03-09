package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.Locale;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdSetWarp implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setwarp");
    }

    @Override
    public String getPermission() {
        return "superior.island.setwarp";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "setwarp <" +
                Locale.COMMAND_ARGUMENT_WARP_NAME.getMessage(locale) + "> [" +
                Locale.COMMAND_ARGUMENT_PRIVATE.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_SET_WARP.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPrivileges.SET_WARP)){
            Locale.NO_SET_WARP_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.SET_WARP));
            return;
        }

        if (!island.hasMoreWarpSlots()) {
            Locale.NO_MORE_WARPS.send(superiorPlayer);
            return;
        }

        if(island.getWarpLocation(args[1]) != null){
            Locale.WARP_ALREADY_EXIST.send(superiorPlayer);
            return;
        }

        if(!island.isInsideRange(superiorPlayer.getLocation())){
            Locale.SET_WARP_OUTSIDE.send(superiorPlayer);
            return;
        }

        boolean privateFlag = args.length == 3 && args[2].equalsIgnoreCase("true");

        island.setWarpLocation(args[1], superiorPlayer.getLocation(), privateFlag);
        Locale.SET_WARP.send(superiorPlayer, SBlockPosition.of(superiorPlayer.getLocation()));
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
