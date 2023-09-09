package com.bgsoftware.superiorskyblock.external.prices;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.shopsbridge.IShopsBridge;
import com.bgsoftware.common.shopsbridge.ShopsProvider;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.PricesProvider;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class PricesProvider_ShopsBridgeWrapper implements PricesProvider {

    private final KeyMap<BigDecimal> cachedPrices = KeyMap.createConcurrentKeyMap();
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
            return this.cachedPrices.computeIfAbsent(key, this::getPriceFromShopsBridge);
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
        ItemStack itemStack;

        try {
            Material material = Material.valueOf(key.getGlobalKey().toUpperCase(Locale.ENGLISH));
            if (material == Materials.SPAWNER.toBukkitType()) {
                EntityType entityType = EntityType.valueOf(key.getSubKey().toUpperCase(Locale.ENGLISH));
                itemStack = new ItemBuilder(material).withEntityType(entityType).build();
            } else {
                short durability = Short.parseShort(key.getSubKey());
                itemStack = new ItemStack(material, 1, durability);
            }
        } catch (Throwable error) {
            return BigDecimal.ZERO;
        }

        switch (plugin.getSettings().getSyncWorth()) {
            case BUY:
                return this.shopsBridge.getBuyPrice(itemStack);
            case SELL:
                return this.shopsBridge.getSellPrice(itemStack);
            default:
                return BigDecimal.ZERO;
        }
    }

}
