package com.bgsoftware.superiorskyblock.nms.v1_19.player;

import com.bgsoftware.superiorskyblock.core.ObjectsPool;
import com.bgsoftware.superiorskyblock.nms.player.OfflinePlayerData;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftPlayer;
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
        ServerLevel serverLevel = server.getLevel(Level.OVERWORLD);

        if (serverLevel == null)
            return null;

        ServerPlayer serverPlayer = new ServerPlayer(server, serverLevel, profile);
        this.fakePlayer = serverPlayer.getBukkitEntity();
        this.fakePlayer.loadData();

        return this;
    }

    @Override
    public Player getFakeOnlinePlayer() {
        return this.fakePlayer;
    }

    @Override
    public void setLocation(Location location) {
        ServerPlayer serverPlayer = ((CraftPlayer) this.fakePlayer).getHandle();
        serverPlayer.setLevel(((CraftWorld) location.getWorld()).getHandle());
        serverPlayer.absMoveTo(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
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
