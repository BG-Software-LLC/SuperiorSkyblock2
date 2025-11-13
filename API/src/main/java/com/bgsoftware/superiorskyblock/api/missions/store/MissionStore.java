package com.bgsoftware.superiorskyblock.api.missions.store;

import java.io.File;


public interface MissionStore {
    UnifiedPayload read(File file);

    void write(File file, UnifiedPayload payload);
}
