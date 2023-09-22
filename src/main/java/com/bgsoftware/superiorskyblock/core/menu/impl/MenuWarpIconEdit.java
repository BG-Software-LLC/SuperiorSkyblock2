package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.IconDisplayButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.IconEditLoreButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.IconEditTypeButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.IconRenameButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.WarpIconEditConfirmButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractIconProviderMenu;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.warp.WarpIcons;
import org.bukkit.configuration.file.YamlConfiguration;

public class MenuWarpIconEdit extends AbstractMenu<AbstractIconProviderMenu.View<IslandWarp>, AbstractIconProviderMenu.Args<IslandWarp>> {

    private MenuWarpIconEdit(MenuParseResult<AbstractIconProviderMenu.View<IslandWarp>> parseResult) {
        super(MenuIdentifiers.MENU_WARP_ICON_EDIT, parseResult);
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, AbstractIconProviderMenu.Args<IslandWarp> args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    @Nullable
    public static MenuWarpIconEdit createInstance() {
        MenuParseResult<AbstractIconProviderMenu.View<IslandWarp>> menuParseResult = MenuParserImpl.getInstance().loadMenu(
                "warp-icon-edit.yml", null);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<AbstractIconProviderMenu.View<IslandWarp>> patternBuilder = menuParseResult.getLayoutBuilder();

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "icon-type", menuPatternSlots),
                new IconEditTypeButton.Builder<>(Message.WARP_ICON_NEW_TYPE));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "icon-rename", menuPatternSlots),
                new IconRenameButton.Builder<>(Message.WARP_ICON_NEW_NAME));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "icon-relore", menuPatternSlots),
                new IconEditLoreButton.Builder<>(Message.WARP_ICON_NEW_LORE));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "icon-confirm", menuPatternSlots),
                new WarpIconEditConfirmButton.Builder());
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "icon-slots", menuPatternSlots),
                new IconDisplayButton.Builder<>());

        return new MenuWarpIconEdit(menuParseResult);
    }

    public static class Args extends AbstractIconProviderMenu.Args<IslandWarp> {

        public Args(IslandWarp islandWarp) {
            super(islandWarp, islandWarp == null ? null : islandWarp.getRawIcon() == null ?
                    WarpIcons.DEFAULT_WARP_ICON : new TemplateItem(new ItemBuilder(islandWarp.getRawIcon())));
        }

    }

    public static class View extends AbstractIconProviderMenu.View<IslandWarp> {

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<AbstractIconProviderMenu.View<IslandWarp>, AbstractIconProviderMenu.Args<IslandWarp>> menu,
             AbstractIconProviderMenu.Args<IslandWarp> args) {
            super(inventoryViewer, previousMenuView, menu, args);
        }

        @Override
        public String replaceTitle(String title) {
            return title.replace("{0}", getIconProvider().getName());
        }
    }

}