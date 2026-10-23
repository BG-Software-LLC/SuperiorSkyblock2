package com.bgsoftware.superiorskyblock.core.key.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.google.common.base.Preconditions;
import org.bukkit.entity.EntityType;

import java.util.EnumMap;
import java.util.Objects;

public class EntityTypeKey extends BaseKey<EntityTypeKey> {

    private static final EnumMap<EntityType, EntityTypeKey> ENTITY_TYPE_KEYS_CACHE = new EnumMap<>(EntityType.class);

    static {
        // Load all entity type keys
        for (EntityType entityType : EntityType.values()) {
            EntityTypeKey entityTypeKey = of(entityType);
            entityTypeKey.loadLazyCaches();
        }
    }

    private final LazyReference<EntityTypeKey> apiKeyCache = new LazyReference<EntityTypeKey>() {
        @Override
        protected EntityTypeKey create() {
            if (EntityTypeKey.this.isAPIKey())
                throw new UnsupportedOperationException();

            return new EntityTypeKey(EntityTypeKey.this.entityType, true);
        }
    };

    private final EntityType entityType;
    @Nullable
    private final String data;
    private final String dataAsString;

    public static EntityTypeKey of(EntityType entityType) {
        return ENTITY_TYPE_KEYS_CACHE.computeIfAbsent(entityType, EntityTypeKey::new);
    }

    public static EntityTypeKey of(EntityType entityType, @Nullable String data) {
        return data == null ? of(entityType) : new EntityTypeKey(entityType, data, false);
    }

    private EntityTypeKey(EntityType entityType) {
        this(entityType, false);
    }

    private EntityTypeKey(EntityType entityType, boolean apiKey) {
        this(entityType, null, apiKey);
    }

    private EntityTypeKey(EntityType entityType, @Nullable String data, boolean apiKey) {
        super(EntityTypeKey.class, apiKey);
        this.entityType = Preconditions.checkNotNull(entityType, "entityType cannot be null");
        this.data = data;
        this.dataAsString = this.data == null ? "" : this.data;
    }

    @Override
    public String getGlobalKey() {
        return this.entityType.name();
    }

    @Override
    public EntityTypeKey toGlobalKey() {
        return this.data == null ? this : EntityTypeKey.of(this.entityType);
    }

    @Override
    public String getSubKey() {
        return this.dataAsString;
    }

    @Override
    protected int compareToInternal(EntityTypeKey other) {
        int compare = Integer.compare(this.entityType.ordinal(), other.entityType.ordinal());
        if (compare != 0) return compare;
        return this.data == null ? other.data == null ? 0 : 1 : this.data.compareTo(other.data);
    }

    @Override
    protected String toStringInternal() {
        return this.data == null ? this.getGlobalKey() : this.getGlobalKey() + ":" + this.getSubKey();
    }

    @Override
    protected int hashCodeInternal() {
        return Objects.hash(this.entityType, this.data);
    }

    @Override
    protected boolean equalsInternal(EntityTypeKey other) {
        return this.entityType == other.entityType && Objects.equals(this.data, other.data);
    }

    @Override
    public EntityTypeKey createAPIKeyInternal() {
        return this.apiKeyCache.get();
    }

    public EntityType getEntityType() {
        return entityType;
    }
}
