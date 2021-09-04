package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.SettingsHandler;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class SuperiorMenuSettings extends PagedSuperiorMenu<ItemStack> {

    private static final List<String> pathSlots = new ArrayList<>();
    private static final String[] ignorePaths = new String[] { "database", "max-island-size", "island-roles",
            "worlds.normal-world", "commands-cooldown", "starter-chest", "event-commands" };
    private static final Map<UUID, Integer> lastPage = new HashMap<>();
    private static final Set<UUID> pageMove = new HashSet<>();
    private static final Set<UUID> activePlayers = new HashSet<>();

    private static CommentedConfiguration config;

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
            String path = pathSlots.get((currentPage - 1) * 36 + e.getRawSlot());

            if(path == null)
                return;

            if(config.isBoolean(path)){
                updateConfig(player, path, !config.getBoolean(path));
                activePlayers.add(player.getUniqueId());

                previousMove = false;
                player.closeInventory();
                reopenMenu(player);
            }
            else {
                activePlayers.add(player.getUniqueId());
                PlayerChat.listen(player, message -> onPlayerChat(player, message, path));

                player.closeInventory();
                player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Please enter a new value (-cancel to cancel):");

                if (config.isList(path) || config.isConfigurationSection(path)) {
                    player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " If you enter a value that is already in the list, it will be removed.");
                }
            }
        }catch(Exception ignored){}
    }

    @Override
    public void open(ISuperiorMenu previousMenu) {
        lastPage.put(superiorPlayer.getUniqueId(), currentPage);
        super.open(previousMenu);
    }

    @Override
    public void closeInventory(SuperiorPlayer superiorPlayer) {
        super.closeInventory(superiorPlayer);

        if(!activePlayers.remove(superiorPlayer.getUniqueId()) && !pageMove.remove(superiorPlayer.getUniqueId())) {
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
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
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

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu){
        openInventory(superiorPlayer, previousMenu, 1);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, int page){
        SuperiorMenuSettings superiorMenuSettings = new SuperiorMenuSettings(superiorPlayer);
        superiorMenuSettings.currentPage = page;
        superiorMenuSettings.open(previousMenu);
    }

    private static boolean onPlayerChat(Player player, Object message, String path){
        if(!message.toString().equalsIgnoreCase("-cancel")){
            if(config.isConfigurationSection(path)){
                Matcher matcher;
                if(!(matcher = Pattern.compile("(.*):(.*)").matcher(message.toString())).matches()){
                    player.sendMessage(ChatColor.RED + "Please follow the <sub-section>:<value> format");
                }else {
                    path = path + "." + matcher.group(1);
                    message = matcher.group(2);

                    if(config.get(path) != null && config.get(path).toString().equals(message)){
                        player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Removed the value " + matcher.group(1) + " from " + path);
                        message = null;
                    }else{
                        player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Added the value " + message.toString() + " to " + path);

                        try{
                            message = Integer.valueOf(message.toString());
                        }catch(IllegalArgumentException ex){
                            if(message.toString().equalsIgnoreCase("true") || message.toString().equalsIgnoreCase("false")){
                                message = Boolean.valueOf(message.toString());
                            }
                        }

                    }

                    config.set(path, message);
                }
            }

            else if(config.isList(path)){
                List<String> list = config.getStringList(path);

                if (list.contains(message.toString())) {
                    list.remove(message.toString());
                    player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Removed the value " + message + " from " + path);
                } else {
                    list.add(message.toString());
                    player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY + " Added the value " + message + " to " + path);
                }

                config.set(path, list);
            }

            else{
                boolean valid = true;
                if(config.isInt(path)){
                    try{
                        message = Integer.valueOf(message.toString());
                    }catch(IllegalArgumentException ex){
                        player.sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                else if(config.isDouble(path)) {
                    try {
                        message = Double.valueOf(message.toString());
                    } catch (IllegalArgumentException ex) {
                        player.sendMessage(ChatColor.RED + "Please specify a valid number");
                        valid = false;
                    }
                }

                if(valid) {
                    updateConfig(player, path, message);
                }
            }
        }

        reopenMenu(player);

        return true;
    }

    private static void updateConfig(Player player, String path, Object value){
        config.set(path, value);
        player.sendMessage("" + ChatColor.YELLOW + ChatColor.BOLD + "SuperiorSkyblock" + ChatColor.GRAY +
                " Changed value of " + path + " to " + value);
    }

    private static void reopenMenu(Player player){
        Executor.sync(() -> {
            Integer page = lastPage.remove(player.getUniqueId());

            if(page == null)
                page = 1;

            PlayerChat.remove(player);
            SuperiorMenuSettings.openInventory(plugin.getPlayers().getSuperiorPlayer(player), null, Math.max(1, page));
        });
    }

    private static void saveConfiguration(){
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
            plugin.setSettings(new SettingsHandler(plugin));
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private static void reloadConfiguration(){
        try {
            config.load(new File(plugin.getDataFolder(), "config.yml"));
        }catch(Exception ex){
            ex.printStackTrace();
        }
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
