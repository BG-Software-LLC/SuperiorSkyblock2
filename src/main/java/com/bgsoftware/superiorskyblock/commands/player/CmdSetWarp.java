package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.Locale;

import java.util.Collections;
import java.util.List;

public final class CmdSetWarp implements IPermissibleCommand {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

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
        StringBuilder usage = new StringBuilder("setwarp <")
                .append(Locale.COMMAND_ARGUMENT_WARP_NAME.getMessage(locale)).append(">");

        if(plugin.getSettings().isWarpCategories())
            usage.append(" [").append(Locale.COMMAND_ARGUMENT_WARP_CATEGORY.getMessage(locale)).append("]");

        return usage.toString();
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
        return plugin.getSettings().isWarpCategories() ? 3 : 2;
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
        if(island.getIslandWarps().size() >= island.getWarpsLimit()) {
            Locale.NO_MORE_WARPS.send(superiorPlayer);
            return;
        }

        String warpName = IslandUtils.getWarpName(args[1]);

        if(warpName.isEmpty()){
            Locale.WARP_ILLEGAL_NAME.send(superiorPlayer);
            return;
        }

        if(!IslandUtils.isWarpNameLengthValid(warpName)) {
            Locale.WARP_NAME_TOO_LONG.send(superiorPlayer);
            return;
        }

        if(island.getWarp(warpName) != null){
            Locale.WARP_ALREADY_EXIST.send(superiorPlayer);
            return;
        }

        if(!island.isInsideRange(superiorPlayer.getLocation())){
            Locale.SET_WARP_OUTSIDE.send(superiorPlayer);
            return;
        }

        String categoryName = null;

        if(args.length == 3){
            categoryName = IslandUtils.getWarpName(args[2]);
            if(categoryName.isEmpty()){
                Locale.WARP_CATEGORY_ILLEGAL_NAME.send(superiorPlayer);
                return;
            }

            if(!IslandUtils.isWarpNameLengthValid(categoryName)) {
                Locale.WARP_CATEGORY_NAME_TOO_LONG.send(superiorPlayer);
                return;
            }
        }

        WarpCategory warpCategory = categoryName == null ? null : island.createWarpCategory(categoryName);

        assert superiorPlayer.getLocation() != null;

        island.createWarp(warpName, superiorPlayer.getLocation(), warpCategory);

        Locale.SET_WARP.send(superiorPlayer, SBlockPosition.of(superiorPlayer.getLocation()));
    }

}
