package com.bgsoftware.superiorskyblock.core.key.types;

import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.google.common.base.Preconditions;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;

public class EntityTypeKey extends BaseKey<EntityTypeKey> {

    private static final EnumMap<EntityType, EntityTypeKey> ENTITY_TYPE_KEYS_CACHE = new EnumMap<>(EntityType.class);

    static {
        // Load all entity type keys
        for (EntityType entityType : EntityType.values()) {
            EntityTypeKey entityTypeKey = of(entityType);
            entityTypeKey.loadLazyCaches();
        }
    }

    private final EntityType entityType;

    public static EntityTypeKey of(EntityType entityType) {
        return ENTITY_TYPE_KEYS_CACHE.computeIfAbsent(entityType, EntityTypeKey::new);
    }

    private EntityTypeKey(EntityType entityType) {
        super(EntityTypeKey.class);
        this.entityType = Preconditions.checkNotNull(entityType, "entityType cannot be null");
    }

    @Override
    public String getGlobalKey() {
        return this.entityType.name();
    }

    @Override
    public EntityTypeKey toGlobalKey() {
        return this;
    }

    @Override
    public String getSubKey() {
        return "";
    }

    @Override
    protected int compareToInternal(EntityTypeKey other) {
        return Integer.compare(this.entityType.ordinal(), other.entityType.ordinal());
    }

    @Override
    protected String toStringInternal() {
        return this.getGlobalKey();
    }

    @Override
    protected int hashCodeInternal() {
        return this.entityType.hashCode();
    }

    @Override
    protected boolean equalsInternal(EntityTypeKey other) {
        return this.entityType == other.entityType;
    }

}
