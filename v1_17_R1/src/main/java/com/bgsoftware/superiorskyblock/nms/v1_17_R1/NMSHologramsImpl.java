package com.bgsoftware.superiorskyblock.nms.v1_17_R1;

import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import com.bgsoftware.superiorskyblock.nms.NMSHolograms;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.World;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.entity.ArmorStand;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class NMSHologramsImpl implements NMSHolograms {

    @Override
    public Hologram createHologram(Location location) {
        assert location.getWorld() != null;
        World world = ((CraftWorld) location.getWorld()).getHandle();
        EntityHologram entityHologram = new EntityHologram(world, location.getX(), location.getY(), location.getZ());
        world.addEntity(entityHologram);
        return entityHologram;
    }

    @Override
    public boolean isHologram(org.bukkit.entity.Entity entity) {
        return ((CraftEntity) entity).getHandle() instanceof Hologram;
    }

    @SuppressWarnings("NullableProblems")
    private static class EntityHologram extends EntityArmorStand implements Hologram {

        private static final AxisAlignedBB EMPTY_BOUND = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);

        private CraftEntity bukkitEntity;

        EntityHologram(World world, double x, double y, double z) {
            super(world, x, y, z);
            setInvisible(true);
            setSmall(true);
            setArms(false);
            setNoGravity(true);
            setBasePlate(true);
            setMarker(true);
            super.collides = false;
            super.setCustomNameVisible(true);
            super.a(EMPTY_BOUND);
        }

        @Override
        public void setHologramName(String name) {
            super.setCustomName(CraftChatMessage.fromString(name)[0]);
        }

        @Override
        public void removeHologram() {
            super.a(Entity.RemovalReason.b);
        }

        @Override
        public ArmorStand getHandle() {
            return this.getBukkitEntity();
        }

        @Override
        public void inactiveTick() {
            // Disable normal ticking for this entity.

            // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
            if (this.z) {
                this.z = false;
            }
        }

        @Override
        public boolean isCollidable() {
            return false;
        }

        @Override
        public AxisAlignedBB cs() {
            return EMPTY_BOUND;
        }

        @Override
        public void setSlot(EnumItemSlot enumitemslot, ItemStack itemstack) {
            // Prevent stand being equipped
        }

        @Override
        public void saveData(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
        }

        @Override
        public void loadData(NBTTagCompound nbttagcompound) {
            // Do not load NBT.
        }

        @Override
        public EnumInteractionResult a(EntityHuman human, Vec3D vec3d, EnumHand enumhand) {
            // Prevent stand being equipped
            return EnumInteractionResult.d;
        }

        @Override
        public void tick() {
            // Disable normal ticking for this entity.

            // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
            if (this.z) {
                this.z = false;
            }
        }

        public void forceSetBoundingBox(AxisAlignedBB boundingBox) {
            super.a(boundingBox);
        }

        @Override
        public CraftArmorStand getBukkitEntity() {
            if (bukkitEntity == null) {
                bukkitEntity = new CraftArmorStand((CraftServer) Bukkit.getServer(), this);
            }
            return (CraftArmorStand) bukkitEntity;
        }

        @Override
        public void a(Entity.RemovalReason entity_removalreason) {
            // Prevent being killed.
        }

        @Override
        public void playSound(SoundEffect soundeffect, float f, float f1) {
            // Remove sounds.
        }

        @Override
        public boolean d(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
            return false;
        }

        @Override
        public NBTTagCompound save(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
            return nbttagcompound;
        }

        @Override
        public void load(NBTTagCompound nbttagcompound) {
            // Do not load NBT.
        }

        @Override
        public boolean isInvulnerable(DamageSource source) {
            /*
             * The field Entity.invulnerable is private.
             * It's only used while saving NBTTags, but since the entity would be killed
             * on chunk unload, we prefer to override isInvulnerable().
             */
            return true;
        }

        @Override
        public void setCustomName(@Nullable IChatBaseComponent ichatbasecomponent) {
            // Locks the custom name.
        }

        @Override
        public void setCustomNameVisible(boolean flag) {
            // Locks the custom name.
        }

    }

}
