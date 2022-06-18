package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.level.block;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.sounds.SoundEffect;

public class SoundEffectType extends MappedObject<net.minecraft.world.level.block.SoundEffectType> {

    public SoundEffectType(net.minecraft.world.level.block.SoundEffectType handle) {
        super(handle);
    }

    public SoundEffect getPlaceSound() {
        return handle.e();
    }

    public float getVolume() {
        return handle.a();
    }

    public float getPitch() {
        return handle.b();
    }

}
