package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class MenuIslandCreation extends SuperiorMenu {

    private final String islandName;

    private MenuIslandCreation(SuperiorPlayer superiorPlayer, String islandName){
        super("menuIslandCreation", superiorPlayer);
        this.islandName = islandName;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        for(String schematic : plugin.getSchematics().getSchematics()){
            if(containsData(schematic + "-slot")) {
                int slot = (int) getData(schematic + "-slot");
                if(slot == e.getRawSlot()) {
                    clickSchematic(schematic, this, e.getClick() == ClickType.RIGHT ||
                            e.getClick() == ClickType.SHIFT_RIGHT, true);
                    break;
                }
            }
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, islandName);
    }

    @Override
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        Inventory inv = super.buildInventory(titleReplacer);

        for(String schematic : plugin.getSchematics().getSchematics()){
            if(containsData(schematic + "-has-access-item")) {
                String permission = (String) getData(schematic + "-permission");
                String schematicItemKey = superiorPlayer.hasPermission(permission) ? schematic + "-has-access-item" : schematic + "-no-access-item";
                ItemBuilder schematicItem = (ItemBuilder) getData(schematicItemKey);
                int slot = (int) getData(schematic + "-slot");
                inv.setItem(slot, schematicItem.clone().build(superiorPlayer));
            }
        }

       return inv;
    }

    private static void clickSchematic(String schematic, MenuIslandCreation menu, boolean rightClick, boolean fromInventory){
        // Checking for preview of islands.
        if(rightClick){
            Location previewLocation = plugin.getSettings().islandPreviewLocations.get(schematic);
            if(previewLocation != null){
                plugin.getGrid().startIslandPreview(menu.superiorPlayer, schematic, menu.islandName);
                return;
            }
        }

        Player player = menu.superiorPlayer.asPlayer();

        if(player == null)
            return;

        String permission = (String) menu.getData(schematic + "-permission", "");
        if (menu.superiorPlayer.hasPermission(permission)) {
            BigDecimal bonusWorth = BigDecimal.valueOf((double) menu.getData(schematic + "-bonus-worth", 0D));
            BigDecimal bonusLevel = BigDecimal.valueOf((double) menu.getData(schematic + "-bonus-level", 0D));
            boolean offset = (boolean) menu.getData(schematic + "-offset", false);

            Biome biome = Biome.valueOf((String) menu.getData(schematic + "-biome", "PLAINS"));

            SoundWrapper sound = (SoundWrapper) menu.getData(schematic + "-has-access-item-sound");
            if (sound != null)
                sound.playSound(player);
            //noinspection unchecked
            List<String> commands = (List<String>) menu.getData(schematic + "-has-access-item-commands");
            if (commands != null)
                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replace("%player%", menu.superiorPlayer.getName())));

            if(fromInventory) {
                menu.previousMove = false;
                player.closeInventory();
            }

            Locale.ISLAND_CREATE_PROCCESS_REQUEST.send(menu.superiorPlayer);
            plugin.getGrid().createIsland(menu.superiorPlayer, schematic, bonusWorth, bonusLevel, biome, menu.islandName, offset);
        }
        else{
            SoundWrapper sound = (SoundWrapper) menu.getData(schematic + "-no-access-item-sound");
            if(sound != null)
                sound.playSound(player);
            //noinspection unchecked
            List<String> commands = (List<String>) menu.getData(schematic + "-no-access-item-commands");
            if(commands != null)
                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        command.replace("%player%", menu.superiorPlayer.getName())));
        }
    }

    private boolean hasOnlyOneItem(){
        return plugin.getSchematics().getSchematics().stream()
                .filter(schematic ->  containsData(schematic + "-has-access-item"))
                .count() == 1;
    }

    private String getOnlyOneItem(){
        return plugin.getSchematics().getSchematics().stream()
                .filter(schematic ->  containsData(schematic + "-has-access-item"))
                .findFirst().orElse(null);
    }

    public static void init(){
        MenuIslandCreation menuIslandCreation = new MenuIslandCreation(null, "");

        File file = new File(plugin.getDataFolder(), "menus/island-creation.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/island-creation.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        /*We must implement our own FileUtils.loadGUI for the menu, because of how complicated the menu is.*/

        menuIslandCreation.resetData();

        menuIslandCreation.setTitle(StringUtils.translateColors(cfg.getString("title", "")));
        menuIslandCreation.setInventoryType(InventoryType.valueOf(cfg.getString("type", "CHEST")));
        menuIslandCreation.setPreviousMoveAllowed(cfg.getBoolean("previous-menu", true));
        menuIslandCreation.setOpeningSound(FileUtils.getSound(cfg.getConfigurationSection("open-sound")));

        List<String> pattern = cfg.getStringList("pattern");

        menuIslandCreation.setRowsSize(pattern.size());
        int backButton = -1;
        char backButtonChar = cfg.getString("back", " ").charAt(0);

        for(int row = 0; row < pattern.size(); row++){
            String patternLine = pattern.get(row);
            int slot = row * 9;

            for(int i = 0; i < patternLine.length(); i++){
                char ch = patternLine.charAt(i);
                if(ch != ' '){
                    if(backButtonChar == ch){
                        backButton = slot;
                    }
                    else if(cfg.contains("items." + ch + ".schematic")){
                        ConfigurationSection itemSection = cfg.getConfigurationSection("items." + ch);
                        ConfigurationSection soundSection = cfg.getConfigurationSection("sounds." + ch);
                        ConfigurationSection commandSection = cfg.getConfigurationSection("commands." + ch);
                        String schematic = itemSection.getString("schematic").toLowerCase();

                        menuIslandCreation.addData(schematic + "-slot", slot);
                        menuIslandCreation.addData(schematic + "-permission", itemSection.getString("required-permission", ""));
                        menuIslandCreation.addData(schematic + "-biome", itemSection.getString("biome", "PLAINS"));
                        menuIslandCreation.addData(schematic + "-bonus-worth", itemSection.getDouble("bonus", itemSection.getDouble("bonus-worth", 0D)));
                        menuIslandCreation.addData(schematic + "-bonus-level", itemSection.getDouble("bonus-level", 0D));
                        menuIslandCreation.addData(schematic + "-offset", itemSection.getBoolean("offset", false));
                        menuIslandCreation.addData(schematic + "-has-access-item", FileUtils.getItemStack("island-creation.yml", itemSection.getConfigurationSection("access")));
                        menuIslandCreation.addData(schematic + "-no-access-item", FileUtils.getItemStack("island-creation.yml", itemSection.getConfigurationSection("no-access")));

                        if(soundSection != null) {
                            menuIslandCreation.addData(schematic + "-has-access-item-sound", FileUtils.getSound(soundSection.getConfigurationSection("access")));
                            menuIslandCreation.addData(schematic + "-no-access-item-sound", FileUtils.getSound(soundSection.getConfigurationSection("no-access")));
                        }
                        if(commandSection != null) {
                            menuIslandCreation.addData(schematic + "-has-access-item-commands", commandSection.getStringList("access"));
                            menuIslandCreation.addData(schematic + "-no-access-item-commands", commandSection.getStringList("no-access"));
                        }
                    }

                    else{
                        menuIslandCreation.addFillItem(slot, FileUtils.getItemStack("island-creation.yml", cfg.getConfigurationSection("items." + ch)));
                        menuIslandCreation.addCommands(slot, cfg.getStringList("commands." + ch));
                        menuIslandCreation.addSound(slot, FileUtils.getSound(cfg.getConfigurationSection("sounds." + ch)));

                        String permission = cfg.getString("permissions." + ch + ".permission");
                        SoundWrapper noAccessSound = FileUtils.getSound(cfg.getConfigurationSection("permissions." + ch + ".no-access-sound"));
                        menuIslandCreation.addPermission(slot, permission, noAccessSound);
                    }

                    slot++;
                }
            }
        }

        menuIslandCreation.setBackButton(backButton);

        if(plugin.getSettings().onlyBackButton && backButton == -1)
            SuperiorSkyblockPlugin.log("&c[biomes.yml] Menu doesn't have a back button, it's impossible to close it.");

        menuIslandCreation.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, String islandName){
        MenuIslandCreation menuIslandCreation = new MenuIslandCreation(superiorPlayer, islandName);
        if(plugin.getSettings().skipOneItemMenus && menuIslandCreation.hasOnlyOneItem()){
            clickSchematic(menuIslandCreation.getOnlyOneItem(), menuIslandCreation, false, false);
        }
        else {
            menuIslandCreation.open(previousMenu);
        }
    }

    public static void simulateClick(SuperiorPlayer superiorPlayer, String islandName, String schematic, boolean rightClick){
        clickSchematic(schematic, new MenuIslandCreation(superiorPlayer, islandName), rightClick, false);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/creation-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("creation-gui.title"));

        int size = cfg.getInt("creation-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("creation-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("creation-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        if(cfg.contains("creation-gui.schematics")) {
            for (String schemName : cfg.getConfigurationSection("creation-gui.schematics").getKeys(false)){
                ConfigurationSection section = cfg.getConfigurationSection("creation-gui.schematics." + schemName);
                char itemChar = itemChars[charCounter++];
                section.set("schematic", schemName);
                MenuConverter.convertItemAccess(section, patternChars, itemChar, itemsSection, commandsSection, soundsSection);
            }
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
