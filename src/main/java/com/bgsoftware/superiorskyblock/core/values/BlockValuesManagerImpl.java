package com.bgsoftware.superiorskyblock.core.values;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.key.KeyImpl;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import com.bgsoftware.superiorskyblock.core.key.KeySetImpl;
import com.bgsoftware.superiorskyblock.core.values.container.BlockValuesContainer;
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

public class BlockValuesManagerImpl extends Manager implements BlockValuesManager {

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

    private static final KeyMap<CustomKeyParser> customKeyParsers = KeyMapImpl.createHashMap();
    private static final KeySet valuesMenuBlocks = KeySetImpl.createHashSet();
    private static final KeySet customBlockKeys = KeySetImpl.createHashSet();

    private final BlockValuesContainer blockWorthValues;
    private final BlockValuesContainer blockLevels;
    private final BlockValuesContainer customBlockWorthValues;
    private final BlockValuesContainer customBlockLevels;

    public BlockValuesManagerImpl(SuperiorSkyblockPlugin plugin,
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
    public BigDecimal getBlockWorth(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        BigDecimal customBlockValue = customBlockWorthValues.getBlockValue(key);
        if (customBlockValue != null) {
            PluginDebugger.debug("Action: Get Worth, Block: " + key + " - Custom Block Worth, Worth: " + customBlockValue);
            return customBlockValue;
        }

        if (blockWorthValues.containsKeyRaw(key)) {
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
    public BigDecimal getBlockLevel(Key key) {
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
    public Key getBlockKey(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        if (((KeyImpl) key).isAPIKey() || isValuesMenu(key)) {
            return getValuesKey(key);
        } else if (customBlockKeys.contains(key)) {
            return customBlockKeys.getKey(key);
        } else if (blockWorthValues.containsKeyRaw(key)) {
            return key;
        } else if (blockLevels.containsKeyRaw(key)) {
            return key;
        } else {
            if (plugin.getSettings().getSyncWorth() != SyncWorthStatus.NONE) {
                Key newKey = plugin.getProviders().getPricesProvider().getBlockKey(key);
                if (newKey != null) {
                    return newKey;
                }
            }

            return blockWorthValues.containsKey(key) ? blockWorthValues.getBlockValueKey(key) :
                    blockLevels.getBlockValueKey(key);
        }
    }

    @Override
    public void registerCustomKey(Key key, @Nullable BigDecimal worthValue, @Nullable BigDecimal levelValue) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        if (worthValue != null && !customBlockWorthValues.hasBlockValue(key)) {
            customBlockWorthValues.setBlockValue(key, worthValue);
        }
        if (levelValue != null && !customBlockLevels.hasBlockValue(key)) {
            customBlockLevels.setBlockValue(key, levelValue);
        }
    }

    @Override
    public void registerKeyParser(CustomKeyParser customKeyParser, Key... blockTypes) {
        Preconditions.checkNotNull(customKeyParser, "customKeyParser parameter cannot be null.");
        Preconditions.checkNotNull(blockTypes, "blockTypes parameter cannot be null.");

        for (Key blockType : blockTypes)
            customKeyParsers.put(blockType, customKeyParser);
    }

    public void registerMenuValueBlocks(KeySet blocks) {
        valuesMenuBlocks.addAll(blocks);
    }

    public boolean isValuesMenu(Key key) {
        return valuesMenuBlocks.contains(key);
    }

    public Key getValuesKey(Key key) {
        return valuesMenuBlocks.getKey(key, key);
    }

    public void addCustomBlockKey(Key key) {
        customBlockKeys.add(key);
    }

    public void addCustomBlockKeys(Collection<Key> blocks) {
        customBlockKeys.addAll(blocks);
    }

    public Key convertKey(Key original, Location location) {
        CustomKeyParser customKeyParser = customKeyParsers.get(original);

        if (customKeyParser == null)
            return original;

        Key key = customKeyParser.getCustomKey(location);

        return key == null ? original : key;
    }

    public Key convertKey(Key original, ItemStack itemStack) {
        CustomKeyParser customKeyParser = customKeyParsers.get(original);

        if (customKeyParser == null)
            return original;

        return customKeyParser.getCustomKey(itemStack, original);
    }

    public Key convertKey(Key original, Entity entity) {
        CustomKeyParser customKeyParser = customKeyParsers.get(original);

        if (customKeyParser == null)
            return original;

        Key key = customKeyParser.getCustomKey(entity);

        return key == null ? original : key;
    }

    public Key convertKey(Key original) {
        for (Map.Entry<Key, CustomKeyParser> entry : customKeyParsers.entrySet()) {
            if (entry.getValue().isCustomKey(original))
                return entry.getKey();
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
        blockWorthValues.forEach((blockKey, blockCount) -> {
            if (!blockLevels.hasBlockValue(blockKey)) {
                blockLevels.setBlockValue(blockKey, convertValueToLevel(blockCount));
            }
        });
    }

    private static BigDecimal fastBigDecimalFromString(String value) {
        return CACHED_BIG_DECIMALS.getOrDefault(value, new BigDecimal(value));
    }

}
