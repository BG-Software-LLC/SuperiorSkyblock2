package com.bgsoftware.superiorskyblock.island.preview;

import com.bgsoftware.common.collections.Maps;
import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DefaultIslandPreviews implements IslandPreviews {

    private final Map<UUID, IslandPreview> islandPreviews = Maps.newConcurrentHashMap();

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
        return new SequentialListBuilder<IslandPreview>().build(this.islandPreviews.values());
    }

}
