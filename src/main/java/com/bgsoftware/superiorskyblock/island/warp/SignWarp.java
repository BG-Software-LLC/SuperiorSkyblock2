package com.bgsoftware.superiorskyblock.island.warp;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SignWarp {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private SignWarp() {

    }

    public static void trySignWarpBreak(IslandWarp islandWarp, CommandSender commandSender) {
        Block signBlock = islandWarp.getLocation().getBlock();
        // We check for a sign block at the warp's location.
        if (signBlock.getState() instanceof Sign) {
            Sign sign = (Sign) signBlock.getState();

            List<String> configSignWarp = new ArrayList<>(plugin.getSettings().getSignWarp());
            configSignWarp.replaceAll(line -> line.replace("{0}", islandWarp.getName()));
            String[] signLines = sign.getLines();

            for (int i = 0; i < signLines.length && i < configSignWarp.size(); ++i) {
                if (!signLines[i].equals(configSignWarp.get(i)))
                    return;
            }

            // Detected warp sign
            signBlock.setType(Material.AIR);
            signBlock.getWorld().dropItemNaturally(signBlock.getLocation(), new ItemStack(Material.SIGN));

            Message.DELETE_WARP_SIGN_BROKE.send(commandSender);
        }
    }

}
