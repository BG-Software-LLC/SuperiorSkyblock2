package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.DimensionSelectionMode;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BiomePagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.parser.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.parser.MenuParserUtils;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.IIslandMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class MenuBiomes extends AbstractPagedMenu<MenuBiomes.View, MenuBiomes.Args, MenuBiomes.BiomeInfo> {

    private final List<BiomeInfo> biomes;
    private final boolean currentBiomeGlow;
    private final DimensionSelectionMode dimensionSelectionMode;

    private MenuBiomes(MenuParseResult<View> parseResult, List<BiomeInfo> islandFlags,
                       boolean currentBiomeGlow, DimensionSelectionMode dimensionSelectionMode) {
        super(MenuIdentifiers.MENU_BIOMES, parseResult, false);
        this.biomes = islandFlags;
        this.currentBiomeGlow = currentBiomeGlow;
        this.dimensionSelectionMode = dimensionSelectionMode;
    }

    public boolean isCurrentBiomeGlow() {
        return currentBiomeGlow;
    }

    public DimensionSelectionMode getDimensionSelectionMode() {
        return dimensionSelectionMode;
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, MenuBiomes.Args args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    @Nullable
    public static MenuBiomes createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("biomes.yml",
                MenuBiomes::convertOldGUI, new BiomePagedObjectButton.Builder());

        if (menuParseResult == null) {
            return null;
        }

        YamlConfiguration cfg = menuParseResult.getConfig();

        boolean shouldCurrentBiomeGlow = cfg.getBoolean("current-biome-glow", false);
        DimensionSelectionMode dimensionSelectionMode;

        try {
            dimensionSelectionMode = DimensionSelectionMode.valueOf(cfg.getString("dimension-selection-mode", "NONE"));
        } catch (IllegalArgumentException e) {
            dimensionSelectionMode = DimensionSelectionMode.DEFAULT;
        }

        List<BiomeInfo> biomes = new LinkedList<>();
        Optional.ofNullable(cfg.getConfigurationSection("biomes")).ifPresent(biomesSection -> {
            for (String biomeName : biomesSection.getKeys(false)) {
                Biome biome = plugin.getNMSAlgorithms().getBiome(biomeName);

                if (biome == null) {
                    Log.warnFromFile("biomes.yml", "Couldn't convert '" + biomeName +
                            "' into an Biome, skipping...");
                    continue;
                }

                Optional.ofNullable(biomesSection.getConfigurationSection(biomeName)).ifPresent(
                        biomeSection -> biomes.add(loadBiomeInfo(biomeSection, biome, biomes.size())));
            }
        });

        return new MenuBiomes(menuParseResult, biomes, shouldCurrentBiomeGlow, dimensionSelectionMode);
    }

    private static BiomeInfo loadBiomeInfo(ConfigurationSection biomeSection, Biome biome, int position) {
        TemplateItem accessItem = null;
        TemplateItem noAccessItem = null;
        GameSound accessSound = null;
        GameSound noAccessSound = null;
        List<String> accessCommands = null;
        List<String> noAccessCommands = null;
        String requiredPermission = null;
        List<Dimension> dimensions = new ArrayList<>();

        if (biomeSection != null) {
            accessItem = MenuParserUtils.getItemStack("menus/biomes.yml",
                    biomeSection.getConfigurationSection("access"));
            noAccessItem = MenuParserUtils.getItemStack("menus/biomes.yml",
                    biomeSection.getConfigurationSection("no-access"));
            accessSound = MenuParserUtils.getSound(
                    biomeSection.getConfigurationSection("access.sound"));
            noAccessSound = MenuParserUtils.getSound(
                    biomeSection.getConfigurationSection("no-access.sound"));
            accessCommands = biomeSection.getStringList("access.commands");
            noAccessCommands = biomeSection.getStringList("no-access.commands");
            requiredPermission = biomeSection.getString("permission");
            if (biomeSection.isString("dimension")) {
                Dimension dimension = getDimensionSafe(biomeSection.getString("dimension"));
                if (dimension != null) {
                    dimensions.add(dimension);
                }
            } else if (biomeSection.isList("dimensions")) {
                for (String dimensionName : biomeSection.getStringList("dimensions")) {
                    Dimension dimension = getDimensionSafe(dimensionName);
                    if (dimension != null) {
                        dimensions.add(dimension);
                    }
                }
            }
        }

        return new BiomeInfo(biome, accessItem, noAccessItem, accessSound, noAccessSound,
                accessCommands, noAccessCommands, requiredPermission, dimensions, position);
    }

    public static class Args extends IslandViewArgs {

        private final Dimension dimension;

        public Args(Island island, Dimension dimension) {
            super(island);
            this.dimension = dimension;
        }

    }

    public class View extends AbstractPagedMenuView<MenuBiomes.View, Args, BiomeInfo> implements IIslandMenuView {

        private final Island island;
        private final Dimension dimension;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, Args> menu, Args args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
            this.dimension = args.dimension;
            this.cachedTitleArgs = new Object[]{Formatters.CAPITALIZED_FORMATTER.format(dimension.getName())};
        }

        @Override
        public Island getIsland() {
            return island;
        }

        public Dimension getDimension() {
            return dimension;
        }

        @Override
        protected List<BiomeInfo> requestObjects() {
            List<BiomeInfo> requestObjects = new LinkedList<>();
            for (BiomeInfo biomeInfo : biomes) {
                if ((biomeInfo.getDimensions().isEmpty() && dimension == plugin.getSettings().getWorlds().getDefaultWorldDimension()) ||
                        biomeInfo.getDimensions().contains(dimension)) {
                    requestObjects.add(biomeInfo);
                }
            }

            return Collections.unmodifiableList(requestObjects);
        }

    }

    public static class BiomeInfo implements Comparable<BiomeInfo> {

        @Nullable
        private final Biome biome;
        private final TemplateItem accessItem;
        private final TemplateItem noAccessItem;
        private final GameSound accessSound;
        private final GameSound noAccessSound;
        private final List<String> accessCommands;
        private final List<String> noAccessCommands;
        private final String requiredPermission;
        private final List<Dimension> dimensions;
        private final int position;

        public BiomeInfo(Biome biome, TemplateItem accessItem, TemplateItem noAccessItem,
                         GameSound accessSound, GameSound noAccessSound, List<String> accessCommands,
                         List<String> noAccessCommands, String requiredPermission, List<Dimension> dimensions,
                         int position) {
            this.biome = biome;
            this.accessItem = accessItem;
            this.noAccessItem = noAccessItem;
            this.accessSound = accessSound;
            this.noAccessSound = noAccessSound;
            this.accessCommands = accessCommands;
            this.noAccessCommands = noAccessCommands;
            this.requiredPermission = requiredPermission;
            this.dimensions = dimensions;
            this.position = position;
        }

        public Biome getBiome() {
            return biome;
        }

        public ItemBuilder getAccessItem() {
            return accessItem.getBuilder();
        }

        public ItemBuilder getNoAccessItem() {
            return noAccessItem.getBuilder();
        }

        public GameSound getAccessSound() {
            return accessSound;
        }

        public GameSound getNoAccessSound() {
            return noAccessSound;
        }

        public List<String> getAccessCommands() {
            return accessCommands;
        }

        public List<String> getNoAccessCommands() {
            return noAccessCommands;
        }

        public String getRequiredPermission() {
            return requiredPermission;
        }

        public List<Dimension> getDimensions() {
            return dimensions;
        }

        @Override
        public int compareTo(@NotNull BiomeInfo other) {
            return Integer.compare(position, other.position);
        }

    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/biomes-gui.yml");

        if (!oldFile.exists())
            return convertToPagedMenu(newMenu);

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

        if (cfg.isConfigurationSection("biomes-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("biomes-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        if (cfg.isConfigurationSection("biomes-gui.biomes")) {
            for (String biomeName : cfg.getConfigurationSection("biomes-gui.biomes").getKeys(false)) {
                ConfigurationSection section = cfg.getConfigurationSection("biomes-gui.biomes." + biomeName);
                char itemChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
                section.set("biome", biomeName.toUpperCase(Locale.ENGLISH));
                MenuConverter.convertItemAccess(section, patternChars, itemChar, itemsSection, commandsSection, soundsSection);
            }
        }

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

    private static boolean convertToPagedMenu(YamlConfiguration cfg) {
        if (cfg.isConfigurationSection("biomes"))
            return false;

        Set<Character> biomeChars = new HashSet<>();

        // We want to transfer the biomes configuration from the old sections to the new 'biomes' section.
        if (cfg.isConfigurationSection("items")) {
            for (String index : cfg.getConfigurationSection("items").getKeys(false)) {
                if (cfg.isString("items." + index + ".biome")) {
                    biomeChars.add(index.charAt(0));

                    ConfigurationSection biomeSection = cfg.createSection("biomes." +
                            cfg.getString("items." + index + ".biome"));

                    if (cfg.isConfigurationSection("items." + index + ".access"))
                        biomeSection.set("access", cfg.getConfigurationSection("items." + index + ".access"));
                    if (cfg.isConfigurationSection("items." + index + ".no-access"))
                        biomeSection.set("no-access", cfg.getConfigurationSection("items." + index + ".no-access"));
                    cfg.set("items." + index, null);

                    if (cfg.isConfigurationSection("sounds." + index + ".access"))
                        biomeSection.set("access.sound", cfg.getConfigurationSection("sounds." + index + ".access"));
                    if (cfg.isConfigurationSection("sounds." + index + ".no-access"))
                        biomeSection.set("no-access.sound", cfg.getConfigurationSection("sounds." + index + ".no-access"));
                    cfg.set("sounds." + index, null);

                    if (cfg.isList("commands." + index + ".access"))
                        biomeSection.set("access.commands", cfg.getStringList("commands." + index + ".access"));
                    if (cfg.isList("commands." + index + ".no-access"))
                        biomeSection.set("no-access.commands", cfg.getStringList("commands." + index + ".no-access"));
                    cfg.set("commands." + index, null);

                    if (cfg.isString("permissions." + index + ".permission"))
                        biomeSection.set("permission", cfg.getString("permissions." + index + ".permission"));
                    cfg.set("permissions." + index, null);
                }
            }
        }

        // We want to convert pattern to paged format, if any biome was found.
        if (!biomeChars.isEmpty()) {
            List<String> pattern = cfg.getStringList("pattern");
            List<String> newPattern = new ArrayList<>(pattern.size());

            char newChar = biomeChars.iterator().next();

            for (String line : pattern) {
                StringBuilder newLine = new StringBuilder();
                char[] chars = line.replace(" ", "").toCharArray();

                for (int i = 0; i < chars.length; i++) {
                    char ch = chars[i];
                    newLine.append(biomeChars.contains(ch) ? newChar : ch);

                    if (i < chars.length - 1)
                        newLine.append(" ");
                }

                newPattern.add(newLine.toString());
            }

            cfg.set("pattern", newPattern);
            cfg.set("slots", String.valueOf(newChar));
            cfg.set("dimension-selection-mode", "DEFAULT");
        }

        return true;
    }

    @Nullable
    private static Dimension getDimensionSafe(String dimensionName) {
        try {
            return Dimension.getByName(dimensionName);
        } catch (Exception e) {
            Log.warnFromFile("biomes.yml", "Couldn't convert '" + dimensionName +
                    "' into an Dimension, skipping...");
            return null;
        }
    }

}
