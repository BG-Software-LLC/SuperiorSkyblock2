package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.IslandCreationButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Locale;

public class MenuIslandCreation extends SuperiorMenu<MenuIslandCreation> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static RegularMenuPattern<MenuIslandCreation> menuPattern;

    private final String islandName;

    private MenuIslandCreation(SuperiorPlayer superiorPlayer, String islandName) {
        super(menuPattern, superiorPlayer);
        this.islandName = islandName;
    }

    public String getIslandName() {
        return islandName;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, islandName);
    }

    @Override
    public void onButtonClickLackPermission(SuperiorMenuButton<MenuIslandCreation> menuButton,
                                            InventoryClickEvent clickEvent) {
        super.onButtonClickLackPermission(menuButton, clickEvent);

        if (menuButton instanceof IslandCreationButton) {
            ((IslandCreationButton) menuButton).getLackPermissionCommands()
                    .forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            command.replace("%player%", clickEvent.getWhoClicked().getName())));
        }
    }

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuIslandCreation> patternBuilder = new RegularMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "island-creation.yml",
                MenuIslandCreation::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        if (cfg.isConfigurationSection("items")) {
            for (String itemSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemSection = cfg.getConfigurationSection("items." + itemSectionName);

                if (!itemSection.isString("schematic"))
                    continue;

                Schematic schematic = plugin.getSchematics().getSchematic(itemSection.getString("schematic"));

                if (schematic == null) {
                    SuperiorSkyblockPlugin.log("&c[island-creation.yml] Invalid schematic for item " + itemSectionName);
                    continue;
                }

                IslandCreationButton.Builder buttonBuilder = new IslandCreationButton.Builder(schematic);

                {
                    String biomeName = itemSection.getString("biome", "PLAINS");
                    try {
                        Biome biome = Biome.valueOf(biomeName.toUpperCase(Locale.ENGLISH));
                        buttonBuilder.setBiome(biome);
                    } catch (IllegalArgumentException error) {
                        SuperiorSkyblockPlugin.log("&c[island-creation.yml] Invalid biome name for item " + itemSectionName + ": " + biomeName);
                    }
                }

                {
                    Object bonusWorth = itemSection.get("bonus", itemSection.get("bonus-worth", 0D));
                    if (bonusWorth instanceof Double) {
                        buttonBuilder.setBonusWorth(BigDecimal.valueOf((double) bonusWorth));
                    } else if (bonusWorth instanceof String) {
                        buttonBuilder.setBonusWorth(new BigDecimal((String) bonusWorth));
                    } else {
                        buttonBuilder.setBonusWorth(BigDecimal.ZERO);
                    }
                }

                {
                    Object bonusLevel = itemSection.get("bonus-level", 0D);
                    if (bonusLevel instanceof Double) {
                        buttonBuilder.setBonusLevel(BigDecimal.valueOf((double) bonusLevel));
                    } else if (bonusLevel instanceof String) {
                        buttonBuilder.setBonusLevel(new BigDecimal((String) bonusLevel));
                    } else {
                        buttonBuilder.setBonusLevel(BigDecimal.ZERO);
                    }
                }

                ConfigurationSection soundSection = cfg.getConfigurationSection("sounds." + itemSectionName);
                if (soundSection != null) {
                    buttonBuilder
                            .setAccessSound(MenuParser.getSound(soundSection.getConfigurationSection("access")))
                            .setNoAccessSound(MenuParser.getSound(soundSection.getConfigurationSection("no-access")));
                }

                ConfigurationSection commandSection = cfg.getConfigurationSection("commands." + itemSectionName);
                if (commandSection != null) {
                    buttonBuilder
                            .setAccessCommands(commandSection.getStringList("access"))
                            .setNoAccessCommands(commandSection.getStringList("no-access"));
                }

                patternBuilder.mapButtons(menuPatternSlots.getSlots(itemSectionName), buttonBuilder
                        .setOffset(itemSection.getBoolean("offset", false))
                        .setAccessItem(MenuParser.getItemStack("island-creation.yml",
                                itemSection.getConfigurationSection("access")))
                        .setNoAccessItem(MenuParser.getItemStack("island-creation.yml",
                                itemSection.getConfigurationSection("no-access"))));
            }
        }

        menuPattern = patternBuilder.build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, String islandName) {
        if (plugin.getSettings().isSkipOneItemMenus() && hasOnlyOneItem()) {
            String schematicName = getOnlyOneItem();
            simulateClick(superiorPlayer, islandName, schematicName, false);
        } else {
            new MenuIslandCreation(superiorPlayer, islandName).open(previousMenu);
        }
    }

    public static void simulateClick(SuperiorPlayer superiorPlayer, String islandName, String schematic, boolean rightClick) {
        IslandCreationButton button = getButtonForSchematic(schematic);
        if (button != null)
            button.clickButton(plugin, superiorPlayer.asPlayer(), rightClick, islandName, null);
    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/creation-gui.yml");

        if (!oldFile.exists())
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

        if (cfg.contains("creation-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("creation-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        if (cfg.contains("creation-gui.schematics")) {
            for (String schemName : cfg.getConfigurationSection("creation-gui.schematics").getKeys(false)) {
                ConfigurationSection section = cfg.getConfigurationSection("creation-gui.schematics." + schemName);
                char itemChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];
                section.set("schematic", schemName);
                MenuConverter.convertItemAccess(section, patternChars, itemChar, itemsSection, commandsSection, soundsSection);
            }
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

    private static boolean hasOnlyOneItem() {
        return menuPattern.getButtons().stream()
                .filter(button -> button instanceof IslandCreationButton)
                .count() == 1;
    }

    private static String getOnlyOneItem() {
        return menuPattern.getButtons().stream()
                .filter(button -> button instanceof IslandCreationButton)
                .map(button -> ((IslandCreationButton) button).getSchematic().getName())
                .findFirst().orElse(null);
    }

    private static IslandCreationButton getButtonForSchematic(String schematicName) {
        return (IslandCreationButton) menuPattern.getButtons().stream()
                .filter(button -> button instanceof IslandCreationButton &&
                        ((IslandCreationButton) button).getSchematic().getName().equals(schematicName))
                .findFirst().orElse(null);
    }

}
