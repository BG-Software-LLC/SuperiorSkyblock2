package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.entity.boss.enderdragon.phases;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.MappedObject;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.IDragonController;

import javax.annotation.Nullable;

public final class DragonControllerManager extends
        MappedObject<net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerManager> {

    public DragonControllerManager(net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerManager handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager",
            name = "setPhase",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setControllerPhase(DragonControllerPhase<?> dragonControllerPhase) {
        handle.a(dragonControllerPhase);
    }

    @Remap(classPath = "net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhaseManager",
            name = "getCurrentPhase",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public IDragonController getDragonController() {
        return handle.a();
    }

}
