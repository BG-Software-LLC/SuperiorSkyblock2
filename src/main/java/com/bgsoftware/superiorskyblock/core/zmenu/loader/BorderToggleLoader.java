package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.border.BorderColorToggleButton;
import fr.maxlego08.menu.MenuItemStack;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import fr.maxlego08.menu.loader.MenuItemStackLoader;
import fr.maxlego08.menu.zcore.utils.loader.Loader;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class BorderToggleLoader extends SuperiorButtonLoader {

    public BorderToggleLoader(SuperiorSkyblockPlugin plugin) {
        super(plugin, "BORDER_TOGGLE");
    }

    @Override
    public Class<? extends Button> getButton() {
        return BorderColorToggleButton.class;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {
        Loader<MenuItemStack> loader = new MenuItemStackLoader(plugin.getZMenumanager().getInventoryManager());
        File file = new File(plugin.getDataFolder(), "inventories/border-color.yml");

        MenuItemStack menuItemStackEnabled = null;
        MenuItemStack menuItemStackDisabled = null;

        try {
            menuItemStackEnabled = loader.load(configuration, path + "enable-border.", file);
            menuItemStackDisabled = loader.load(configuration, path + "disable-border.", file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return new BorderColorToggleButton(plugin, menuItemStackEnabled, menuItemStackDisabled);
    }
}
