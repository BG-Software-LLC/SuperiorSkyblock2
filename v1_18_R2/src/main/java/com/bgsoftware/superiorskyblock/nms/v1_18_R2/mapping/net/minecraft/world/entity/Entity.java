package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.entity;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.nbt.NBTTagCompound;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.network.PlayerConnection;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.server.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class Entity extends MappedObject<net.minecraft.world.entity.Entity> {

    private static final ReflectMethod<Float> ENTITY_GET_X_ROT = new ReflectMethod<>(net.minecraft.world.entity.Entity.class, "do");

    public Entity(net.minecraft.world.entity.Entity handle) {
        super(handle);
    }

    @Nullable
    public static Entity ofNullable(net.minecraft.world.entity.Entity handle) {
        return handle == null ? null : new Entity(handle);
    }

    public void setRemoved(net.minecraft.world.entity.Entity.RemovalReason removalReason) {
        handle.b(removalReason);
    }

    public float getXRot() {
        return ENTITY_GET_X_ROT.invoke(handle);
    }

    public float getYRot() {
        return handle.dn();
    }

    public UUID getUniqueID() {
        return handle.cm();
    }

    public void setPositionRotation(double x, double y, double z, float yaw, float pitch) {
        handle.a(x, y, z, yaw, pitch);
    }

    public boolean isBreedItem(ItemStack itemStack) {
        return ((EntityAnimal) handle).n(itemStack);
    }

    public GameProfile getProfile() {
        return ((EntityHuman) handle).fq();
    }

    public UUID getThrower() {
        return ((EntityItem) handle).j();
    }

    public void save(NBTTagCompound nbtTagCompound) {
        handle.f(nbtTagCompound.getHandle());
    }

    public DragonControllerManager getDragonControllerManager() {
        return new DragonControllerManager(((EntityEnderDragon) handle).fx());
    }

    public PlayerConnection getPlayerConnection() {
        return new PlayerConnection(((EntityPlayer) handle).b);
    }

    public CraftEntity getBukkitEntity() {
        return handle.getBukkitEntity();
    }

    public void setWorld(WorldServer worldServer) {
        handle.s = worldServer.getHandle();
    }

    public WorldServer getWorld() {
        return new WorldServer((net.minecraft.server.level.WorldServer) handle.s);
    }

    public boolean isInWorld(WorldServer worldServer) {
        return worldServer.getHandle().equals(handle.s);
    }

}
