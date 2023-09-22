package com.bgsoftware.superiorskyblock.island.warp;

import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import org.bukkit.Material;

public class WarpIcons {

    public static final TemplateItem DEFAULT_WARP_CATEGORY_ICON = new TemplateItem(new ItemBuilder(Material.BOOK)
            .withName("&6{0}"));
    public static TemplateItem DEFAULT_WARP_ICON;

    private WarpIcons() {

    }

}
