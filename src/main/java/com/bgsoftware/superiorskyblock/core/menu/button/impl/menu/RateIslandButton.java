package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandRate;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RateIslandButton extends SuperiorMenuButton<MenuIslandRate> {

    private final Rating rating;
    private final boolean removingRatingButton;

    private RateIslandButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                             String requiredPermission, GameSound lackPermissionSound, Rating rating) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.rating = rating;
        this.removingRatingButton = rating == Rating.UNKNOWN;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandRate superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island island = superiorMenu.getTargetIsland();

        if (removingRatingButton) {
            if (!plugin.getEventsBus().callIslandRemoveRatingEvent(clickedPlayer, clickedPlayer, island))
                return;

            island.removeRating(clickedPlayer);
        } else {
            if (!plugin.getEventsBus().callIslandRateEvent(clickedPlayer, clickedPlayer, island, rating))
                return;

            island.setRating(clickedPlayer, rating);
        }

        Message.RATE_SUCCESS.send(clickedPlayer, rating.getValue());

        IslandUtils.sendMessage(island, Message.RATE_ANNOUNCEMENT, Collections.emptyList(),
                clickedPlayer.getName(), rating.getValue());

        BukkitExecutor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, RateIslandButton, MenuIslandRate> {

        private Rating rating;

        public Builder setRating(Rating rating) {
            this.rating = rating;
            return this;
        }

        @Override
        public RateIslandButton build() {
            return new RateIslandButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, rating);
        }

    }

}
