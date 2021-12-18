package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.lang.PlayerLocales;
import com.bgsoftware.superiorskyblock.threads.Executor;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;

public final class MenuPlayerLanguage extends SuperiorMenu {

    private MenuPlayerLanguage(SuperiorPlayer superiorPlayer) {
        super("menuPlayerLanguage", superiorPlayer);
    }

    public static void init() {
        MenuPlayerLanguage menuPlayerLanguage = new MenuPlayerLanguage(null);

        File file = new File(plugin.getDataFolder(), "menus/player-language.yml");

        if (!file.exists())
            FileUtils.saveResource("menus/player-language.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        MenuPatternSlots menuPatternSlots = FileUtils.loadGUI(menuPlayerLanguage, "player-language.yml", cfg);

        for (char ch : menuPatternSlots.getChars()) {
            if (cfg.contains("items." + ch + ".language")) {
                String language = cfg.getString("items." + ch + ".language");
                for (int slot : menuPatternSlots.getSlots(ch)) {
                    try {
                        java.util.Locale locale = PlayerLocales.getLocale(language);
                        if (PlayerLocales.isValidLocale(locale))
                            menuPlayerLanguage.addData(slot + "", locale);
                        else throw new IllegalArgumentException();
                    } catch (IllegalArgumentException ex) {
                        SuperiorSkyblockPlugin.log("&c[player-language.yml] The language " + language + " is not valid.");
                        SuperiorSkyblockPlugin.debug(ex);
                    }
                }
            }
        }

        menuPlayerLanguage.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu) {
        new MenuPlayerLanguage(superiorPlayer).open(previousMenu);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if (!containsData(e.getRawSlot() + ""))
            return;

        java.util.Locale locale = (java.util.Locale) getData(e.getRawSlot() + "");
        superiorPlayer.setUserLocale(locale);
        Message.CHANGED_LANGUAGE.send(superiorPlayer);

        Executor.sync(() -> e.getWhoClicked().closeInventory(), 1L);
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }

}
