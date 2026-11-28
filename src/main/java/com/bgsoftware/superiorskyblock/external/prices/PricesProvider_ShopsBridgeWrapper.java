package com.bgsoftware.superiorskyblock.external.prices;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.shopsbridge.IShopsBridge;
import com.bgsoftware.common.shopsbridge.ShopsProvider;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.PricesProvider;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class PricesProvider_ShopsBridgeWrapper implements PricesProvider {

    private final KeyMap<BigDecimal> cachedPrices = KeyMap.createConcurrentKeyMap();
    private final Function<Key, BigDecimal> priceLoader = this::getPriceFromShopsBridge;

    private final SuperiorSkyblockPlugin plugin;
    private final IShopsBridge shopsBridge;

    public PricesProvider_ShopsBridgeWrapper(SuperiorSkyblockPlugin plugin, ShopsProvider shopsProvider, IShopsBridge shopsBridge) {
        Log.info("Using " + shopsProvider.getPluginName() + " as a prices provider.");
        this.plugin = plugin;
        this.shopsBridge = shopsBridge;
    }

    @Override
    public BigDecimal getPrice(Key key) {
        try {
            return this.cachedPrices.computeIfAbsent(key, this.priceLoader);
        } catch (Throwable error) {
            Log.error(error, "Failed to load prices for item " + key);
            return BigDecimal.ZERO;
        }
    }

    @Nullable
    @Override
    public Key getBlockKey(Key blockKey) {
        return this.cachedPrices.getKey(blockKey, null);
    }

    @Override
    public CompletableFuture<Void> getWhenPricesAreReady() {
        return this.shopsBridge.getWhenShopsLoaded();
    }

    private BigDecimal getPriceFromShopsBridge(Key key) {
        Material material = EnumHelper.getEnum(Material.class, key.getGlobalKey().toUpperCase(Locale.ENGLISH));
        if (material == null)
            return BigDecimal.ZERO;

        String subKey = key.getSubKey();
        ItemStack itemStack;

        try {
            if (material == Materials.SPAWNER.toBukkitType()) {
                EntityType entityType = subKey == null ? null :
                        EnumHelper.getEnum(EntityType.class, subKey.toUpperCase(Locale.ENGLISH));
                if (entityType == null)
                    return BigDecimal.ZERO;

                itemStack = new ItemBuilder(material).withEntityType(entityType).build();
            } else {
                // All the short values are valid durabilities, so the sub-key is parsed as an int and checked
                // to be a short. This also catches sub-keys that are not numbers, as the default is not a short.
                int durability = Text.parseInt(subKey, Integer.MIN_VALUE);
                if (durability < Short.MIN_VALUE || durability > Short.MAX_VALUE)
                    return BigDecimal.ZERO;

                itemStack = new ItemStack(material, 1, (short) durability);
            }
        } catch (Throwable error) {
            return BigDecimal.ZERO;
        }

        switch (plugin.getSettings().getSyncWorth()) {
            case BUY:
                return this.shopsBridge.getBuyPrice(itemStack).getPrice();
            case SELL:
                return this.shopsBridge.getSellPrice(itemStack).getPrice();
            default:
                return BigDecimal.ZERO;
        }
    }

}
