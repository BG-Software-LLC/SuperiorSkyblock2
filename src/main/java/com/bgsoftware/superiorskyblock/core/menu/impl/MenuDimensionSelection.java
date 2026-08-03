package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuSlotsMap;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DimensionSelectButton;
import com.bgsoftware.superiorskyblock.core.menu.parser.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractMenuView;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.function.Consumer;

public class MenuDimensionSelection extends AbstractMenu<MenuDimensionSelection.View, MenuDimensionSelection.Args> {

    private MenuDimensionSelection(MenuParseResult<View> parseResult) {
        super(MenuIdentifiers.MENU_DIMENSION_SELECTION, parseResult);
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, Args args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    @Nullable
    public static MenuDimensionSelection createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("dimension-selection.yml", null);

        if (menuParseResult == null) {
            return null;
        }

        MenuSlotsMap menuSlotsMap = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<View> patternBuilder = menuParseResult.getLayoutBuilder();

        if (cfg.isConfigurationSection("items")) {
            for (String itemsSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemsSection = cfg.getConfigurationSection("items." + itemsSectionName);

                if (!itemsSection.isString("dimension")) {
                    continue;
                }

                String dimensionName = itemsSection.getString("dimension");

                Dimension dimension;
                try {
                    dimension = Dimension.getByName(dimensionName);
                } catch (IllegalArgumentException e) {
                    Log.warnFromFile("dimension-selection.yml", "Couldn't convert '", dimensionName,
                            "' into an dimension at ", itemsSection.getCurrentPath(), ", skipping...");
                    continue;
                }

                patternBuilder.mapButtons(menuSlotsMap.getSlots(itemsSectionName),
                        new DimensionSelectButton.Builder().setDimension(dimension));
            }
        }

        return new MenuDimensionSelection(menuParseResult);
    }

    public static class Args implements ViewArgs {

        private final Consumer<Dimension> onSelect;

        public Args(Consumer<Dimension> onSelect) {
            this.onSelect = onSelect;
        }

    }

    public static class View extends AbstractMenuView<View, Args> implements ViewArgs {

        private final Consumer<Dimension> onSelect;

        protected View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
                       Menu<View, Args> menu, Args args) {
            super(inventoryViewer, previousMenuView, menu);
            this.onSelect = args.onSelect;
        }

        public void accept(Dimension dimension) {
            onSelect.accept(dimension);
        }

    }

}
