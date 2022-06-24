package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.server.level;

import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.entity.Entity;
import net.minecraft.server.level.EntityPlayer;

import java.util.Collection;

public final class BossBattleServer extends MappedObject<net.minecraft.server.level.BossBattleServer> {

    public BossBattleServer(net.minecraft.server.level.BossBattleServer handle) {
        super(handle);
    }

    public Collection<Entity> getPlayers() {
        return new SequentialListBuilder<Entity>().build(handle.h(), Entity::new);
    }

    public void removePlayer(Entity entity) {
        handle.b((EntityPlayer) entity.getHandle());
    }

    public void addPlayer(Entity entity) {
        handle.a((EntityPlayer) entity.getHandle());
    }

}
