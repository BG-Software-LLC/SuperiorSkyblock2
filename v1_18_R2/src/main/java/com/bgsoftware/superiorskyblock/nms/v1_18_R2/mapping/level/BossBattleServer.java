package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.entity.Entity;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;

import java.util.Collection;
import java.util.stream.Collectors;

public final class BossBattleServer extends MappedObject<net.minecraft.server.level.BossBattleServer> {

    public BossBattleServer(net.minecraft.server.level.BossBattleServer handle) {
        super(handle);
    }

    public void setVisible(boolean visible) {
        handle.d(visible);
    }

    public Collection<Entity> getPlayers() {
        return handle.h().stream().map(Entity::new).collect(Collectors.toList());
    }

    public void setProgress(float progress) {
        handle.a(progress);
    }

    public void removePlayer(Entity entity) {
        handle.b((EntityPlayer) entity.getHandle());
    }

    public void addPlayer(Entity entity) {
        handle.a((EntityPlayer) entity.getHandle());
    }

    public void setName(IChatBaseComponent component) {
        handle.a(component);
    }

}
