package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.signs.IslandSigns;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.world.SignType;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class SignsListener extends AbstractGameEventListener {

    private static final BlockFace[] NEARBY_BLOCKS = new BlockFace[]{
            BlockFace.UP, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST
    };

    public SignsListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        registerListeners();
    }

    private void onSignPlace(GameEvent<GameEventArgs.SignChangeEvent> e) {
        Player player = e.getArgs().player;
        Block block = e.getArgs().block;
        String[] lines = e.getArgs().lines;

        String[] signLines;

        IslandSigns.Result result;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location warpLocation = block.getLocation(wrapper.getHandle());
            SignType signType = plugin.getNMSWorld().getSignType(block);

            // Hanging signs are not allowed
            if (signType == SignType.HANGING_SIGN || signType == SignType.HANGING_WALL_SIGN)
                return;

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
            signLines = lines.clone();

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
        if (Arrays.equals(signLines, lines))
            return;

        // We want to update the sign only one tick later, so other plugins don't interface with it
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/1916
        BukkitExecutor.sync(() -> {
            BlockState blockState = block.getState();
            if (blockState instanceof Sign) {
                Sign sign = (Sign) blockState;
                for (int i = 0; i < signLines.length; ++i)
                    sign.setLine(i, signLines[i]);
                sign.update();
            }
        }, 1L);
    }

    public void onSignBreak(GameEvent<GameEventArgs.BlockBreakEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        Block block = e.getArgs().block;

        if (block.getState() instanceof Sign) {
            IslandSigns.Result result = IslandSigns.handleSignBreak(superiorPlayer, (Sign) block.getState());
            if (result.isCancelEvent())
                e.setCancelled();

            return;
        }

        for (BlockFace blockFace : NEARBY_BLOCKS) {
            Block faceBlock = block.getRelative(blockFace);
            BlockState blockState = faceBlock.getState();
            if (!(blockState instanceof Sign))
                continue;

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
                    e.setCancelled();
                    return;
                }
            }
        }
    }

    private void registerListeners() {
        registerCallback(GameEventType.SIGN_CHANGE_EVENT, GameEventPriority.NORMAL, this::onSignPlace);
        registerCallback(GameEventType.BLOCK_BREAK_EVENT, GameEventPriority.NORMAL, this::onSignBreak);
    }

}
