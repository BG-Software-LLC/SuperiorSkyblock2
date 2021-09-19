package com.bgsoftware.superiorskyblock.values;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.handler.AbstractHandler;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.key.dataset.KeySet;
import com.bgsoftware.superiorskyblock.values.container.BlockValuesContainer;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.math.BigDecimal;
import java.util.Map;

public final class BlockValuesHandler extends AbstractHandler implements BlockValuesManager {

    private static final Bindings bindings = createBindings();

    private static final KeyMap<CustomKeyParser> customKeyParsers = new KeyMap<>();
    private static final KeySet valuesMenuBlocks = new KeySet();

    private final BlockValuesContainer blockWorthValues, blockLevels, customBlockWorthValues, customBlockLevels;

    public BlockValuesHandler(SuperiorSkyblockPlugin plugin,
                              BlockValuesContainer blockWorthValuesContainer,
                              BlockValuesContainer blockLevelsContainer,
                              BlockValuesContainer customBlockWorthValuesContainer,
                              BlockValuesContainer customBlockLevelsContainer){
        super(plugin);
        this.blockWorthValues = blockWorthValuesContainer;
        this.blockLevels = blockLevelsContainer;
        this.customBlockWorthValues = customBlockWorthValuesContainer;
        this.customBlockLevels = customBlockLevelsContainer;
    }

    @Override
    public void loadData(){
        blockWorthValues.loadDefaultValues(plugin);
        blockLevels.loadDefaultValues(plugin);
        convertValuesToLevels();
    }

    @Override
    public BigDecimal getBlockWorth(com.bgsoftware.superiorskyblock.api.key.Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key);

        BigDecimal customBlockValue = customBlockWorthValues.getBlockValue(key);
        if(customBlockValue != null) {
            SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key + " - Custom Block Worth");
            return customBlockValue;
        }

        if(blockWorthValues.containsKeyRaw((Key) key)) {
            BigDecimal value = blockWorthValues.getBlockValue(key);

            if (value != null) {
                SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key + " - Worth File");
                return value;
            }
        }

        if(plugin.getSettings().getSyncWorth() != SyncWorthStatus.NONE) {
            BigDecimal price = plugin.getProviders().getPrice((Key) key);
            SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key + " - Price");
            return price;
        }

        BigDecimal value = blockWorthValues.getBlockValue(key);

        if (value != null) {
            SuperiorSkyblockPlugin.debug("Action: Get Worth, Block: " + key + " - Worth File");
            return value;
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

        BigDecimal customBlockLevel = customBlockLevels.getBlockValue(key);
        if(customBlockLevel != null) {
            SuperiorSkyblockPlugin.debug("Action: Get Level, Block: " + key + " - Custom Block Level");
            return customBlockLevel;
        }

        BigDecimal level = blockLevels.getBlockValue(key);

        if(level == null) {
            level = convertValueToLevel(getBlockWorth(key));
            blockLevels.setBlockValue(key, level);
            SuperiorSkyblockPlugin.debug("Action: Get Level, Block: " + key + " - Converted From Worth");
        }

        return level;
    }

    @Override
    public Key getBlockKey(com.bgsoftware.superiorskyblock.api.key.Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        Key convertedKey = (Key) key;

        if(convertedKey.isAPIKey() || isValuesMenu(convertedKey)) {
            return getValuesKey(convertedKey);
        }

        if(blockWorthValues.containsKeyRaw(convertedKey)) {
            return convertedKey;
        }
        else if(blockLevels.containsKeyRaw(convertedKey)) {
            return convertedKey;
        }
        else {
            if(plugin.getSettings().getSyncWorth() != SyncWorthStatus.NONE) {
                Key newKey = plugin.getProviders().getPriceBlockKey(convertedKey);
                if(newKey != null) {
                    return newKey;
                }
            }

            return blockWorthValues.containsKey(key) ? blockWorthValues.getBlockValueKey(convertedKey) :
                    blockLevels.getBlockValueKey(convertedKey);
        }
    }

    @Override
    public void registerCustomKey(com.bgsoftware.superiorskyblock.api.key.Key key, @Nullable BigDecimal worthValue, @Nullable BigDecimal levelValue) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        if(worthValue != null && !customBlockWorthValues.hasBlockValue(key)){
            customBlockWorthValues.setBlockValue(key, worthValue);
        }
        if(levelValue != null && !customBlockLevels.hasBlockValue(key)){
            customBlockLevels.setBlockValue(key, levelValue);
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
            Object obj = plugin.getScriptEngine().eval(plugin.getSettings().getIslandLevelFormula()
                    .replace("{}", value.toString()), bindings);

            // Checking for division by 0
            if(obj.equals(Double.POSITIVE_INFINITY) || obj.equals(Double.NEGATIVE_INFINITY))
                obj = 0D;

            return new BigDecimal(obj.toString());
        }catch(ScriptException ex){
            ex.printStackTrace();
            return value;
        }
    }

    private void convertValuesToLevels() {
        for(Map.Entry<com.bgsoftware.superiorskyblock.api.key.Key, BigDecimal> entry : blockWorthValues.getBlockValues()){
            if(!blockLevels.hasBlockValue(entry.getKey())){
                blockLevels.setBlockValue(entry.getKey(), convertValueToLevel(entry.getValue()));
            }
        }
    }

    private static Bindings createBindings() {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("Math", Math.class);
        return bindings;
    }

}
