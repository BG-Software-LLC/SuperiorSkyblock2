package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.top.IslandTopButton;
import fr.maxlego08.menu.MenuItemStack;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import fr.maxlego08.menu.loader.MenuItemStackLoader;
import fr.maxlego08.menu.zcore.utils.loader.Loader;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class IslandTopLoader extends SuperiorButtonLoader {

    public IslandTopLoader(SuperiorSkyblockPlugin plugin) {
        super(plugin, "TOP");
    }

    @Override
    public Class<? extends Button> getButton() {
        return IslandTopButton.class;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {

        Loader<MenuItemStack> loader = new MenuItemStackLoader(this.plugin.getZMenumanager().getInventoryManager());
        File file = new File(plugin.getDataFolder(), "inventories/settings.yml");

        List<Integer> positions = configuration.getIntegerList(path + "positions");
        MenuItemStack menuItemStackIsland = null;
        MenuItemStack menuItemStackNoIsland = null;

        try {
            menuItemStackIsland = loader.load(configuration, path + "island.", file);
            menuItemStackNoIsland = loader.load(configuration, path + "no-island.", file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return new IslandTopButton(plugin, menuItemStackIsland, menuItemStackNoIsland, positions);
    }
}
