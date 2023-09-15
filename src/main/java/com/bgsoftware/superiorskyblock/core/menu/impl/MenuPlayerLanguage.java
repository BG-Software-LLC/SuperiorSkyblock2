package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.LanguageButton;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.EmptyViewArgs;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class MenuPlayerLanguage extends AbstractMenu<BaseMenuView, EmptyViewArgs> {

    private MenuPlayerLanguage(MenuParseResult<BaseMenuView> parseResult) {
        super(MenuIdentifiers.MENU_PLAYER_LANGUAGE, parseResult);
    }

    @Override
    protected BaseMenuView createViewInternal(SuperiorPlayer superiorPlayer, EmptyViewArgs unused,
                                              @Nullable MenuView<?, ?> previousMenuView) {
        return new BaseMenuView(superiorPlayer, previousMenuView, this);
    }

    @Nullable
    public static MenuPlayerLanguage createInstance() {
        MenuParseResult<BaseMenuView> menuParseResult = MenuParserImpl.getInstance().loadMenu("player-language.yml",
                null);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<BaseMenuView> patternBuilder = menuParseResult.getLayoutBuilder();

        if (cfg.contains("items")) {
            for (String itemsSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemSection = cfg.getConfigurationSection("items." + itemsSectionName);

                String languageName = itemSection.getString("language");

                if (languageName == null)
                    continue;

                java.util.Locale locale = null;

                try {
                    locale = PlayerLocales.getLocale(languageName);
                    if (!PlayerLocales.isValidLocale(locale))
                        locale = null;
                } catch (IllegalArgumentException ignored) {
                }

                if (locale == null) {
                    Log.warnFromFile("player-language.yml", "The language ", languageName, " is not valid.");
                    continue;
                }

                patternBuilder.mapButtons(menuPatternSlots.getSlots(itemsSectionName),
                        new LanguageButton.Builder().setLanguage(locale));
            }
        }

        return new MenuPlayerLanguage(menuParseResult);
    }

}
