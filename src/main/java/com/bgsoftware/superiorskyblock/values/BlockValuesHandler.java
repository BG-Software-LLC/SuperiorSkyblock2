package com.bgsoftware.superiorskyblock.values;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.handler.AbstractHandler;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import com.bgsoftware.superiorskyblock.key.dataset.KeySet;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.values.container.BlockValuesContainer;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

public final class BlockValuesHandler extends AbstractHandler implements BlockValuesManager {

    private static final Map<String, BigDecimal> CACHED_BIG_DECIMALS;

    static {
        ImmutableMap.Builder<String, BigDecimal> mapBuilder = new ImmutableMap.Builder<>();
        mapBuilder.put("", BigDecimal.ZERO);

        for (int i = 0; i < 10; ++i) {
            mapBuilder.put(i + "", BigDecimal.valueOf(i));
        }

        for (int i = 10; i < 100; i *= 10) {
            mapBuilder.put(i + "", BigDecimal.valueOf(i));
        }

        for (int i = 100; i <= 1000; i *= 100) {
            mapBuilder.put(i + "", BigDecimal.valueOf(i));
        }

        CACHED_BIG_DECIMALS = mapBuilder.build();
    }

    private static final Bindings bindings = createBindings();

    private static final KeyMap<CustomKeyParser> customKeyParsers = new KeyMap<>();
    private static final KeySet valuesMenuBlocks = new KeySet();
    private static final KeySet customBlockKeys = new KeySet();

    private final BlockValuesContainer blockWorthValues;
    private final BlockValuesContainer blockLevels;
    private final BlockValuesContainer customBlockWorthValues;
    private final BlockValuesContainer customBlockLevels;

    public BlockValuesHandler(SuperiorSkyblockPlugin plugin,
                              BlockValuesContainer blockWorthValuesContainer,
                              BlockValuesContainer blockLevelsContainer,
                              BlockValuesContainer customBlockWorthValuesContainer,
                              BlockValuesContainer customBlockLevelsContainer) {
        super(plugin);
        this.blockWorthValues = blockWorthValuesContainer;
        this.blockLevels = blockLevelsContainer;
        this.customBlockWorthValues = customBlockWorthValuesContainer;
        this.customBlockLevels = customBlockLevelsContainer;
    }

    private static Bindings createBindings() {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("Math", Math.class);
        return bindings;
    }

    @Override
    public void loadData() {
        this.blockWorthValues.clear();
        this.blockLevels.clear();

        this.blockWorthValues.loadDefaultValues(plugin);
        this.blockLevels.loadDefaultValues(plugin);
        convertValuesToLevels();
    }

