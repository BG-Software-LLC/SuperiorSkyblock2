package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.entity.boss.enderdragon.phases;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.IDragonController;
import org.jetbrains.annotations.Nullable;

public final class DragonController extends MappedObject<IDragonController> {

    public DragonController(IDragonController handle) {
        super(handle);
    }

    @Nullable
    public static DragonController ofNullable(IDragonController handle) {
        return handle == null ? null : new DragonController(handle);
    }

    public DragonControllerPhase<? extends IDragonController> getControllerPhase() {
        return handle.i();
    }

    public boolean isSitting() {
        return handle.a();
    }

}
