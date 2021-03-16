package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.key.KeySet;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;
import java.io.File;
import java.math.BigDecimal;
import java.util.Map;

public final class BlockValuesHandler extends AbstractHandler implements BlockValuesManager {

    private static final ScriptEngine engine = new ScriptEngineManager(null).getEngineByName("JavaScript");
    private static final Bindings bindings = createBindings();

    private static final KeyMap<BigDecimal> customBlockValues = new KeyMap<>(), customBlockLevels = new KeyMap<>();
    private static final KeyMap<CustomKeyParser> customKeyParsers = new KeyMap<>();
    private static final KeySet valuesMenuBlocks = new KeySet();

    private final KeyMap<BigDecimal> blockValues = new KeyMap<>(), blockLevels = new KeyMap<>();

    public BlockValuesHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
    }

    @Override
    public void loadData(){
        loadBlockValues(plugin);
        loadBlockLevels(plugin);
        convertValuesToLevels();
    }

    @Override
    public BigDecimal getBlockWorth(com.bgsoftware.superiorskyblock.api.key.Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key);

        BigDecimal customBlockValue = customBlockValues.get(key);
        if(customBlockValue != null) {
            SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key + " - Custom Block Worth");
            return customBlockValue;
        }

        BigDecimal value = blockValues.get(key);

        if(value != null) {
            SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key + " - Worth File");
            return value;
        }

        if(plugin.getSettings().syncWorth != SyncWorthStatus.NONE) {
            BigDecimal price = plugin.getProviders().getPrice((Key) key);
            SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key + " - Price");
            return price;
        }

        return BigDecimal.ZERO;
    }

    public void registerMenuValueBlocks(KeySet blocks){
        valuesMenuBlocks.addAll(blocks);
    }

    public boolean isValuesMenu(com.bgsoftware.superiorskyblock.api.key.Key key){
        return valuesMenuBlocks.contains(key);
    }

    public Key getValuesKey(com.bgsoftware.superiorskyblock.api.key.Key key){
        return valuesMenuBlocks.getKey(key);
    }

    @Override
    public BigDecimal getBlockLevel(com.bgsoftware.superiorskyblock.api.key.Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        SuperiorSkyblockPlugin.debug("Action: Get Level, Block: " + key);

        BigDecimal customBlockLevel = customBlockLevels.get(key);
        if(customBlockLevel != null) {
            SuperiorSkyblockPlugin.debug("Action: Get Level, Block: " + key + " - Custom Block Level");
            return customBlockLevel;
        }

        BigDecimal level = blockLevels.get(key);

        if(level == null) {
            level = convertValueToLevel(getBlockWorth(key));
            blockLevels.put(key, level);
            SuperiorSkyblockPlugin.debug("Action: Get Level, Block: " + key + " - Converted From Worth");
        }

        return level;
    }

    @Override
    public Key getBlockKey(com.bgsoftware.superiorskyblock.api.key.Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return ((Key) key).isAPIKey() || isValuesMenu(key) ? getValuesKey(key) :
                blockValues.containsKey(key) ? blockValues.getKey((Key) key) : blockLevels.getKey((Key) key);
    }

    @Override
    public void registerCustomKey(com.bgsoftware.superiorskyblock.api.key.Key key, @Nullable BigDecimal worthValue, @Nullable BigDecimal levelValue) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        if(worthValue != null && !customBlockValues.containsKey(key)){
            customBlockValues.put(key, worthValue);
        }
        if(levelValue != null && !customBlockLevels.containsKey(key)){
            customBlockLevels.put(key, levelValue);
        }
    }

    @Override
    public void registerKeyParser(CustomKeyParser customKeyParser, com.bgsoftware.superiorskyblock.api.key.Key... blockTypes) {
        Preconditions.checkNotNull(customKeyParser, "customKeyParser parameter cannot be null.");
        Preconditions.checkNotNull(blockTypes, "blockTypes parameter cannot be null.");

        for(com.bgsoftware.superiorskyblock.api.key.Key blockType : blockTypes)
            customKeyParsers.put(blockType, customKeyParser);
    }

    public Key convertKey(Key original, Location location){
        CustomKeyParser customKeyParser = customKeyParsers.get(original);

        if(customKeyParser == null)
            return original;

        com.bgsoftware.superiorskyblock.api.key.Key key = customKeyParser.getCustomKey(location);

        return key == null ? original : (Key) key;
    }

    public Key convertKey(Key original, ItemStack itemStack){
        CustomKeyParser customKeyParser = customKeyParsers.get(original);

        if(customKeyParser == null)
            return original;

        return (Key) customKeyParser.getCustomKey(itemStack, original);
    }

    public Key convertKey(Key original, Entity entity){
        CustomKeyParser customKeyParser = customKeyParsers.get(original);

        if(customKeyParser == null)
            return original;

        com.bgsoftware.superiorskyblock.api.key.Key key = customKeyParser.getCustomKey(entity);

        return key == null ? original : (Key) key;
    }

    public Key convertKey(Key original){
        for(Map.Entry<com.bgsoftware.superiorskyblock.api.key.Key, CustomKeyParser> entry : customKeyParsers.entrySet()) {
            if (entry.getValue().isCustomKey(original))
                return (Key) entry.getKey();
        }

        return original;
    }

    public BigDecimal convertValueToLevel(BigDecimal value){
        try {
            Object obj = engine.eval(plugin.getSettings().islandLevelFormula.replace("{}", value.toString()), bindings);

            // Checking for division by 0
            if(obj.equals(Double.POSITIVE_INFINITY) || obj.equals(Double.NEGATIVE_INFINITY))
                obj = 0D;

            return new BigDecimal(obj.toString());
        }catch(Exception ex){
            ex.printStackTrace();
            return value;
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

        for(String key : valuesSection.getKeys(false)) {
            String value = valuesSection.getString(key);
            try {
                blockValues.put(getBlockKey(com.bgsoftware.superiorskyblock.utils.key.Key.of(key)), new BigDecimal(value));
            }catch (Exception ex){
                SuperiorSkyblockPlugin.log("&cInvalid worth value: " + value);
            }
        }
    }

    private void loadBlockLevels(SuperiorSkyblockPlugin plugin){
        File file = new File(plugin.getDataFolder(), "block-values/levels.yml");

        if(!file.exists())
            plugin.saveResource("block-values/levels.yml", true);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection valuesSection = cfg.getConfigurationSection("");

        for(String key : valuesSection.getKeys(false)) {
            String value = valuesSection.getString(key);
            try {
                blockLevels.put(getBlockKey(com.bgsoftware.superiorskyblock.utils.key.Key.of(key)), new BigDecimal(value));
            }catch (Exception ex){
                SuperiorSkyblockPlugin.log("&cInvalid level value: " + value);
            }
        }
    }

    private void convertValuesToLevels() {
        for(Map.Entry<com.bgsoftware.superiorskyblock.api.key.Key, BigDecimal> entry : blockValues.entrySet()){
            if(!blockLevels.containsKey(entry.getKey())){
                blockLevels.put(entry.getKey(), convertValueToLevel(entry.getValue()));
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
