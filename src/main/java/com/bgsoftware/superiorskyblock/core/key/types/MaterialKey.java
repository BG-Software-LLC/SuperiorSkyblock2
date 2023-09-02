package com.bgsoftware.superiorskyblock.core.key.types;

import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.bukkit.Material;

import java.util.EnumMap;

public class MaterialKey extends BaseKey<MaterialKey> {

    private static final EnumMap<Material, MaterialKey> GLOBAL_MATERIAL_KEYS_CACHE = new EnumMap<>(Material.class);
    private static final EnumMap<Material, MaterialKey> ID_0_MATERIAL_KEYS_CACHE = new EnumMap<>(Material.class);

    static {
        // Load all material type keys
        for (Material material : Material.values()) {
            if (material != Materials.SPAWNER.toBukkitType()) {
                MaterialKey globalKey = of(material);
                globalKey.loadLazyCaches();
                MaterialKey id0Key = of(material, (short) 0);
                id0Key.loadLazyCaches();
            }
        }
    }

    protected final Material type;
    private final short durability;
    private final String durabilityAsString;
    protected final boolean isGlobalType;

    public static MaterialKey of(Material type, short durability) {
        Preconditions.checkArgument(type != Materials.SPAWNER.toBukkitType());

        if (durability == 0)
            return ID_0_MATERIAL_KEYS_CACHE.computeIfAbsent(type, unused -> new MaterialKey(type, (short) 0, false));

        return new MaterialKey(type, durability, false);
    }

    public static MaterialKey of(Material type) {
        return GLOBAL_MATERIAL_KEYS_CACHE.computeIfAbsent(type, unused -> new MaterialKey(type, (short) 0, true));
    }

    protected MaterialKey(Material type, short durability, boolean isGlobalType) {
        super(MaterialKey.class);
        this.type = Preconditions.checkNotNull(type, "type parameter cannot be null");
        this.durability = durability;
        this.durabilityAsString = isGlobalType ? "" : String.valueOf(this.durability);
        this.isGlobalType = isGlobalType;
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

    public Material getMaterial() {
        return this.type;
    }

}
