package com.bgsoftware.superiorskyblock.nms.v1_12_R1.player;

import com.bgsoftware.superiorskyblock.core.ObjectsPool;
import com.bgsoftware.superiorskyblock.nms.player.OfflinePlayerData;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

public class OfflinePlayerDataImpl implements OfflinePlayerData {

    private static final ObjectsPool<OfflinePlayerDataImpl> POOL = new ObjectsPool<>(OfflinePlayerDataImpl::new);

    private Player fakePlayer;

    public static OfflinePlayerDataImpl create(OfflinePlayer offlinePlayer) {
        return POOL.obtain().initialize(offlinePlayer);
    }

    private OfflinePlayerDataImpl() {

    }

    private OfflinePlayerDataImpl initialize(OfflinePlayer offlinePlayer) {
        GameProfile profile = new GameProfile(offlinePlayer.getUniqueId(),
                Optional.ofNullable(offlinePlayer.getName()).orElse(""));

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.getWorldServer(0);
        EntityPlayer entityPlayer = new EntityPlayer(server, worldServer, profile, new PlayerInteractManager(worldServer));
        this.fakePlayer = entityPlayer.getBukkitEntity();
        this.fakePlayer.loadData();

        return this;
    }

    @Override
    public Player getFakeOnlinePlayer() {
        return this.fakePlayer;
    }

    @Override
    public void setLocation(Location location) {
        EntityPlayer entityPlayer = ((CraftPlayer) this.fakePlayer).getHandle();
        entityPlayer.world = ((CraftWorld) location.getWorld()).getHandle();
        entityPlayer.setPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    @Override
    public void applyChanges() {
        this.fakePlayer.saveData();
    }

    @Override
    public void release() {
        this.fakePlayer = null;
        POOL.release(this);
    }

}
