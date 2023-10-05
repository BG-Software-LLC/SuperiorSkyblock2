package com.bgsoftware.superiorskyblock.core.io;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.parser.MenuParseException;
import com.bgsoftware.superiorskyblock.api.menu.parser.MenuParser;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.GlowEnchantment;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.itemstack.MinecraftNamesMapper;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BackButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.core.menu.layout.PagedMenuLayoutImpl;
import com.bgsoftware.superiorskyblock.core.menu.layout.RegularMenuLayoutImpl;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class MenuParserImpl implements MenuParser {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final MenuParserImpl INSTANCE = new MenuParserImpl();
    private static final LazyReference<MinecraftNamesMapper> NAMES_MAPPER = new LazyReference<MinecraftNamesMapper>() {
        @Override
        protected MinecraftNamesMapper create() {
            return new MinecraftNamesMapper();
        }
    };

    public static MenuParserImpl getInstance() {
        return INSTANCE;
    }

    private MenuParserImpl() {

    }

    @Override
    public <V extends MenuView<V, ?>> MenuParseResult<V> parseMenu(String callerName, YamlConfiguration cfg) throws MenuParseException {
        RegularMenuLayoutImpl.Builder<V> menuLayoutBuilder = new RegularMenuLayoutImpl.Builder<>();

        menuLayoutBuilder.setTitle(Formatters.COLOR_FORMATTER.format(cfg.getString("title", "")));
        menuLayoutBuilder.setInventoryType(getMinecraftEnum(InventoryType.class, cfg.getString("type", "CHEST")));

        MenuPatternSlots menuPatternSlots = new MenuPatternSlots();
        List<String> pattern = cfg.getStringList("pattern");

        menuLayoutBuilder.setRowsCount(pattern.size());

        for (int row = 0; row < pattern.size() && row < 6; row++) {
            String patternLine = pattern.get(row).replace(" ", "");
            for (int i = 0; i < patternLine.length() && i < 9; i++) {
                int slot = row * 9 + i;

                char ch = patternLine.charAt(i);

                AbstractMenuTemplateButton.AbstractBuilder<V> buttonBuilder = new DummyButton.Builder<>();

                buttonBuilder.setButtonItem(getItemStack(callerName, cfg.getConfigurationSection("items." + ch)));
                buttonBuilder.setClickCommands(cfg.getStringList("commands." + ch));
                buttonBuilder.setClickSound(getSound(cfg.getConfigurationSection("sounds." + ch)));
                buttonBuilder.setRequiredPermission(cfg.getString("permissions." + ch + ".permission"));
                buttonBuilder.setLackPermissionsSound(getSound(cfg.getConfigurationSection("permissions." + ch + ".no-access-sound")));

                menuLayoutBuilder.setButton(slot, buttonBuilder.build());

                menuPatternSlots.addSlot(ch, slot);
            }
        }

        boolean previousMoveAllowed = cfg.getBoolean("previous-menu", true);
        boolean skipOneItem = cfg.getBoolean("skip-one-item", false);
        GameSound openingSound = getSound(cfg.getConfigurationSection("open-sound"));

        return new MenuParseResult<>(menuLayoutBuilder, openingSound, previousMoveAllowed, skipOneItem, menuPatternSlots, cfg);
    }

    @Override
    public <V extends PagedMenuView<V, ?, E>, E> MenuParseResult<V> parseMenu(
            String callerName, YamlConfiguration cfg, PagedMenuTemplateButton.Builder<V, E> pagedButtonBuilder) throws MenuParseException {
        PagedMenuLayoutImpl.Builder<V, E> menuLayoutBuilder = new PagedMenuLayoutImpl.Builder<>();

        menuLayoutBuilder.setTitle(Formatters.COLOR_FORMATTER.format(cfg.getString("title", "")));
        menuLayoutBuilder.setInventoryType(getMinecraftEnum(InventoryType.class, cfg.getString("type", "CHEST")));

        MenuPatternSlots menuPatternSlots = new MenuPatternSlots();
        List<String> pattern = cfg.getStringList("pattern");

        menuLayoutBuilder.setRowsCount(pattern.size());

        String backButton = cfg.getString("back", "");
        boolean backButtonFound = false;

        for (int row = 0; row < pattern.size() && row < 6; row++) {
            String patternLine = pattern.get(row).replace(" ", "");
            for (int i = 0; i < patternLine.length() && i < 9; i++) {
                int slot = row * 9 + i;

                char ch = patternLine.charAt(i);

                boolean isBackButton = backButton.contains(ch + "");

                if (isBackButton) {
                    backButtonFound = true;
                }

                AbstractMenuTemplateButton.AbstractBuilder<V> buttonBuilder = isBackButton ?
                        new BackButton.Builder<>() : new DummyButton.Builder<>();

                buttonBuilder.setButtonItem(getItemStackUnsafe(callerName, cfg.getConfigurationSection("items." + ch)));
                buttonBuilder.setClickCommands(cfg.getStringList("commands." + ch));
                buttonBuilder.setClickSound(getSound(cfg.getConfigurationSection("sounds." + ch)));
                buttonBuilder.setRequiredPermission(cfg.getString("permissions." + ch + ".permission"));
                buttonBuilder.setLackPermissionsSound(getSound(cfg.getConfigurationSection("permissions." + ch + ".no-access-sound")));

                menuLayoutBuilder.setButton(slot, buttonBuilder.build());

                menuPatternSlots.addSlot(ch, slot);
            }
        }

        if (plugin.getSettings().isOnlyBackButton() && !backButtonFound) {
            throw new MenuParseException("Menu doesn't have a back button, it's impossible to close it.");
        }

        menuLayoutBuilder.setPreviousPageSlots(parseButtonSlots(cfg, "previous-page", menuPatternSlots));
        menuLayoutBuilder.setCurrentPageSlots(parseButtonSlots(cfg, "current-page", menuPatternSlots));
        menuLayoutBuilder.setNextPageSlots(parseButtonSlots(cfg, "next-page", menuPatternSlots));
        menuLayoutBuilder.setPagedObjectSlots(parseButtonSlots(cfg, "slots", menuPatternSlots), pagedButtonBuilder);

        if (cfg.isList("custom-order"))
            menuLayoutBuilder.setCustomLayoutOrder(cfg.getIntegerList("custom-order"));

        boolean previousMoveAllowed = cfg.getBoolean("previous-menu", true);
        boolean skipOneItem = cfg.getBoolean("skip-one-item", false);
        GameSound openingSound = getSound(cfg.getConfigurationSection("open-sound"));

        return new MenuParseResult<>(menuLayoutBuilder, openingSound, previousMoveAllowed, skipOneItem, menuPatternSlots, cfg);
    }

    @Nullable
    public <V extends MenuView<V, ?>> MenuParseResult<V> loadCustomMenu(String fileName, @Nullable IMenuConverter converter) {
        return loadMenuInternal(fileName, true, converter);
    }

    @Nullable
    public <V extends MenuView<V, ?>> MenuParseResult<V> loadMenu(String fileName, @Nullable IMenuConverter converter) {
        return loadMenuInternal(fileName, false, converter);
    }

    @Nullable
    public <V extends PagedMenuView<V, ?, E>, E> MenuParseResult<V> loadMenu(String fileName, @Nullable IMenuConverter converter,
                                                                             PagedMenuTemplateButton.Builder<V, E> pagedButtonItemBuilder) {
        File file = new File(plugin.getDataFolder(), "menus/" + fileName);

        CommentedConfiguration cfg = loadMenuFile(file, fileName, false, converter);

        if (cfg != null) {
            try {
                return parseMenu(fileName, cfg, pagedButtonItemBuilder);
            } catch (MenuParseException error) {
                Log.errorFromFile(fileName, error.getMessage());
            }
        }

        return null;
    }

    @Nullable
    public TemplateItem getItemStack(String fileName, ConfigurationSection section) {
        try {
            return getItemStackUnsafe(fileName, section);
        } catch (MenuParseException error) {
            Log.errorFromFile(fileName, error.getMessage());
            return null;
        }
    }

    @Nullable
    public GameSound getSound(ConfigurationSection section) {
        if (section == null)
            return null;

        String soundType = section.getString("type");

        if (soundType == null)
            return null;

        Sound sound;

        try {
            sound = getMinecraftEnum(Sound.class, soundType);
        } catch (Exception ignored) {
            return null;
        }

        return new GameSoundImpl(sound, (float) section.getDouble("volume", 1),
                (float) section.getDouble("pitch", 1));
    }

    @Nullable
    private <V extends MenuView<V, ?>> MenuParseResult<V> loadMenuInternal(String fileName, boolean customMenu,
                                                                           @Nullable IMenuConverter converter) {
        String menuPath = customMenu ? "custom/" : "";

        File file = new File(plugin.getDataFolder(), "menus/" + menuPath + fileName);

        CommentedConfiguration cfg = loadMenuFile(file, fileName, customMenu, converter);

        if (cfg != null) {
            try {
                return parseMenu(fileName, cfg);
            } catch (MenuParseException error) {
                Log.errorFromFile(fileName, error.getMessage());
            }
        }

        return null;
    }

    @Nullable
    private static CommentedConfiguration loadMenuFile(File file, String fileName, boolean customMenu,
                                                       @Nullable IMenuConverter converter) {
        if (!file.exists() && !customMenu)
            Resources.saveResource("menus/" + fileName);

        CommentedConfiguration cfg = new CommentedConfiguration();

        try {
            cfg.load(file);
        } catch (InvalidConfigurationException error) {
            Log.errorFromFile(error, fileName, "There is an issue with the format of the file:");
            return null;
        } catch (IOException error) {
            Log.errorFromFile(error, fileName, "An unexpected error occurred while parsing file:");
            return null;
        }

        if (converter != null && converter.convert(plugin, cfg)) {
            try {
                cfg.save(file);
            } catch (Exception error) {
                Log.errorFromFile(error, fileName, "An unexpected error occurred while saving file:");
            }
        }

        return cfg;
    }

    private static TemplateItem getItemStackUnsafe(String fileName, ConfigurationSection section) throws MenuParseException {
        if (section == null)
            return null;

        TemplateItem templateItem;

        String sourceItem = section.getString("source");
        if (sourceItem != null) {
            templateItem = getItemStackUnsafe(fileName, section.getRoot().getConfigurationSection(sourceItem));
        } else {
            if (!section.contains("type"))
                return null;

            Material type;
            short data;

            try {
                String materialType = section.getString("type");
                materialType = MinecraftNamesMapper.getMinecraftName(materialType)
                        .map(minecraftKey -> NAMES_MAPPER.get().getMappedName(Material.class, minecraftKey).orElse(minecraftKey))
                        .orElse(materialType);
                if (materialType.contains(":")) {
                    String[] materialSections = materialType.toUpperCase(Locale.ENGLISH).split(":");
                    if (materialSections.length < 2)
                        throw new IllegalArgumentException();
                    type = Material.valueOf(materialSections[0]);
                    data = Short.parseShort(materialSections[1]);
                } else {
                    type = Material.valueOf(materialType.toUpperCase(Locale.ENGLISH));
                    data = (short) section.getInt("data");
                }
            } catch (IllegalArgumentException error) {
                throw new MenuParseException("Couldn't convert " + section.getCurrentPath() + " into an itemstack. Check type & data sections!");
            }

            templateItem = new TemplateItem(new ItemBuilder(type, data));
        }

        ItemBuilder itemBuilder = templateItem.getEditableBuilder();

        if (section.contains("name"))
            itemBuilder.withName(Formatters.COLOR_FORMATTER.format(section.getString("name")));

        if (section.contains("lore"))
            itemBuilder.withLore(section.getStringList("lore"));

        if (section.contains("enchants")) {
            for (String _enchantment : section.getConfigurationSection("enchants").getKeys(false)) {
                Enchantment enchantment;

                try {
                    enchantment = getMinecraftEnum(Enchantment.class, _enchantment, Enchantment::getByName);
                } catch (IllegalArgumentException ex) {
                    Log.warnFromFile(fileName, "Couldn't convert ", section.getCurrentPath(),
                            ".enchants.", _enchantment, " into an enchantment, skipping...");
                    continue;
                }

                itemBuilder.withEnchant(enchantment, section.getInt("enchants." + _enchantment));
            }
        }

        if (section.getBoolean("glow", false)) {
            itemBuilder.withEnchant(GlowEnchantment.getGlowEnchant(), 1);
        }

        if (section.contains("flags")) {
            for (String flag : section.getStringList("flags"))
                itemBuilder.withFlags(ItemFlag.valueOf(flag));
        }

        if (section.contains("skull")) {
            itemBuilder.asSkullOf(section.getString("skull"));
        }

        if (section.getBoolean("unbreakable", false)) {
            itemBuilder.setUnbreakable();
        }

        if (section.contains("effects")) {
            ConfigurationSection effectsSection = section.getConfigurationSection("effects");
            for (String _effect : effectsSection.getKeys(false)) {
                PotionEffectType potionEffectType;

                try {
                    potionEffectType = getMinecraftEnum(PotionEffectType.class, _effect, PotionEffectType::getByName);
                } catch (IllegalArgumentException error) {
                    Log.warnFromFile(fileName, "Couldn't convert ", effectsSection.getCurrentPath(),
                            ".", _effect, " into a potion effect, skipping...");
                    continue;
                }

                int duration = effectsSection.getInt(_effect + ".duration", -1);
                int amplifier = effectsSection.getInt(_effect + ".amplifier", 0);

                if (duration == -1) {
                    Log.warnFromFile(fileName, "Potion effect ", effectsSection.getCurrentPath(),
                            ".", _effect, " is missing duration, skipping...");
                    continue;
                }

                itemBuilder.withPotionEffect(new PotionEffect(potionEffectType, duration, amplifier));
            }
        }

        if (section.contains("entity")) {
            String entity = section.getString("entity");
            try {
                itemBuilder.withEntityType(getMinecraftEnum(EntityType.class, entity));
            } catch (IllegalArgumentException ex) {
                Log.warnFromFile(fileName, "Couldn't convert ", entity, " into an entity type, skipping...");
            }
        }

        if (section.contains("customModel")) {
            itemBuilder.withCustomModel(section.getInt("customModel"));
        }

        if (section.contains("leatherColor")) {
            String leatherColor = section.getString("leatherColor");
            if (leatherColor.startsWith("#"))
                leatherColor = leatherColor.substring(1);

            try {
                itemBuilder.withLeatherColor(Integer.parseInt(leatherColor, 16));
            } catch (IllegalArgumentException error) {
                Log.warnFromFile(fileName, "Couldn't convert ", leatherColor, " into a color, skipping...");
            }
        }

        return templateItem;
    }

    public List<Integer> parseButtonSlots(ConfigurationSection section, String key, MenuPatternSlots menuPatternSlots) {
        return !section.contains(key) ? Collections.emptyList() : menuPatternSlots.getSlots(section.getString(key));
    }

    private static <E extends Enum<E>> E getMinecraftEnum(Class<E> type, String name) throws IllegalArgumentException {
        String mappedName = MinecraftNamesMapper.getMinecraftName(name)
                .map(minecraftKey -> NAMES_MAPPER.get().getMappedName(type, minecraftKey).orElse(minecraftKey))
                .orElse(name);

        return Enum.valueOf(type, mappedName.toUpperCase(Locale.ENGLISH));
    }

    private static <T> T getMinecraftEnum(Class<T> type, String name, Function<String, T> enumCreator) throws IllegalArgumentException {
        String mappedName = MinecraftNamesMapper.getMinecraftName(name)
                .map(minecraftKey -> NAMES_MAPPER.get().getMappedName(type, minecraftKey).orElse(minecraftKey))
                .orElse(name);

        return Optional.ofNullable(enumCreator.apply(mappedName.toUpperCase(Locale.ENGLISH)))
                .orElseThrow(() -> new IllegalArgumentException("No enum constant " + type.getCanonicalName() + "." + name));
    }

    public interface IMenuConverter {

        boolean convert(SuperiorSkyblockPlugin plugin, YamlConfiguration cfg);

    }

}
