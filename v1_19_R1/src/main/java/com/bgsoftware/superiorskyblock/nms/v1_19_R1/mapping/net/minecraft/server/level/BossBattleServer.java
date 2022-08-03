package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.server.level;

import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.entity.Entity;
import net.minecraft.server.level.EntityPlayer;

import java.util.Collection;

public final class BossBattleServer extends MappedObject<net.minecraft.server.level.BossBattleServer> {

    public BossBattleServer(net.minecraft.server.level.BossBattleServer handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerBossEvent",
            name = "getPlayers",
            type = Remap.Type.METHOD,
            remappedName = "h")
    public Collection<Entity> getPlayers() {
        return new SequentialListBuilder<Entity>().build(handle.h(), Entity::new);
    }

    @Remap(classPath = "net.minecraft.server.level.ServerBossEvent",
            name = "removePlayer",
            type = Remap.Type.METHOD,
            remappedName = "b")
    public void removePlayer(Entity entity) {
        handle.b((EntityPlayer) entity.getHandle());
    }

    @Remap(classPath = "net.minecraft.server.level.ServerBossEvent",
            name = "addPlayer",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void addPlayer(Entity entity) {
        handle.a((EntityPlayer) entity.getHandle());
    }

}
