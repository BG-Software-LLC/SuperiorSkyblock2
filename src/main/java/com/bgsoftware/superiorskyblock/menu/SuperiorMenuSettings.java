package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handlers.SettingsHandler;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class SuperiorMenuSettings extends PagedSuperiorMenu<ItemStack> {

    private static final List<String> pathSlots = new ArrayList<>();
    private static final String[] ignorePaths = new String[] { "database", "max-island-size", "island-roles",
            "worlds.normal-world", "commands-cooldown", "starter-chest", "event-commands" };
    public static final Map<UUID, String> configValues = new HashMap<>();
    public static final Map<UUID, Integer> lastPage = new HashMap<>();
    public static final Set<UUID> pageMove = new HashSet<>();

    public static CommentedConfiguration config;

    private SuperiorMenuSettings(SuperiorPlayer superiorPlayer){
        super("menuConfigSettings", superiorPlayer, true);
        setPageMoveRunnable(_superiorPlayer -> pageMove.add(_superiorPlayer.getUniqueId()));
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e, ItemStack clickedObject) {
        Player player = (Player) e.getWhoClicked();

        if(e.getRawSlot() == 40){
            Executor.async(() -> {
                saveConfiguration();
                player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Saved configuration successfully.");
                Executor.sync(player::closeInventory);
            });
            return;
        }

        try{
            String value = pathSlots.get((currentPage - 1) * 36 + e.getRawSlot());

            if(value == null)
                return;

            configValues.put(player.getUniqueId(), value);
            player.closeInventory();
            player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

            if(config.isList(configValues.get(player.getUniqueId())) ||
                    config.isConfigurationSection(configValues.get(player.getUniqueId()))){
                player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
            }
        }catch(Exception ignored){}
    }

    @Override
    public void open(SuperiorMenu previousMenu) {
        lastPage.put(superiorPlayer.getUniqueId(), currentPage);
        super.open(previousMenu);
    }

    @Override
    public void closeInventory(SuperiorPlayer superiorPlayer) {
        super.closeInventory(superiorPlayer);
        if(!pageMove.remove(superiorPlayer.getUniqueId()) && !configValues.containsKey(superiorPlayer.getUniqueId())) {
            reloadConfiguration();
            lastPage.remove(superiorPlayer.getUniqueId());
        }
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, ItemStack value) {
        return value;
    }

    @Override
    protected List<ItemStack> requestObjects() {
        List<ItemStack> itemStacks = new ArrayList<>();
        buildFromSection(itemStacks, config.getConfigurationSection(""));
        return itemStacks;
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }

    protected static void saveConfiguration(){
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
            plugin.setSettings(new SettingsHandler(plugin));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void reloadConfiguration(){
        try {
            config.load(new File(plugin.getDataFolder(), "config.yml"));
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public static void init(){
        SuperiorMenuSettings menuConfigSettings = new SuperiorMenuSettings(null);

        File file = new File(plugin.getDataFolder(), "config.yml");

        config = CommentedConfiguration.loadConfiguration(file);

        menuConfigSettings.resetData();

        menuConfigSettings.setTitle(ChatColor.BOLD + "Settings Editor");
        menuConfigSettings.setInventoryType(InventoryType.CHEST);
        menuConfigSettings.setRowsSize(6);
        menuConfigSettings.setBackButton(-1);
        menuConfigSettings.setPreviousSlot(Collections.singletonList(47));
        menuConfigSettings.setCurrentSlot(Collections.singletonList(49));
        menuConfigSettings.setNextSlot(Collections.singletonList(51));
        menuConfigSettings.setSlots(IntStream.range(0, 36).boxed().collect(Collectors.toList()));

        menuConfigSettings.addFillItem(47, new ItemBuilder(Material.PAPER).withName("{0}Previous Page"));
        menuConfigSettings.addFillItem(49, new ItemBuilder(Materials.SUNFLOWER.toBukkitType()).withName("&aCurrent Page").withLore("&7Page {0}"));
        menuConfigSettings.addFillItem(51, new ItemBuilder(Material.PAPER).withName("{0}Next Page"));

        for(int i = 36; i < 45; i++)
            menuConfigSettings.addFillItem(i, new ItemBuilder(Materials.BLACK_STAINED_GLASS_PANE.toBukkitItem()).withName(" "));

        menuConfigSettings.addFillItem(40, new ItemBuilder(Material.EMERALD).withName("&aSave Changes"));

        menuConfigSettings.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        openInventory(superiorPlayer, previousMenu, 1);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, int page){
        SuperiorMenuSettings superiorMenuSettings = new SuperiorMenuSettings(superiorPlayer);
        superiorMenuSettings.currentPage = page;
        superiorMenuSettings.open(previousMenu);
    }

    private static void buildFromSection(List<ItemStack> itemStacks, ConfigurationSection section){
        for(String path : section.getKeys(false)){
            String fullPath = section.getCurrentPath().isEmpty() ? path : section.getCurrentPath() + "." + path;

            if(Arrays.stream(ignorePaths).anyMatch(fullPath::contains))
                continue;

            if(section.isConfigurationSection(path)){
                buildFromSection(itemStacks, section.getConfigurationSection(path));
            }
            else{
                ItemBuilder itemBuilder = new ItemBuilder(Materials.CLOCK.toBukkitItem()).withName("&6" +
                        StringUtils.format(fullPath.replace("-", "_")
                                .replace(".", "_").replace(" ", "_"))
                );

                if(section.isBoolean(path))
                    itemBuilder.withLore("&7Value: " + section.getBoolean(path));
                else if(section.isInt(path))
                    itemBuilder.withLore("&7Value: " + section.getInt(path));
                else if(section.isDouble(path))
                    itemBuilder.withLore("&7Value: " + section.getDouble(path));
                else if(section.isString(path))
                    itemBuilder.withLore("&7Value: " + section.getString(path));
                else if(section.isList(path))
                    itemBuilder.withLore("&7Value:", section.getStringList(path));
                else if(section.isConfigurationSection(path))
                    itemBuilder.withLore("&7Value:", section.getConfigurationSection(path));

                pathSlots.add(fullPath);
                itemStacks.add(itemBuilder.build());
            }
        }
    }

}
