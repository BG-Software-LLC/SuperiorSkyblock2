package com.bgsoftware.superiorskyblock.external.permissions;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.PermissionsProvider;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.player.permissions.PlayerPermissionsStore;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.node.types.RegexPermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.UUID;

@SuppressWarnings("unused")
public class PermissionsProvider_LuckPerms implements PermissionsProvider {

    private final LuckPerms luckPerms;

    public static boolean isCompatible() {
        try {
            CachedDataManager.class.getMethod("getPermissionData");
            return true;
        } catch (Throwable ex) {
            Log.warn("You are using an outdated version of LuckPerms. It's recommended to update for a more optimized experience (v5.1+).");
            return false;
        }
    }

    public PermissionsProvider_LuckPerms(SuperiorSkyblockPlugin plugin) {
        this.luckPerms = Bukkit.getServicesManager().getRegistration(LuckPerms.class).getProvider();
        this.luckPerms.getEventBus().subscribe(plugin, NodeAddEvent.class, this::onPermissionsAdd);
        this.luckPerms.getEventBus().subscribe(plugin, NodeRemoveEvent.class, this::onPermissionsRemove);
        Log.info("Using LuckPerms as a permissions provider.");
    }

    @Override
    public boolean hasPermission(Player player, String permission) {
        User user = this.luckPerms.getUserManager().getUser(player.getUniqueId());
        return user != null && user.getCachedData().getPermissionData().getPermissionMap()
                .getOrDefault(permission.toLowerCase(Locale.ENGLISH), false);
    }

    private void onPermissionsAdd(NodeAddEvent e) {
        if (e.getTarget() instanceof User && isTrackableNode(e.getNode()))
            notifyPermissionsUpdate(((User) e.getTarget()).getUniqueId());
    }

    private void onPermissionsRemove(NodeRemoveEvent e) {
        if (e.getTarget() instanceof User && isTrackableNode(e.getNode()))
            notifyPermissionsUpdate(((User) e.getTarget()).getUniqueId());
    }

    private void notifyPermissionsUpdate(UUID playerUUID) {
        PlayerPermissionsStore permissionsStore = PlayerPermissionsStore.getPermissionsStore(playerUUID);
        if (permissionsStore != null)
            permissionsStore.markDirty();
    }

    private static boolean isTrackableNode(Node node) {
        if (node instanceof PermissionNode) {
            return ((PermissionNode) node).getPermission().contains("superior");
        }

        // In the case of RegexPermissionNode - we are not going to try and parse the regex.
        // In the case of InheritanceNode - we are not going to go through the inherited group and check for permissions.
        // We will always mark cache as dirty for both of these events.
        return node instanceof RegexPermissionNode || node instanceof InheritanceNode;
    }

}
