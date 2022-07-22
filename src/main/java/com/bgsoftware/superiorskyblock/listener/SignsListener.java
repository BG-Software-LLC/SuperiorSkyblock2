package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.List;

public class SignsListener implements Listener {

    private final SuperiorSkyblockPlugin plugin;

    public SignsListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    /* SIGN PLACES */

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private void onSignPlace(SignChangeEvent e) {
        Island island = plugin.getGrid().getIslandAt(e.getBlock().getLocation());
        if (island != null) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
            String[] signLines = e.getLines();
            onSignPlace(superiorPlayer, island, e.getBlock().getLocation(), signLines, true);
            // In 1.16.5+ of Paper, the SignChangeEvent doesn't have the lines array of the signs.
            // Therefore, we manually need to set them.
            plugin.getNMSWorld().setSignLines(e, signLines);
        }
    }

    public void onSignPlace(SuperiorPlayer superiorPlayer, Island island, Location warpLocation,
                            String[] warpLines, boolean sendMessage) {
        /* Alias for shouldReplaceSignLines */
        shouldReplaceSignLines(superiorPlayer, island, warpLocation, warpLines, sendMessage);
    }

    public boolean shouldReplaceSignLines(SuperiorPlayer superiorPlayer, Island island, Location warpLocation,
                                          String[] warpLines, boolean sendMessage) {
        Location playerLocation = superiorPlayer.getLocation();
        if (playerLocation != null)
            warpLocation.setYaw(playerLocation.getYaw());

        if (isWarpSign(warpLines[0])) {
            handleWarpSignPlace(superiorPlayer, island, warpLocation, warpLines, sendMessage);
            return true;
        } else if (isVisitorsSign(warpLines[0])) {
            return handleVisitorsSignPlace(superiorPlayer, island, warpLocation, warpLines, sendMessage);
        }

        return false;
    }

    /* SIGN BREAKS */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignBreak(BlockBreakEvent e) {
        if (e.getBlock().getState() instanceof Sign) {
            if (preventSignBreak(e.getPlayer(), (Sign) e.getBlock().getState()))
                e.setCancelled(true);
        } else {
            for (BlockFace blockFace : BlockFace.values()) {
                Block faceBlock = e.getBlock().getRelative(blockFace);
                if (faceBlock.getState() instanceof Sign) {
                    boolean isSign;

                    if (ServerVersion.isLegacy()) {
                        org.bukkit.material.Sign sign = (org.bukkit.material.Sign) faceBlock.getState().getData();
                        isSign = sign.getAttachedFace().getOppositeFace() == blockFace;
                    } else {
                        Object blockData = plugin.getNMSWorld().getBlockData(faceBlock);
                        if (blockData instanceof org.bukkit.block.data.type.Sign) {
                            isSign = ((org.bukkit.block.data.type.Sign) blockData).getRotation().getOppositeFace() == blockFace;
                        } else {
                            isSign = ((org.bukkit.block.data.Directional) blockData).getFacing().getOppositeFace() == blockFace;
                        }
                    }

                    if (isSign && preventSignBreak(e.getPlayer(), (Sign) faceBlock.getState()))
                        e.setCancelled(true);
                }
            }
        }
    }

    public boolean preventSignBreak(Player player, Sign sign) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        Island island = plugin.getGrid().getIslandAt(sign.getLocation());

        if (island == null)
            return false;

        IslandWarp islandWarp = island.getWarp(sign.getLocation());

        if (islandWarp != null) {
            if (!plugin.getEventsBus().callIslandDeleteWarpEvent(superiorPlayer, island, islandWarp))
                return true;

            island.deleteWarp(superiorPlayer, sign.getLocation());
        } else {
            if (sign.getLine(0).equalsIgnoreCase(plugin.getSettings().getVisitorsSign().getActive())) {
                if (!plugin.getEventsBus().callIslandRemoveVisitorHomeEvent(superiorPlayer, island))
                    return true;

                island.setVisitorsLocation(null);
            }
        }

        return false;
    }

    private void handleWarpSignPlace(SuperiorPlayer superiorPlayer, Island island, Location warpLocation,
                                     String[] signLines, boolean sendMessage) {
        if (island.getIslandWarps().size() >= island.getWarpsLimit()) {
            if (sendMessage)
                Message.NO_MORE_WARPS.send(superiorPlayer);
            for (int i = 0; i < 4; i++)
                signLines[i] = "";
        }

        String warpName = Formatters.STRIP_COLOR_FORMATTER.format(signLines[1].trim());
        boolean privateFlag = signLines[2].equalsIgnoreCase("private");

        boolean creationFailed = false;

        if (warpName.isEmpty()) {
            if (sendMessage)
                Message.WARP_ILLEGAL_NAME.send(superiorPlayer);
            creationFailed = true;
        } else if (island.getWarp(warpName) != null) {
            if (sendMessage)
                Message.WARP_ALREADY_EXIST.send(superiorPlayer);
            creationFailed = true;
        } else if (!IslandUtils.isWarpNameLengthValid(warpName)) {
            if (sendMessage)
                Message.WARP_NAME_TOO_LONG.send(superiorPlayer);
            creationFailed = true;
        }

        if (!plugin.getEventsBus().callIslandCreateWarpEvent(superiorPlayer, island, warpName, warpLocation, !privateFlag, null))
            creationFailed = true;

        if (creationFailed) {
            for (int i = 0; i < 4; i++) {
                signLines[i] = "";
            }
        } else {
            List<String> signWarp = plugin.getSettings().getSignWarp();

            for (int i = 0; i < signWarp.size(); i++)
                signLines[i] = signWarp.get(i).replace("{0}", warpName);

            IslandWarp islandWarp = island.createWarp(warpName, warpLocation, null);
            islandWarp.setPrivateFlag(privateFlag);
            if (sendMessage)
                Message.SET_WARP.send(superiorPlayer, Formatters.LOCATION_FORMATTER.format(warpLocation));
        }
    }

    private boolean handleVisitorsSignPlace(SuperiorPlayer superiorPlayer, Island island, Location visitorsLocation,
                                            String[] warpLines, boolean sendMessage) {
        if (island.getIslandWarps().size() >= island.getWarpsLimit()) {
            if (sendMessage)
                Message.NO_MORE_WARPS.send(superiorPlayer);
            for (int i = 0; i < 4; i++)
                warpLines[i] = "";
            return true;
        }

        EventResult<Location> eventResult = plugin.getEventsBus().callIslandSetVisitorHomeEvent(superiorPlayer, island, visitorsLocation);

        if (eventResult.isCancelled())
            return false;

        StringBuilder descriptionBuilder = new StringBuilder();

        for (int i = 1; i < 4; i++) {
            String line = warpLines[i];
            if (!line.isEmpty())
                descriptionBuilder.append("\n").append(ChatColor.RESET).append(line);
        }

        String description = descriptionBuilder.length() < 1 ? "" : descriptionBuilder.substring(1);

        warpLines[0] = plugin.getSettings().getVisitorsSign().getActive();

        for (int i = 1; i <= 3; i++)
            warpLines[i] = Formatters.COLOR_FORMATTER.format(warpLines[i]);

        Location islandVisitorsLocation = island.getVisitorsLocation(null /* unused */);
        Block oldWelcomeSignBlock = islandVisitorsLocation == null ? null : islandVisitorsLocation.getBlock();

        if (oldWelcomeSignBlock != null && Materials.isSign(oldWelcomeSignBlock.getType())) {
            Sign oldWelcomeSign = (Sign) oldWelcomeSignBlock.getState();
            oldWelcomeSign.setLine(0, plugin.getSettings().getVisitorsSign().getInactive());
            oldWelcomeSign.update();
        }

        island.setVisitorsLocation(eventResult.getResult());

        EventResult<String> descriptionEventResult = plugin.getEventsBus().callIslandChangeDescriptionEvent(superiorPlayer, island, description);

        if (!descriptionEventResult.isCancelled())
            island.setDescription(descriptionEventResult.getResult());

        if (sendMessage)
            Message.SET_WARP.send(superiorPlayer, Formatters.LOCATION_FORMATTER.format(visitorsLocation));

        return true;
    }

    private boolean isWarpSign(String firstSignLine) {
        return firstSignLine.equalsIgnoreCase(plugin.getSettings().getSignWarpLine());
    }

    private boolean isVisitorsSign(String firstSignLine) {
        return firstSignLine.equalsIgnoreCase(plugin.getSettings().getVisitorsSign().getLine());
    }

}
