package com.bgsoftware.superiorskyblock.core.key.types;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.MaterialKeySource;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.bukkit.Material;

import java.util.EnumMap;

public class MaterialKey extends BaseKey<MaterialKey> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final EnumMap<Material, MaterialKey> GLOBAL_MATERIAL_KEYS_CACHE = new EnumMap<>(Material.class);
    private static final EnumMap<Material, MaterialKey[]> ID_MATERIAL_KEYS_CACHE = new EnumMap<>(Material.class);

    static {
        // Load all material type keys
        for (Material material : Material.values()) {
            if (material != Materials.SPAWNER.toBukkitType()) {
                MaterialKey globalKey = of(material);
                globalKey.loadLazyCaches();

                int maxDataValue = plugin.getNMSAlgorithms().getMaxBlockDataValue(material) + 1;
                for (int i = 0; i < maxDataValue; ++i) {
                    MaterialKey materialKey = of(material, (short) i, MaterialKeySource.BLOCK);
                    materialKey.loadLazyCaches();
                }
            }
        }
    }

    private final LazyReference<MaterialKey> apiKeyCache = new LazyReference<MaterialKey>() {
        @Override
        protected MaterialKey create() {
            if (MaterialKey.this.isAPIKey())
                throw new UnsupportedOperationException();

            return createAPIKeyForCacheInternal();
        }
    };

    protected final Material type;
    private final short durability;
    private final String durabilityAsString;
    protected final boolean isGlobalType;
    private final MaterialKeySource materialKeySource;

    public static MaterialKey of(Material type, short durability, MaterialKeySource materialKeySource) {
        Preconditions.checkArgument(type != Materials.SPAWNER.toBukkitType());

        if (materialKeySource == MaterialKeySource.BLOCK) {
            MaterialKey[] cachedIds = ID_MATERIAL_KEYS_CACHE.computeIfAbsent(type, MaterialKey::createMaterialKeyCacheForType);
            if (durability < cachedIds.length) {
                return cachedIds[durability];
            }
        }

        return new MaterialKey(type, durability, false, materialKeySource);
    }

    public static MaterialKey of(Material type) {
        return GLOBAL_MATERIAL_KEYS_CACHE.computeIfAbsent(type, unused ->
                new MaterialKey(type, (short) 0, true, MaterialKeySource.BLOCK));
    }

    protected MaterialKey(Material type, short durability, boolean isGlobalType, MaterialKeySource materialKeySource) {
        this(type, durability, isGlobalType, materialKeySource, false);
    }

    protected MaterialKey(Material type, short durability, boolean isGlobalType, MaterialKeySource materialKeySource, boolean apiKey) {
        super(MaterialKey.class, apiKey);
        this.type = Preconditions.checkNotNull(type, "type parameter cannot be null");
        this.durability = durability;
        this.durabilityAsString = isGlobalType ? "" : String.valueOf(this.durability);
        this.isGlobalType = isGlobalType;
        this.materialKeySource = materialKeySource;
    }

    @Override
    public String getGlobalKey() {
        return this.type.name();
    }

    @Override
    public MaterialKey toGlobalKey() {
        return this.isGlobalType ? this : MaterialKey.of(this.type);
    }

    @Override
    public String getSubKey() {
        return this.durabilityAsString;
    }

    public MaterialKeySource getMaterialKeySource() {
        return materialKeySource;
    }

    @Override
    protected String toStringInternal() {
        return this.isGlobalType ? this.getGlobalKey() : this.getGlobalKey() + ":" + this.getSubKey();
    }

    @Override
    protected int hashCodeInternal() {
        return Objects.hashCode(this.type, this.durability);
    }

    @Override
    protected boolean equalsInternal(MaterialKey other) {
        return this.type == other.type && this.durability == other.durability && this.isGlobalType == other.isGlobalType;
    }

    @Override
    protected int compareToInternal(MaterialKey other) {
        return this.toString().compareTo(other.toString());
    }

    @Override
    public MaterialKey createAPIKeyInternal() {
        return this.apiKeyCache.get();
    }

    protected MaterialKey createAPIKeyForCacheInternal() {
        return new MaterialKey(MaterialKey.this.type, MaterialKey.this.durability, MaterialKey.this.isGlobalType,
                MaterialKey.this.materialKeySource, true);
    }

    public Material getMaterial() {
        return this.type;
    }

    public short getDurability() {
        return this.durability;
    }

    private static MaterialKey[] createMaterialKeyCacheForType(Material type) {
        MaterialKey[] cache = new MaterialKey[plugin.getNMSAlgorithms().getMaxBlockDataValue(type) + 1];
        for (int i = 0; i < cache.length; ++i)
            cache[i] = new MaterialKey(type, (short) i, false, MaterialKeySource.BLOCK);
        return cache;
    }

}
