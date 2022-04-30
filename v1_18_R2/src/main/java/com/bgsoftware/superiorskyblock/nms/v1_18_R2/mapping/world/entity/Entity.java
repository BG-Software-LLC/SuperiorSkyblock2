package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.entity;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.pathfinder.PathEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.nbt.NBTTagCompound;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.network.PlayerConnection;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.entity.boss.enderdragon.phases.DragonControllerManager;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.phys.AxisAlignedBB;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderCrystal;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import net.minecraft.world.level.pathfinder.PathPoint;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

public final class Entity extends MappedObject<net.minecraft.world.entity.Entity> {

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

    public AxisAlignedBB getBoundingBox() {
        return new AxisAlignedBB(handle.cw());
    }

    public Random getRandom() {
        return ((EntityLiving) handle).dL();
    }

    public float getXRot() {
        return ENTITY_GET_X_ROT.invoke(handle);
    }

    public float getYRot() {
        return handle.dn();
    }

    public void setXRot(float rotation) {
        handle.p(rotation);
    }

    public Vec3D getPositionVector() {
        return handle.ac();
    }

    public UUID getUniqueID() {
        return handle.cm();
    }

    public float getHealth() {
        return (float) ((LivingEntity) handle.getBukkitEntity()).getHealth();
    }

    public float getMaxHealth() {
        //noinspection deprecation
        return (float) ((LivingEntity) handle.getBukkitEntity()).getMaxHealth();
    }

    public boolean hasCustomName() {
        return handle.Y();
    }

    public IChatBaseComponent getScoreboardDisplayName() {
        return handle.C_();
    }

    public BlockPosition getChunkCoordinates() {
        return new BlockPosition(handle.cW());
    }

    public void setInvulnerable(boolean invulnerable) {
        handle.m(invulnerable);
    }

    public void setBeamTarget(@Nullable BlockPosition blockPosition) {
        ((EntityEnderCrystal) handle).a(blockPosition == null ? null : blockPosition.getHandle());
    }

    public void setPositionRotation(double x, double y, double z, float yaw, float pitch) {
        handle.a(x, y, z, yaw, pitch);
    }

    public double locX() {
        return getPositionVector().b;
    }

    public double locY() {
        return getPositionVector().c;
    }

    public double locZ() {
        return getPositionVector().d;
    }

    public void setHealth(float health) {
        ((LivingEntity) handle.getBukkitEntity()).setHealth(health);
    }

    @Nullable
    public EnderDragonBattle getEnderDragonBattle() {
        return ((EntityEnderDragon) handle).fy();
    }

    public PathEntity findPath(int from, int to, @Nullable PathPoint pathNode) {
        return new PathEntity(((EntityEnderDragon) handle).a(from, to, pathNode));
    }

    public boolean isInvisible() {
        return handle.bU();
    }

    public Vec3D getMot() {
        return handle.da();
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

    public void onCrystalDestroyed(EntityEnderCrystal entityEnderCrystal, BlockPosition blockPosition, DamageSource damageSource) {
        ((EntityEnderDragon) handle).a(entityEnderCrystal, blockPosition.getHandle(), damageSource);
    }

    public Entity getEntityHeadComplexPart() {
        return new Entity(((EntityEnderDragon) handle).e);
    }

}
