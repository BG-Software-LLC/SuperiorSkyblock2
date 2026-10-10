package com.bgsoftware.superiorskyblock.core.menu.parser;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramButton;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramMenuStyle;
import com.bgsoftware.superiorskyblock.api.menu.layout.HologramMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.MenuSlotsMap;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.core.menu.hologram.HologramMenuStyleImpl;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HologramMenuParser {

    public static <V extends MenuView<V, ?>> MenuSlotsMap parseRegularMenuPatternInternal(
            String callerName, YamlConfiguration cfg, HologramMenuLayout.Builder<V> menuLayoutBuilder) {
        MenuSlotsMap menuSlotsMap = new MenuSlotsMap();

        HologramMenuStyle style = parseStyle(callerName, cfg.getConfigurationSection("hologram"));
        menuLayoutBuilder.setStyle(style);

        if (cfg.isString("body")) {
            addBodyLines(callerName, cfg.getString("body"), menuLayoutBuilder);
        } else if (cfg.isList("body")) {
            for (Object entry : cfg.getList("body")) {
                addBodyLines(callerName, entry, menuLayoutBuilder);
            }
        }

        List<String> pattern = cfg.getStringList("pattern");

        int slot = 0;

        if (!pattern.isEmpty()) {
            // Each line of the pattern is a row of buttons.
            for (String patternLine : pattern) {
                patternLine = patternLine.replace(" ", "");
                List<Integer> row = new ArrayList<>(patternLine.length());
                for (int i = 0; i < patternLine.length(); ++i) {
                    char buttonId = patternLine.charAt(i);
                    menuLayoutBuilder.setButton(slot, parseButton(callerName, cfg, buttonId, style));
                    menuSlotsMap.addSlot(buttonId, slot);
                    row.add(slot);
                    ++slot;
                }
                menuLayoutBuilder.addRow(row);
            }
        } else if (cfg.isConfigurationSection("buttons")) {
            // Each button is in a row of its own.
            for (String key : cfg.getConfigurationSection("buttons").getKeys(false)) {
                char buttonId = key.charAt(0);
                menuLayoutBuilder.setButton(slot, parseButton(callerName, cfg, buttonId, style));
                menuSlotsMap.addSlot(buttonId, slot);
                ++slot;
            }
        }

        return menuSlotsMap;
    }

    private static <V extends MenuView<V, ?>> MenuTemplateButton<V> parseButton(
            String callerName, YamlConfiguration cfg, char buttonId, HologramMenuStyle style) {
        ConfigurationSection buttonSection = cfg.getConfigurationSection("buttons." + buttonId);

        // The item of the button can also be configured the same way it is done in inventory-based menus.
        ConfigurationSection itemSection = buttonSection != null && buttonSection.isConfigurationSection("item") ?
                buttonSection.getConfigurationSection("item") : cfg.getConfigurationSection("items." + buttonId);
        TemplateItem buttonItem = MenuParserUtils.getItemStack(callerName, itemSection);

        AbstractMenuTemplateButton.AbstractBuilder<V> buttonBuilder = new DummyButton.Builder<>();

        if (buttonItem != null)
            buttonBuilder.setButtonItem(buttonItem);

        if (buttonSection != null) {
            String label = buttonSection.getString("label");
            if (label == null && buttonItem == null)
                label = String.valueOf(buttonId);

            HologramButton.Builder hologramButtonBuilder = parseButtonStyle(callerName, buttonSection, style.getDefaultButton());

            if (label != null) {
                hologramButtonBuilder.setLabel(Formatters.COLOR_FORMATTER.format(label));
            } else if (!buttonSection.contains("width")) {
                // Buttons that only have an item are squares by default.
                hologramButtonBuilder.setWidth((float) buttonSection.getDouble("height", style.getDefaultButton().getHeight()));
            }

            buttonBuilder.setButtonHologram(hologramButtonBuilder.build());
        }

        DialogMenuParser.parseButtonActions(cfg, buttonId, buttonBuilder);

        return buttonBuilder.build();
    }

    private static HologramMenuStyle parseStyle(String callerName, @Nullable ConfigurationSection section) {
        if (section == null)
            return HologramMenuStyleImpl.DEFAULT;

        HologramMenuStyle defaults = HologramMenuStyleImpl.DEFAULT;
        HologramMenuStyle.Builder styleBuilder = new HologramMenuStyleImpl.Builder();

        float distance = getFloat(section, "distance", defaults.getDistance());
        if (distance <= 0f) {
            Log.warnFromFile(callerName, "Invalid hologram distance: ", distance, " - using the default one...");
        } else {
            styleBuilder.setDistance(distance);
        }

        styleBuilder.setBackgroundColor(parseColor(callerName, section.getString("background.color"), defaults.getBackgroundColor()));
        styleBuilder.setMinWidth(getFloat(section, "background.width", defaults.getMinWidth()));
        styleBuilder.setPadding(getFloat(section, "background.padding", defaults.getPadding()));

        TemplateItem backgroundItem = MenuParserUtils.getItemStack(callerName, section.getConfigurationSection("background.item"));
        if (backgroundItem != null)
            styleBuilder.setBackgroundItem(backgroundItem.build());

        styleBuilder.setTitleScale(getFloat(section, "title.scale", defaults.getTitleScale()));
        styleBuilder.setTitleHeight(getFloat(section, "title.height", defaults.getTitleHeight()));
        styleBuilder.setTitleShadowed(section.getBoolean("title.shadow", defaults.isTitleShadowed()));

        styleBuilder.setBodyScale(getFloat(section, "body.scale", defaults.getBodyScale()));
        styleBuilder.setBodyLineHeight(getFloat(section, "body.line-height", defaults.getBodyLineHeight()));
        styleBuilder.setBodyShadowed(section.getBoolean("body.shadow", defaults.isBodyShadowed()));

        styleBuilder.setSectionsSpacing(getFloat(section, "spacing.sections", defaults.getSectionsSpacing()));
        styleBuilder.setRowsSpacing(getFloat(section, "spacing.rows", defaults.getRowsSpacing()));
        styleBuilder.setButtonsSpacing(getFloat(section, "spacing.buttons", defaults.getButtonsSpacing()));

        styleBuilder.setCursorText(Formatters.COLOR_FORMATTER.format(section.getString("cursor.text", defaults.getCursorText())));
        styleBuilder.setHoveredCursorText(Formatters.COLOR_FORMATTER.format(section.getString("cursor.hovered-text", defaults.getHoveredCursorText())));
        styleBuilder.setCursorScale(getFloat(section, "cursor.scale", defaults.getCursorScale()));

        styleBuilder.setTooltipEnabled(section.getBoolean("tooltip.enabled", defaults.isTooltipEnabled()));
        styleBuilder.setTooltipScale(getFloat(section, "tooltip.scale", defaults.getTooltipScale()));
        styleBuilder.setTooltipColor(parseColor(callerName, section.getString("tooltip.color"), defaults.getTooltipColor()));

        ConfigurationSection buttonsSection = section.getConfigurationSection("buttons");
        if (buttonsSection != null)
            styleBuilder.setDefaultButton(parseButtonStyle(callerName, buttonsSection, defaults.getDefaultButton()).build());

        return styleBuilder.build();
    }

    /**
     * Parse the appearance of a button.
     * Values that are not configured in {@code section} are taken from {@code defaults}.
     */
    private static HologramButton.Builder parseButtonStyle(String callerName, ConfigurationSection section, HologramButton defaults) {
        HologramButton.Builder hologramButtonBuilder = HologramButton.newBuilder(defaults)
                .setLabel(null)
                .setWidth(getFloat(section, "width", defaults.getWidth()))
                .setHeight(getFloat(section, "height", defaults.getHeight()))
                .setColor(parseColor(callerName, section.getString("color"), defaults.getColor()))
                .setHoveredColor(parseColor(callerName, section.getString("hovered-color"), defaults.getHoveredColor()))
                .setLabelScale(getFloat(section, "label-scale", defaults.getLabelScale()))
                .setItemScale(getFloat(section, "item-scale", defaults.getItemScale()))
                .setLabelShadowed(section.getBoolean("shadow", defaults.isLabelShadowed()));

        if (section.isList("tooltip")) {
            List<String> tooltip = new ArrayList<>();
            for (String tooltipLine : section.getStringList("tooltip"))
                tooltip.add(Formatters.COLOR_FORMATTER.format(tooltipLine));
            hologramButtonBuilder.setTooltip(tooltip);
        } else if (section.isString("tooltip")) {
            hologramButtonBuilder.setTooltip(Collections.singletonList(
                    Formatters.COLOR_FORMATTER.format(section.getString("tooltip"))));
        } else if (section.isBoolean("tooltip") && !section.getBoolean("tooltip")) {
            // The button has no tooltip at all.
            hologramButtonBuilder.setTooltip(Collections.emptyList());
        }

        return hologramButtonBuilder;
    }

    private static float getFloat(ConfigurationSection section, String path, float def) {
        return (float) section.getDouble(path, def);
    }

    /**
     * Parse a color in the format of #AARRGGBB or #RRGGBB.
     */
    private static int parseColor(String callerName, @Nullable String color, int def) {
        if (color == null)
            return def;

        String hex = color.startsWith("#") ? color.substring(1) : color;

        try {
            if (hex.length() == 6)
                return 0xFF000000 | Integer.parseInt(hex, 16);
            if (hex.length() == 8)
                return (int) Long.parseLong(hex, 16);
        } catch (NumberFormatException ignored) {
        }

        Log.warnFromFile(callerName, "Invalid color '", color, "', it must be in the format of #AARRGGBB or #RRGGBB - using the default one...");

        return def;
    }

    private static void addBodyLines(String callerName, Object entry, HologramMenuLayout.Builder<?> menuLayoutBuilder) {
        Object text = entry instanceof Map ? ((Map<?, ?>) entry).get("text") : entry;
        if (!(text instanceof String)) {
            Log.warnFromFile(callerName, "Invalid hologram body entry: ", entry, " - skipping...");
            return;
        }

        // Each line of the text is rendered as a separate line in the hologram.
        for (String bodyLine : ((String) text).split("\n")) {
            menuLayoutBuilder.addBodyLine(Formatters.COLOR_FORMATTER.format(bodyLine));
        }
    }

}
