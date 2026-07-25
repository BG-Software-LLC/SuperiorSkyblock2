package com.bgsoftware.superiorskyblock.island.preview;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.menu.MenuIslandCreationConfig;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.MenuActions;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import com.google.common.base.Preconditions;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SIslandPreview implements IslandPreview {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final SuperiorPlayer superiorPlayer;
    private final Location previewLocation;
    private final Schematic schematic;
    private final String islandName;
    private final GameMode previousGameMode;

    public SIslandPreview(SuperiorPlayer superiorPlayer, Location previewLocation, Schematic schematic, String islandName, GameMode previousGameMode) {
        this.superiorPlayer = superiorPlayer;
        this.previewLocation = previewLocation;
        this.schematic = schematic;
        this.islandName = islandName;
        this.previousGameMode = previousGameMode;

        Player player = superiorPlayer.asPlayer();
        Preconditions.checkNotNull(player, "Cannot start island preview to an offline player.");

        PlayerChat.listen(player, message -> {
            if (message.equalsIgnoreCase(Message.ISLAND_PREVIEW_CONFIRM_TEXT.getMessage(superiorPlayer.getUserLocale()))) {
                handleConfirm();
                return true;
            } else if (message.equalsIgnoreCase(Message.ISLAND_PREVIEW_CANCEL_TEXT.getMessage(superiorPlayer.getUserLocale()))) {
                handleCancel();
                return true;
            }

            return false;
        });
    }

    @Override
    public SuperiorPlayer getPlayer() {
        return superiorPlayer;
    }

    @Override
    public Location getLocation() {
        return previewLocation.clone();
    }

    @Override
    public Location getLocation(Location location) {
        if (location != null) {
            location.setWorld(this.previewLocation.getWorld());
            location.setX(this.previewLocation.getX());
            location.setY(this.previewLocation.getY());
            location.setZ(this.previewLocation.getZ());
            location.setYaw(this.previewLocation.getYaw());
            location.setPitch(this.previewLocation.getPitch());
        }

        return location;
    }

    @Override
    public String getSchematic() {
        return this.schematic.getName();
    }

    @Override
    public String getIslandName() {
        return this.islandName;
    }

    @Override
    public GameMode getPreviousGameMode() {
        return this.previousGameMode;
    }

    @Override
    public void handleConfirm() {
        MenuIslandCreationConfig creationConfig = plugin.getProviders().getMenusProvider().getIslandCreationConfig(this.schematic);
        MenuActions.simulateIslandCreationClick(superiorPlayer, islandName, creationConfig, false, superiorPlayer.getOpenedView());
        superiorPlayer.runIfOnline(PlayerChat::remove);
    }

    @Override
    public void handleCancel() {
        plugin.getGrid().cancelIslandPreview(superiorPlayer);
        Message.ISLAND_PREVIEW_CANCEL.send(superiorPlayer);
    }

    @Override
    public void handleEscape() {
        plugin.getGrid().cancelIslandPreview(superiorPlayer);
        Message.ISLAND_PREVIEW_CANCEL_DISTANCE.send(superiorPlayer);
    }

}
