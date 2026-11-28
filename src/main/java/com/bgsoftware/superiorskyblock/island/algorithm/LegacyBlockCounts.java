package com.bgsoftware.superiorskyblock.island.algorithm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.values.BlockValue;

import java.math.BigInteger;
import java.util.Map;

/**
 * Converts legacy saved block counts (the aggregated map, stored as a JSON array) to individual counts.
 * <p>
 * The conversion is exact only if block values and limits are unchanged since the row was saved,
 * as the aggregate part that was added for specific keys is derived from the current configuration.
 */
public class LegacyBlockCounts {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private LegacyBlockCounts() {
    }

    public static KeyMap<BigInteger> toIndividualCounts(Map<Key, BigInteger> legacyCounts, Island island) {
        KeyMap<BigInteger> individual = KeyMaps.createHashMap(KeyIndicator.MATERIAL);
        KeyMap<BigInteger> aggregated = KeyMaps.createHashMap(KeyIndicator.MATERIAL);

        // 1) Specific keys are always exact placements (only the value key can be specific).
        legacyCounts.forEach((key, count) -> {
            Key globalKey = ((BaseKey<?>) key).toGlobalKey();
            if (globalKey.equals(key))
                return;

            individual.put(key, individual.getRaw(key, BigInteger.ZERO).add(count));

            boolean limitAggregate = !island.getBlockLimitKey(key).equals(key);
            boolean valueAggregate = plugin.getBlockValues().getBlockValue(globalKey) != BlockValue.ZERO;

            if (limitAggregate || valueAggregate)
                aggregated.put(globalKey, aggregated.getRaw(globalKey, BigInteger.ZERO).add(count));
        });

        // 2) Global keys: strip the aggregate part added by addCounts for the specific keys.
        legacyCounts.forEach((key, count) -> {
            if (!((BaseKey<?>) key).toGlobalKey().equals(key))
                return;

            BigInteger remaining = count.subtract(aggregated.getRaw(key, BigInteger.ZERO));
            if (remaining.signum() > 0)
                individual.put(key, individual.getRaw(key, BigInteger.ZERO).add(remaining));
        });

        return individual;
    }

}
