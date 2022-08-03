package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.entity;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.nbt.NBTTagCompound;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.server.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.server.network.PlayerConnection;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class Entity extends MappedObject<net.minecraft.world.entity.Entity> {

    public Entity(net.minecraft.world.entity.Entity handle) {
        super(handle);
    }

    @Nullable
    public static Entity ofNullable(net.minecraft.world.entity.Entity handle) {
        return handle == null ? null : new Entity(handle);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "remove",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setRemoved(net.minecraft.world.entity.Entity.RemovalReason removalReason) {
        handle.a(removalReason);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getXRot",
            type = Remap.Type.METHOD,
            remappedName = "dn")
    public float getXRot() {
        return handle.dn();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getYRot",
            type = Remap.Type.METHOD,
            remappedName = "dm")
    public float getYRot() {
        return handle.dm();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "getUUID",
            type = Remap.Type.METHOD,
            remappedName = "cm")
    public UUID getUniqueID() {
        return handle.cm();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "absMoveTo",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void setPositionRotation(double x, double y, double z, float yaw, float pitch) {
        handle.a(x, y, z, yaw, pitch);
    }

    @Remap(classPath = "net.minecraft.world.entity.animal.Animal",
            name = "isFood",
            type = Remap.Type.METHOD,
            remappedName = "n")
    public boolean isBreedItem(ItemStack itemStack) {
        return ((EntityAnimal) handle).n(itemStack);
    }

    @Remap(classPath = "net.minecraft.world.entity.player.Player",
            name = "getGameProfile",
            type = Remap.Type.METHOD,
            remappedName = "fp")
    public GameProfile getProfile() {
        return ((EntityHuman) handle).fp();
    }

    @Remap(classPath = "net.minecraft.world.entity.item.ItemEntity",
            name = "getOwner",
            type = Remap.Type.METHOD,
            remappedName = "i")
    public UUID getThrower() {
        return ((EntityItem) handle).i();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "saveWithoutId",
            type = Remap.Type.METHOD,
            remappedName = "f")
    public void save(NBTTagCompound nbtTagCompound) {
        handle.f(nbtTagCompound.getHandle());
    }

    @Remap(classPath = "net.minecraft.world.entity.boss.enderdragon.EnderDragon",
            name = "getPhaseManager",
            type = Remap.Type.METHOD,
            remappedName = "fw")
    public DragonControllerManager getDragonControllerManager() {
        return new DragonControllerManager(((EntityEnderDragon) handle).fw());
    }

    @Remap(classPath = "net.minecraft.server.network.ServerGamePacketListenerImpl",
            name = "connection",
            type = Remap.Type.FIELD,
            remappedName = "a")
    public PlayerConnection getPlayerConnection() {
        return new PlayerConnection(((EntityPlayer) handle).b);
    }

    public CraftEntity getBukkitEntity() {
        return handle.getBukkitEntity();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "level",
            type = Remap.Type.FIELD,
            remappedName = "t")
    public void setWorld(WorldServer worldServer) {
        handle.t = worldServer.getHandle();
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "level",
            type = Remap.Type.FIELD,
            remappedName = "t")
    public WorldServer getWorld() {
        return new WorldServer((net.minecraft.server.level.WorldServer) handle.t);
    }

    @Remap(classPath = "net.minecraft.world.entity.Entity",
            name = "level",
            type = Remap.Type.FIELD,
            remappedName = "t")
    public boolean isInWorld(WorldServer worldServer) {
        return worldServer.getHandle().equals(handle.t);
    }

}
