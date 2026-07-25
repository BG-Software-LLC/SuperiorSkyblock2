package com.bgsoftware.superiorskyblock.island.preview;

import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.List;

public interface IslandPreviews {

    void startIslandPreview(IslandPreview islandPreview);

    IslandPreview endIslandPreview(SuperiorPlayer superiorPlayer);

    IslandPreview getIslandPreview(SuperiorPlayer superiorPlayer);

    List<IslandPreview> getActivePreviews();

}
