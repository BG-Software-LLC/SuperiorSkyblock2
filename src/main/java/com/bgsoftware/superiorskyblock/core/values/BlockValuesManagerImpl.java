package com.bgsoftware.superiorskyblock.core.values;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.KeySets;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.values.container.BlockValuesContainer;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

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

    private static final KeyMap<CustomKeyParser> customKeyParsers = KeyMaps.createArrayMap(KeyIndicator.MATERIAL);
    private static final KeySet valuesMenuBlocks = KeySets.createHashSet(KeyIndicator.MATERIAL);
    private static final KeySet customBlockKeys = KeySets.createHashSet(KeyIndicator.MATERIAL);

    private final BlockValuesContainer blockValuesContainer;
    private final BlockValuesContainer customValuesContainer;

    public BlockValuesManagerImpl(SuperiorSkyblockPlugin plugin,
                                  BlockValuesContainer blockValuesContainer,
                                  BlockValuesContainer customValuesContainer) {
        super(plugin);
        this.blockValuesContainer = blockValuesContainer;
        this.customValuesContainer = customValuesContainer;
    }

    private static Bindings createBindings() {
        SimpleBindings bindings = new SimpleBindings();
        bindings.put("Math", Math.class);
        return bindings;
    }

    @Override
    public void loadData() {
        this.blockValuesContainer.clear();
        this.customValuesContainer.clear();

        loadDefaultValues();
        plugin.getProviders().addPricesLoadCallback(this::convertWorthValuesToLevels);
    }

    @Override
    public BigDecimal getBlockWorth(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return getBlockValue(key, true).getWorth();
    }

    @Override
    public BigDecimal getBlockLevel(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return getBlockValue(key, false).getLevel();
    }

    public BlockValue getBlockValue(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        return getBlockValue(key, true);
    }

    private BlockValue getBlockValue(Key key, boolean checkPrices) {
        Log.debug(Debug.GET_VALUE, key);

        BlockValue customBlockValue = customValuesContainer.getBlockValue(key);
        if (customBlockValue != null) {
            Log.debugResult(Debug.GET_VALUE, "Return Custom Block Value", customBlockValue);
            return customBlockValue;
        }

        if (checkPrices) {
            if (blockValuesContainer.containsKeyRaw(key)) {
                BlockValue value = blockValuesContainer.getBlockValue(key);

                if (value != null) {
                    Log.debugResult(Debug.GET_VALUE, "Return File", value);
                    return value;
                }
            }

            if (plugin.getSettings().getSyncWorth() != SyncWorthStatus.NONE) {
                BigDecimal price = plugin.getProviders().getPricesProvider().getPrice(key);
                if (price.compareTo(BigDecimal.ZERO) >= 0) {
                    BlockValue blockValue = BlockValue.ofWorth(price);
                    Log.debugResult(Debug.GET_VALUE, "Return Price", blockValue);
                    return blockValue;
                }
            }
        }

        BlockValue value = blockValuesContainer.getBlockValue(key);

        if (value != null) {
            Log.debugResult(Debug.GET_VALUE, "Return File", value);
            return value;
        }

        Log.debugResult(Debug.GET_VALUE, "Return File", 0);

        return BlockValue.ZERO;
    }

    @Override
    public Key getBlockKey(Key key) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");

        if (((BaseKey<?>) key).isAPIKey() || isValuesMenu(key)) {
            return getValuesKey(key);
        }

        if (customBlockKeys.contains(key)) {
            return customBlockKeys.getKey(key);
        }

        if (blockValuesContainer.containsKeyRaw(key)) {
            return key;
        }

        if (plugin.getSettings().getSyncWorth() != SyncWorthStatus.NONE) {
            Key newKey = plugin.getProviders().getPricesProvider().getBlockKey(key);
            if (newKey != null) {
                return newKey;
            }
        }

        return blockValuesContainer.getBlockValueKey(key);
    }

    @Override
    public void registerCustomKey(Key key, @Nullable BigDecimal worthValue, @Nullable BigDecimal levelValue) {
        Preconditions.checkNotNull(key, "key parameter cannot be null.");
        if (!customValuesContainer.containsKey(key)) {
            customValuesContainer.setBlockValue(key, BlockValue.ofWorthAndLevel(worthValue, levelValue));
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

    @Nullable
    public Pair<Key, ItemStack> convertCustomKeyItem(Key original) {
        for (Map.Entry<Key, CustomKeyParser> entry : customKeyParsers.entrySet()) {
            if (entry.getValue().isCustomKey(original)) {
                return new Pair<>(entry.getKey(), entry.getValue().getCustomKeyItem(original));
            }
        }

        return new Pair<>(original, null);
    }

    private BigDecimal convertWorthToLevel(BigDecimal value) {
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
        } catch (ScriptException error) {
            Log.entering("ENTER", value);
            Log.error(error, "An unexpected error occurred while converting level from worth:");
            return value;
        }
    }

    private void convertWorthValuesToLevels() {
        blockValuesContainer.forEach((blockKey, blockCount) -> {
            BlockValue realBlockValue = blockValuesContainer.getBlockValue(blockKey);
            if (realBlockValue != null && !realBlockValue.hasLevel()) {
                BlockValue newBlockValue = realBlockValue.setLevel(convertWorthToLevel(realBlockValue.getWorth()));
                blockValuesContainer.setBlockValue(blockKey, newBlockValue);
            }
        });
    }

    private void loadDefaultValues() {
        // First, convert old file
        File blockValuesFile = new File(plugin.getDataFolder(), "blockvalues.yml");
        if (blockValuesFile.exists()) {
            File worthFile = new File("block-values/worth.yml");
            if (!worthFile.getParentFile().mkdirs() || !blockValuesFile.renameTo(worthFile))
                Log.error("Failed to convert old block values to the new format.");
        }

        // Load level and worth values
        loadValuesFromFile("block-values/worth.yml", (key, worth) -> {
            blockValuesContainer.setBlockValue(key, BlockValue.ofWorth(worth));
        });

        loadValuesFromFile("block-values/levels.yml", (key, level) -> {
            BlockValue currValue = blockValuesContainer.getBlockValue(key);
            BlockValue newValue = currValue == null ? BlockValue.ofLevel(level) : currValue.setLevel(level);
            blockValuesContainer.setBlockValue(key, newValue);
        });
    }

    private void loadValuesFromFile(String fileName, BiConsumer<Key, BigDecimal> consumer) {
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists())
            plugin.saveResource(fileName, true);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection valuesSection = cfg.isConfigurationSection("block-values") ?
                cfg.getConfigurationSection("block-values") :
                cfg.getConfigurationSection("");

        for (String key : valuesSection.getKeys(false)) {
            String value = valuesSection.getString(key);
            try {
                consumer.accept(Keys.ofMaterialAndData(key), new BigDecimal(value));
            } catch (Exception ex) {
                Log.warnFromFile("levels.yml", "Cannot parse value for ", key, " in file ", fileName, ", skipping...");
            }
        }

    }

    private static BigDecimal fastBigDecimalFromString(String value) {
        return CACHED_BIG_DECIMALS.getOrDefault(value, new BigDecimal(value));
    }

}
