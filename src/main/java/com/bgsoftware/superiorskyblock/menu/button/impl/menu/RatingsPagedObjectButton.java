package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandRatings;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class RatingsPagedObjectButton extends PagedObjectButton<MenuIslandRatings.RatingInfo> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private RatingsPagedObjectButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                     String requiredPermission, SoundWrapper lackPermissionSound,
                                     ItemBuilder nullItem) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, SuperiorMenu superiorMenu, InventoryClickEvent clickEvent) {
        Preconditions.checkArgument(superiorMenu instanceof MenuIslandRatings, "superiorMenu must be MenuIslandRatings");
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, SuperiorPlayer inventoryViewer,
                                      SuperiorPlayer targetPlayer, MenuIslandRatings.RatingInfo ratingInfo) {
        SuperiorPlayer ratingPlayer = plugin.getPlayers().getSuperiorPlayer(ratingInfo.getPlayerUUID());

        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", ratingPlayer.getName())
                .replaceAll("{1}", StringUtils.formatRating(ratingPlayer.getUserLocale(), ratingInfo.getRating().getValue()))
                .asSkullOf(ratingPlayer)
                .build(ratingPlayer);
    }

    public static class Builder extends PagedObjectBuilder<Builder, RatingsPagedObjectButton> {

        @Override
        public RatingsPagedObjectButton build() {
            return new RatingsPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem);
        }

    }

}
