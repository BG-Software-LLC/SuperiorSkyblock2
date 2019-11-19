package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandSettings;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class IslandSettingsMenu extends SuperiorMenu {

    private static Map<String, Object> settingsData = new HashMap<>();

    private static Inventory inventory = null;
    private static String title;
    private static List<Integer> slots;
    private static int previousSlot, currentSlot, nextSlot;
    private static ItemStack previousButton, currentButton, nextButton;
    private static List<IslandSettings> islandSettings = new ArrayList<>();

    private Island island;

    private int currentPage = 1;

    private IslandSettingsMenu(Island island){
        super("settingsPage");
        this.island = island;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked().getUniqueId());

        if(e.getRawSlot() == previousSlot || e.getRawSlot() == nextSlot || e.getRawSlot() == currentSlot){
            if(e.getRawSlot() == currentSlot)
                return;

            boolean nextPage = slots.size() * currentPage < islandSettings.size();

            if((!nextPage && e.getRawSlot() == nextSlot) || (currentPage == 1 && e.getRawSlot() == previousSlot))
                return;

            open(superiorPlayer, e.getRawSlot() == nextSlot ? currentPage + 1 : currentPage - 1, null);
        }
        else {
            if(e.getCurrentItem() == null)
                return;

            int settingsAmount = islandSettings.size();

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf >= settingsAmount || indexOf == -1)
                return;

            IslandSettings settings = islandSettings.get(indexOf + (slots.size() * (currentPage - 1)));
            String settingsName = settings.name().toLowerCase();

            if(!settingsData.containsKey(settingsName + "-settings-enabled"))
                return;

            if(island.hasSettingsEnabled(settings)){
                island.disableSettings(settings);
            }
            else{
                island.enableSettings(settings);
            }

            Locale.UPDATED_SETTINGS.send(superiorPlayer, StringUtils.format(settingsName));

            SoundWrapper soundWrapper = get(settingsName + "-sound", SoundWrapper.class);
            if (soundWrapper != null)
                soundWrapper.playSound(superiorPlayer.asPlayer());
            //noinspection unchecked
            List<String> commands = get(settingsName + "-commands", List.class);
            if (commands != null)
                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));

            open(superiorPlayer, currentPage, null);

        }
    }

    @Override
    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        open(superiorPlayer, 1, previousMenu);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    private void open(SuperiorPlayer superiorPlayer, int page, SuperiorMenu previousMenu) {
        if (Bukkit.isPrimaryThread()) {
            Executor.async(() -> open(superiorPlayer, page, previousMenu));
            return;
        }

        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), title);
        inv.setContents(inventory.getContents());

        int settingsAmount = islandSettings.size();

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < settingsAmount; i++){
            IslandSettings settings = islandSettings.get(i + (slots.size() * (page - 1)));

            ItemStack settingsItem = new ItemStack(Material.AIR);
            String settingsName = settings.name().toLowerCase();

            if (settingsData.containsKey(settingsName + "-settings-enabled"))
                settingsItem = get(settingsName + "-settings-" + (island.hasSettingsEnabled(settings) ? "enabled" : "disabled"), ItemStack.class);

            inv.setItem(slots.get(i), settingsItem);
        }

        inv.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceAll("{0}", (page == 1 ? "&c" : "&a")).build());

        inv.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceAll("{0}", page + "").build());

        inv.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceAll("{0}", (settingsAmount > page * slots.size() ? "&a" : "&c")).build());

        this.currentPage = page;

        this.previousMenu = null;

        Executor.sync(() -> {
            superiorPlayer.asPlayer().openInventory(inv);
            this.previousMenu = previousMenu;
        });
    }

    private static <T> T get(String key, Class<T> type){
        return type.cast(settingsData.get(key));
    }

    public static void init(){
        IslandSettingsMenu islandSettingsMenu = new IslandSettingsMenu(null);

        islandSettingsMenu.resetData();
        islandSettings.clear();
        settingsData.clear();

        File file = new File(plugin.getDataFolder(), "guis/settings-gui.yml");

        if(!file.exists())
            FileUtils.saveResource("guis/settings-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("settings-gui.title", "&lIsland Settings"));
        inventory = FileUtils.loadGUI(islandSettingsMenu, cfg.getConfigurationSection("settings-gui"), 5, "&lIsland Settings");

        slots = new ArrayList<>();
        Arrays.stream(cfg.getString("settings-gui.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));

        previousButton = FileUtils.getItemStack(cfg.getConfigurationSection("settings-gui.previous-page"));
        currentButton = FileUtils.getItemStack(cfg.getConfigurationSection("settings-gui.current-page"));
        nextButton = FileUtils.getItemStack(cfg.getConfigurationSection("settings-gui.next-page"));
        previousSlot = cfg.getInt("settings-gui.previous-page.slot");
        currentSlot = cfg.getInt("settings-gui.current-page.slot");
        nextSlot = cfg.getInt("settings-gui.next-page.slot");
        inventory.setItem(previousSlot, previousButton);
        inventory.setItem(currentSlot, currentButton);
        inventory.setItem(nextSlot, nextButton);

        ConfigurationSection section = cfg.getConfigurationSection("settings-gui.settings");

        for(IslandSettings islandSettings : IslandSettings.values()){
            String settings = islandSettings.name().toLowerCase();
            if(section.contains(settings)){
                settingsData.put(settings + "-sound", FileUtils.getSound(section.getConfigurationSection(settings + ".sound")));
                settingsData.put(settings + "-commands", FileUtils.getSound(section.getConfigurationSection(settings + ".commands")));
                settingsData.put(settings + "-settings-enabled",
                        FileUtils.getItemStack(section.getConfigurationSection(settings + ".settings-enabled")));
                settingsData.put(settings + "-settings-disabled",
                        FileUtils.getItemStack(section.getConfigurationSection(settings + ".settings-disabled")));
                IslandSettingsMenu.islandSettings.add(islandSettings);
            }
        }
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new IslandSettingsMenu(island).open(superiorPlayer, previousMenu);
    }

}
