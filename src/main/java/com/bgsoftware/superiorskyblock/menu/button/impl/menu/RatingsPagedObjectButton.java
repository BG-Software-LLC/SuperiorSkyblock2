package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.formatting.Formatters;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandRatings;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class RatingsPagedObjectButton extends PagedObjectButton<MenuIslandRatings, MenuIslandRatings.RatingInfo> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private RatingsPagedObjectButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                                     String requiredPermission, SoundWrapper lackPermissionSound,
                                     TemplateItem nullItem, int objectIndex) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem, objectIndex);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandRatings superiorMenu, InventoryClickEvent clickEvent) {
        // Dummy button
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuIslandRatings superiorMenu,
                                      MenuIslandRatings.RatingInfo ratingInfo) {
        SuperiorPlayer ratingPlayer = plugin.getPlayers().getSuperiorPlayer(ratingInfo.getPlayerUUID());

        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", ratingPlayer.getName())
                .replaceAll("{1}", Formatters.RATING_FORMATTER.format(ratingInfo.getRating().getValue(), ratingPlayer.getUserLocale()))
                .asSkullOf(ratingPlayer)
                .build(ratingPlayer);
    }

    public static class Builder extends PagedObjectBuilder<Builder, RatingsPagedObjectButton, MenuIslandRatings> {

        @Override
        public RatingsPagedObjectButton build() {
            return new RatingsPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getObjectIndex());
        }

    }

}
