package com.bgsoftware.superiorskyblock.core.key.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.key.BaseKey;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.Locale;
import java.util.Objects;

public class CustomKey extends BaseKey<CustomKey> {

    private final LazyReference<CustomKey> apiKeyCache = new LazyReference<CustomKey>() {
        @Override
        protected CustomKey create() {
            if (CustomKey.this.isAPIKey())
                throw new UnsupportedOperationException();

            return new CustomKey(CustomKey.this.globalKey, CustomKey.this.subKey, true);
        }
    };

    private final String globalKey;
    private final String subKey;

    public static CustomKey of(String globalKey, @Nullable String subKey, KeyIndicator keyType) {
        switch (keyType) {
            case ENTITY_TYPE:
                Preconditions.checkArgument(EnumHelper.getEnum(EntityType.class, globalKey) == null,
                        "CustomKey received a valid EntityType type: " + globalKey);
                break;
            case MATERIAL:
                Preconditions.checkArgument(EnumHelper.getEnum(Material.class, globalKey) == null,
                        "CustomKey received a valid Material type: " + globalKey);
                break;
        }

        return new CustomKey(globalKey, subKey, false);
    }

    private CustomKey(String globalKey, @Nullable String subKey, boolean apiKey) {
        super(CustomKey.class, apiKey);
        this.globalKey = Preconditions.checkNotNull(globalKey, "globalKey cannot be null").toUpperCase(Locale.ENGLISH);
        this.subKey = Text.isBlank(subKey) ? null : subKey.toUpperCase(Locale.ENGLISH);
    }

    @Override
    public String getGlobalKey() {
        return this.globalKey;
    }

    @Override
    public CustomKey toGlobalKey() {
        return this.subKey == null ? this : CustomKey.of(this.globalKey, null, KeyIndicator.CUSTOM);
    }

    @Override
    public String getSubKey() {
        return this.subKey;
    }

    @Override
    protected String toStringInternal() {
        return this.subKey == null ? this.globalKey : this.globalKey + ":" + this.subKey;
    }

    @Override
    protected int hashCodeInternal() {
        return Objects.hash(this.globalKey, this.subKey);
    }

    @Override
    protected boolean equalsInternal(CustomKey other) {
        return this.globalKey.equals(other.globalKey) && Objects.equals(this.subKey, other.subKey);
    }

    @Override
    protected int compareToInternal(CustomKey other) {
        return toString().compareTo(other.toString());
    }

    @Override
    public CustomKey createAPIKeyInternal() {
        return this.apiKeyCache.get();
    }

}
