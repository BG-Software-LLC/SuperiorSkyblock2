package com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2;

import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import com.bgsoftware.superiorskyblock.nms.NMSHolograms;
import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.level.WorldServer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.sounds.SoundEffect;
import net.minecraft.world.EnumHand;
import net.minecraft.world.EnumInteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftServer;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftChatMessage;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

@SuppressWarnings("unused")
public class NMSHologramsImpl implements NMSHolograms {

    @Override
    public Hologram createHologram(Location location) {
        assert location.getWorld() != null;
        WorldServer world = new WorldServer(((CraftWorld) location.getWorld()).getHandle());
        EntityHologram entityHologram = new EntityHologram(world, location.getX(), location.getY(), location.getZ());
        world.addEntity(entityHologram);
        return entityHologram;
    }

    @Override
    public boolean isHologram(Entity entity) {
        return ((CraftEntity) entity).getHandle() instanceof Hologram;
    }

    private static class EntityHologram extends EntityArmorStand implements Hologram {

        private static final AxisAlignedBB EMPTY_BOUND = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);

        private CraftEntity bukkitEntity;

        EntityHologram(WorldServer world, double x, double y, double z) {
            super(world.getHandle(), x, y, z);
            j(true); // Invisible
            a(true); // Small
            r(false); // Arms
            e(true); // No Gravity
            s(true); // Base Plate
            t(true); // Marker
            super.collides = false;
            super.n(true); // Custom name visible
            super.a(EMPTY_BOUND);
        }

        @Override
        public void setHologramName(String name) {
            super.a(CraftChatMessage.fromString(name)[0]);
        }

        @Override
        public void removeHologram() {
            super.a(RemovalReason.b);
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
        public boolean bi() {
            return false;
        }

        @Override
        public AxisAlignedBB cx() {
            return EMPTY_BOUND;
        }

        @Override
        public void setItemSlot(EnumItemSlot enumitemslot, ItemStack itemstack, boolean silence) {
            // Prevent stand being equipped
        }

        @Override
        public void b(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
        }

        @Override
        public void a(NBTTagCompound nbttagcompound) {
            // Do not load NBT.
        }

        @Override
        public EnumInteractionResult a(EntityHuman human, Vec3D vec3d, EnumHand enumhand) {
            // Prevent stand being equipped
            return EnumInteractionResult.d;
        }

        @Override
        public void k() {
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
        public void a(RemovalReason entity_removalreason) {
            // Prevent being killed.
        }

        @Override
        public void a(SoundEffect soundeffect, float f, float f1) {
            // Remove sounds.
        }

        @Override
        public boolean d(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
            return false;
        }

        @Override
        public NBTTagCompound f(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
            return nbttagcompound;
        }

        @Override
        public void g(NBTTagCompound nbttagcompound) {
            // Do not load NBT.
        }

        @Override
        public boolean b(DamageSource source) {
            /*
             * The field Entity.invulnerable is private.
             * It's only used while saving NBTTags, but since the entity would be killed
             * on chunk unload, we prefer to override isInvulnerable().
             */
            return true;
        }

        @Override
        public void a(IChatBaseComponent ichatbasecomponent) {
            // Locks the custom name.
        }

        @Override
        public void n(boolean flag) {
            // Locks the custom name.
        }

    }

}
