package com.bgsoftware.superiorskyblock.external.permissions;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.hooks.PermissionsProvider;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.Locale;
import java.util.Map;

public class PermissionsProvider_Default implements PermissionsProvider {

    private static final ReflectField<PermissibleBase> HUMAN_ENTITY_PERMS = new ReflectField<>(
            new ClassInfo("entity.CraftHumanEntity", ClassInfo.PackageType.CRAFTBUKKIT),
            PermissibleBase.class, "perm");
    private static final ReflectField<Map<String, PermissionAttachmentInfo>> PERMISSIBLE_BASE_PERMISSIONS =
            new ReflectField<>(PermissibleBase.class, Map.class, "permissions");

    @Override
    public boolean hasPermission(Player player, String permission) {
        PermissibleBase permissibleBase = HUMAN_ENTITY_PERMS.get(player);
        PermissionAttachmentInfo permissionAttachmentInfo = PERMISSIBLE_BASE_PERMISSIONS.get(permissibleBase)
                .get(permission.toLowerCase(Locale.ENGLISH));
        return permissionAttachmentInfo != null && permissionAttachmentInfo.getValue();
    }

}
