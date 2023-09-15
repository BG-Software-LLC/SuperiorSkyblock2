package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.WarpCategoryManageIconButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.WarpCategoryManageRenameButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.WarpCategoryManageWarpsButton;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import org.bukkit.configuration.file.YamlConfiguration;

public class MenuWarpCategoryManage extends AbstractMenu<MenuWarpCategoryManage.View, MenuWarpCategoryManage.Args> {

    private final GameSound successUpdateSound;

    private MenuWarpCategoryManage(MenuParseResult<View> parseResult, @Nullable GameSound successUpdateSound) {
        super(MenuIdentifiers.MENU_WARP_CATEGORIES_MANAGE, parseResult);
        this.successUpdateSound = successUpdateSound;
    }

    @Nullable
    public GameSound getSuccessUpdateSound() {
        return successUpdateSound;
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, Args args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    public void refreshViews(WarpCategory warpCategory) {
        refreshViews(view -> view.warpCategory.equals(warpCategory));
    }

    @Nullable
    public static MenuWarpCategoryManage createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("warp-category-manage.yml",
                null);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<View> patternBuilder = menuParseResult.getLayoutBuilder();

        GameSound successUpdateSound = cfg.isConfigurationSection("success-update-sound") ?
                MenuParserImpl.getInstance().getSound(cfg.getConfigurationSection("success-update-sound")) : null;

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "category-rename", menuPatternSlots),
                new WarpCategoryManageRenameButton.Builder());
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "category-icon", menuPatternSlots),
                new WarpCategoryManageIconButton.Builder());
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "category-warps", menuPatternSlots),
                new WarpCategoryManageWarpsButton.Builder());

        return new MenuWarpCategoryManage(menuParseResult, successUpdateSound);
    }

    public static class Args implements ViewArgs {

        private final WarpCategory warpCategory;

        public Args(WarpCategory warpCategory) {
            this.warpCategory = warpCategory;
        }

    }

    public static class View extends AbstractMenuView<View, Args> {

        private final WarpCategory warpCategory;

        protected View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
                       Menu<View, Args> menu, Args args) {
            super(inventoryViewer, previousMenuView, menu);
            this.warpCategory = args.warpCategory;
        }

        public WarpCategory getWarpCategory() {
            return warpCategory;
        }

        @Override
        public String replaceTitle(String title) {
            return title.replace("{0}", warpCategory.getName());
        }

    }

}