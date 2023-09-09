package com.bgsoftware.superiorskyblock.core.key.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.google.common.base.Objects;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

public class SpawnerKey extends MaterialKey {

    private static final Material SPAWNER_TYPE = Materials.SPAWNER.toBukkitType();

    private static final KeyMap<SpawnerKey> SPAWNER_KEYS_CACHE = KeyMaps.createIdentityHashMap(KeyIndicator.ENTITY_TYPE);
    public static final SpawnerKey GLOBAL_KEY = new SpawnerKey(null);

    static {
        // Load all spawner type keys
        for (EntityType entityType : EntityType.values()) {
            SpawnerKey spawnerKey = of(Keys.of(entityType));
            spawnerKey.loadLazyCaches();
        }
    }

    private final Key spawnerTypeKey;

    public static SpawnerKey of(@Nullable Key spawnerTypeKey) {
        return spawnerTypeKey == null ? GLOBAL_KEY : SPAWNER_KEYS_CACHE.computeIfAbsent(spawnerTypeKey, SpawnerKey::new);
    }

    private SpawnerKey(@Nullable Key spawnerTypeKey) {
        super(SPAWNER_TYPE, (short) 0, spawnerTypeKey == null);
        this.spawnerTypeKey = spawnerTypeKey;
    }

    @Override
    public SpawnerKey toGlobalKey() {
        return GLOBAL_KEY;
    }

    @Override
    public String getSubKey() {
        return this.isGlobalType ? "" : getSubKeyInternal();
    }

    private String getSubKeyInternal() {
        assert this.spawnerTypeKey != null;
        return this.spawnerTypeKey.toString();
    }

    @Override
    protected String toStringInternal() {
        return this.isGlobalType ? this.getGlobalKey() : this.getGlobalKey() + ":" + this.getSubKeyInternal();
    }

    @Override
    protected int hashCodeInternal() {
        return Objects.hashCode(SPAWNER_TYPE, this.spawnerTypeKey);
    }

    @Override
    protected boolean equalsInternal(MaterialKey other) {
        if (other instanceof SpawnerKey) {
            return Objects.equal(this.spawnerTypeKey, ((SpawnerKey) other).spawnerTypeKey);
        }

        return this.isGlobalType && other.isGlobalType && other.type == SPAWNER_TYPE;
    }

}
