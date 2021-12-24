package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.world.entity.boss.enderdragon.phases;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerPhase;

public final class DragonControllerManager extends
        MappedObject<net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerManager> {

    public DragonControllerManager(net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerManager handle) {
        super(handle);
    }

    public void setControllerPhase(DragonControllerPhase<?> dragonControllerPhase) {
        handle.a(dragonControllerPhase);
    }

    public DragonController getDragonController() {
        return DragonController.ofNullable(handle.a());
    }

}
