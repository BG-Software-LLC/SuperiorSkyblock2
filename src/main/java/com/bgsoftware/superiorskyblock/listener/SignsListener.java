package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.signs.IslandSigns;
import com.bgsoftware.superiorskyblock.world.SignType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
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
        String[] signLines;

        IslandSigns.Result result;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location warpLocation = e.getBlock().getLocation(wrapper.getHandle());
            SignType signType = plugin.getNMSWorld().getSignType(e.getBlock());

            // Hanging signs are not allowed
            if (signType == SignType.HANGING_SIGN || signType == SignType.HANGING_WALL_SIGN)
                return;

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            signLines = e.getLines().clone();

            result = IslandSigns.handleSignPlace(superiorPlayer, warpLocation, signLines, true);
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
                        switch (plugin.getNMSWorld().getSignType(blockData)) {
                            case WALL_SIGN:
                                // Wall signs will only be broken if they are attached to the block
                                isSignGonnaBreak = ((Directional) blockData).getFacing() == blockFace;
                                break;
                            case STANDING_SIGN:
                                // Standing signs will only be broken if they are placed on top of the block
                                isSignGonnaBreak = blockFace == BlockFace.UP;
                                break;
                            case HANGING_WALL_SIGN:
                            case HANGING_SIGN:
                                // Hanging signs are not allowed as warp signs
                                isSignGonnaBreak = false;
                                break;
                            default:
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
