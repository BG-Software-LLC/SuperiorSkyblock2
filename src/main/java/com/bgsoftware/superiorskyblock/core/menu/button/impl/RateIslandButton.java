package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.IslandMenuView;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RateIslandButton extends AbstractMenuViewButton<IslandMenuView> {

    private RateIslandButton(AbstractMenuTemplateButton<IslandMenuView> templateButton, IslandMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        Island island = menuView.getIsland();
        Rating rating = getTemplate().rating;

        if (rating == Rating.UNKNOWN) {
            if (!plugin.getEventsBus().callIslandRemoveRatingEvent(inventoryViewer, inventoryViewer, island))
                return;

            island.removeRating(inventoryViewer);
        } else {
            if (!plugin.getEventsBus().callIslandRateEvent(inventoryViewer, inventoryViewer, island, rating))
                return;

            island.setRating(inventoryViewer, rating);
        }

        Message.RATE_SUCCESS.send(inventoryViewer, rating.getValue());

        IslandUtils.sendMessage(island, Message.RATE_ANNOUNCEMENT, Collections.emptyList(),
                inventoryViewer.getName(), rating.getValue());

        BukkitExecutor.sync(menuView::closeView, 1L);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<IslandMenuView> {

        private Rating rating;

        public Builder setRating(Rating rating) {
            this.rating = rating;
            return this;
        }

        @Override
        public MenuTemplateButton<IslandMenuView> build() {
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, rating);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<IslandMenuView> {

        private final Rating rating;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, Rating rating) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    RateIslandButton.class, RateIslandButton::new);
            this.rating = Objects.requireNonNull(rating, "rating cannot be null");
        }

    }

}
