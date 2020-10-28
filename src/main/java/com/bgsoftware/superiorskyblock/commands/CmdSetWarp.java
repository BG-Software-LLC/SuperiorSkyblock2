package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.Locale;

import java.util.Collections;
import java.util.List;

public final class CmdSetWarp implements IPermissibleCommand {

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
                Locale.COMMAND_ARGUMENT_PRIVATE.getMessage(locale) + "=true/false]";
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
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.SET_WARP;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_SET_WARP_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
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

        island.setWarpLocation(args[1].trim(), superiorPlayer.getLocation(), privateFlag);
        Locale.SET_WARP.send(superiorPlayer, SBlockPosition.of(superiorPlayer.getLocation()));
    }

}
