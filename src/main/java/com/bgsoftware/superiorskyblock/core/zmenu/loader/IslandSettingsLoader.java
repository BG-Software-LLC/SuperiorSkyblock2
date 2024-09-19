package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.IslandCreationButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.IslandSettingsButton;
import com.bgsoftware.superiorskyblock.core.zmenu.utils.Setting;
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

public class IslandSettingsLoader extends SuperiorButtonLoader {

    public IslandSettingsLoader(SuperiorSkyblockPlugin plugin) {
        super(plugin, "SETTINGS");
    }

    @Override
    public Class<? extends Button> getButton() {
        return IslandCreationButton.class;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {

        Loader<MenuItemStack> loader = new MenuItemStackLoader(plugin.getZMenumanager().getInventoryManager());

        List<Setting> settings = new ArrayList<>();
        Optional.ofNullable(configuration.getConfigurationSection(path + "settings")).ifPresent(settingsSection -> {
            for (String islandFlagName : settingsSection.getKeys(false)) {
                Optional.ofNullable(settingsSection.getConfigurationSection(islandFlagName)).ifPresent(islandFlagSection -> {
                    settings.add(loadIslandFlagInfo(islandFlagName, settings.size(), configuration, path + "settings." + islandFlagName + ".", loader));
                });
            }
        });

        return new IslandSettingsButton(plugin, settings);
    }

    private Setting loadIslandFlagInfo(String islandFlagName, int position, YamlConfiguration configuration, String path, Loader<MenuItemStack> loader) {

        File file = new File(plugin.getDataFolder(), "inventories/settings.yml");

        MenuItemStack itemStackEnabled = null;
        try {
            itemStackEnabled = loader.load(configuration, path + "settings-enabled.", file);
        } catch (InventoryException exception) {
            exception.printStackTrace();
        }

        MenuItemStack itemStackDisabled = null;
        try {
            itemStackDisabled = loader.load(configuration, path + "settings-disabled.", file);
        } catch (InventoryException exception) {
            exception.printStackTrace();
        }

        return new Setting(islandFlagName, itemStackEnabled, itemStackDisabled, position);
    }
}
