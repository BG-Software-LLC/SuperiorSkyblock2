package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.LanguageButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import org.bukkit.configuration.ConfigurationSection;

public class MenuPlayerLanguage extends SuperiorMenu<MenuPlayerLanguage> {

    private static RegularMenuPattern<MenuPlayerLanguage> menuPattern;

    private MenuPlayerLanguage(SuperiorPlayer superiorPlayer) {
        super(menuPattern, superiorPlayer);
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu);
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuPlayerLanguage> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "player-language.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

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
                    SuperiorSkyblockPlugin.log("&c[player-language.yml] The language " + languageName + " is not valid.");
                    continue;
                }

                patternBuilder
                        .mapButtons(menuPatternSlots.getSlots(itemsSectionName),
                                new LanguageButton.Builder().setLanguage(locale));
            }
        }

        menuPattern = patternBuilder.build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu) {
        new MenuPlayerLanguage(superiorPlayer).open(previousMenu);
    }

}
