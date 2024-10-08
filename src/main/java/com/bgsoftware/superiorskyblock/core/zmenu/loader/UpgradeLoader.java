package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.UpgradeButton;
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

public class UpgradeLoader extends SuperiorButtonLoader {
    public UpgradeLoader(SuperiorSkyblockPlugin plugin) {
        super(plugin, "UPGRADE");
    }

    @Override
    public Class<? extends Button> getButton() {
        return UpgradeButton.class;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {

        File file = new File(this.plugin.getDataFolder(), "inventories/upgrades.yml");
        Loader<MenuItemStack> loader = new MenuItemStackLoader(this.plugin.getZMenumanager().getInventoryManager());
        String upgradeName = configuration.getString(path + "upgrade");
        int level = configuration.getInt(path + "level", 1);

        Upgrade upgrade = plugin.getUpgrades().getUpgrade(upgradeName);
        if (upgrade == null) {
            this.plugin.getLogger().severe("Upgrade " + upgradeName + " was not found !");
            return new ZNoneButton();
        }

        SUpgradeLevel upgradeLevel = (SUpgradeLevel) upgrade.getUpgradeLevel(level);
        if (upgradeLevel == null) {
            this.plugin.getLogger().severe("Upgrade " + upgradeName + " with level " + level + "was not found !");
            return new ZNoneButton();
        }

        MenuItemStack errorItemStack = null;
        try {
            errorItemStack = loader.load(configuration, path + "error-item.", file);
        } catch (InventoryException exception) {
            exception.printStackTrace();
        }

        return new UpgradeButton(plugin, upgradeLevel, upgrade, errorItemStack);
    }
}
