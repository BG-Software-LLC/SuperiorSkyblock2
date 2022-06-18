package com.bgsoftware.superiorskyblock.core.values.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.math.BigDecimal;

public class BlockLevelsContainer extends BlockValuesContainer {

    @Override
    public void loadDefaultValues(SuperiorSkyblockPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "block-values/levels.yml");

        if (!file.exists())
            plugin.saveResource("block-values/levels.yml", true);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection valuesSection = cfg.getConfigurationSection("");

        for (String key : valuesSection.getKeys(false)) {
            String value = valuesSection.getString(key);
            try {
                setBlockValue(KeyImpl.of(key), new BigDecimal(value));
            } catch (Exception ex) {
                SuperiorSkyblockPlugin.log("&cInvalid level value: " + value);
                PluginDebugger.debug(ex);
            }
        }
    }

}