    @Override
    public BigDecimal getBlockWorth(com.bgsoftware.superiorskyblock.api.key.Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        BigDecimal customBlockValue = customBlockWorthValues.getBlockValue(key);
        if (customBlockValue != null) {
            PluginDebugger.debug("Action: Get Worth, Block: " + key + " - Custom Block Worth, Worth: " + customBlockValue);
            return customBlockValue;
        }

        if (blockWorthValues.containsKeyRaw((Key) key)) {
            BigDecimal value = blockWorthValues.getBlockValue(key);

            if (value != null) {
                PluginDebugger.debug("Action: Get Worth, Block: " + key + " - Worth File, Worth: " + value);
                return value;
            }
        }

        if (plugin.getSettings().getSyncWorth() != SyncWorthStatus.NONE) {
            BigDecimal price = plugin.getProviders().getPricesProvider().getPrice(key);
            PluginDebugger.debug("Action: Get Worth, Block: " + key + " - Price, Worth: " + price);
            return price;
        }

        BigDecimal value = blockWorthValues.getBlockValue(key);

        if (value != null) {
            PluginDebugger.debug("Action: Get Worth, Block: " + key + " - Worth File, Worth: " + value);
            return value;
        }

        PluginDebugger.debug("Action: Get Worth, Block: " + key + " - Worth File, Worth: 0");

        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getBlockLevel(com.bgsoftware.superiorskyblock.api.key.Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        BigDecimal customBlockLevel = customBlockLevels.getBlockValue(key);
        if (customBlockLevel != null) {
            PluginDebugger.debug("Action: Get Level, Block: " + key + " - Custom Block Level, Level: " + customBlockLevel);
            return customBlockLevel;
        }

        BigDecimal level = blockLevels.getBlockValue(key);

        if (level == null) {
            level = convertValueToLevel(getBlockWorth(key));
            blockLevels.setBlockValue(key, level);
            PluginDebugger.debug("Action: Get Level, Block: " + key + " - Converted From Worth, Level: " + level);
        } else {
            PluginDebugger.debug("Action: Get Level, Block: " + key + " - Levels File, Level: " + level);
        }

        return level;
    }

    @Override
    public Key getBlockKey(com.bgsoftware.superiorskyblock.api.key.Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        Key convertedKey = (Key) key;

        if (convertedKey.isAPIKey() || isValuesMenu(convertedKey)) {
            return getValuesKey(convertedKey);
        } else if (customBlockKeys.contains(convertedKey)) {
            return customBlockKeys.getKey(convertedKey);
        } else if (blockWorthValues.containsKeyRaw(convertedKey)) {
            return convertedKey;
        } else if (blockLevels.containsKeyRaw(convertedKey)) {
            return convertedKey;
        } else {
            if (plugin.getSettings().getSyncWorth() != SyncWorthStatus.NONE) {
                Key newKey = (Key) plugin.getProviders().getPricesProvider().getBlockKey(convertedKey);
                if (newKey != null) {
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
        if (worthValue != null && !customBlockWorthValues.hasBlockValue(key)) {
            customBlockWorthValues.setBlockValue(key, worthValue);
        }
        if (levelValue != null && !customBlockLevels.hasBlockValue(key)) {
            customBlockLevels.setBlockValue(key, levelValue);
        }
    }

    @Override
    public void registerKeyParser(CustomKeyParser customKeyParser, com.bgsoftware.superiorskyblock.api.key.Key... blockTypes) {
        Preconditions.checkNotNull(customKeyParser, "customKeyParser parameter cannot be null.");
        Preconditions.checkNotNull(blockTypes, "blockTypes parameter cannot be null.");

        for (com.bgsoftware.superiorskyblock.api.key.Key blockType : blockTypes)
            customKeyParsers.put(blockType, customKeyParser);
    }

    public void registerMenuValueBlocks(KeySet blocks) {
        valuesMenuBlocks.addAll(blocks);
    }

    public boolean isValuesMenu(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return valuesMenuBlocks.contains(key);
    }

    public Key getValuesKey(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return valuesMenuBlocks.getKey(key);
    }

    public void addCustomBlockKey(com.bgsoftware.superiorskyblock.api.key.Key key) {
        customBlockKeys.add(key);
    }

    public void addCustomBlockKeys(Collection<com.bgsoftware.superiorskyblock.api.key.Key> blocks) {
        customBlockKeys.addAll(blocks);
    }

    public Key convertKey(Key original, Location location) {
        CustomKeyParser customKeyParser = customKeyParsers.get(original);

        if (customKeyParser == null)
            return original;

        com.bgsoftware.superiorskyblock.api.key.Key key = customKeyParser.getCustomKey(location);

        return key == null ? original : (Key) key;
    }

    public Key convertKey(Key original, ItemStack itemStack) {
        CustomKeyParser customKeyParser = customKeyParsers.get(original);

        if (customKeyParser == null)
            return original;

        return (Key) customKeyParser.getCustomKey(itemStack, original);
    }

    public Key convertKey(Key original, Entity entity) {
        CustomKeyParser customKeyParser = customKeyParsers.get(original);

        if (customKeyParser == null)
            return original;

        com.bgsoftware.superiorskyblock.api.key.Key key = customKeyParser.getCustomKey(entity);

        return key == null ? original : (Key) key;
    }

    public Key convertKey(Key original) {
        for (Map.Entry<com.bgsoftware.superiorskyblock.api.key.Key, CustomKeyParser> entry : customKeyParsers.entrySet()) {
            if (entry.getValue().isCustomKey(original))
                return (Key) entry.getKey();
        }

        return original;
    }

    public BigDecimal convertValueToLevel(BigDecimal value) {
        // If the formula contains no mathematical operations or the placeholder for the worth value,
        // we can directly create the BigDecimal instance from it.
        try {
            return fastBigDecimalFromString(plugin.getSettings().getIslandLevelFormula());
        } catch (NumberFormatException ignored) {
        }

        try {
            Object evaluated = plugin.getScriptEngine().eval(plugin.getSettings().getIslandLevelFormula()
                    .replace("{}", value.toString()), bindings);

            // Checking for division by 0
            if (evaluated.equals(Double.POSITIVE_INFINITY) || evaluated.equals(Double.NEGATIVE_INFINITY))
                return BigDecimal.ZERO;

            return fastBigDecimalFromString(evaluated.toString());
        } catch (ScriptException ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
            return value;
        }
    }

    private void convertValuesToLevels() {
        for (Map.Entry<com.bgsoftware.superiorskyblock.api.key.Key, BigDecimal> entry : blockWorthValues.getBlockValues()) {
            if (!blockLevels.hasBlockValue(entry.getKey())) {
                blockLevels.setBlockValue(entry.getKey(), convertValueToLevel(entry.getValue()));
            }
        }
    }

    private static BigDecimal fastBigDecimalFromString(String value) {
        return CACHED_BIG_DECIMALS.getOrDefault(value, new BigDecimal(value));
    }

}
