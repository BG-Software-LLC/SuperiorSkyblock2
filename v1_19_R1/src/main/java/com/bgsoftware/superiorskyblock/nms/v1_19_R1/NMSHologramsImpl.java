package com.bgsoftware.superiorskyblock.nms.v1_19_R1;

import com.bgsoftware.superiorskyblock.api.service.hologram.Hologram;
import com.bgsoftware.superiorskyblock.nms.NMSHolograms;
import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.server.level.WorldServer;
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
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

@SuppressWarnings("unused")
public final class NMSHologramsImpl implements NMSHolograms {

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

    private static final class EntityHologram extends EntityArmorStand implements Hologram {

        private static final AxisAlignedBB EMPTY_BOUND = new AxisAlignedBB(0D, 0D, 0D, 0D, 0D, 0D);

        private CraftEntity bukkitEntity;

        @Remap(classPath = "net.minecraft.world.entity.decoration.ArmorStand", name = "setInvisible", type = Remap.Type.METHOD, remappedName = "j")
        @Remap(classPath = "net.minecraft.world.entity.decoration.ArmorStand", name = "setSmall", type = Remap.Type.METHOD, remappedName = "a")
        @Remap(classPath = "net.minecraft.world.entity.decoration.ArmorStand", name = "setShowArms", type = Remap.Type.METHOD, remappedName = "r")
        @Remap(classPath = "net.minecraft.world.entity.Entity", name = "setNoGravity", type = Remap.Type.METHOD, remappedName = "e")
        @Remap(classPath = "net.minecraft.world.entity.decoration.ArmorStand", name = "setNoBasePlate", type = Remap.Type.METHOD, remappedName = "s")
        @Remap(classPath = "net.minecraft.world.entity.decoration.ArmorStand", name = "setMarker", type = Remap.Type.METHOD, remappedName = "t")
        @Remap(classPath = "net.minecraft.world.entity.Entity", name = "setCustomNameVisible", type = Remap.Type.METHOD, remappedName = "n")
        @Remap(classPath = "net.minecraft.world.entity.Entity", name = "setBoundingBox", type = Remap.Type.METHOD, remappedName = "a")
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

        @Remap(classPath = "net.minecraft.world.entity.Entity",
                name = "setCustomName",
                type = Remap.Type.METHOD,
                remappedName = "b")
        @Override
        public void setHologramName(String name) {
            super.b(CraftChatMessage.fromStringOrNull(name));
        }

        @Override
        public void removeHologram() {
            super.a(RemovalReason.b);
        }

        @Override
        public ArmorStand getHandle() {
            return this.getBukkitEntity();
        }

        @Remap(classPath = "net.minecraft.world.entity.Entity",
                name = "onGround",
                type = Remap.Type.FIELD,
                remappedName = "y")
        @Override
        public void inactiveTick() {
            // Disable normal ticking for this entity.

            // Workaround to force EntityTrackerEntry to send a teleport packet immediately after spawning this entity.
            if (this.y) {
                this.y = false;
            }
        }

        @Remap(classPath = "net.minecraft.world.entity.Entity",
                name = "repositionEntityAfterLoad",
                type = Remap.Type.METHOD,
                remappedName = "bm")
        @Override
        public boolean bm() {
            return false;
        }

        @Remap(classPath = "net.minecraft.world.entity.Entity",
                name = "getBoundingBoxForCulling",
                type = Remap.Type.METHOD,
                remappedName = "cz")
        @Override
        public AxisAlignedBB cz() {
            return EMPTY_BOUND;
        }

        @Override
        public void setItemSlot(EnumItemSlot enumitemslot, ItemStack itemstack, boolean silence) {
            // Prevent stand being equipped
        }

        @Remap(classPath = "net.minecraft.world.entity.decoration.ArmorStand",
                name = "addAdditionalSaveData",
                type = Remap.Type.METHOD,
                remappedName = "b")
        @Override
        public void b(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
        }

        @Remap(classPath = "net.minecraft.world.entity.decoration.ArmorStand",
                name = "readAdditionalSaveData",
                type = Remap.Type.METHOD,
                remappedName = "a")
        @Override
        public void a(NBTTagCompound nbttagcompound) {
            // Do not load NBT.
        }

        @Remap(classPath = "net.minecraft.world.entity.decoration.ArmorStand",
                name = "interactAt",
                type = Remap.Type.METHOD,
                remappedName = "a")
        @Override
        public EnumInteractionResult a(EntityHuman human, Vec3D vec3d, EnumHand enumhand) {
            // Prevent stand being equipped
            return EnumInteractionResult.d;
        }

        @Remap(classPath = "net.minecraft.world.entity.decoration.ArmorStand",
                name = "tick",
                type = Remap.Type.METHOD,
                remappedName = "k")
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

        @Remap(classPath = "net.minecraft.world.entity.Entity",
                name = "remove",
                type = Remap.Type.METHOD,
                remappedName = "a")
        @Override
        public void a(RemovalReason entity_removalreason) {
            // Prevent being killed.
        }

        @Remap(classPath = "net.minecraft.world.entity.Entity",
                name = "playSound",
                type = Remap.Type.METHOD,
                remappedName = "a")
        @Override
        public void a(SoundEffect soundeffect, float f, float f1) {
            // Remove sounds.
        }

        @Remap(classPath = "net.minecraft.world.entity.Entity",
                name = "saveAsPassenger",
                type = Remap.Type.METHOD,
                remappedName = "d")
        @Override
        public boolean d(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
            return false;
        }

        @Remap(classPath = "net.minecraft.world.entity.Entity",
                name = "saveWithoutId",
                type = Remap.Type.METHOD,
                remappedName = "f")
        @Override
        public NBTTagCompound f(NBTTagCompound nbttagcompound) {
            // Do not save NBT.
            return nbttagcompound;
        }

        @Remap(classPath = "net.minecraft.world.entity.Entity",
                name = "load",
                type = Remap.Type.METHOD,
                remappedName = "g")
        @Override
        public void g(NBTTagCompound nbttagcompound) {
            // Do not load NBT.
        }

        @Remap(classPath = "net.minecraft.world.entity.Entity",
                name = "isInvulnerableTo",
                type = Remap.Type.METHOD,
                remappedName = "b")
        @Override
        public boolean b(DamageSource source) {
            /*
             * The field Entity.invulnerable is private.
             * It's only used while saving NBTTags, but since the entity would be killed
             * on chunk unload, we prefer to override isInvulnerable().
             */
            return true;
        }

        @Remap(classPath = "net.minecraft.world.entity.Entity",
                name = "setCustomName",
                type = Remap.Type.METHOD,
                remappedName = "b")
        @Override
        public void b(IChatBaseComponent ichatbasecomponent) {
            // Locks the custom name.
        }

        @Remap(classPath = "net.minecraft.world.entity.Entity",
                name = "setCustomNameVisible",
                type = Remap.Type.METHOD,
                remappedName = "n")
        @Override
        public void n(boolean flag) {
            // Locks the custom name.
        }

    }

}
