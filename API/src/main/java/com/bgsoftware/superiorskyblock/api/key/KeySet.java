package com.bgsoftware.superiorskyblock.api.key;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * {@link Set} implementation for handling keys.
 * The difference between this set and a regular {@link Set} is that this set handles checks for
 * global keys as well as individual ones.
 * <p>
 * For example, if this set has "STONE" as a key inside it, the {@link #contains(Object)} method will return true
 * if "STONE" is provided, as well as any other key with a different sub-key ("STONE:0", for example).
 * <p>
 * However, if this set only has "STONE:0" as a key inside it, the {@link #contains(Object)} method will only return
 * true for the exact same keys (Therefore, "STONE" will return false alongside of any other key with a different
 * sub-key)
 */
public interface KeySet extends Set<Key> {

    /**
     * Create a new empty {@link KeySet} instance.
     *
     * @param setCreator The set creator for the inner-set of the new {@link KeySet}
     */
    static KeySet createKeySet(Supplier<Set<String>> setCreator) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().createKeySet(setCreator);
    }

    /**
     * Create a new empty {@link KeySet} instance based on {@link HashSet}
     */
    static KeySet createKeySet() {
        return createKeySet(HashSet::new);
    }

    /**
     * Create a new {@link KeySet} instance from the given collection based on {@link HashSet}.
     * If the provided collection is also a {@link KeySet}, the exact same instance of that set is returned.
     * Otherwise, the returned {@link KeySet} is a copy of that collection.
     *
     * @param collection The collection to create {@link KeySet} from.
     */
    static KeySet createKeySet(Collection<Key> collection) {
        return SuperiorSkyblockAPI.getSuperiorSkyblock().getKeys().createKeySet(HashSet::new, collection);
    }

    /**
     * Get the key that is similar to the provided key.
     * For example, if {@param original} is "STONE:0", and the set contains only "STONE", "STONE" will be returned.
     * However, if the set contains "STONE" as well as "STONE:0", "STONE:0" will be returned.
     * If the key is not inside the map, null will be returned.
     *
     * @param original The original key.
     */
    @Nullable
    Key getKey(Key original);

    /**
     * Get the key that is similar to the provided key.
     * For example, if {@param original} is "STONE:0", and the set contains only "STONE", "STONE" will be returned.
     * However, if the set contains "STONE" as well as "STONE:0", "STONE:0" will be returned.
     * If the key is not inside the map, {@param def} will be returned.
     *
     * @param original The original key.
     * @param def      Default key to be returned if {@param original} is not in the map.
     */
    Key getKey(Key original, Key def);

    /**
     * Return a regular {@link java.util.HashSet} with the keys of this set.
     */
    Set<Key> asSet();

}
