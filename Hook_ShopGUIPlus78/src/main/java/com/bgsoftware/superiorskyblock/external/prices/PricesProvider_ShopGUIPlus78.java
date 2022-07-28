package com.bgsoftware.superiorskyblock.external.prices;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.PricesProvider;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.item.ShopItem;

import java.math.BigDecimal;

public class PricesProvider_ShopGUIPlus78 implements PricesProvider {

    private final ShopGuiPlugin shopPlugin = ShopGuiPlugin.getInstance();
    private final KeyMap<Double> cachedPrices = KeyMap.createConcurrentKeyMap();

    private final SuperiorSkyblockPlugin plugin;

    public PricesProvider_ShopGUIPlus78(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        SuperiorSkyblockPlugin.log("Using ShopGUIPlus as a prices provider.");
    }

    @Override
    public BigDecimal getPrice(Key key) {
        double price = cachedPrices.getOrDefault(key, 0D);

        if (price == 0) {
            for (Shop shop : shopPlugin.getShopManager().getShops()) {
                for (ShopItem shopItem : shop.getShopItems()) {
                    if (Key.of(shopItem.getItem()).equals(key)) {
                        double shopPrice;

                        switch (plugin.getSettings().getSyncWorth()) {
                            case BUY:
                                shopPrice = shopItem.getBuyPriceForAmount(1);
                                break;
                            case SELL:
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

}
