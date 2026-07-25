package com.bgsoftware.superiorskyblock.player.permissions;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.StampedLock;

public class PlayerPermissionsStore {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Map<UUID, PlayerPermissionsStore> PERMISSIONS_STORE_MAP = new ConcurrentHashMap<>();

    private final EnumerateMap<IslandPrivilege, Boolean> bypassPermissionsStore = new EnumerateMap<>(IslandPrivilege.values());
    private final Map<String, Boolean> customPermissionsStore = new ConcurrentHashMap<>();
    private final StampedLock lock = new StampedLock();

    private final AtomicBoolean dirty = new AtomicBoolean(true);

    @Nullable
    public static PlayerPermissionsStore getPermissionsStore(UUID uuid) {
        return PERMISSIONS_STORE_MAP.get(uuid);
    }

    public PlayerPermissionsStore(SuperiorPlayer superiorPlayer) {
        PERMISSIONS_STORE_MAP.put(superiorPlayer.getUniqueId(), this);
    }

    public void markDirty() {
        this.dirty.set(true);
    }

    public void refreshCache(Player player) {
        // We want to refresh cache for common permissions
        hasBypassPermission(player, IslandPrivileges.BUILD);
        hasBypassPermission(player, IslandPrivileges.BREAK);
        hasCustomPermission(player, "superior.admin.bypass.*");
    }

    public PermissionResult hasBypassPermission(@Nullable Player player, IslandPrivilege islandPrivilege) {
        if (this.dirty.get()) {
            checkDirty();
        }

        long stamp = this.lock.readLock();
        Boolean value;
        try {
            value = this.bypassPermissionsStore.get(islandPrivilege);
        } finally {
            this.lock.unlockRead(stamp);
        }

        if (value == null) {
            if (player == null) return PermissionResult.NOT_ONLINE;

            value = checkPermission(player, "superior.admin.bypass." + islandPrivilege.getName());

            stamp = this.lock.writeLock();
            try {
                this.bypassPermissionsStore.put(islandPrivilege, value);
            } finally {
                this.lock.unlockWrite(stamp);
            }
        }

        return value ? PermissionResult.PRIVILEGED : PermissionResult.RESTRICTED;
    }

    public PermissionResult hasCustomPermission(@Nullable Player player, String permission) {
        long stamp = this.lock.tryOptimisticRead();
        Boolean value = this.customPermissionsStore.get(permission);
        boolean isDirty = this.dirty.get();

        if (!this.lock.validate(stamp) || isDirty) {
            if (isDirty) {
                checkDirty();
            }

            stamp = this.lock.readLock();
            try {
                value = this.customPermissionsStore.get(permission);
            } finally {
                this.lock.unlockRead(stamp);
            }
        }

        if (value == null) {
            if (player == null) return PermissionResult.NOT_ONLINE;

            value = checkPermission(player, permission);

            stamp = this.lock.writeLock();
            try {
                this.customPermissionsStore.put(permission, value);
            } finally {
                this.lock.unlockWrite(stamp);
            }
        }

        return value ? PermissionResult.PRIVILEGED : PermissionResult.RESTRICTED;
    }

    private void checkDirty() {
        if (dirty.compareAndSet(true, false)) {
            long stamp = this.lock.writeLock();
            try {
                this.bypassPermissionsStore.clear();
                this.customPermissionsStore.clear();
            } finally {
                this.lock.unlockWrite(stamp);
            }
        }
    }

    private static boolean checkPermission(Player player, String permission) {
        return plugin.getProviders().getPermissionsProvider().hasPermission(player, permission);
    }

    public enum PermissionResult {

        PRIVILEGED,
        RESTRICTED,
        NOT_ONLINE

    }

}
