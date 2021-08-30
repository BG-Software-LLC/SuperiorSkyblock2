package com.bgsoftware.superiorskyblock.world.preview;

import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultIslandPreviews implements IslandPreviews {

    private final Map<UUID, IslandPreview> islandPreviews = new ConcurrentHashMap<>();

    @Override
    public void startIslandPreview(IslandPreview islandPreview) {
        this.islandPreviews.put(islandPreview.getPlayer().getUniqueId(), islandPreview);
    }

    @Override
    public IslandPreview endIslandPreview(SuperiorPlayer superiorPlayer) {
        return this.islandPreviews.remove(superiorPlayer.getUniqueId());
    }

    @Override
    public IslandPreview getIslandPreview(SuperiorPlayer superiorPlayer) {
        return this.islandPreviews.get(superiorPlayer.getUniqueId());
    }

    @Override
    public List<IslandPreview> getActivePreviews() {
        return Collections.unmodifiableList(new ArrayList<>(this.islandPreviews.values()));
    }

}
