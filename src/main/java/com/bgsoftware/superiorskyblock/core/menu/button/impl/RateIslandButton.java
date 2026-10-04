package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.impl.IslandMenuView;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;

import java.util.Collections;
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
    public void onButtonClick(ButtonClickContext<IslandMenuView> context) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        Island island = menuView.getIsland();
        Rating rating = getTemplate().rating;

        if (rating == Rating.UNKNOWN) {
            if (!PluginEventsFactory.callIslandRemoveRatingEvent(island, inventoryViewer, inventoryViewer))
                return;

            island.removeRating(inventoryViewer);
        } else {
            if (!PluginEventsFactory.callIslandRateEvent(island, inventoryViewer, inventoryViewer, rating))
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
            return new Template(this, rating);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<IslandMenuView> {

        private final Rating rating;

        Template(AbstractBuilder<IslandMenuView> builder, Rating rating) {
            super(builder, RateIslandButton.class, RateIslandButton::new);
            this.rating = Objects.requireNonNull(rating, "rating cannot be null");
        }

    }

}
