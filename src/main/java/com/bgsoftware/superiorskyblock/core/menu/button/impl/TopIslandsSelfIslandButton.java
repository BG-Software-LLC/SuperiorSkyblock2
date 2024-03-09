package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.enums.TopIslandMembersSorting;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuTopIslands;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TopIslandsSelfIslandButton extends AbstractMenuViewButton<MenuTopIslands.View> {

    private static final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
        @Override
        protected PlaceholdersService create() {
            return plugin.getServices().getService(PlaceholdersService.class);
        }
    };

    private TopIslandsSelfIslandButton(MenuTemplateButton<MenuTopIslands.View> templateButton, MenuTopIslands.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public ItemStack createViewItem() {
        Island island = menuView.getInventoryViewer().getIsland();
        return island == null ? getTemplate().noIslandItem.build() :
                modifyViewItem(menuView, island, getTemplate().islandItem);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        onButtonClick(clickEvent, menuView, menuView.getInventoryViewer().getIsland(), getTemplate().islandSound,
                getTemplate().islandCommands, getTemplate().noIslandSound, getTemplate().noIslandCommands);
    }

    public static void onButtonClick(InventoryClickEvent clickEvent, MenuTopIslands.View menuView,
                                     @Nullable Island island, @Nullable GameSound islandSound,
                                     List<String> islandCommands, @Nullable GameSound noIslandSound,
                                     List<String> noIslandCommands) {
        Player player = (Player) clickEvent.getWhoClicked();

        if (island != null) {
            GameSoundImpl.playSound(player, islandSound);

            if (islandCommands != null) {
                islandCommands.forEach(command -> Bukkit.dispatchCommand(command.startsWith("PLAYER:") ?
                                player : Bukkit.getConsoleSender(),
                        command.replace("PLAYER:", "")
                                .replace("%player%", player.getName())
                                .replace("%island%", island.getName())
                                .replace("%owner%", island.getOwner().getName())
                ));
            }

            menuView.setPreviousMove(false);

            if (clickEvent.getClick().isRightClick()) {
                if (Menus.MENU_GLOBAL_WARPS.isVisitorWarps()) {
                    plugin.getCommands().dispatchSubCommand(player, "visit", island.getOwner().getName());
                } else {
                    Menus.MENU_WARP_CATEGORIES.openMenu(menuView.getInventoryViewer(), menuView, island);
                }
            } else if (plugin.getSettings().isValuesMenu()) {
                plugin.getMenus().openValues(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), island);
            }

            return;
        }

        GameSoundImpl.playSound(player, noIslandSound);

        if (noIslandCommands != null)
            noIslandCommands.forEach(command -> Bukkit.dispatchCommand(command.startsWith("PLAYER:") ?
                            player : Bukkit.getConsoleSender(),
                    command.replace("PLAYER:", "").replace("%player%", player.getName())));
    }

    public static ItemStack modifyViewItem(MenuTopIslands.View menuView, Island island, @Nullable TemplateItem islandItem) {
        if (islandItem == null)
            return null;

        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();

        SuperiorPlayer islandOwner = island.getOwner();
        int place = plugin.getGrid().getIslandPosition(island, menuView.getSortingType()) + 1;
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
                        if (plugin.getSettings().getTopIslandMembersSorting() != TopIslandMembersSorting.NAMES)
                            members.sort(plugin.getSettings().getTopIslandMembersSorting().getComparator());

                        members.forEach(member -> {
                            String onlineMessage = member.isOnline() ?
                                    Message.ISLAND_TOP_STATUS_ONLINE.getMessage(inventoryViewer.getUserLocale()) :
                                    Message.ISLAND_TOP_STATUS_OFFLINE.getMessage(inventoryViewer.getUserLocale());

                            lore.add(placeholdersService.get().parsePlaceholders(member.asOfflinePlayer(), memberFormat
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

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuTopIslands.View> {

        private TemplateItem noIslandItem;
        private GameSound noIslandSound;
        private List<String> noIslandCommands;

        public void setIslandItem(TemplateItem islandItem) {
            this.buttonItem = islandItem;
        }

        public void setNoIslandItem(TemplateItem noIslandItem) {
            this.noIslandItem = noIslandItem;
        }

        public void setIslandSound(GameSound islandSound) {
            this.clickSound = islandSound;
        }

        public void setNoIslandSound(GameSound noIslandSound) {
            this.noIslandSound = noIslandSound;
        }

        public void setIslandCommands(List<String> islandCommands) {
            this.commands = islandCommands;
        }

        public void setNoIslandCommands(List<String> noIslandCommands) {
            this.noIslandCommands = noIslandCommands;
        }

        @Override
        public MenuTemplateButton<MenuTopIslands.View> build() {
            return new Template(requiredPermission, lackPermissionSound, buttonItem,
                    clickSound, commands, noIslandItem, noIslandSound, noIslandCommands);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuTopIslands.View> {

        private final TemplateItem islandItem;
        private final TemplateItem noIslandItem;
        @Nullable
        private final GameSound islandSound;
        @Nullable
        private final GameSound noIslandSound;
        private final List<String> islandCommands;
        private final List<String> noIslandCommands;

        Template(@Nullable String requiredPermission, @Nullable GameSound lackPermissionSound,
                 @Nullable TemplateItem islandItem, @Nullable GameSound islandSound, @Nullable List<String> islandCommands,
                 @Nullable TemplateItem noIslandItem, @Nullable GameSound noIslandSound,
                 @Nullable List<String> noIslandCommands) {
            super(null, null, null, requiredPermission, lackPermissionSound,
                    TopIslandsSelfIslandButton.class, TopIslandsSelfIslandButton::new);
            this.islandItem = islandItem == null ? TemplateItem.AIR : islandItem;
            this.noIslandItem = noIslandItem == null ? TemplateItem.AIR : noIslandItem;
            this.islandSound = islandSound;
            this.islandCommands = islandCommands == null ? Collections.emptyList() : islandCommands;
            this.noIslandSound = noIslandSound;
            this.noIslandCommands = noIslandCommands == null ? Collections.emptyList() : noIslandCommands;
            if (noIslandItem != null)
                noIslandItem.getEditableBuilder().asSkullOf((SuperiorPlayer) null);
        }

    }

}
