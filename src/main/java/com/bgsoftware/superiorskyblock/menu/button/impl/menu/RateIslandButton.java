package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandRate;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public final class RateIslandButton extends SuperiorMenuButton<MenuIslandRate> {

    private final Rating rating;

    private RateIslandButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                             String requiredPermission, SoundWrapper lackPermissionSound, Rating rating) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.rating = rating;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandRate superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island island = superiorMenu.getTargetIsland();

        island.setRating(clickedPlayer, rating);

        Message.RATE_SUCCESS.send(clickedPlayer, rating.getValue());

        IslandUtils.sendMessage(island, Message.RATE_ANNOUNCEMENT, new ArrayList<>(),
                clickedPlayer.getName(), rating.getValue());

        Executor.sync(superiorMenu::closePage, 1L);
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
