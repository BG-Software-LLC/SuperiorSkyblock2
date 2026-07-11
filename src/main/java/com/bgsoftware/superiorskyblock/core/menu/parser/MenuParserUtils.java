package com.bgsoftware.superiorskyblock.core.menu.parser;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.parser.MenuParseException;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.itemstack.MinecraftNamesMapper;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class MenuParserUtils {

    private static final LazyReference<MinecraftNamesMapper> NAMES_MAPPER = new LazyReference<MinecraftNamesMapper>() {
        @Override
        protected MinecraftNamesMapper create() {
            return new MinecraftNamesMapper();
        }
    };

    private MenuParserUtils() {

    }

    @Nullable
    public static TemplateItem getItemStack(String fileName, ConfigurationSection section) {
        try {
            return getItemStackUnsafe(fileName, section);
        } catch (MenuParseException error) {
            Log.errorFromFile(fileName, error.getMessage());
            return null;
        }
    }

    @Nullable
    public static GameSound getSound(ConfigurationSection section) {
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

    public static <T> T getMinecraftEnum(Class<T> type, String name) throws IllegalArgumentException {
        return getMinecraftEnum(type, name, mappedName -> EnumHelper.getEnum(type, mappedName));
    }

    public static TemplateItem getItemStackUnsafe(String fileName, ConfigurationSection section) throws MenuParseException {
        if (section == null)
            return null;

        TemplateItem templateItem;

        String sourceItem = section.getString("source");
        if (sourceItem != null) {
            templateItem = getItemStackUnsafe(fileName, section.getRoot().getConfigurationSection(sourceItem));
        } else {
            if (!section.isString("type"))
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
                throw new MenuParseException("Couldn't convert " + section.getCurrentPath() + " into an item stack. Check type & data sections!");
            }

            templateItem = new TemplateItem(new ItemBuilder(type, data));
        }

        ItemBuilder itemBuilder = templateItem.getEditableBuilder();

        if (section.isString("name"))
            itemBuilder.withName(Formatters.COLOR_FORMATTER.format(section.getString("name")));

        if (section.isList("lore"))
            itemBuilder.withLore(section.getStringList("lore"));

        if (section.isInt("amount"))
            itemBuilder.withAmount(section.getInt("amount"));

        if (section.isConfigurationSection("enchants")) {
            for (String enchantmentName : section.getConfigurationSection("enchants").getKeys(false)) {
                Enchantment enchantment;

                try {
                    enchantment = getMinecraftEnum(Enchantment.class, enchantmentName, Enchantment::getByName);
                } catch (IllegalArgumentException ex) {
                    Log.warnFromFile(fileName, "Couldn't convert ", section.getCurrentPath(),
                            ".enchants.", enchantmentName.toUpperCase(Locale.ENGLISH), " into an enchantment, skipping...");
                    continue;
                }

                itemBuilder.withEnchant(enchantment, section.getInt("enchants." + enchantmentName));
            }
        }

        if (section.getBoolean("glow", false)) {
            itemBuilder.makeItemGlow();
        }

        if (section.isList("flags")) {
            for (String flag : section.getStringList("flags")) {
                String flagName = flag.toUpperCase(Locale.ENGLISH);
                try {
                    itemBuilder.withFlags(ItemFlag.valueOf(flagName));
                } catch (IllegalArgumentException error) {
                    Log.warnFromFile(fileName, "Couldn't convert ", section.getCurrentPath(),
                            " (", flagName, ") into an item flag, skipping...");
                }
            }
        }

        if (section.isString("skull")) {
            itemBuilder.asSkullOf(section.getString("skull"));
        }

        if (section.getBoolean("unbreakable", false)) {
            itemBuilder.setUnbreakable();
        }

        if (section.getBoolean("hideTooltip", false)) {
            itemBuilder.setHideTooltip();
        }

        if (section.isConfigurationSection("effects")) {
            ConfigurationSection effectsSection = section.getConfigurationSection("effects");
            for (String effectName : effectsSection.getKeys(false)) {
                PotionEffectType potionEffectType;

                try {
                    potionEffectType = getMinecraftEnum(PotionEffectType.class, effectName, PotionEffectType::getByName);
                } catch (IllegalArgumentException error) {
                    Log.warnFromFile(fileName, "Couldn't convert ", effectsSection.getCurrentPath(),
                            ".", effectName.toUpperCase(Locale.ENGLISH), " into a potion effect, skipping...");
                    continue;
                }

                int duration = effectsSection.getInt(effectName + ".duration", -1);
                int amplifier = effectsSection.getInt(effectName + ".amplifier", 0);

                if (duration == -1) {
                    Log.warnFromFile(fileName, "Potion effect ", effectsSection.getCurrentPath(),
                            ".", effectName, " is missing duration, skipping...");
                    continue;
                }

                itemBuilder.withPotionEffect(new PotionEffect(potionEffectType, duration, amplifier));
            }
        }

        if (section.isString("entity")) {
            String entity = section.getString("entity");
            try {
                itemBuilder.withEntityType(getMinecraftEnum(EntityType.class, entity));
            } catch (IllegalArgumentException ex) {
                Log.warnFromFile(fileName, "Couldn't convert ", entity, " into an entity type, skipping...");
            }
        }

        if (section.isConfigurationSection("bannerMeta")) {
            for (String dyeColorName : section.getConfigurationSection("bannerMeta").getKeys(false)) {
                DyeColor dyeColor;
                PatternType patternType;

                try {
                    dyeColor = DyeColor.valueOf(dyeColorName.toUpperCase(Locale.ENGLISH));
                } catch (IllegalArgumentException error) {
                    Log.warnFromFile(fileName, "Couldn't convert ", section.getCurrentPath(),
                            ".bannerMeta.", dyeColorName.toUpperCase(Locale.ENGLISH), " into an dye color, skipping...");
                    continue;
                }

                try {
                    patternType = PatternType.valueOf(section.getString("bannerMeta." + dyeColorName));
                } catch (IllegalArgumentException error) {
                    Log.warnFromFile(fileName, "Couldn't convert ", section.getCurrentPath(),
                            ".bannerMeta.", dyeColorName.toUpperCase(Locale.ENGLISH), ".",
                            section.getString("bannerMeta." + dyeColorName), " into an pattern type, skipping...");
                    continue;
                }

                itemBuilder.withBannerMeta(dyeColor, patternType);
            }
        }

        if (section.isInt("customModel")) {
            itemBuilder.withCustomModel(section.getInt("customModel"));
        }

        if (section.isString("itemModel")) {
            itemBuilder.withItemModel(section.getString("itemModel"));
        }

        if (section.isString("rarity")) {
            String rarity = section.getString("rarity");

            try {
                itemBuilder.withRarity(rarity);
            } catch (IllegalArgumentException error) {
                Log.warnFromFile(fileName, "Couldn't convert ", rarity, " into a rarity, skipping...");
            }
        }

        if (section.isConfigurationSection("trim")) {
            String trimMaterial = section.getString("trim.material");
            String trimPattern = section.getString("trim.pattern");

            if (trimMaterial == null) {
                Log.warnFromFile(fileName, "Couldn't find trim material for item with trim pattern, skipping...");
            } else if (trimPattern == null) {
                Log.warnFromFile(fileName, "Couldn't find trim pattern for item with trim material, skipping...");
            } else {
                try {
                    itemBuilder.withTrim(trimMaterial, trimPattern);
                } catch (IllegalArgumentException error) {
                    Log.warnFromFile(fileName, error.getMessage());
                }
            }
        }

        if (section.isString("leatherColor")) {
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

    private static <T> T getMinecraftEnum(Class<T> type, String name, Function<String, T> enumCreator) throws IllegalArgumentException {
        String mappedName = MinecraftNamesMapper.getMinecraftName(name)
                .map(minecraftKey -> NAMES_MAPPER.get().getMappedName(type, minecraftKey).orElse(minecraftKey))
                .orElse(name);

        return Optional.ofNullable(enumCreator.apply(mappedName.toUpperCase(Locale.ENGLISH)))
                .orElseThrow(() -> new IllegalArgumentException("No enum constant " + type.getCanonicalName() + "." + name));
    }

}
