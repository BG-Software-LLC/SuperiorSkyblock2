package com.bgsoftware.superiorskyblock.hooks.provider;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMap;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.ShopItem;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public final class PricesProvider_ShopGUIPlus implements PricesProvider{

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final ShopGuiPlugin shopPlugin = ShopGuiPlugin.getInstance();
    private static final KeyMap<Double> cachedPrices = new KeyMap<>();

    public PricesProvider_ShopGUIPlus(){
        SuperiorSkyblockPlugin.log("Using ShopGUIPlus as a prices provider.");
    }

    @Override
    public BigDecimal getPrice(Key key) {
        double price = cachedPrices.getOrDefault(key, 0D);

        if(price == 0) {
            Map<String, Shop> shops = new HashMap<>(shopPlugin.getShopManager().shops);
            for (Shop shop : shops.values()) {
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
    public com.bgsoftware.superiorskyblock.key.Key getBlockKey(com.bgsoftware.superiorskyblock.key.Key blockKey) {
        return cachedPrices.getKey(blockKey, null);
    }

}
