package com.bgsoftware.superiorskyblock.nms.v1_20_2;

import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import com.bgsoftware.superiorskyblock.nms.NMSHolograms;
import com.google.common.base.Preconditions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.util.CraftChatMessage;
import org.bukkit.entity.Entity;

@SuppressWarnings("unused")
public class NMSHologramsImpl implements NMSHolograms {

    @Override
    public Hologram createHologram(Location location) {
        Preconditions.checkNotNull(location.getWorld(), "Cannot create hologram in null worlds.");
        ServerLevel serverLevel = ((CraftWorld) location.getWorld()).getHandle();
        EntityHologram entityHologram = new EntityHologram(serverLevel, location.getX(), location.getY(), location.getZ());
        serverLevel.addFreshEntity(entityHologram);
        return entityHologram;
    }

    @Override
    public boolean isHologram(Entity entity) {
        return ((CraftEntity) entity).getHandle() instanceof Hologram;
    }

    public static class EntityHologram extends ArmorStand implements Hologram {

        private static final AABB EMPTY_BOUND = new AABB(0D, 0D, 0D, 0D, 0D, 0D);

        private CraftEntity bukkitEntity;

        EntityHologram(ServerLevel serverLevel, double x, double y, double z) {
            super(serverLevel, x, y, z);

            setInvisible(true);
            setSmall(true);
            setShowArms(false);
            setNoGravity(true);
            setNoBasePlate(true);
            setMarker(true);
            forceSetBoundingBox(EMPTY_BOUND);

            super.collides = false;
            super.setCustomNameVisible(true);
        }

        @Override
        public void setHologramName(String name) {
            super.setCustomName(CraftChatMessage.fromStringOrNull(name));
        }

        @Override
        public void removeHologram() {
            super.remove(RemovalReason.DISCARDED);
        }

        @Override
        public org.bukkit.entity.ArmorStand getHandle() {
            return this.getBukkitEntity();
        }

        @Override
        public void inactiveTick() {
            // Disable normal ticking for this entity.

            // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
            if (this.onGround) {
                this.onGround = false;
            }
        }

        @Override
        public boolean repositionEntityAfterLoad() {
            return false;
        }

        @Override
        public AABB getBoundingBoxForCulling() {
            return EMPTY_BOUND;
        }

        @Override
        public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack, boolean silence) {
            // Prevent stand being equipped
        }

        @Override
        public void addAdditionalSaveData(CompoundTag compoundTag) {
            // Do not save NBT.
        }

        @Override
        public void readAdditionalSaveData(CompoundTag compoundTag) {
            // Do not load NBT.
        }

        @Override
        public InteractionResult interactAt(Player player, Vec3 hitPos, InteractionHand hand) {
            // Prevent stand being equipped
            return InteractionResult.PASS;
        }

        @Override
        public void tick() {
            // Disable normal ticking for this entity.

            // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
            if (this.onGround) {
                this.onGround = false;
            }
        }

        public void forceSetBoundingBox(AABB boundingBox) {
            super.setBoundingBox(boundingBox);
        }

        @Override
        public CraftArmorStand getBukkitEntity() {
            if (bukkitEntity == null) {
                bukkitEntity = new CraftArmorStand((CraftServer) Bukkit.getServer(), this);
            }
            return (CraftArmorStand) bukkitEntity;
        }

        @Override
        public void remove(RemovalReason removalReason) {
            // Prevent being killed.
        }

        @Override
        public void playSound(SoundEvent soundEvent, float volume, float pitch) {
            // Remove sounds.
        }

        @Override
        public boolean saveAsPassenger(CompoundTag compoundTag) {
            // Do not save NBT.
            return false;
        }

        @Override
        public CompoundTag saveWithoutId(CompoundTag compoundTag) {
            // Do not save NBT.
            return compoundTag;
        }

        @Override
        public void load(CompoundTag compoundTag) {
            // Do not load NBT.
        }

        @Override
        public boolean isInvulnerableTo(DamageSource damageSource) {
            /*
             * The field Entity.invulnerable is private.
             * It's only used while saving NBTTags, but since the entity would be killed
             * on chunk unload, we prefer to override isInvulnerable().
             */
            return true;
        }

        @Override
        public void setCustomName(Component component) {
            // Locks the custom name.
        }

        @Override
        public void setCustomNameVisible(boolean isCustomNameVisible) {
            // Locks the custom name.
        }

    }

}
