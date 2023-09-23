package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.island.signs.IslandSigns;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Arrays;

public class SignsListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public SignsListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    /* SIGN PLACES */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onSignPlace(SignChangeEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        String[] signLines = e.getLines();

        IslandSigns.Result result = IslandSigns.handleSignPlace(superiorPlayer, e.getBlock().getLocation(), signLines, true);
        switch (result.getReason()) {
            case NOT_IN_ISLAND:
                return;
            case SUCCESS:
                break;
            default:
                Arrays.fill(signLines, "");
                break;
        }

        // In 1.16.5+ of Paper, the SignChangeEvent doesn't have the lines array of the signs.
        // Therefore, we manually need to set them.
        plugin.getNMSWorld().setSignLines(e, signLines);
    }

    /* SIGN BREAKS */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (e.getBlock().getState() instanceof Sign) {
            IslandSigns.Result result = IslandSigns.handleSignBreak(superiorPlayer, (Sign) e.getBlock().getState());
            if (result.isCancelEvent())
                e.setCancelled(true);
        } else {
            for (BlockFace blockFace : BlockFace.values()) {
                Block faceBlock = e.getBlock().getRelative(blockFace);
                BlockState blockState = faceBlock.getState();
                if (blockState instanceof Sign) {
                    boolean isSign;

                    if (ServerVersion.isLegacy()) {
                        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) blockState.getData();
                        isSign = sign.getAttachedFace().getOppositeFace() == blockFace;
                    } else {
                        Object blockData = plugin.getNMSWorld().getBlockData(faceBlock);
                        if (blockData instanceof org.bukkit.block.data.Rotatable) {
                            isSign = ((org.bukkit.block.data.Rotatable) blockData).getRotation().getOppositeFace() == blockFace;
                        } else if (blockData instanceof org.bukkit.block.data.Directional) {
                            isSign = ((org.bukkit.block.data.Directional) blockData).getFacing().getOppositeFace() == blockFace;
                        } else {
                            throw new RuntimeException("Found sign that cannot be handled: " + blockData);
                        }
                    }

                    if (isSign) {
                        IslandSigns.Result result = IslandSigns.handleSignBreak(superiorPlayer, (Sign) blockState);
                        if (result.isCancelEvent()) {
                            e.setCancelled(true);
                            break;
                        }
                    }
                }
            }
        }
    }

}
