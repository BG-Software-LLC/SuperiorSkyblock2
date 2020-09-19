package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuIslandCreation;
import org.bukkit.Location;

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
