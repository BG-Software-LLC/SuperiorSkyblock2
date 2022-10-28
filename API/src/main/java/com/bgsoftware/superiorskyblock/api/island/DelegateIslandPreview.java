package com.bgsoftware.superiorskyblock.api.island;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;

public class DelegateIslandPreview implements IslandPreview {

    protected final IslandPreview handle;

    protected DelegateIslandPreview(IslandPreview handle) {
        this.handle = handle;
    }

    @Override
    public SuperiorPlayer getPlayer() {
        return this.handle.getPlayer();
    }

    @Override
    public Location getLocation() {
        return this.handle.getLocation();
    }

    @Override
    public String getSchematic() {
        return this.handle.getSchematic();
    }

    @Override
    public String getIslandName() {
        return this.handle.getIslandName();
    }

    @Override
    public void handleConfirm() {
        this.handle.handleConfirm();
    }

    @Override
    public void handleCancel() {
        this.handle.handleCancel();
    }

    @Override
    public void handleEscape() {
        this.handle.handleEscape();
    }

}
