package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.enums.TopIslandMembersSorting;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButton;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Either;
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
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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
        return island == null ? getTemplate().noIslandItem.getLeft().build() :
                modifyViewItem(menuView, island, getTemplate().islandItem.getLeft());
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuTopIslands.View> context) {
        onButtonClick(context, menuView, menuView.getInventoryViewer().getIsland(), getTemplate().islandSound,
                getTemplate().islandCommands, getTemplate().noIslandSound, getTemplate().noIslandCommands);
    }

    public static void onButtonClick(ButtonClickContext<MenuTopIslands.View> context, MenuTopIslands.View menuView,
                                     @Nullable Island island, @Nullable GameSound islandSound,
                                     List<String> islandCommands, @Nullable GameSound noIslandSound,
                                     List<String> noIslandCommands) {
        Player player = context.getPlayer();

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

            if (context.getClickType().isRightClick()) {
                if (Menus.MENU_GLOBAL_WARPS.isVisitorWarps()) {
                    plugin.getCommands().dispatchSubCommand(player, "visit", island.getOwner().getName());
                } else {
                    plugin.getProviders().getMenusProvider().openWarpCategories(
                            menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), island);
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

        Locale locale = menuView.getInventoryViewer().getUserLocale();

        SuperiorPlayer islandOwner = island.getOwner();
        ItemBuilder itemBuilder = islandItem.getBuilder().asSkullOf(islandOwner);

        if (itemBuilder.hasLore()) {
            List<String> newLore = new LinkedList<>();
            List<String> lore = itemBuilder.getLore();

            for (String line : lore) {
                if (line.contains("{4}")) {
                    List<SuperiorPlayer> members = new LinkedList<>(island.getIslandMembers(plugin.getSettings().isIslandTopIncludeLeader()));
                    String memberFormat = line.split("\\{4}:")[1];
                    if (members.isEmpty()) {
                        lore.add(memberFormat.replace("{}", IslandUtils.DEFAULT_NONE_VALUE));
                    } else {
                        if (plugin.getSettings().getTopIslandMembersSorting() != TopIslandMembersSorting.NAMES)
                            members.sort(plugin.getSettings().getTopIslandMembersSorting().getComparator());

                        members.forEach(member -> {
                            String onlineMessage = member.isOnline() ?
                                    Message.ISLAND_TOP_STATUS_ONLINE.getMessage(locale) :
                                    Message.ISLAND_TOP_STATUS_OFFLINE.getMessage(locale);

                            newLore.add(placeholdersService.get().parsePlaceholders(member.asOfflinePlayer(), memberFormat
                                    .replace("{}", member.getName())
                                    .replace("{0}", member.getName())
                                    .replace("{1}", onlineMessage == null ? "" : onlineMessage)
                                    .replace("{2}", member.getPlayerRole().getDisplayName()))
                            );
                        });
                    }
                } else {
                    newLore.add(line);
                }
            }

            itemBuilder.withLore(newLore);
        }

        String islandName = !plugin.getSettings().getIslandNames().isIslandTop() ||
                island.getName().isEmpty() ? islandOwner.getName() : island.getName();
        int place = plugin.getGrid().getIslandPosition(island, menuView.getSortingType()) + 1;

        return itemBuilder.replaceAll("{0}", islandName)
                .replaceAll("{1}", String.valueOf(place))
                .replaceAll("{2}", Formatters.NUMBER_FORMATTER.format(island.getIslandLevel()))
                .replaceAll("{3}", Formatters.NUMBER_FORMATTER.format(island.getWorth()))
                .replaceAll("{5}", Formatters.FANCY_NUMBER_FORMATTER.format(island.getIslandLevel(), locale))
                .replaceAll("{6}", Formatters.FANCY_NUMBER_FORMATTER.format(island.getWorth(), locale))
                .replaceAll("{7}", Formatters.NUMBER_FORMATTER.format(island.getTotalRating()))
                .replaceAll("{8}", Formatters.RATING_FORMATTER.format(island.getTotalRating(), locale))
                .replaceAll("{9}", Formatters.NUMBER_FORMATTER.format(island.getRatingAmount()))
                .replaceAll("{10}", Formatters.NUMBER_FORMATTER.format(island.getAllPlayersInside().size()))
                .replaceAll("{11}", Formatters.NUMBER_FORMATTER.format(island.getIslandBank().getBalance()))
                .replaceAll("{12}", Formatters.FANCY_NUMBER_FORMATTER.format(island.getIslandBank().getBalance(), locale))
                .build(islandOwner);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuTopIslands.View> {

        private Either<TemplateItem, DialogButton> noIslandItem;
        private GameSound noIslandSound;
        private List<String> noIslandCommands;

        public void setIslandItem(TemplateItem islandItem) {
            this.buttonData = Either.left(islandItem);
        }

        public void setIslandDialog(DialogButton dialogButton) {
            this.buttonData = Either.right(dialogButton);
        }

        public void setNoIslandItem(TemplateItem noIslandItem) {
            this.noIslandItem = Either.left(noIslandItem);
        }

        public void setNoIslandDialog(DialogButton buttonDialog) {
            this.noIslandItem = Either.right(buttonDialog);
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
            Either<TemplateItem, DialogButton> islandItem = buttonData;
            GameSound islandSound = clickSound;
            List<String> islandCommands = commands;
            this.buttonData = null;
            this.clickSound = null;
            this.commands = null;
            try {
                return new Template(this, islandItem, islandSound, islandCommands, noIslandItem, noIslandSound, noIslandCommands);
            } finally {
                this.buttonData = islandItem;
                this.clickSound = islandSound;
                this.commands = islandCommands;
            }
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuTopIslands.View> {

        private final Either<TemplateItem, DialogButton> islandItem;
        private final Either<TemplateItem, DialogButton> noIslandItem;
        @Nullable
        private final GameSound islandSound;
        @Nullable
        private final GameSound noIslandSound;
        private final List<String> islandCommands;
        private final List<String> noIslandCommands;

        Template(AbstractBuilder<MenuTopIslands.View> builder,
                 @Nullable Either<TemplateItem, DialogButton> islandItem, @Nullable GameSound islandSound, @Nullable List<String> islandCommands,
                 @Nullable Either<TemplateItem, DialogButton> noIslandItem, @Nullable GameSound noIslandSound,
                 @Nullable List<String> noIslandCommands) {
            super(builder, TopIslandsSelfIslandButton.class, TopIslandsSelfIslandButton::new);
            this.islandItem = islandItem == null ? Either.left(TemplateItem.AIR) : islandItem;
            this.noIslandItem = noIslandItem == null ? Either.left(TemplateItem.AIR) : noIslandItem;
            this.islandSound = islandSound;
            this.islandCommands = islandCommands == null ? Collections.emptyList() : islandCommands;
            this.noIslandSound = noIslandSound;
            this.noIslandCommands = noIslandCommands == null ? Collections.emptyList() : noIslandCommands;
            if (noIslandItem != null && noIslandItem.isLeft())
                noIslandItem.getLeft().getEditableBuilder().asSkullOf((SuperiorPlayer) null);
        }

    }

}
