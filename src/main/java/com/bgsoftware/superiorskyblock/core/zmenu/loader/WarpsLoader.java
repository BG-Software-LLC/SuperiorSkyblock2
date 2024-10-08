package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.UpgradeButton;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.warps.WarpsButton;
import com.bgsoftware.superiorskyblock.island.upgrade.SUpgradeLevel;
import fr.maxlego08.menu.MenuItemStack;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import fr.maxlego08.menu.button.buttons.ZNoneButton;
import fr.maxlego08.menu.exceptions.InventoryException;
import fr.maxlego08.menu.loader.MenuItemStackLoader;
import fr.maxlego08.menu.zcore.utils.loader.Loader;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class WarpsLoader extends SuperiorButtonLoader {
    public WarpsLoader(SuperiorSkyblockPlugin plugin) {
        super(plugin, "WARPS");
    }

    @Override
    public Class<? extends Button> getButton() {
        return UpgradeButton.class;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {

        File file = new File(this.plugin.getDataFolder(), "inventories/warps.yml");
        Loader<MenuItemStack> loader = new MenuItemStackLoader(this.plugin.getZMenumanager().getInventoryManager());

        MenuItemStack editItemStack = null;
        try {
            editItemStack = loader.load(configuration, path + "edit-item.", file);
        } catch (InventoryException exception) {
            exception.printStackTrace();
        }

        return new WarpsButton(plugin, editItemStack);
    }
}
