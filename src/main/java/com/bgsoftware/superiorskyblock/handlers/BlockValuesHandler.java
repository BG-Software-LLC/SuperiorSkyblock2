package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.utils.BigDecimalFormatted;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

public final class BlockValuesHandler implements BlockValuesManager {

    private static final ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    private static final KeyMap<String> customBlockValues = new KeyMap<>(), customBlockLevels = new KeyMap<>();
    private static final KeyMap<CustomKeyParser> customKeyParsers = new KeyMap<>();

    private final KeyMap<String> blockValues = new KeyMap<>(), blockLevels = new KeyMap<>();
    private final SuperiorSkyblockPlugin plugin;

    public BlockValuesHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        loadBlockValues(plugin);
        loadBlockLevels(plugin);
    }

    @Override
    public BigDecimal getBlockWorth(com.bgsoftware.superiorskyblock.api.key.Key key) {
        if(((Key) key).isAPIKey())
            return BigDecimalFormatted.of(0);

        if(plugin.getSettings().syncWorth)
            return BigDecimalFormatted.of(plugin.getProviders().getPrice((Key) key));

        String customWorth = customBlockValues.get(key);

        return customWorth != null ? BigDecimalFormatted.of(customWorth) :
                BigDecimalFormatted.of(blockValues.getOrDefault(key, "-1"));
    }

    public void setBlockWorth(Key key, BigDecimal worth){
        blockValues.put(key, worth.toString());
    }

    @Override
    public BigDecimal getBlockLevel(com.bgsoftware.superiorskyblock.api.key.Key key) {
        if(((Key) key).isAPIKey())
            return BigDecimalFormatted.of(0);

        String customLevel = customBlockLevels.get(key);

        return customLevel != null ? BigDecimalFormatted.of(customLevel) :
                BigDecimalFormatted.of(blockLevels.getOrDefault(key, convertValueToLevel((BigDecimalFormatted) getBlockWorth(key))));
    }

    @Override
    public Key getBlockKey(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return ((Key) key).isAPIKey() ? (Key) key : blockValues.containsKey(key) ? blockValues.getKey((Key) key) : blockLevels.getKey((Key) key);
    }

    @Override
    public void registerCustomKey(com.bgsoftware.superiorskyblock.api.key.Key key, BigDecimal worthValue, BigDecimal levelValue) {
        if(worthValue != null && !customBlockValues.containsKey(key)){
            customBlockValues.put(key, worthValue.toString());
        }
        if(levelValue != null && !customBlockLevels.containsKey(key)){
            customBlockLevels.put(key, levelValue.toString());
        }
    }

    @Override
    public void registerKeyParser(CustomKeyParser customKeyParser, com.bgsoftware.superiorskyblock.api.key.Key... blockTypes) {
        for(com.bgsoftware.superiorskyblock.api.key.Key blockType : blockTypes)
            customKeyParsers.put(blockType, customKeyParser);
    }

    public Key convertKey(Key original, Location location){
        CustomKeyParser customKeyParser = customKeyParsers.get(original);

        if(customKeyParser == null)
            return original;

        return (Key) customKeyParser.getCustomKey(location);
    }

    public Key convertKey(Key original){
        for(Map.Entry<com.bgsoftware.superiorskyblock.api.key.Key, CustomKeyParser> entry : customKeyParsers.entrySet()) {
            if (entry.getValue().isCustomKey(original))
                return (Key) entry.getKey();
        }

        return original;
    }

    public String convertValueToLevel(BigDecimalFormatted value){
        try {
            return new BigDecimal(engine.eval(plugin.getSettings().islandLevelFormula.replace("{}", value.getAsString())).toString()).toString();
        }catch(Exception ex){
            ex.printStackTrace();
            return value.toString();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadBlockValues(SuperiorSkyblockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "block-values/worth.yml");
        File blockValuesFile = new File(plugin.getDataFolder(), "blockvalues.yml");

        if(blockValuesFile.exists()){
            file.getParentFile().mkdirs();
            blockValuesFile.renameTo(file);
            file = new File(plugin.getDataFolder(), "block-values/worth.yml");
        }

        if(!file.exists())
            plugin.saveResource("block-values/worth.yml", true);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection valuesSection = cfg.contains("block-values") ? cfg.getConfigurationSection("block-values") : cfg.getConfigurationSection("");

        for(String key : valuesSection.getKeys(false))
            blockValues.put(getBlockKey(com.bgsoftware.superiorskyblock.utils.key.Key.of(key)),
                    String.valueOf(valuesSection.isDouble(key) ? valuesSection.getDouble(key) : (double) valuesSection.getInt(key)));
    }

    private void loadBlockLevels(SuperiorSkyblockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "block-values/levels.yml");

        if(!file.exists())
            plugin.saveResource("block-values/levels.yml", true);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection valuesSection = cfg.getConfigurationSection("");

        for(String key : valuesSection.getKeys(false))
            blockLevels.put(getBlockKey(com.bgsoftware.superiorskyblock.utils.key.Key.of(key)),
                    String.valueOf(valuesSection.isDouble(key) ? valuesSection.getDouble(key) : (double) valuesSection.getInt(key)));
    }

}
