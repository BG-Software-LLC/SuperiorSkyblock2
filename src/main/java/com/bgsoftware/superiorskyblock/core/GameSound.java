package com.bgsoftware.superiorskyblock.core;

import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class GameSound {

    private final Sound sound;
    private final float volume;
    private final float pitch;

    public GameSound(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void playSound(HumanEntity humanEntity) {
        playSound((Player) humanEntity);
    }

    public void playSound(Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
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

    public GameSound copy() {
        return new GameSound(this.sound, this.volume, this.pitch);
    }

    public static boolean isEmpty(@Nullable GameSound soundWrapper) {
        return soundWrapper == null || soundWrapper.sound == null || soundWrapper.volume <= 0 || soundWrapper.pitch <= 0;
    }

}
