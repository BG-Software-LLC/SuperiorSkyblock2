package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.IslandPermissionButton;
import com.bgsoftware.superiorskyblock.core.zmenu.utils.Permission;
import fr.maxlego08.menu.MenuItemStack;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import fr.maxlego08.menu.exceptions.InventoryException;
import fr.maxlego08.menu.loader.MenuItemStackLoader;
import fr.maxlego08.menu.zcore.utils.loader.Loader;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IslandPermissionLoader extends SuperiorButtonLoader {

    public IslandPermissionLoader(SuperiorSkyblockPlugin plugin) {
        super(plugin, "PERMISSIONS");
    }

    @Override
    public Class<? extends Button> getButton() {
        return IslandPermissionButton.class;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {

        Loader<MenuItemStack> loader = new MenuItemStackLoader(plugin.getZMenumanager().getInventoryManager());

        String noRolePermission = configuration.getString(path + "no-role-permission", "");
        String exactRolePermission = configuration.getString(path + "exact-role-permission", "");
        String higherRolePermission = configuration.getString(path + "higher-role-permission", "");

        List<Permission> permissions = new ArrayList<>();
        Optional.ofNullable(configuration.getConfigurationSection(path + "permissions")).ifPresent(permissionSection -> {
            for (String permission : permissionSection.getKeys(false)) {
                Optional.ofNullable(permissionSection.getConfigurationSection(permission)).ifPresent(section -> {
                    if (section.getBoolean("display-menu")) {
                        permissions.add(loadPermission(permission, configuration, path + "permissions." + permission + ".", loader));
                    }
                });
            }
        });

        return new IslandPermissionButton(plugin, noRolePermission, exactRolePermission, higherRolePermission, permissions);
    }

    private Permission loadPermission(String permission, YamlConfiguration configuration, String path, Loader<MenuItemStack> loader) {

        File file = new File(plugin.getDataFolder(), "inventories/permissions.yml");
        MenuItemStack itemStackEnabled = null;
        MenuItemStack itemStackDisabled = null;
        MenuItemStack itemStackPermission = null;

        try {
            itemStackEnabled = loader.load(configuration, path + "permission-enabled.", file);
            itemStackDisabled = loader.load(configuration, path + "permission-disabled.", file);
            itemStackPermission = loader.load(configuration, path + "role-permission.", file);
        } catch (InventoryException exception) {
            exception.printStackTrace();
        }

        IslandPrivilege islandPrivilege = IslandPrivilege.getByName(permission);

        return new Permission(islandPrivilege, itemStackEnabled, itemStackDisabled, itemStackPermission);
    }
}
