package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.lang.PlayerLocales;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuGlobalWarps;
import com.bgsoftware.superiorskyblock.menu.impl.MenuTopIslands;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class TopIslandsPagedObjectButton extends PagedObjectButton<MenuTopIslands, Island> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final ItemBuilder islandItem;
    private final ItemBuilder noIslandItem;
    private final SoundWrapper islandSound;
    private final SoundWrapper noIslandSound;
    private final List<String> islandCommands;
    private final List<String> noIslandCommands;
    private final boolean isSelfPlayerIsland;

    private TopIslandsPagedObjectButton(String requiredPermission, SoundWrapper lackPermissionSound,
                                        ItemBuilder islandItem, SoundWrapper islandSound, List<String> islandCommands,
                                        ItemBuilder noIslandItem, SoundWrapper noIslandSound,
                                        List<String> noIslandCommands, boolean isSelfPlayerIsland, int objectIndex) {
        super(null, null, null, requiredPermission, lackPermissionSound, null,
                objectIndex);
        this.islandItem = islandItem;
        this.islandSound = islandSound;
        this.islandCommands = islandCommands == null ? Collections.emptyList() : islandCommands;
        this.noIslandItem = noIslandItem == null ? new ItemBuilder(Material.AIR) : noIslandItem.asSkullOf((SuperiorPlayer) null);
        this.noIslandSound = noIslandSound;
        this.noIslandCommands = noIslandCommands == null ? Collections.emptyList() : noIslandCommands;
        this.isSelfPlayerIsland = isSelfPlayerIsland;
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuTopIslands superiorMenu, Island island) {
        if (islandItem == null)
            return null;

        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();

        if (isSelfPlayerIsland && inventoryViewer.getIsland() != null)
            island = inventoryViewer.getIsland();

        SuperiorPlayer islandOwner = island.getOwner();
        int place = plugin.getGrid().getIslandPosition(island, superiorMenu.getSortingType()) + 1;
        ItemBuilder itemBuilder = islandItem.copy();

        String islandName = !plugin.getSettings().getIslandNames().isIslandTop() ||
                island.getName().isEmpty() ? islandOwner.getName() :
                plugin.getSettings().getIslandNames().isColorSupport() ?
                        StringUtils.translateColors(island.getName()) : island.getName();

        itemBuilder.replaceName("{0}", islandName)
                .replaceName("{1}", String.valueOf(place))
                .replaceName("{2}", StringUtils.format(island.getIslandLevel()))
                .replaceName("{3}", StringUtils.format(island.getWorth()))
                .replaceName("{5}", StringUtils.fancyFormat(island.getIslandLevel(), inventoryViewer.getUserLocale()))
                .replaceName("{6}", StringUtils.fancyFormat(island.getWorth(), inventoryViewer.getUserLocale()))
                .replaceName("{7}", StringUtils.format(island.getTotalRating()))
                .replaceName("{8}", StringUtils.formatRating(PlayerLocales.getDefaultLocale(), island.getTotalRating()))
                .replaceName("{9}", StringUtils.format(island.getRatingAmount()))
                .replaceName("{10}", StringUtils.format(island.getAllPlayersInside().size()));

        ItemMeta itemMeta = itemBuilder.getItemMeta();

        if (itemMeta != null && itemMeta.hasLore()) {
            List<String> lore = new ArrayList<>();

            for (String line : itemMeta.getLore()) {
                if (line.contains("{4}")) {
                    List<SuperiorPlayer> members = island.getIslandMembers(plugin.getSettings().isIslandTopIncludeLeader());
                    String memberFormat = line.split("\\{4}:")[1];
                    if (members.size() == 0) {
                        lore.add(memberFormat.replace("{}", "None"));
                    } else {
                        members.forEach(member -> {
                            String onlineMessage = member.isOnline() ?
                                    Message.ISLAND_TOP_STATUS_ONLINE.getMessage(inventoryViewer.getUserLocale()) :
                                    Message.ISLAND_TOP_STATUS_OFFLINE.getMessage(inventoryViewer.getUserLocale());

                            lore.add(PlaceholderHook.parse(member, memberFormat
                                    .replace("{}", member.getName())
                                    .replace("{0}", member.getName())
                                    .replace("{1}", onlineMessage == null ? "" : onlineMessage))
                            );
                        });
                    }
                } else {
                    lore.add(line
                            .replace("{0}", island.getOwner().getName())
                            .replace("{1}", String.valueOf(place))
                            .replace("{2}", StringUtils.format(island.getIslandLevel()))
                            .replace("{3}", StringUtils.format(island.getWorth()))
                            .replace("{5}", StringUtils.fancyFormat(island.getIslandLevel(), inventoryViewer.getUserLocale()))
                            .replace("{6}", StringUtils.fancyFormat(island.getWorth(), inventoryViewer.getUserLocale()))
                            .replace("{7}", StringUtils.format(island.getTotalRating()))
                            .replace("{8}", StringUtils.formatRating(PlayerLocales.getDefaultLocale(), island.getTotalRating()))
                            .replace("{9}", StringUtils.format(island.getRatingAmount()))
                            .replace("{10}", StringUtils.format(island.getAllPlayersInside().size())));
                }
            }

            itemBuilder.withLore(lore);
        }

        return itemBuilder.build(islandOwner);
    }

    @Override
    public ItemBuilder getNullItem() {
        return noIslandItem;
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

    public static class Builder extends PagedObjectBuilder<Builder, TopIslandsPagedObjectButton, MenuTopIslands> {

        private ItemBuilder noIslandItem;
        private SoundWrapper noIslandSound;
        private List<String> noIslandCommands;
        private boolean isPlayerSelfIsland;

        public Builder setIslandItem(ItemBuilder islandItem) {
            this.buttonItem = islandItem;
            return this;
        }

        public Builder setNoIslandItem(ItemBuilder noIslandItem) {
            this.noIslandItem = noIslandItem;
            return this;
        }

        public Builder setIslandSound(SoundWrapper islandSound) {
            this.clickSound = islandSound;
            return this;
        }

        public Builder setNoIslandSound(SoundWrapper noIslandSound) {
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

    }

}
