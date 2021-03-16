package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.util.List;

public final class MenuPlayerLanguage extends SuperiorMenu {

    private MenuPlayerLanguage(SuperiorPlayer superiorPlayer){
        super("menuPlayerLanguage", superiorPlayer);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        if(!containsData(e.getRawSlot() + ""))
            return;

        java.util.Locale locale = (java.util.Locale) getData(e.getRawSlot() + "");
        superiorPlayer.setUserLocale(locale);
        Locale.CHANGED_LANGUAGE.send(superiorPlayer);

        Executor.sync(() -> e.getWhoClicked().closeInventory(), 1L);
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }

    public static void init(){
        MenuPlayerLanguage menuPlayerLanguage = new MenuPlayerLanguage(null);

        File file = new File(plugin.getDataFolder(), "menus/player-language.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/player-language.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuPlayerLanguage, "player-language.yml", cfg);

        for(char ch : charSlots.keys()){
            if(cfg.contains("items." + ch + ".language")) {
                String language = cfg.getString("items." + ch + ".language");
                for(int slot : charSlots.get(ch)) {
                    try {
                        java.util.Locale locale = LocaleUtils.getLocale(language);
                        if(Locale.isValidLocale(locale))
                            menuPlayerLanguage.addData(slot + "", locale);
                        else throw new IllegalArgumentException();
                    }catch(IllegalArgumentException ex){
                        SuperiorSkyblockPlugin.log("&c[player-language.yml] The language " + language + " is not valid.");
                    }
                }
            }
        }

        charSlots.delete();

        menuPlayerLanguage.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuPlayerLanguage(superiorPlayer).open(previousMenu);
    }

}
