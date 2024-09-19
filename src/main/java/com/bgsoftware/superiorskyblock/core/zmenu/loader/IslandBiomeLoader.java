package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.IslandBiomeButton;
import fr.maxlego08.menu.api.button.Button;
import fr.maxlego08.menu.api.button.DefaultButtonValue;
import org.bukkit.block.Biome;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Locale;

public class IslandBiomeLoader extends SuperiorButtonLoader {
    public IslandBiomeLoader(SuperiorSkyblockPlugin plugin) {
        super(plugin, "BIOMES");
    }

    @Override
    public Class<? extends Button> getButton() {
        return IslandBiomeButton.class;
    }

    @Override
    public Button load(YamlConfiguration configuration, String path, DefaultButtonValue defaultButtonValue) {
        boolean shouldCurrentBiomeGlow = configuration.getBoolean("current-biome-glow", false);
        Biome biome = Biome.PLAINS;

        String biomeName = configuration.getString(path + "biome", Biome.PLAINS.name()).toUpperCase(Locale.ENGLISH);
        try {
            biome = Biome.valueOf(biomeName);
        } catch (IllegalArgumentException error) {
            Log.warnFromFile("biomes.yml", "Biome '", biomeName, "' is not valid, skipping...");
        }
        return new IslandBiomeButton(plugin, biome, shouldCurrentBiomeGlow);
    }
}
