package com.bgsoftware.superiorskyblock.core.zmenu.buttons.top;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.TopIslandMembersSorting;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.zmenu.PlayerCache;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import fr.maxlego08.menu.MenuItemStack;
import fr.maxlego08.menu.api.utils.MetaUpdater;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import fr.maxlego08.menu.zcore.utils.inventory.Pagination;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class IslandTopButton extends SuperiorButton {

    private final MenuItemStack menuItemStackIsland;
    private final MenuItemStack menuItemStackNoIsland;
    private final List<Integer> positions;

    public IslandTopButton(SuperiorSkyblockPlugin plugin, MenuItemStack menuItemStackIsland, MenuItemStack menuItemStackNoIsland, List<Integer> positions) {
        super(plugin);
        this.menuItemStackIsland = menuItemStackIsland;
        this.menuItemStackNoIsland = menuItemStackNoIsland;
        this.positions = positions;
    }

    @Override
    public boolean hasSpecialRender() {
        return true;
    }

    @Override
    public void onRender(Player player, InventoryDefault inventory) {

        if (this.slots.size() != this.positions.size()) {
            plugin.getLogger().severe("You must have the number of slots equal to the number of positions for the top-islands.yml inventory!");
            return;
        }

        PlayerCache playerCache = getCache(player);
        List<Island> islands = plugin.getGrid().getIslands(playerCache.getSortingType());
        Pagination<Island> islandPagination = new Pagination<>();
        islands = islandPagination.paginate(islands, this.slots.size(), inventory.getPage());

        for (int i = 0; i != this.slots.size(); i++) {
            int slot = this.slots.get(i);
            int position = this.positions.get(i) + ((inventory.getPage() - 1) * this.positions.size());
            Island island = i < islands.size() ? islands.get(i) : null;

            onRenderIsland(player, inventory, position, island, slot);
        }
    }

    private void onRenderIsland(Player player, InventoryDefault inventory, int position, Island island, int slot) {

        SuperiorPlayer inventoryViewer = getSuperiorPlayer(player);
        MetaUpdater updater = this.plugin.getZMenumanager().getInventoryManager().getMeta();

        Placeholders placeholders = new Placeholders();
        placeholders.register("island-position", String.valueOf(position));

        if (island == null) {
            inventory.addItem(slot, this.menuItemStackNoIsland.build(player, false, placeholders));
            return;
        }

        SuperiorPlayer islandOwner = island.getOwner();
        String islandName = !plugin.getSettings().getIslandNames().isIslandTop() || island.getName().isEmpty() ? islandOwner.getName() : plugin.getSettings().getIslandNames().isColorSupport() ? Formatters.COLOR_FORMATTER.format(island.getName()) : island.getName();
        placeholders.register("island-name", islandName);
        placeholders.register("island-level", Formatters.NUMBER_FORMATTER.format(island.getIslandLevel()));
        placeholders.register("island-worth", Formatters.NUMBER_FORMATTER.format(island.getWorth()));
        placeholders.register("island-level-formatted", Formatters.FANCY_NUMBER_FORMATTER.format(island.getIslandLevel(), inventoryViewer.getUserLocale()));
        placeholders.register("island-worth-formatted", Formatters.FANCY_NUMBER_FORMATTER.format(island.getWorth(), inventoryViewer.getUserLocale()));
        placeholders.register("island-total-rating-formatted", Formatters.NUMBER_FORMATTER.format(island.getTotalRating()));
        placeholders.register("island-total-rating", Formatters.RATING_FORMATTER.format(island.getTotalRating(), inventoryViewer.getUserLocale()));
        placeholders.register("island-rating-amount", Formatters.NUMBER_FORMATTER.format(island.getRatingAmount()));
        placeholders.register("island-players", Formatters.NUMBER_FORMATTER.format(island.getAllPlayersInside().size()));

        ItemStack itemStack = this.menuItemStackIsland.build(player, false, placeholders);
        ItemMeta itemMeta = itemStack.getItemMeta();

        List<String> lore = new ArrayList<>();
        for (String line : this.menuItemStackIsland.getLore()) {
            if (line.contains("%members%")) {
                List<SuperiorPlayer> members = new LinkedList<>(island.getIslandMembers(plugin.getSettings().isIslandTopIncludeLeader()));
                String memberFormat = line.replace("%members%", "");
                if (members.size() == 0) {
                    lore.add(memberFormat.replace("%member-name%", "None"));
                } else {
                    if (plugin.getSettings().getTopIslandMembersSorting() != TopIslandMembersSorting.NAMES) {
                        members.sort(plugin.getSettings().getTopIslandMembersSorting().getComparator());
                    }

                    members.forEach(member -> {
                        String onlineMessage = member.isOnline() ? Message.ISLAND_TOP_STATUS_ONLINE.getMessage(inventoryViewer.getUserLocale()) : Message.ISLAND_TOP_STATUS_OFFLINE.getMessage(inventoryViewer.getUserLocale());
                        Placeholders memberPlaceholders = new Placeholders();
                        memberPlaceholders.register("member-name", member.getName());
                        memberPlaceholders.register("online", onlineMessage);
                        memberPlaceholders.register("role", member.getPlayerRole().getDisplayName());

                        lore.add(memberPlaceholders.parse(memberFormat));
                    });
                }
            } else lore.add(line);
        }
        updater.updateLore(itemMeta, lore.stream().map(placeholders::parse).collect(Collectors.toList()), player);

        itemStack.setItemMeta(itemMeta);
        itemStack = ItemSkulls.getPlayerHead(itemStack, islandOwner.getTextureValue());
        inventory.addItem(slot, itemStack).setClick(event -> {

        });
    }
}
