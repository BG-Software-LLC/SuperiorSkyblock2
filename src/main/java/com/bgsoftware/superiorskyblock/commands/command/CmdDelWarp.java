package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdDelWarp implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("delwarp");
    }

    @Override
    public String getPermission() {
        return "superior.island.delwarp";
    }

    @Override
    public String getUsage() {
        return "island delwarp <" + Locale.COMMAND_ARGUMENT_WARP_NAME.getMessage() + ">";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_DEL_WARP.getMessage();
    }

    @Override
    public int getMinArgs() {
        return 2;
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
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPermission.DELETE_WARP)){
            Locale.NO_DELETE_WARP_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPermission.DELETE_WARP));
            return;
        }

        if(island.getWarpLocation(args[1]) == null){
            Locale.INVALID_WARP.send(superiorPlayer, args[1]);
            return;
        }

        boolean breakSign = false;

        Block signBlock = island.getWarpLocation(args[1]).getBlock();
        if(signBlock.getState() instanceof Sign){
            signBlock.setType(Material.AIR);
            signBlock.getWorld().dropItemNaturally(signBlock.getLocation(), new ItemStack(Material.SIGN));
            breakSign = true;
        }

        if(args[1].equalsIgnoreCase(SIsland.VISITORS_WARP_NAME)){
            island.setVisitorsLocation(null);
        }
        else{
            island.deleteWarp(args[1]);
        }

        Locale.DELETE_WARP.send(superiorPlayer, args[1]);

        if(breakSign){
            Locale.DELETE_WARP_SIGN_BROKE.send(superiorPlayer);
        }

    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPermission.DELETE_WARP)){
            List<String> list = new ArrayList<>();

            for(String warpName : island.getAllWarps()){
                if(warpName.toLowerCase().startsWith(args[1].toLowerCase()))
                    list.add(warpName);
            }

            return list;
        }

        return new ArrayList<>();
    }
}
