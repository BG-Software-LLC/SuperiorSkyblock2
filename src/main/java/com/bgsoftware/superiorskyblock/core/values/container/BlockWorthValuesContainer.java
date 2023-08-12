package com.bgsoftware.superiorskyblock.core.values.container;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.math.BigDecimal;

public class BlockWorthValuesContainer extends BlockValuesContainer {

    @Override
    public void loadDefaultValues(SuperiorSkyblockPlugin plugin) {
        File worthFile = new File(plugin.getDataFolder(), "block-values/worth.yml");

        File blockValuesFile = new File(plugin.getDataFolder(), "blockvalues.yml");
        if (blockValuesFile.exists()) {
            if (!worthFile.getParentFile().mkdirs() || !blockValuesFile.renameTo(worthFile))
                Log.error("Failed to convert old block values to the new format.");
        }

        if (!worthFile.exists())
            plugin.saveResource("block-values/worth.yml", true);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(worthFile);
        ConfigurationSection valuesSection = cfg.contains("block-values") ? cfg.getConfigurationSection("block-values") : cfg.getConfigurationSection("");

        for (String key : valuesSection.getKeys(false)) {
            String value = valuesSection.getString(key);
            try {
                setBlockValue(Keys.ofMaterialAndData(key), new BigDecimal(value));
            } catch (Exception ex) {
                Log.warnFromFile("worth.yml", "Cannot parse worth value for ", key + ", skipping...");
            }
        }
    }

}
