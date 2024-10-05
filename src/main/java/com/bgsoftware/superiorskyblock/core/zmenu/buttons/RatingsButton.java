package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import fr.maxlego08.menu.api.button.PaginateButton;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class RatingsButton extends SuperiorButton implements PaginateButton {

    private final Function<Map.Entry<UUID, Rating>, RatingInfo> RATING_INFO_MAPPER = entry -> new RatingInfo(entry.getKey(), entry.getValue());

    public RatingsButton(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public boolean hasPermission() {
        return true;
    }

    @Override
    public boolean checkPermission(Player player, InventoryDefault inventory, Placeholders placeholders) {
        return getPaginationSize(player) != 0;
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {

        Pagination<RatingInfo> pagination = new Pagination<>();
        List<RatingInfo> blockCounts = pagination.paginate(requestObjects(player), this.slots.size(), inventory.getPage());

        for (int i = 0; i != Math.min(blockCounts.size(), this.slots.size()); i++) {

            int slot = slots.get(i);
            RatingInfo ratingInfo = blockCounts.get(i);

            SuperiorPlayer ratingPlayer = plugin.getPlayers().getSuperiorPlayer(ratingInfo.getPlayerUUID());
            Placeholders placeholders = new Placeholders();
            placeholders.register("player", ratingPlayer.getName());
            placeholders.register("rating", Formatters.RATING_FORMATTER.format(ratingInfo.getRating().getValue(), ratingPlayer.getUserLocale()));

            inventory.addItem(slot, getItemStack().build(player, false, placeholders));
        }
    }

    @Override
    public int getPaginationSize(Player player) {
        return requestObjects(player).size();
    }

    protected List<RatingInfo> requestObjects(Player player) {
        return new SequentialListBuilder<RatingInfo>().build(getCache(player).getIsland().getRatings().entrySet(), RATING_INFO_MAPPER);
    }

    public static class RatingInfo {

        private final UUID playerUUID;
        private final Rating rating;

        public RatingInfo(UUID playerUUID, Rating rating) {
            this.playerUUID = playerUUID;
            this.rating = rating;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

        public Rating getRating() {
            return rating;
        }

    }
}
