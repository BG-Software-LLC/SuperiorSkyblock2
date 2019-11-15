package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.utils.BigDecimalFormatted;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.math.BigDecimal;

public final class BlockValuesHandler implements BlockValuesManager {

    private static ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    private final KeyMap<String> blockValues = new KeyMap<>(), blockLevels = new KeyMap<>();
    private final SuperiorSkyblockPlugin plugin;

    public BlockValuesHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        loadBlockValues(plugin);
        loadBlockLevels(plugin);
    }

    @Override
    public BigDecimal getBlockWorth(Key key) {
        return BigDecimalFormatted.of(blockValues.getOrDefault(key, "-1"));
    }

    @Override
    public BigDecimal getBlockLevel(Key key) {
        return BigDecimalFormatted.of(blockLevels.getOrDefault(key, convertValueToLevel((BigDecimalFormatted) getBlockWorth(key))));
    }

    @Override
    public Key getBlockKey(Key key) {
        return blockValues.containsKey(key) ? blockValues.getKey(key) : blockLevels.getKey(key);
    }

    public String convertValueToLevel(BigDecimalFormatted value){
        try {
            return new BigDecimal(engine.eval(plugin.getSettings().islandLevelFormula.replace("{}", value.getAsString())).toString()).toString();
        }catch(Exception ex){
            ex.printStackTrace();
            return value.toString();
        }
    }

    private void loadBlockValues(SuperiorSkyblockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "block-values/worth.yml");
        File blockValuesFile = new File(plugin.getDataFolder(), "blockvalues.yml");

        if(blockValuesFile.exists()){
            file.getParentFile().mkdirs();
            //noinspection ResultOfMethodCallIgnored
            blockValuesFile.renameTo(file);
            file = new File(plugin.getDataFolder(), "block-values/worth.yml");
        }

        if(!file.exists())
            plugin.saveResource("block-values/worth.yml", true);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection valuesSection = cfg.contains("block-values") ? cfg.getConfigurationSection("block-values") : cfg.getConfigurationSection("");

        for(String key : valuesSection.getKeys(false))
            blockValues.put(getBlockKey(Key.of(key)), String.valueOf(valuesSection.isDouble(key) ? valuesSection.getDouble(key) : (double) valuesSection.getInt(key)));
    }

    private void loadBlockLevels(SuperiorSkyblockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "block-values/levels.yml");

        if(!file.exists())
            plugin.saveResource("block-values/levels.yml", true);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection valuesSection = cfg.getConfigurationSection("");

        for(String key : valuesSection.getKeys(false))
            blockLevels.put(getBlockKey(Key.of(key)), String.valueOf(valuesSection.isDouble(key) ? valuesSection.getDouble(key) : (double) valuesSection.getInt(key)));
    }

}
