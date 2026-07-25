package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class GameSoundImpl implements GameSound {

    private final Sound sound;
    private final float volume;
    private final float pitch;

    public GameSoundImpl(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public static boolean isEmpty(@Nullable GameSound gameSound) {
        return gameSound == null || gameSound.getSound() == null || gameSound.getVolume() <= 0 || gameSound.getPitch() <= 0;
    }

    public static void playSound(HumanEntity humanEntity, @Nullable GameSound gameSound) {
        if (!isEmpty(gameSound))
            playSound((Player) humanEntity, gameSound);
    }

    public static void playSound(Player player, @Nullable GameSound gameSound) {
        if (!isEmpty(gameSound)) {
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                player.playSound(player.getLocation(wrapper.getHandle()), gameSound.getSound(),
                        gameSound.getVolume(), gameSound.getPitch());
            }
        }
    }

}
