package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.TopIslandMembersSorting;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuGlobalWarps;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuTopIslands;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TopIslandsPagedObjectButton extends PagedObjectButton<MenuTopIslands, Island> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final TemplateItem islandItem;
    private final GameSound islandSound;
    private final GameSound noIslandSound;
    private final List<String> islandCommands;
    private final List<String> noIslandCommands;
    private final boolean isSelfPlayerIsland;

    private TopIslandsPagedObjectButton(String requiredPermission, GameSound lackPermissionSound,
                                        TemplateItem islandItem, GameSound islandSound, List<String> islandCommands,
                                        TemplateItem noIslandItem, GameSound noIslandSound,
                                        List<String> noIslandCommands, boolean isSelfPlayerIsland, int objectIndex) {
        super(null, null, null, requiredPermission, lackPermissionSound, noIslandItem,
                objectIndex);
        this.islandItem = islandItem;
        this.islandSound = islandSound;
        this.islandCommands = islandCommands == null ? Collections.emptyList() : islandCommands;
        this.noIslandSound = noIslandSound;
        this.noIslandCommands = noIslandCommands == null ? Collections.emptyList() : noIslandCommands;
        this.isSelfPlayerIsland = isSelfPlayerIsland;
        this.getNullItem().getEditableBuilder().asSkullOf((SuperiorPlayer) null);
    }

    @Override
    public boolean countTowardsPageObjects() {
        return !this.isSelfPlayerIsland;
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuTopIslands superiorMenu, Island island) {
        if (islandItem == null)
            return null;

        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();

        if (isSelfPlayerIsland) {
            island = inventoryViewer.getIsland();
            if (island == null)
                return getNullItem().build();
        }

        SuperiorPlayer islandOwner = island.getOwner();
        int place = plugin.getGrid().getIslandPosition(island, superiorMenu.getSortingType()) + 1;
        ItemBuilder itemBuilder = islandItem.getBuilder().asSkullOf(islandOwner);

        String islandName = !plugin.getSettings().getIslandNames().isIslandTop() ||
                island.getName().isEmpty() ? islandOwner.getName() :
                plugin.getSettings().getIslandNames().isColorSupport() ?
                        Formatters.COLOR_FORMATTER.format(island.getName()) : island.getName();

        itemBuilder.replaceName("{0}", islandName)
                .replaceName("{1}", String.valueOf(place))
                .replaceName("{2}", Formatters.NUMBER_FORMATTER.format(island.getIslandLevel()))
                .replaceName("{3}", Formatters.NUMBER_FORMATTER.format(island.getWorth()))
                .replaceName("{5}", Formatters.FANCY_NUMBER_FORMATTER.format(island.getIslandLevel(), inventoryViewer.getUserLocale()))
                .replaceName("{6}", Formatters.FANCY_NUMBER_FORMATTER.format(island.getWorth(), inventoryViewer.getUserLocale()))
                .replaceName("{7}", Formatters.NUMBER_FORMATTER.format(island.getTotalRating()))
                .replaceName("{8}", Formatters.RATING_FORMATTER.format(island.getTotalRating(), inventoryViewer.getUserLocale()))
                .replaceName("{9}", Formatters.NUMBER_FORMATTER.format(island.getRatingAmount()))
                .replaceName("{10}", Formatters.NUMBER_FORMATTER.format(island.getAllPlayersInside().size()));

        ItemMeta itemMeta = itemBuilder.getItemMeta();

        if (itemMeta != null && itemMeta.hasLore()) {
            List<String> lore = new LinkedList<>();

            for (String line : itemMeta.getLore()) {
                if (line.contains("{4}")) {
                    List<SuperiorPlayer> members = new LinkedList<>(island.getIslandMembers(plugin.getSettings().isIslandTopIncludeLeader()));
                    String memberFormat = line.split("\\{4}:")[1];
                    if (members.size() == 0) {
                        lore.add(memberFormat.replace("{}", "None"));
                    } else {
                        PlaceholdersService placeholdersService = plugin.getServices().getPlaceholdersService();

                        if (plugin.getSettings().getTopIslandMembersSorting() != TopIslandMembersSorting.NAMES)
                            members.sort(plugin.getSettings().getTopIslandMembersSorting().getComparator());

                        members.forEach(member -> {
                            String onlineMessage = member.isOnline() ?
                                    Message.ISLAND_TOP_STATUS_ONLINE.getMessage(inventoryViewer.getUserLocale()) :
                                    Message.ISLAND_TOP_STATUS_OFFLINE.getMessage(inventoryViewer.getUserLocale());

                            lore.add(placeholdersService.parsePlaceholders(member.asOfflinePlayer(), memberFormat
                                    .replace("{}", member.getName())
                                    .replace("{0}", member.getName())
                                    .replace("{1}", onlineMessage == null ? "" : onlineMessage)
                                    .replace("{2}", member.getPlayerRole().getDisplayName()))
                            );
                        });
                    }
                } else {
                    lore.add(line
                            .replace("{0}", island.getOwner().getName())
                            .replace("{1}", String.valueOf(place))
                            .replace("{2}", Formatters.NUMBER_FORMATTER.format(island.getIslandLevel()))
                            .replace("{3}", Formatters.NUMBER_FORMATTER.format(island.getWorth()))
                            .replace("{5}", Formatters.FANCY_NUMBER_FORMATTER.format(island.getIslandLevel(), inventoryViewer.getUserLocale()))
                            .replace("{6}", Formatters.FANCY_NUMBER_FORMATTER.format(island.getWorth(), inventoryViewer.getUserLocale()))
                            .replace("{7}", Formatters.NUMBER_FORMATTER.format(island.getTotalRating()))
                            .replace("{8}", Formatters.RATING_FORMATTER.format(island.getTotalRating(), inventoryViewer.getUserLocale()))
                            .replace("{9}", Formatters.NUMBER_FORMATTER.format(island.getRatingAmount()))
                            .replace("{10}", Formatters.NUMBER_FORMATTER.format(island.getAllPlayersInside().size())));
                }
            }

            itemBuilder.withLore(lore);
        }

        return itemBuilder.build(islandOwner);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuTopIslands superiorMenu,
                              InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        Island island = this.isSelfPlayerIsland ? clickedPlayer.getIsland() : pagedObject;

        if (island != null) {
            if (islandSound != null)
                islandSound.playSound(player);

            if (islandCommands != null) {
                islandCommands.forEach(command -> Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? player : Bukkit.getConsoleSender(),
                        command.replace("PLAYER:", "").replace("%player%", clickedPlayer.getName())));
            }

            superiorMenu.setPreviousMove(false);

            if (clickEvent.getClick().isRightClick()) {
                if (MenuGlobalWarps.visitorWarps) {
                    plugin.getCommands().dispatchSubCommand(player, "visit", island.getOwner().getName());
                } else {
                    plugin.getMenus().openWarpCategories(clickedPlayer, superiorMenu, island);
                }
            } else if (plugin.getSettings().isValuesMenu()) {
                plugin.getMenus().openValues(clickedPlayer, superiorMenu, island);
            }

            return;
        }

        if (noIslandSound != null)
            noIslandSound.playSound(player);

        if (noIslandCommands != null)
            noIslandCommands.forEach(command -> Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? player : Bukkit.getConsoleSender(),
                    command.replace("PLAYER:", "").replace("%player%", clickedPlayer.getName())));
    }

    @Override
    public boolean ignorePagedButton() {
        return this.isSelfPlayerIsland;
    }

    public static class Builder extends PagedObjectBuilder<Builder, TopIslandsPagedObjectButton, MenuTopIslands> {

        private TemplateItem noIslandItem;
        private GameSound noIslandSound;
        private List<String> noIslandCommands;
        private boolean isPlayerSelfIsland;

        public Builder setIslandItem(TemplateItem islandItem) {
            this.buttonItem = islandItem;
            return this;
        }

        public Builder setNoIslandItem(TemplateItem noIslandItem) {
            this.noIslandItem = noIslandItem;
            return this;
        }

        public Builder setIslandSound(GameSound islandSound) {
            this.clickSound = islandSound;
            return this;
        }

        public Builder setNoIslandSound(GameSound noIslandSound) {
            this.noIslandSound = noIslandSound;
            return this;
        }

        public Builder setIslandCommands(List<String> islandCommands) {
            this.commands = islandCommands;
            return this;
        }

        public Builder setNoIslandCommands(List<String> noIslandCommands) {
            this.noIslandCommands = noIslandCommands;
            return this;
        }

        public Builder setPlayerSelfIsland(boolean playerSelfIsland) {
            this.isPlayerSelfIsland = playerSelfIsland;
            return this;
        }

        @Override
        public TopIslandsPagedObjectButton build() {
            return new TopIslandsPagedObjectButton(requiredPermission, lackPermissionSound, buttonItem,
                    clickSound, commands, noIslandItem, noIslandSound, noIslandCommands, isPlayerSelfIsland,
                    getObjectIndex());
        }

        public Builder copy() {
            Builder cloned = new Builder();
            cloned.requiredPermission = requiredPermission;
            cloned.lackPermissionSound = lackPermissionSound == null ? null : lackPermissionSound.copy();
            cloned.buttonItem = buttonItem == null ? null : buttonItem.copy();
            cloned.clickSound = clickSound == null ? null : clickSound.copy();
            cloned.commands = commands == null ? null : new ArrayList<>(commands);
            cloned.noIslandItem = noIslandItem == null ? null : noIslandItem.copy();
            cloned.noIslandSound = noIslandSound == null ? null : noIslandSound.copy();
            cloned.noIslandCommands = noIslandCommands == null ? null : new ArrayList<>(noIslandCommands);
            cloned.isPlayerSelfIsland = isPlayerSelfIsland;
            return cloned;
        }

    }

}
