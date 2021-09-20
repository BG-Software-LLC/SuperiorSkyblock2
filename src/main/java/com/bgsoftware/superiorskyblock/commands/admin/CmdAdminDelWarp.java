package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminDelWarp implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("delwarp");
    }

    @Override
    public String getPermission() {
        return "superior.admin.delwarp";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin delwarp <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_WARP_NAME.getMessage(locale) + "...>";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_DEL_WARP.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultipleIslands() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, Island island, String[] args) {
        IslandWarp islandWarp = CommandArguments.getWarp(sender, island, args, 3);

        if(islandWarp == null)
            return;

        boolean breakSign = false;

        Block signBlock = islandWarp.getLocation().getBlock();
        if(signBlock.getState() instanceof Sign){
            signBlock.setType(Material.AIR);
            signBlock.getWorld().dropItemNaturally(signBlock.getLocation(), new ItemStack(Material.SIGN));
            breakSign = true;
        }

        island.deleteWarp(islandWarp.getName());

        Locale.DELETE_WARP.send(sender, islandWarp.getName());

        if(breakSign){
            Locale.DELETE_WARP_SIGN_BROKE.send(sender);
        }
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getIslandWarps(island, args[3]) : new ArrayList<>();
    }

}
