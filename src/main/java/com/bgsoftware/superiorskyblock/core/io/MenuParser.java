package com.bgsoftware.superiorskyblock.core.io;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.GlowEnchantment;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BackButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
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
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

public class MenuParser {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private MenuParser() {

    }

    @Nullable
    public static <M extends ISuperiorMenu> MenuParseResult loadMenu(
            SuperiorMenuPattern.AbstractBuilder<?, ?, M> menuPattern,
            String fileName,
            @Nullable BiFunction<SuperiorSkyblockPlugin, YamlConfiguration, Boolean> convertOldMenu) {
        return loadMenu(menuPattern, fileName, false, convertOldMenu);
    }

    @Nullable
    public static <M extends ISuperiorMenu> MenuParseResult loadMenu(
            SuperiorMenuPattern.AbstractBuilder<?, ?, M> menuPattern,
            String fileName,
            boolean customMenu,
            @Nullable BiFunction<SuperiorSkyblockPlugin, YamlConfiguration, Boolean> convertOldMenu) {
        String menuPath = customMenu ? "custom/" : "";

        File file = new File(plugin.getDataFolder(), "menus/" + menuPath + fileName);

        if (!file.exists() && !customMenu)
            Resources.saveResource("menus/" + fileName);

        CommentedConfiguration cfg = new CommentedConfiguration();

        try {
            cfg.load(file);
        } catch (InvalidConfigurationException error) {
            SuperiorSkyblockPlugin.log("&c[" + fileName + "] There is an issue with the format of the file.");
            PluginDebugger.debug(error);
            return null;
        } catch (IOException error) {
            SuperiorSkyblockPlugin.log("&c[" + fileName + "] An unexpected error occurred while parsing the file:");
            PluginDebugger.debug(error);
            error.printStackTrace();
            return null;
        }

        if (convertOldMenu != null && convertOldMenu.apply(plugin, cfg)) {
            try {
                cfg.save(file);
            } catch (Exception ex) {
                ex.printStackTrace();
                PluginDebugger.debug(ex);
            }
        }

        menuPattern.setTitle(Formatters.COLOR_FORMATTER.format(cfg.getString("title", "")))
                .setInventoryType(InventoryType.valueOf(cfg.getString("type", "CHEST")))
                .setPreviousMoveAllowed(cfg.getBoolean("previous-menu", true))
                .setOpeningSound(getSound(cfg.getConfigurationSection("open-sound")));

        MenuPatternSlots menuPatternSlots = new MenuPatternSlots();
        List<String> pattern = cfg.getStringList("pattern");

        menuPattern.setRowsSize(pattern.size());

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

                SuperiorMenuButton.AbstractBuilder<?, ?, M> buttonBuilder = isBackButton ?
                        new BackButton.Builder<>() : new DummyButton.Builder<>();

                menuPattern.setButton(slot, buttonBuilder
                        .setButtonItem(getItemStack(fileName, cfg.getConfigurationSection("items." + ch)))
                        .setCommands(cfg.getStringList("commands." + ch))
                        .setClickSound(getSound(cfg.getConfigurationSection("sounds." + ch)))
                        .setRequiredPermission(cfg.getString("permissions." + ch + ".permission"))
                        .setLackPermissionsSound(getSound(cfg.getConfigurationSection("permissions." + ch + ".no-access-sound"))));

                menuPatternSlots.addSlot(ch, slot);
            }
        }

        if (plugin.getSettings().isOnlyBackButton() && !backButtonFound) {
            SuperiorSkyblockPlugin.log("&c[" + fileName + "] Menu doesn't have a back button, it's impossible to close it.");
            return null;
        }

        return new MenuParseResult(menuPatternSlots, cfg);
    }

    @Nullable
    public static TemplateItem getItemStack(String fileName, ConfigurationSection section) {
        if (section == null)
            return null;

        TemplateItem templateItem;

        String sourceItem = section.getString("source");
        if (sourceItem != null) {
            templateItem = getItemStack(fileName, section.getRoot().getConfigurationSection(sourceItem));

            if (templateItem == null)
                return null;
        } else {
            if (!section.contains("type"))
                return null;

            Material type;
            short data;

            try {
                type = Material.valueOf(section.getString("type"));
                data = (short) section.getInt("data");
            } catch (IllegalArgumentException ex) {
                SuperiorSkyblockPlugin.log("&c[" + fileName + "] Couldn't convert " + section.getCurrentPath() + " into an itemstack. Check type & data sections!");
                PluginDebugger.debug(ex);
                return null;
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
                    enchantment = Enchantment.getByName(_enchantment);
                } catch (Exception ex) {
                    SuperiorSkyblockPlugin.log("&c[" + fileName + "] Couldn't convert " + section.getCurrentPath() + ".enchants." + _enchantment + " into an enchantment, skipping...");
                    PluginDebugger.debug(ex);
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
                PotionEffectType potionEffectType = PotionEffectType.getByName(_effect);

                if (potionEffectType == null) {
                    SuperiorSkyblockPlugin.log("&c[" + fileName + "] Couldn't convert " + effectsSection.getCurrentPath() + "." + _effect + " into a potion effect, skipping...");
                    continue;
                }

                int duration = effectsSection.getInt(_effect + ".duration", -1);
                int amplifier = effectsSection.getInt(_effect + ".amplifier", 0);

                if (duration == -1) {
                    SuperiorSkyblockPlugin.log("&c[" + fileName + "] Potion effect " + effectsSection.getCurrentPath() + "." + _effect + " is missing duration, skipping...");
                    continue;
                }

                itemBuilder.withPotionEffect(new PotionEffect(potionEffectType, duration, amplifier));
            }
        }

        if (section.contains("entity")) {
            String entity = section.getString("entity");
            try {
                itemBuilder.withEntityType(EntityType.valueOf(entity.toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException ex) {
                SuperiorSkyblockPlugin.log("&c[" + fileName + "] Couldn't convert " + entity + " into an entity type, skipping...");
                PluginDebugger.debug(ex);
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
                SuperiorSkyblockPlugin.log("&c[" + fileName + "] Couldn't convert " + leatherColor + " into a color, skipping...");
                PluginDebugger.debug(error);
            }
        }

        return templateItem;
    }

    @Nullable
    public static GameSound getSound(ConfigurationSection section) {
        if (section == null)
            return null;

        String soundType = section.getString("type");

        if (soundType == null)
            return null;

        Sound sound = null;

        try {
            sound = Sound.valueOf(soundType);
        } catch (Exception error) {
            PluginDebugger.debug(error);
        }

        if (sound == null)
            return null;

        return new GameSound(sound, (float) section.getDouble("volume", 1),
                (float) section.getDouble("pitch", 1));
    }

}
