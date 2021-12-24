package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.world.entity;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.dimension.end.EnderDragonBattle;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.pathfinder.PathEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.pathfinder.PathPoint;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.nbt.NBTTagCompound;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.network.PlayerConnection;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.network.chat.ChatBaseComponent;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.world.entity.boss.enderdragon.phases.DragonControllerManager;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.world.phys.AxisAlignedBB;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.animal.EntityAnimal;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderCrystal;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Random;
import java.util.UUID;

public final class Entity extends MappedObject<net.minecraft.world.entity.Entity> {

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
        return ((EntityLiving) handle).dK();
    }

    public float getXRot() {
        return handle.dn();
    }

    public float getYRot() {
        return handle.dm();
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

    public ChatBaseComponent getScoreboardDisplayName() {
        return new ChatBaseComponent(handle.C_());
    }

    public Entity getEntity(WorldServer worldServer, UUID uuid) {
        return new Entity(worldServer.getHandle().a(uuid));
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
        return EnderDragonBattle.ofNullable(((EntityEnderDragon) handle).fx());
    }

    public PathEntity findPath(int from, int to, @Nullable PathPoint pathNode) {
        return new PathEntity(((EntityEnderDragon) handle).a(from, to, pathNode == null ? null : pathNode.getHandle()));
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
        return ((EntityHuman) handle).fp();
    }

    public UUID getThrower() {
        return ((EntityItem) handle).j();
    }

    public void save(NBTTagCompound nbtTagCompound) {
        handle.f(nbtTagCompound.getHandle());
    }

    public DragonControllerManager getDragonControllerManager() {
        return new DragonControllerManager(((EntityEnderDragon) handle).fw());
    }

    public PlayerConnection getPlayerConnection() {
        return new PlayerConnection(((EntityPlayer) handle).b);
    }

    public CraftEntity getBukkitEntity() {
        return handle.getBukkitEntity();
    }

    public void setWorld(WorldServer worldServer) {
        handle.t = worldServer.getHandle();
    }

    public WorldServer getWorld() {
        return new WorldServer((net.minecraft.server.level.WorldServer) handle.t);
    }

    public boolean isInWorld(WorldServer worldServer) {
        return worldServer.getHandle().equals(handle.t);
    }

    public void onCrystalDestroyed(EntityEnderCrystal entityEnderCrystal, BlockPosition blockPosition, DamageSource damageSource) {
        ((EntityEnderDragon) handle).a(entityEnderCrystal, blockPosition.getHandle(), damageSource);
    }

    public Entity getEntityHeadComplexPart() {
        return new Entity(((EntityEnderDragon) handle).e);
    }

}
