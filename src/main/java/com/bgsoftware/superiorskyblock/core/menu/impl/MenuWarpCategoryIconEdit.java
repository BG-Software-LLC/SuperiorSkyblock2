package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
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
import com.bgsoftware.superiorskyblock.core.menu.button.impl.WarpCategoryIconEditConfirmButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractIconProviderMenu;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.configuration.file.YamlConfiguration;

public class MenuWarpCategoryIconEdit extends AbstractMenu<AbstractIconProviderMenu.View<WarpCategory>, AbstractIconProviderMenu.Args<WarpCategory>> {

    private MenuWarpCategoryIconEdit(MenuParseResult<AbstractIconProviderMenu.View<WarpCategory>> parseResult) {
        super(MenuIdentifiers.MENU_WARP_CATEGORIES_ICON_EDIT, parseResult);
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, AbstractIconProviderMenu.Args<WarpCategory> args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    @Nullable
    public static MenuWarpCategoryIconEdit createInstance() {
        MenuParseResult<AbstractIconProviderMenu.View<WarpCategory>> menuParseResult = MenuParserImpl.getInstance().loadMenu(
                "warp-category-icon-edit.yml", null);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<AbstractIconProviderMenu.View<WarpCategory>> patternBuilder = menuParseResult.getLayoutBuilder();

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "icon-type", menuPatternSlots),
                new IconEditTypeButton.Builder<>(Message.WARP_CATEGORY_ICON_NEW_TYPE));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "icon-rename", menuPatternSlots),
                new IconRenameButton.Builder<>(Message.WARP_CATEGORY_ICON_NEW_NAME));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "icon-relore", menuPatternSlots),
                new IconEditLoreButton.Builder<>(Message.WARP_CATEGORY_ICON_NEW_LORE));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "icon-confirm", menuPatternSlots),
                new WarpCategoryIconEditConfirmButton.Builder());
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "icon-slots", menuPatternSlots),
                new IconDisplayButton.Builder<>());

        return new MenuWarpCategoryIconEdit(menuParseResult);
    }

    public static class Args extends AbstractIconProviderMenu.Args<WarpCategory> {

        public Args(WarpCategory warpCategory) {
            super(warpCategory, warpCategory == null ? null : new TemplateItem(new ItemBuilder(warpCategory.getRawIcon())));
        }

    }

    public static class View extends AbstractIconProviderMenu.View<WarpCategory> {

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<AbstractIconProviderMenu.View<WarpCategory>, AbstractIconProviderMenu.Args<WarpCategory>> menu,
             AbstractIconProviderMenu.Args<WarpCategory> args) {
            super(inventoryViewer, previousMenuView, menu, args);
        }

        @Override
        public String replaceTitle(String title) {
            return title.replace("{0}", getIconProvider().getName());
        }
    }

}