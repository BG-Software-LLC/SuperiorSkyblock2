package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.utils.holograms.Hologram;
import net.minecraft.server.v1_15_R1.AxisAlignedBB;
import net.minecraft.server.v1_15_R1.DamageSource;
import net.minecraft.server.v1_15_R1.EntityArmorStand;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EnumHand;
import net.minecraft.server.v1_15_R1.EnumInteractionResult;
import net.minecraft.server.v1_15_R1.EnumItemSlot;
import net.minecraft.server.v1_15_R1.IChatBaseComponent;
import net.minecraft.server.v1_15_R1.ItemStack;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.SoundEffect;
import net.minecraft.server.v1_15_R1.Vec3D;
import net.minecraft.server.v1_15_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_15_R1.util.CraftChatMessage;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public final class NMSHolograms_v1_15_R1 implements NMSHolograms {

    @Override
    public Hologram createHologram(Location location) {
        assert location.getWorld() != null;
        World world = ((CraftWorld) location.getWorld()).getHandle();
        EntityHologram entityHologram = new EntityHologram(world, location.getX(), location.getY(), location.getZ());
        world.addEntity(entityHologram);
        return entityHologram;
    }

    private static final class EntityHologram extends EntityArmorStand implements Hologram {

        private CraftEntity bukkitEntity;

        EntityHologram(World world, double x, double y, double z){
            super(world, x, y, z);
            setInvisible(true);
            setSmall(true);
            setArms(false);
            setNoGravity(true);
            setBasePlate(true);
            setMarker(true);
            super.collides = false;
            super.setCustomNameVisible(true);
            super.a(new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D));
        }

        @Override
        public void setHologramName(String name) {
            super.setCustomName(CraftChatMessage.fromString(name)[0]);
        }

        @Override
        public void removeHologram() {
            super.die();
        }

        @Override
        public void tick() {
            // Disable normal ticking for this entity.

            // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
            if (this.onGround) {
                this.onGround = false;
            }
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
        public void b(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
        }

        @Override
        public boolean c(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
            return false;
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
        public void f(NBTTagCompound nbttagcompound) {
            // Do not load NBT.
        }

        @Override
        public void a(NBTTagCompound nbttagcompound) {
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
        public boolean isCollidable() {
            return false;
        }

        @Override
        public void setCustomName(@Nullable IChatBaseComponent ichatbasecomponent) {
            // Locks the custom name.
        }

        @Override
        public void setCustomNameVisible(boolean flag) {
            // Locks the custom name.
        }

        @Override
        public EnumInteractionResult a(EntityHuman human, Vec3D vec3d, EnumHand enumhand) {
            // Prevent stand being equipped
            return EnumInteractionResult.PASS;
        }

        @Override
        public boolean a_(int i, ItemStack item) {
            // Prevent stand being equipped
            return false;
        }

        @Override
        public void setSlot(EnumItemSlot enumitemslot, ItemStack itemstack) {
            // Prevent stand being equipped
        }

        @Override
        public void a(AxisAlignedBB boundingBox) {
            // Do not change it!
        }

        public void forceSetBoundingBox(AxisAlignedBB boundingBox) {
            super.a(boundingBox);
        }

        @Override
        public void a(SoundEffect soundeffect, float f, float f1) {
            // Remove sounds.
        }

        @Override
        public void die() {
            // Prevent being killed.
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (bukkitEntity == null) {
                bukkitEntity = new CraftArmorStand(super.world.getServer(), this);
            }
            return bukkitEntity;
        }

    }

}
