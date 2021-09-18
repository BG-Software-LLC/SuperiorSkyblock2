package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.Locale;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdDelWarp implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("delwarp");
    }

    @Override
    public String getPermission() {
        return "superior.island.delwarp";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "delwarp <" + Locale.COMMAND_ARGUMENT_WARP_NAME.getMessage(locale) + "...>";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_DEL_WARP.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.DELETE_WARP;
    }

    @Override
    public Locale getPermissionLackMessage() {
        return Locale.NO_DELETE_WARP_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        StringBuilder warpNameBuilder = new StringBuilder();

        for(int i = 1; i < args.length; i++)
            warpNameBuilder.append(" ").append(args[i]);

        String warpName = warpNameBuilder.length() == 0 ? "" : warpNameBuilder.substring(1);

        IslandWarp islandWarp = island.getWarp(warpName);

        if(islandWarp == null){
            Locale.INVALID_WARP.send(superiorPlayer, warpName);
            return;
        }

        boolean breakSign = false;

        Block signBlock = islandWarp.getLocation().getBlock();

        if(signBlock.getState() instanceof Sign){
            signBlock.setType(Material.AIR);
            signBlock.getWorld().dropItemNaturally(signBlock.getLocation(), new ItemStack(Material.SIGN));
            breakSign = true;
        }

        island.deleteWarp(warpName);

        Locale.DELETE_WARP.send(superiorPlayer, warpName);

        if(breakSign){
            Locale.DELETE_WARP_SIGN_BROKE.send(superiorPlayer);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getIslandWarps(island, args[1]) : new ArrayList<>();
    }

}
