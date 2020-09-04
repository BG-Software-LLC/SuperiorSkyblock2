package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.utils.BigDecimalFormatted;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.key.KeySet;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

public final class BlockValuesHandler implements BlockValuesManager {

    private static final ScriptEngine engine = new ScriptEngineManager(null).getEngineByName("JavaScript");
    private static final Bindings bindings = createBindings();

    private static final KeyMap<String> customBlockValues = new KeyMap<>(), customBlockLevels = new KeyMap<>();
    private static final KeyMap<CustomKeyParser> customKeyParsers = new KeyMap<>();
    private static final KeySet valuesMenuBlocks = new KeySet();

    private final KeyMap<String> blockValues = new KeyMap<>(), blockLevels = new KeyMap<>();
    private final SuperiorSkyblockPlugin plugin;

    public BlockValuesHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
        loadBlockValues(plugin);
        loadBlockLevels(plugin);
        convertValuesToLevels();
    }

    @Override
    public BigDecimal getBlockWorth(com.bgsoftware.superiorskyblock.api.key.Key key) {
        SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key);

        if(((Key) key).isAPIKey()){
            String customBlockValue = customBlockValues.get(key);
            if(customBlockValue != null) {
                SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key + " - Custom Block Worth");
                return BigDecimalFormatted.of(customBlockValue);
            }
        }

        String value = blockValues.get(key);

        if(value != null) {
            SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key + " - Worth File");
            return BigDecimalFormatted.of(value);
        }

        if(plugin.getSettings().syncWorth != SyncWorthStatus.NONE) {
            BigDecimal price = plugin.getProviders().getPrice((Key) key);
            SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key + " - Price");
            return BigDecimalFormatted.of(price);
        }

        return BigDecimalFormatted.ZERO;
    }

    public void registerMenuValueBlocks(KeySet blocks){
        valuesMenuBlocks.addAll(blocks);
    }

    public boolean isValuesMenu(com.bgsoftware.superiorskyblock.api.key.Key key){
        return valuesMenuBlocks.contains(key);
    }

    @Override
    public BigDecimal getBlockLevel(com.bgsoftware.superiorskyblock.api.key.Key key) {
        SuperiorSkyblockPlugin.debug("Action: Get Level, Block: " + key);

        if(((Key) key).isAPIKey()){
            String customBlockLevel = customBlockLevels.get(key);
            if(customBlockLevel != null) {
                SuperiorSkyblockPlugin.debug("Action: Get Level, Block: " + key + " - Custom Block Level");
                return BigDecimalFormatted.of(customBlockLevel);
            }
        }

        String level = blockLevels.get(key);

        if(level == null) {
            level = convertValueToLevel((BigDecimalFormatted) getBlockWorth(key));
            blockLevels.put(key, level);
            SuperiorSkyblockPlugin.debug("Action: Get Level, Block: " + key + " - Converted From Worth");
        }

        return BigDecimalFormatted.of(level);
    }

    @Override
    public Key getBlockKey(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return ((Key) key).isAPIKey() ? (Key) key : blockValues.containsKey(key) ? blockValues.getKey((Key) key) : blockLevels.getKey((Key) key);
    }

    @Override
    public void registerCustomKey(com.bgsoftware.superiorskyblock.api.key.Key key, BigDecimal worthValue, BigDecimal levelValue) {
        if(worthValue instanceof BigDecimalFormatted)
            worthValue = new BigDecimal(((BigDecimalFormatted) worthValue).getAsString());

        if(levelValue instanceof BigDecimalFormatted)
            levelValue = new BigDecimal(((BigDecimalFormatted) levelValue).getAsString());

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
            return new BigDecimal(engine.eval(plugin.getSettings().islandLevelFormula.replace("{}", value.getAsString()), bindings).toString()).toString();
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
            blockValues.put(getBlockKey(com.bgsoftware.superiorskyblock.utils.key.Key.of(key)), valuesSection.getString(key));
    }

    private void loadBlockLevels(SuperiorSkyblockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "block-values/levels.yml");

        if(!file.exists())
            plugin.saveResource("block-values/levels.yml", true);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection valuesSection = cfg.getConfigurationSection("");

        for(String key : valuesSection.getKeys(false))
            blockLevels.put(getBlockKey(com.bgsoftware.superiorskyblock.utils.key.Key.of(key)), valuesSection.getString(key));
    }

    private void convertValuesToLevels() {
        for(Map.Entry<com.bgsoftware.superiorskyblock.api.key.Key, String> entry : blockValues.entrySet()){
            if(!blockLevels.containsKey(entry.getKey())){
                blockLevels.put(entry.getKey(), convertValueToLevel(BigDecimalFormatted.of(entry.getValue())));
            }
        }
    }

    private static Bindings createBindings() {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("Math", Math.class);
        return bindings;
    }

    public enum SyncWorthStatus{

        NONE,
        BUY,
        SELL;

        public static SyncWorthStatus of(String name){
            try{
                return valueOf(name);
            }catch (Exception ex){
                return NONE;
            }
        }

    }

}
