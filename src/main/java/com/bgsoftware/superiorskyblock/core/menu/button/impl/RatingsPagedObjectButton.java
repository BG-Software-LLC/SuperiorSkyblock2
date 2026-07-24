package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandRatings;
import org.bukkit.inventory.ItemStack;

public class RatingsPagedObjectButton extends AbstractPagedMenuButton<MenuIslandRatings.View, MenuIslandRatings.RatingInfo> {

    private RatingsPagedObjectButton(MenuTemplateButton<MenuIslandRatings.View> templateButton, MenuIslandRatings.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public ItemStack modifyViewItem(ItemBuilder itemBuilder) {
        SuperiorPlayer ratingPlayer = plugin.getPlayers().getSuperiorPlayer(pagedObject.getPlayerUUID());

        return itemBuilder
                .replaceAll("{0}", ratingPlayer.getName())
                .replaceAll("{1}", Formatters.RATING_FORMATTER.format(pagedObject.getRating().getValue(), ratingPlayer.getUserLocale()))
                .asSkullOf(ratingPlayer)
                .build(ratingPlayer);
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuIslandRatings.View, MenuIslandRatings.RatingInfo> {

        @Override
        public PagedMenuTemplateButton<MenuIslandRatings.View, MenuIslandRatings.RatingInfo> build() {
            return new PagedMenuTemplateButtonImpl<>(this, RatingsPagedObjectButton.class,
                    RatingsPagedObjectButton::new);
        }

    }

}
