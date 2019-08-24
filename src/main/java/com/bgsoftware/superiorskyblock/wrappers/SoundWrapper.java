package com.bgsoftware.superiorskyblock.wrappers;

import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public final class SoundWrapper {

    private Sound sound;
    private float volume, pitch;

    public SoundWrapper(Sound sound, float volume, float pitch){
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public void playSound(HumanEntity humanEntity){
        playSound((Player) humanEntity);
    }

    public void playSound(Player player){
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

}
