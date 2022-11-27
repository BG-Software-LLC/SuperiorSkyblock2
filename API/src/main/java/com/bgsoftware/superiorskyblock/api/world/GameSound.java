package com.bgsoftware.superiorskyblock.api.world;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.google.common.base.Preconditions;
import org.bukkit.Sound;

public interface GameSound {

    Sound getSound();

    float getVolume();

    float getPitch();

    static GameSound of(Sound sound, float volume, float pitch) {
        Preconditions.checkNotNull(sound, "sound parameter cannot be null");
        return SuperiorSkyblockAPI.getFactory().createGameSound(sound, volume, pitch);
    }

}
