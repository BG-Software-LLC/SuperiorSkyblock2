package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.key.Key;
import net.brcdev.shopgui.ShopGuiPlugin;
import net.brcdev.shopgui.shop.Shop;
import net.brcdev.shopgui.shop.ShopItem;
import org.bukkit.Material;

import java.math.BigDecimal;
import java.util.Map;

public final class PricesProvider_ShopGUIPlus implements PricesProvider{

    private final ShopGuiPlugin plugin = ShopGuiPlugin.getInstance();

    @Override
    public BigDecimal getPrice(Key key) {
        double price = 0;

        Material material = Material.matchMaterial(key.getGlobalKey());
        short data = !key.getSubKey().isEmpty() ? Short.parseShort(key.getSubKey()) : 0;

        Map<String, Shop> shops = plugin.getShopManager().shops;
        for(Shop shop : shops.values()){
            for(ShopItem shopItem : shop.getShopItems()){
                if(shopItem.getItem().getType() == material && shopItem.getItem().getDurability() == data){
                    //noinspection deprecation
                    double shopPrice = shopItem.getBuyPriceForAmount(1);
                    if(shopPrice > price)
                        price = shopPrice;
                }
            }
        }

        return BigDecimal.valueOf(price);
    }

}
