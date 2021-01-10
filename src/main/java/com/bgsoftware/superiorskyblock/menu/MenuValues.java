package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.HeadUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.key.KeySet;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;

import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MenuValues extends SuperiorMenu {

    private static final BigInteger MAX_STACK = BigInteger.valueOf(64);
    private final Island island;

    private MenuValues(SuperiorPlayer superiorPlayer, Island island){
        super("menuValues", superiorPlayer);
        this.island = island;
        if(island != null)
            updateTargetPlayer(island.getOwner());
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

    @Override
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        Inventory inventory = super.buildInventory(titleReplacer);

        for(int slot = 0; slot < inventory.getSize(); slot++){
            if(containsData(slot + "")){
                Key block = (Key) getData(slot + "");
                BigDecimal amount = new BigDecimal(block.getGlobalKey().contains("SPAWNER") ?
                        island.getExactBlockCountAsBigInteger(block) : island.getBlockCountAsBigInteger(block));
                if(inventory.getItem(slot) != null) {
                    ItemStack itemStack = new ItemBuilder(inventory.getItem(slot))
                            .replaceAll("{0}", amount + "")
                            .replaceAll("{1}", StringUtils.format(plugin.getBlockValues().getBlockWorth(block).multiply(amount)))
                            .replaceAll("{2}", StringUtils.format(plugin.getBlockValues().getBlockLevel(block).multiply(amount)))
                            .replaceAll("{3}", StringUtils.fancyFormat(plugin.getBlockValues().getBlockWorth(block).multiply(amount), superiorPlayer.getUserLocale()))
                            .replaceAll("{4}", StringUtils.fancyFormat(plugin.getBlockValues().getBlockLevel(block).multiply(amount), superiorPlayer.getUserLocale()))
                            .build();
                    itemStack.setAmount(BigInteger.ONE.max(MAX_STACK.min(amount.toBigInteger())).intValue());
                    inventory.setItem(slot, itemStack);
                }
            }
        }

        return inventory;
    }

    @Override
    public Inventory getInventory() {
        return buildInventory(title -> title
                .replace("{0}", island.getOwner().getName())
                .replace("{1}", StringUtils.format(island.getWorth()))
        );
    }

    public static void init(){
        MenuValues menuValues = new MenuValues(null, null);

        File file = new File(plugin.getDataFolder(), "menus/values.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/values.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        /*We must implement our own FileUtils.loadGUI for the menu, because of how complicated the menu is.*/

        menuValues.resetData();

        menuValues.setTitle(StringUtils.translateColors(cfg.getString("title", "")));
        menuValues.setInventoryType(InventoryType.valueOf(cfg.getString("type", "CHEST")));
        menuValues.setPreviousMoveAllowed(cfg.getBoolean("previous-menu", true));
        menuValues.setOpeningSound(FileUtils.getSound(cfg.getConfigurationSection("open-sound")));

        List<String> pattern = cfg.getStringList("pattern");
        int backButton = -1;
        char backButtonChar = cfg.getString("back", " ").charAt(0);

        menuValues.setRowsSize(pattern.size());

        KeySet keysToUpdate = new KeySet();

        for(int row = 0; row < pattern.size(); row++){
            String patternLine = pattern.get(row);
            int slot = row * 9;

            for(int i = 0; i < patternLine.length(); i++){
                char ch = patternLine.charAt(i);
                if(ch != ' '){
                    if(backButtonChar == ch){
                        backButton = slot;
                    }
                    else if(cfg.contains("items." + ch + ".block")) {
                        Key key = Key.of(cfg.getString("items." + ch + ".block"));
                        menuValues.addData(slot + "", key);
                        keysToUpdate.add(key);
                    }

                    menuValues.addFillItem(slot, FileUtils.getItemStack("values.yml", cfg.getConfigurationSection("items." + ch)));
                    menuValues.addCommands(slot, cfg.getStringList("commands." + ch));
                    menuValues.addSound(slot, FileUtils.getSound(cfg.getConfigurationSection("sounds." + ch)));

                    String permission = cfg.getString("permissions." + ch + ".permission");
                    SoundWrapper noAccessSound = FileUtils.getSound(cfg.getConfigurationSection("permissions." + ch + ".no-access-sound"));
                    menuValues.addPermission(slot, permission, noAccessSound);

                    slot++;
                }
            }
        }

        plugin.getBlockValues().registerMenuValueBlocks(keysToUpdate);

        menuValues.setBackButton(backButton);

        if(plugin.getSettings().onlyBackButton && backButton == -1)
            SuperiorSkyblockPlugin.log("&c[biomes.yml] Menu doesn't have a back button, it's impossible to close it.");

        menuValues.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuValues(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island){
        refreshMenus(MenuValues.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/values-gui.yml");

        if(!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("values-gui.title"));

        int size = cfg.getInt("values-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if(cfg.contains("values-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("values-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        ConfigurationSection blockItemSection = cfg.getConfigurationSection("values-gui.block-item");

        for(String material : cfg.getStringList("values-gui.materials")){
            char itemChar = itemChars[charCounter++];
            ConfigurationSection section = itemsSection.createSection(itemChar + "");
            String[] materialSections = material.split(":");
            String block = materialSections.length == 2 ? materialSections[0] : materialSections[0] + ":" + materialSections[1];
            int slot = Integer.parseInt(materialSections.length == 2 ? materialSections[1] : materialSections[2]);
            copySection(blockItemSection, section, str ->
                    str.replace("{0}", StringUtils.format(block)).replace("{1}", "{0}"));
            section.set("block", block);
            convertType(section, block);
            patternChars[slot] = itemChar;
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

    private static void copySection(ConfigurationSection source, ConfigurationSection dest, Function<String, String> stringReplacer){
        for(String key : source.getKeys(false)) {
            if(source.isConfigurationSection(key)){
                copySection(source.getConfigurationSection(key), dest.createSection(key), stringReplacer);
            }
            else if(source.isList(key)) {
                dest.set(key, source.getStringList(key).stream().map(stringReplacer).collect(Collectors.toList()));
            }
            else if(source.isString(key)){
                dest.set(key, stringReplacer.apply(source.getString(key)));
            }
            else{
                dest.set(key, source.getString(key));
            }
        }
    }

    private static void convertType(ConfigurationSection section, String block){
        String[] materialSections = block.split(":");
        String spawnerType = materialSections[0],
                entityType = (materialSections.length >= 2 ? materialSections[1] : "PIG").toUpperCase();
        if(spawnerType.equals(Materials.SPAWNER.toBukkitType() + "")){
            String texture = HeadUtils.getTexture(entityType);
            if(!texture.isEmpty()) {
                section.set("type", Materials.PLAYER_HEAD.toBukkitType().name());
                if (section.getString("type").equalsIgnoreCase("SKULL_ITEM"))
                    section.set("data", 3);
                section.set("skull", texture);
                return;
            }
        }

        section.set("type", spawnerType.equals(Materials.SPAWNER.toBukkitType() + "") ? spawnerType : block);
    }

}
