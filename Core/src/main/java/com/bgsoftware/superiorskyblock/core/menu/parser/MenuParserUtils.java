package com.bgsoftware.superiorskyblock.core.menu.parser;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.parser.MenuParseException;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.LazyReference;
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
        if (section == null) {
            return null;
        }

        TemplateItem templateItem;

        if (section.isString("source")) {
            templateItem = getItemStackUnsafe(fileName, section.getRoot().getConfigurationSection(section.getString("source")));
        } else {
            if (!section.isString("type")) {
                return null;
            }

            String materialName = section.getString("type");

            Material type;
            short data;
            try {
                materialName = MinecraftNamesMapper.getMinecraftName(materialName).map(minecraftKey ->
                                NAMES_MAPPER.get().getMappedName(Material.class, minecraftKey).orElse(minecraftKey)).orElse(materialName);

                if (materialName.contains(":")) {
                    String[] materialSections = materialName.toUpperCase(Locale.ENGLISH).split(":");

                    if (materialSections.length < 2) {
                        throw new IllegalArgumentException();
                    }

                    type = Material.valueOf(materialSections[0]);
                    data = Short.parseShort(materialSections[1]);
                } else {
                    type = Material.valueOf(materialName.toUpperCase(Locale.ENGLISH));
                    data = (short) section.getInt("data");
                }
            } catch (IllegalArgumentException error) {
                throw new MenuParseException("Couldn't convert '" + materialName.toUpperCase(Locale.ENGLISH) + "' into an item stack at "
                        + section.getCurrentPath() + ", check type and data section!");
            }

            templateItem = new TemplateItem(new ItemBuilder(type, data));
        }

        ItemBuilder itemBuilder = templateItem.getEditableBuilder();

        if (section.isString("name")) {
            itemBuilder.withName(section.getString("name"));
        }

        if (section.isList("lore")) {
            itemBuilder.withLore(section.getStringList("lore"));
        }

        if (section.isInt("amount")) {
            itemBuilder.withAmount(section.getInt("amount"));
        }

        if (section.isInt("customModel")) {
            itemBuilder.withCustomModel(section.getInt("customModel"));
        }

        if (section.isString("itemModel")) {
            itemBuilder.withItemModel(section.getString("itemModel"));
        }

        if (section.isString("skull")) {
            itemBuilder.asSkullOf(section.getString("skull"));
        }

        if (section.getBoolean("glow", false)) {
            itemBuilder.makeItemGlow();
        }

        if (section.getBoolean("hideTooltip", false)) {
            itemBuilder.setHideTooltip();
        }

        if (section.getBoolean("unbreakable", false)) {
            itemBuilder.setUnbreakable();
        }

        if (section.isConfigurationSection("bannerMeta")) {
            for (String color : section.getConfigurationSection("bannerMeta").getKeys(false)) {
                String dyeColorName = color.toUpperCase(Locale.ENGLISH);

                DyeColor dyeColor;
                try {
                    dyeColor = DyeColor.valueOf(dyeColorName);
                } catch (IllegalArgumentException error) {
                    Log.warnFromFile(fileName, "Couldn't convert '", dyeColorName,
                            "' into an dye color at ", section.getCurrentPath(), ".bannerMeta, skipping...");
                    continue;
                }

                String patternTypeName = section.getString("bannerMeta." + color);

                PatternType patternType;
                try {
                    patternType = PatternType.valueOf(patternTypeName);
                } catch (IllegalArgumentException error) {
                    Log.warnFromFile(fileName, "Couldn't convert '", patternTypeName,
                            "' into an pattern type at ", section.getCurrentPath(), ".bannerMeta, skipping...");
                    continue;
                }

                itemBuilder.withBannerMeta(dyeColor, patternType);
            }
        }

        if (section.isConfigurationSection("effects")) {
            ConfigurationSection effectsSection = section.getConfigurationSection("effects");
            for (String effect : effectsSection.getKeys(false)) {
                String effectName = effect.toUpperCase(Locale.ENGLISH);

                PotionEffectType potionEffectType;
                try {
                    potionEffectType = getMinecraftEnum(PotionEffectType.class, effectName, PotionEffectType::getByName);
                } catch (IllegalArgumentException error) {
                    Log.warnFromFile(fileName, "Couldn't convert '", effectName,
                            "' into an potion effect type at ", section.getCurrentPath(), ".effects, skipping...");
                    continue;
                }

                int duration = effectsSection.getInt(effect + ".duration", -1);
                int amplifier = effectsSection.getInt(effect + ".amplifier", 0);

                if (duration == -1) {
                    Log.warnFromFile(fileName, "Couldn't find duration for potion effect type '",
                            effectName, "' at ", section.getCurrentPath(), ".effects, skipping...");
                    continue;
                }

                itemBuilder.withPotionEffect(new PotionEffect(potionEffectType, duration, amplifier));
            }
        }

        if (section.isConfigurationSection("enchants")) {
            for (String enchant : section.getConfigurationSection("enchants").getKeys(false)) {
                String enchantmentName = enchant.toUpperCase(Locale.ENGLISH);

                try {
                    Enchantment enchantment = getMinecraftEnum(Enchantment.class, enchantmentName, Enchantment::getByName);

                    itemBuilder.withEnchant(enchantment, section.getInt("enchants." + enchant));
                } catch (IllegalArgumentException error) {
                    Log.warnFromFile(fileName, "Couldn't convert '", enchantmentName,
                            "' into an enchantment at ", section.getCurrentPath(), ".enchants', skipping...");
                }
            }
        }

        if (section.isString("entity")) {
            String entityName = section.getString("entity").toUpperCase(Locale.ENGLISH);

            try {
                itemBuilder.withEntityType(getMinecraftEnum(EntityType.class, entityName));
            } catch (IllegalArgumentException error) {
                Log.warnFromFile(fileName, "Couldn't convert '", entityName,
                        "' into an entity at " + section.getCurrentPath(), ".entity, skipping...");
            }
        }

        if (section.isList("flags")) {
            for (String flag : section.getStringList("flags")) {
                String flagName = flag.toUpperCase(Locale.ENGLISH);

                try {
                    itemBuilder.withFlags(ItemFlag.valueOf(flagName));
                } catch (IllegalArgumentException error) {
                    Log.warnFromFile(fileName, "Couldn't convert '", flagName,
                            "' into an item flag at ", section.getCurrentPath(), ".flags, skipping...");
                }
            }
        }

        if (section.isString("leatherColor")) {
            String leatherColor = section.getString("leatherColor").toUpperCase(Locale.ENGLISH);
            if (leatherColor.startsWith("#"))
                leatherColor = leatherColor.substring(1);

            try {
                itemBuilder.withLeatherColor(Integer.parseInt(leatherColor, 16));
            } catch (IllegalArgumentException error) {
                Log.warnFromFile(fileName, "Couldn't convert '", leatherColor,
                        "' into a color at ", section.getCurrentPath(), ".leatherColor, skipping...");
            }
        }

        if (section.isString("rarity")) {
            String rarityName = section.getString("rarity").toUpperCase(Locale.ENGLISH);

            try {
                itemBuilder.withRarity(rarityName);
            } catch (IllegalArgumentException error) {
                Log.warnFromFile(fileName, "Couldn't convert '", rarityName,
                        "' into an rarity at ", section.getCurrentPath(), ".rarity, skipping...");
            }
        }

        if (section.isConfigurationSection("trim")) {
            String trimMaterial = section.getString("trim.material");
            String trimPattern = section.getString("trim.pattern");

            if (trimMaterial == null) {
                Log.warnFromFile(fileName, "Couldn't find trim material for item with trim pattern at ",
                        section.getCurrentPath(), ".trim, skipping...");
            } else if (trimPattern == null) {
                Log.warnFromFile(fileName, "Couldn't find trim pattern for item with trim material at ",
                        section.getCurrentPath(), ".trim, skipping...");
            } else {
                try {
                    itemBuilder.withTrim(trimMaterial, trimPattern);
                } catch (IllegalArgumentException error) {
                    Log.warnFromFile(fileName, error.getMessage(), " at ", section.getCurrentPath(), ".trim, skipping...");
                }
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
