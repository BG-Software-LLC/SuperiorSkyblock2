package com.bgsoftware.superiorskyblock.nms.v1_8_R3;

import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import com.bgsoftware.superiorskyblock.nms.NMSHolograms;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.DamageSource;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.Vec3D;
import net.minecraft.server.v1_8_R3.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

@SuppressWarnings("unused")
public class NMSHologramsImpl implements NMSHolograms {

    @Override
    public Hologram createHologram(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        EntityHologram entityHologram = new EntityHologram(world, location.getX(), location.getY(), location.getZ());
        world.addEntity(entityHologram);
        return entityHologram;
    }

    @Override
    public boolean isHologram(Entity entity) {
        return ((CraftEntity) entity).getHandle() instanceof Hologram;
    }

    private static class EntityHologram extends EntityArmorStand implements Hologram {

        EntityHologram(World world, double x, double y, double z) {
            super(world, x, y, z);
            setInvisible(true);
            setSmall(true);
            setArms(false);
            setGravity(false);
            setBasePlate(true);
            n(true);
            super.setCustomNameVisible(true);
            super.a(new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D));
            this.onGround = true; // Workaround to force EntityTrackerEntry to send a teleport packet.
        }

        @Override
        public void setHologramName(String name) {
            super.setCustomName(name);
        }

        @Override
        public void removeHologram() {
            super.die();
        }

        @Override
        public ArmorStand getHandle() {
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
        public void setEquipment(int i, ItemStack item) {
            // Prevent stand being equipped
        }

        @Override
        public boolean d(int i, ItemStack item) {
            // Prevent stand being equipped
            return false;
        }

        @Override
        public void b(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
        }

        @Override
        public boolean a(EntityHuman human, Vec3D vec3d) {
            // Prevent stand being equipped
            return true;
        }

        @Override
        public void t_() {
            // Disable normal ticking for this entity.

            // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
            if (this.onGround) {
                this.onGround = false;
            }
        }

        @Override
        public void die() {
            // Prevent being killed.
        }

        @Override
        public void makeSound(String sound, float f1, float f2) {
            // Remove sounds.
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
        public void e(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
        }

        @Override
        public CraftArmorStand getBukkitEntity() {
            if (super.bukkitEntity == null) {
                this.bukkitEntity = new CraftArmorStand(this.world.getServer(), this);
            }
            return (CraftArmorStand) this.bukkitEntity;
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
        public void setCustomName(String customName) {
            // Locks the custom name.
        }

        @Override
        public void setCustomNameVisible(boolean visible) {
            // Locks the custom name.
        }

        @Override
        public void a(AxisAlignedBB boundingBox) {
            // Do not change it!
        }

    }

}
