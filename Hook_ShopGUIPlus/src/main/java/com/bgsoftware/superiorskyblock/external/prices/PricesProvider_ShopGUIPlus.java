package com.bgsoftware.superiorskyblock.external.prices;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.PricesProvider;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.ShopItem;
import net.brcdev.shopgui.shop.ShopManager;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Set;

public class PricesProvider_ShopGUIPlus implements PricesProvider {

    private static final ReflectMethod<Set<Shop>> GET_SHOPS_METHOD = new ReflectMethod<>(ShopManager.class, Set.class, "getShops");

    private final ShopGuiPlugin shopPlugin = ShopGuiPlugin.getInstance();
    private final KeyMap<Double> cachedPrices = KeyMap.createConcurrentKeyMap();

    private final SuperiorSkyblockPlugin plugin;

    public PricesProvider_ShopGUIPlus(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        SuperiorSkyblockPlugin.log("Using ShopGUIPlus as a prices provider.");
    }

    @Override
    public BigDecimal getPrice(Key key) {
        double price = cachedPrices.getOrDefault(key, 0D);

        if (price == 0) {
            for (Shop shop : getShops()) {
                for (ShopItem shopItem : shop.getShopItems()) {
                    if (Key.of(shopItem.getItem()).equals(key)) {
                        double shopPrice;

                        switch (plugin.getSettings().getSyncWorth()) {
                            case BUY:
                                //noinspection deprecation
                                shopPrice = shopItem.getBuyPriceForAmount(1);
                                break;
                            case SELL:
                                //noinspection deprecation
                                shopPrice = shopItem.getSellPriceForAmount(1);
                                break;
                            default:
                                shopPrice = 0;
                                break;
                        }

                        if (shopPrice > price) {
                            price = shopPrice;
                            cachedPrices.put(key, price);
                        }
                    }
                }
            }
        }

        return BigDecimal.valueOf(price);
    }

    @Override
    public Key getBlockKey(Key blockKey) {
        return cachedPrices.getKey(blockKey, null);
    }

    private Collection<Shop> getShops() {
        return GET_SHOPS_METHOD.isValid() ? GET_SHOPS_METHOD.invoke(shopPlugin.getShopManager()) :
                shopPlugin.getShopManager().shops.values();
    }

}
