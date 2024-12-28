package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.signs.IslandSigns;
import org.bukkit.Location;
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

    private static final BlockFace[] NEARBY_BLOCKS = new BlockFace[]{
            BlockFace.UP, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST
    };

    private final SuperiorSkyblockPlugin plugin;

    public SignsListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    /* SIGN PLACES */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onSignPlace(SignChangeEvent e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        String[] signLines = e.getLines().clone();

        IslandSigns.Result result;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            result = IslandSigns.handleSignPlace(superiorPlayer,
                    e.getBlock().getLocation(wrapper.getHandle()), signLines, true);
        }
        switch (result.getReason()) {
            case NOT_IN_ISLAND:
                return;
            case SUCCESS:
                break;
            default:
                Arrays.fill(signLines, "");
                break;
        }

        // Only update the lines if changed
        if (Arrays.equals(signLines, e.getLines()))
            return;

        // We want to update the sign only one tick later, so other plugins don't interface with it
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/1916
        BukkitExecutor.sync(() -> {
            BlockState blockState = e.getBlock().getState();
            if (blockState instanceof Sign) {
                Sign sign = (Sign) blockState;
                for (int i = 0; i < signLines.length; ++i)
                    sign.setLine(i, signLines[i]);
                sign.update();
            }
        }, 1L);
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
            for (BlockFace blockFace : NEARBY_BLOCKS) {
                Block faceBlock = e.getBlock().getRelative(blockFace);
                BlockState blockState = faceBlock.getState();
                if (blockState instanceof Sign) {
                    boolean isSignGonnaBreak;

                    if (ServerVersion.isLegacy()) {
                        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) blockState.getData();
                        isSignGonnaBreak = sign.getAttachedFace().getOppositeFace() == blockFace;
                    } else {
                        Object blockData = plugin.getNMSWorld().getBlockData(faceBlock);
                        if (blockData instanceof org.bukkit.block.data.type.WallSign) {
                            // Wall signs will only be broken if they are attached to the block
                            isSignGonnaBreak = ((org.bukkit.block.data.type.WallSign) blockData).getFacing() == blockFace;
                        } else if (blockData instanceof org.bukkit.block.data.type.Sign) {
                            // Standing signs will only be broken if they are placed on top of the block
                            isSignGonnaBreak = blockFace == BlockFace.UP;
                        } else {
                            throw new RuntimeException("Found sign that cannot be handled: " + blockData);
                        }
                    }

                    if (isSignGonnaBreak) {
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
