package com.bgsoftware.superiorskyblock.values.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.key.Key;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.math.BigDecimal;

public final class BlockLevelsContainer extends BlockValuesContainer {

    @Override
    public void loadDefaultValues(SuperiorSkyblockPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "block-values/levels.yml");

        if(!file.exists())
            plugin.saveResource("block-values/levels.yml", true);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection valuesSection = cfg.getConfigurationSection("");

        for(String key : valuesSection.getKeys(false)) {
            String value = valuesSection.getString(key);
            try {
                setBlockValue(Key.of(key), new BigDecimal(value));
            }catch (Exception ex){
                SuperiorSkyblockPlugin.log("&cInvalid level value: " + value);
            }
        }
    }

}
