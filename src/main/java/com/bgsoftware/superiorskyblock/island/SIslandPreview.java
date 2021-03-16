package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuIslandCreation;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class SIslandPreview implements IslandPreview {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final SuperiorPlayer superiorPlayer;
    private final Location previewLocation;
    private final String schematic;
    private final String islandName;

    public SIslandPreview(SuperiorPlayer superiorPlayer, Location previewLocation, String schematic, String islandName){
        this.superiorPlayer = superiorPlayer;
        this.previewLocation = previewLocation;
        this.schematic = schematic;
        this.islandName = islandName;

        Player player = superiorPlayer.asPlayer();
        Preconditions.checkNotNull(player, "Cannot start island preview to an offline player.");

        PlayerChat.listen(player, message -> {
            if(message.equalsIgnoreCase(Locale.ISLAND_PREVIEW_CONFIRM_TEXT.getMessage(superiorPlayer.getUserLocale()))){
                handleConfirm();
                return true;
            }
            else if(message.equalsIgnoreCase(Locale.ISLAND_PREVIEW_CANCEL_TEXT.getMessage(superiorPlayer.getUserLocale()))){
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
        return previewLocation;
    }

    @Override
    public String getSchematic() {
        return schematic;
    }

    @Override
    public String getIslandName() {
        return islandName;
    }

    @Override
    public void handleConfirm() {
        MenuIslandCreation.simulateClick(superiorPlayer, islandName, schematic, false);
        Player player = superiorPlayer.asPlayer();
        assert player != null;
        PlayerChat.remove(player);
    }

    @Override
    public void handleCancel() {
        plugin.getGrid().cancelIslandPreview(superiorPlayer);
        Locale.ISLAND_PREVIEW_CANCEL.send(superiorPlayer);
    }

    @Override
    public void handleEscape() {
        plugin.getGrid().cancelIslandPreview(superiorPlayer);
        Locale.ISLAND_PREVIEW_CANCEL_DISTANCE.send(superiorPlayer);
    }

}
