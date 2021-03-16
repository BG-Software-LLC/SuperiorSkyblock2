package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class MenuBiomes extends SuperiorMenu {

    private static boolean currentBiomeGlow = false;

    private MenuBiomes(SuperiorPlayer superiorPlayer){
        super("menuBiomes", superiorPlayer);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        for(Biome biome : Biome.values()){
            String biomeName = biome.name().toLowerCase();
            if(containsData(biomeName + "-slot")) {
                int slot = (int) getData(biomeName + "-slot");
                String permission = (String) getData(biomeName + "-permission");

                if(slot == e.getRawSlot()){
                    if (superiorPlayer.hasPermission(permission)) {
                        Island island = superiorPlayer.getIsland();
                        assert island != null;
                        EventResult<Biome> event = EventsCaller.callIslandBiomeChangeEvent(superiorPlayer, island, biome);
                        if(!event.isCancelled()){
                            SoundWrapper soundWrapper = (SoundWrapper) getData(biomeName + "-has-access-item-sound");
                            if(soundWrapper != null)
                                soundWrapper.playSound(e.getWhoClicked());
                            //noinspection unchecked
                            List<String> commands = (List<String>) getData(biomeName + "-has-access-item-commands");
                            if (commands != null)
                                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                        command.replace("%player%", superiorPlayer.getName())));

                            island.setBiome(event.getResult());
                            Locale.CHANGED_BIOME.send(superiorPlayer, event.getResult().name().toLowerCase());

                            Executor.sync(() -> {
                                previousMove = false;
                                e.getWhoClicked().closeInventory();
                            }, 1L);

                            break;
                        }
                    }

                    SoundWrapper soundWrapper = (SoundWrapper) getData(biomeName + "-no-access-item-sound");
                    if(soundWrapper != null)
                        soundWrapper.playSound(e.getWhoClicked());
                    //noinspection unchecked
                    List<String> commands = (List<String>) getData(biomeName + "-no-access-item-commands");
                    if(commands != null)
                        commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName())));

                    break;
                }
            }
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu);
    }

    @Override
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        Inventory inv = super.buildInventory(titleReplacer);
        Island island = superiorPlayer.getIsland();

        if(island != null) {
            for (Biome biome : Biome.values()) {
                String biomeName = biome.name().toLowerCase();
                if (containsData(biomeName + "-has-access-item")) {
                    ItemBuilder biomeItem = (ItemBuilder) getData(biomeName + "-has-access-item");
                    String permission = (String) getData(biomeName + "-permission");
                    int slot = (int) getData(biomeName + "-slot");

                    if (!superiorPlayer.hasPermission(permission))
                        biomeItem = (ItemBuilder) getData(biomeName + "-no-access-item");

                    biomeItem = biomeItem.clone();

                    if (currentBiomeGlow && island.getBiome() == biome)
                        biomeItem.withEnchant(EnchantsUtils.getGlowEnchant(), 1);

                    inv.setItem(slot, biomeItem.build(superiorPlayer));
                }
            }
        }

       return inv;
    }

    public static void init(){
        MenuBiomes menuBiomes = new MenuBiomes(null);

        File file = new File(plugin.getDataFolder(), "menus/biomes.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/biomes.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        currentBiomeGlow = cfg.getBoolean("current-biome-glow", false);

        /*We must implement our own FileUtils.loadGUI for the menu, because of how complicated the menu is.*/

        menuBiomes.resetData();

        menuBiomes.setTitle(StringUtils.translateColors(cfg.getString("title", "")));
        menuBiomes.setInventoryType(InventoryType.valueOf(cfg.getString("type", "CHEST")));
        menuBiomes.setPreviousMoveAllowed(cfg.getBoolean("previous-menu", true));
        menuBiomes.setOpeningSound(FileUtils.getSound(cfg.getConfigurationSection("open-sound")));

        List<String> pattern = cfg.getStringList("pattern");

        menuBiomes.setRowsSize(pattern.size());
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

                    if(cfg.contains("items." + ch + ".biome")){
                        ConfigurationSection itemSection = cfg.getConfigurationSection("items." + ch);
                        ConfigurationSection soundSection = cfg.getConfigurationSection("sounds." + ch);
                        ConfigurationSection commandSection = cfg.getConfigurationSection("commands." + ch);
                        String biome = itemSection.getString("biome").toLowerCase();

                        menuBiomes.addData(biome + "-slot", slot);

                        menuBiomes.addData(biome + "-permission", itemSection.getString("required-permission"));
                        menuBiomes.addData(biome + "-has-access-item", FileUtils.getItemStack("biomes.yml", itemSection.getConfigurationSection("access")));
                        menuBiomes.addData(biome + "-no-access-item", FileUtils.getItemStack("biomes.yml", itemSection.getConfigurationSection("no-access")));

                        if(soundSection != null) {
                            menuBiomes.addData(biome + "-has-access-item-sound", FileUtils.getSound(soundSection.getConfigurationSection("access")));
                            menuBiomes.addData(biome + "-no-access-item-sound", FileUtils.getSound(soundSection.getConfigurationSection("no-access")));
                        }
                        if(commandSection != null) {
                            menuBiomes.addData(biome + "-has-access-item-commands", commandSection.getStringList("access"));
                            menuBiomes.addData(biome + "-no-access-item-commands", commandSection.getStringList("no-access"));
                        }
                    }

                    else{
                        menuBiomes.addFillItem(slot, FileUtils.getItemStack("biomes.yml", cfg.getConfigurationSection("items." + ch)));
                        menuBiomes.addCommands(slot, cfg.getStringList("commands." + ch));
                        menuBiomes.addSound(slot, FileUtils.getSound(cfg.getConfigurationSection("sounds." + ch)));

                        String permission = cfg.getString("permissions." + ch + ".permission");
                        SoundWrapper noAccessSound = FileUtils.getSound(cfg.getConfigurationSection("permissions." + ch + ".no-access-sound"));
                        menuBiomes.addPermission(slot, permission, noAccessSound);
                    }

                    slot++;
                }
            }
        }

        menuBiomes.setBackButton(backButton);

        if(plugin.getSettings().onlyBackButton && backButton == -1)
            SuperiorSkyblockPlugin.log("&c[biomes.yml] Menu doesn't have a back button, it's impossible to close it.");

        menuBiomes.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new MenuBiomes(superiorPlayer).open(previousMenu);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/biomes-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("biomes-gui.title"));

        int size = cfg.getInt("biomes-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("biomes-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("biomes-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        if(cfg.contains("biomes-gui.biomes")) {
            for (String biomeName : cfg.getConfigurationSection("biomes-gui.biomes").getKeys(false)){
                ConfigurationSection section = cfg.getConfigurationSection("biomes-gui.biomes." + biomeName);
                char itemChar = itemChars[charCounter++];
                section.set("biome", biomeName.toUpperCase());
                MenuConverter.convertItemAccess(section, patternChars, itemChar, itemsSection, commandsSection, soundsSection);
            }
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
