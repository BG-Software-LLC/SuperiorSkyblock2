package com.bgsoftware.superiorskyblock.nms.v26_2_folia.dragon;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.google.gson.Gson;

import java.util.List;
import java.util.UUID;

public record DragonFightState(boolean killed, boolean previouslyKilled, UUID dragon,
                               List<Integer> gateways, String phase, int time, List<UUID> crystals, Integer portalY) {
    private static final Gson GSON = new Gson();

    public DragonFightState {
        gateways = List.copyOf(gateways);
        crystals = List.copyOf(crystals);
    }

    public static String key(Dimension dimension) {
        return "folia-dragon:" + dimension.getName();
    }

    public static DragonFightState load(Island island, String key, boolean fresh, UUID existing, boolean previouslyKilled) {
        if (!fresh) {
            String stored = island.getPersistentDataContainer().get(key, PersistentDataType.STRING);
            if (stored != null) {
                DragonFightState state = GSON.fromJson(stored, DragonFightState.class);
                if (existing == null)
                    return state;
                return new DragonFightState(false, state.previouslyKilled || previouslyKilled, existing,
                        state.gateways, null, 0, List.of(), state.portalY);
            }
        }
        return new DragonFightState(existing == null, previouslyKilled, existing, List.of(), null, 0, List.of(), null);
    }

    public String serialize() {
        return GSON.toJson(this);
    }
}
