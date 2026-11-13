package com.bgsoftware.superiorskyblock.core.menu.parser;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramButton;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramMenuStyle;
import com.bgsoftware.superiorskyblock.api.menu.layout.HologramMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.parser.MenuParseException;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.MenuSlotsMap;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BackButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.core.menu.hologram.HologramMenuStyleImpl;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class HologramMenuParser {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    /* The default size of cursors that are items, in blocks. */
    private static final float DEFAULT_CURSOR_ITEM_SIZE = 0.25f;

    public static <V extends MenuView<V, ?>> MenuSlotsMap parseRegularMenuPatternInternal(
            String callerName, YamlConfiguration cfg, HologramMenuLayout.Builder<V> menuLayoutBuilder) {
        return parseMenuPatternInternal(callerName, cfg, menuLayoutBuilder, buttonId -> new DummyButton.Builder<>());
    }

    public static <V extends PagedMenuView<V, ?, E>, E> MenuSlotsMap parsePagedMenuPatternInternal(
            String callerName, YamlConfiguration cfg, HologramMenuLayout.Builder<V> menuLayoutBuilder) throws MenuParseException {
        String backButton = cfg.getString("back", "");

        MenuSlotsMap menuSlotsMap = parseMenuPatternInternal(callerName, cfg, menuLayoutBuilder, buttonId ->
                backButton.indexOf(buttonId) != -1 ? new BackButton.Builder<>() : new DummyButton.Builder<>());

        if (plugin.getSettings().isOnlyBackButton() && menuSlotsMap.getSlots(backButton).isEmpty())
            throw new MenuParseException("Menu doesn't have a back button, it's impossible to close it.");

        return menuSlotsMap;
    }

    private static <V extends MenuView<V, ?>> MenuSlotsMap parseMenuPatternInternal(
            String callerName, YamlConfiguration cfg, HologramMenuLayout.Builder<V> menuLayoutBuilder,
            Function<Character, AbstractMenuTemplateButton.AbstractBuilder<V>> buttonBuilderCreator) {
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
                    menuLayoutBuilder.setButton(slot, parseButton(callerName, cfg, buttonId, style,
                            buttonBuilderCreator.apply(buttonId)));
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
                menuLayoutBuilder.setButton(slot, parseButton(callerName, cfg, buttonId, style,
                        buttonBuilderCreator.apply(buttonId)));
                menuSlotsMap.addSlot(buttonId, slot);
                ++slot;
            }
        }

        return menuSlotsMap;
    }

    private static <V extends MenuView<V, ?>> MenuTemplateButton<V> parseButton(
            String callerName, YamlConfiguration cfg, char buttonId, HologramMenuStyle style,
            AbstractMenuTemplateButton.AbstractBuilder<V> buttonBuilder) {
        ConfigurationSection buttonSection = cfg.getConfigurationSection("buttons." + buttonId);

        // The item of the button can also be configured the same way it is done in inventory-based menus.
        ConfigurationSection itemSection = buttonSection != null && buttonSection.isConfigurationSection("item") ?
                buttonSection.getConfigurationSection("item") : cfg.getConfigurationSection("items." + buttonId);
        TemplateItem buttonItem = MenuParserUtils.getItemStack(callerName, itemSection);

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

        parseCursor(callerName, section, styleBuilder, defaults);

        float cursorSpeed = getFloat(section, "cursor.speed", defaults.getCursorSpeed());
        if (cursorSpeed <= 0f) {
            Log.warnFromFile(callerName, "Invalid hologram cursor speed: ", cursorSpeed, " - using the default one...");
        } else {
            styleBuilder.setCursorSpeed(cursorSpeed);
        }

        String cursorArea = section.getString("cursor.area", defaults.isCursorLimitedToMenu() ? "MENU" : "SCREEN");
        if (cursorArea.equalsIgnoreCase("MENU")) {
            styleBuilder.setCursorLimitedToMenu(true);
        } else if (cursorArea.equalsIgnoreCase("SCREEN")) {
            styleBuilder.setCursorLimitedToMenu(false);
        } else {
            Log.warnFromFile(callerName, "Invalid hologram cursor area '", cursorArea, "', it must be SCREEN or MENU - using the default one...");
        }

        float screenFov = getFloat(section, "cursor.screen-fov", defaults.getScreenFov());
        if (screenFov <= 0f || screenFov >= 180f) {
            Log.warnFromFile(callerName, "Invalid hologram screen fov: ", screenFov, " - using the default one...");
        } else {
            styleBuilder.setScreenFov(screenFov);
        }

        float screenAspectRatio = getFloat(section, "cursor.screen-aspect-ratio", defaults.getScreenAspectRatio());
        if (screenAspectRatio <= 0f) {
            Log.warnFromFile(callerName, "Invalid hologram screen aspect ratio: ", screenAspectRatio, " - using the default one...");
        } else {
            styleBuilder.setScreenAspectRatio(screenAspectRatio);
        }

        styleBuilder.setCrosshairHidden(section.getBoolean("hide-crosshair", defaults.isCrosshairHidden()));

        int pageAnimationDuration = section.getInt("page-animation.duration", defaults.getPageAnimationDuration());
        if (pageAnimationDuration < 0) {
            Log.warnFromFile(callerName, "Invalid hologram page animation duration: ", pageAnimationDuration, " - using the default one...");
        } else {
            styleBuilder.setPageAnimationDuration(pageAnimationDuration);
        }
        styleBuilder.setPageAnimationDistance(getFloat(section, "page-animation.distance", defaults.getPageAnimationDistance()));

        styleBuilder.setTooltipEnabled(section.getBoolean("tooltip.enabled", defaults.isTooltipEnabled()));
        styleBuilder.setTooltipScale(getFloat(section, "tooltip.scale", defaults.getTooltipScale()));
        styleBuilder.setTooltipColor(parseColor(callerName, section.getString("tooltip.color"), defaults.getTooltipColor()));
        styleBuilder.setTooltipOffset(getFloat(section, "tooltip.offset", defaults.getTooltipOffset()));

        ConfigurationSection buttonsSection = section.getConfigurationSection("buttons");
        if (buttonsSection != null)
            styleBuilder.setDefaultButton(parseButtonStyle(callerName, buttonsSection, defaults.getDefaultButton()).build());

        return styleBuilder.build();
    }

    /**
     * Parse the cursor of a menu. Its type decides how it looks:
     * ARROW is the cursor of a mouse, CROSSHAIR is a plus sign, TEXT is any text, and ITEM is any item.
     */
    private static void parseCursor(String callerName, ConfigurationSection section,
                                    HologramMenuStyle.Builder styleBuilder, HologramMenuStyle defaults) {
        // Menus that only set the text of their cursor keep using it.
        String type = section.getString("cursor.type", section.isString("cursor.text") ? "TEXT" : "ARROW")
                .toUpperCase(Locale.ENGLISH);

        float defaultScale = defaults.getCursorScale();
        float defaultOffsetX = 0f;
        float defaultOffsetY = 0f;

        if (type.equals("ITEM")) {
            TemplateItem cursorItem = MenuParserUtils.getItemStack(callerName, section.getConfigurationSection("cursor.item"));
            if (cursorItem == null) {
                Log.warnFromFile(callerName, "Invalid item of the hologram cursor - using an arrow instead...");
                type = "ARROW";
            } else {
                TemplateItem hoveredCursorItem = MenuParserUtils.getItemStack(callerName, section.getConfigurationSection("cursor.hovered-item"));
                styleBuilder.setCursorItem(cursorItem.build());
                styleBuilder.setHoveredCursorItem((hoveredCursorItem == null ? cursorItem : hoveredCursorItem).build());
                // The scale of items is their size.
                defaultScale = DEFAULT_CURSOR_ITEM_SIZE;
            }
        }

        if (type.equals("TEXT")) {
            String text = section.getString("cursor.text", "+");
            styleBuilder.setCursorText(Formatters.COLOR_FORMATTER.format(text));
            styleBuilder.setHoveredCursorText(Formatters.COLOR_FORMATTER.format(section.getString("cursor.hovered-text", text)));
        } else if (!type.equals("ITEM")) {
            if (!type.equals("ARROW") && !type.equals("CROSSHAIR")) {
                Log.warnFromFile(callerName, "Invalid hologram cursor type '", type, "', it must be ARROW, CROSSHAIR, TEXT or ITEM - using an arrow instead...");
                type = "ARROW";
            }

            String symbol = type.equals("ARROW") ? HologramMenuStyleImpl.POINTER : "+";
            styleBuilder.setCursorText(Formatters.COLOR_FORMATTER.format(section.getString("cursor.color", "&7") + symbol));
            styleBuilder.setHoveredCursorText(Formatters.COLOR_FORMATTER.format(section.getString("cursor.hovered-color", "&f") + symbol));

            if (type.equals("ARROW")) {
                // The tip of the arrow is the point that clicks.
                defaultOffsetX = HologramMenuStyleImpl.ARROW_OFFSET_X;
                defaultOffsetY = HologramMenuStyleImpl.ARROW_OFFSET_Y;
            }
        }

        float cursorScale = getFloat(section, "cursor.scale", defaultScale);
        styleBuilder.setCursorScale(cursorScale);
        styleBuilder.setHoveredCursorScale(getFloat(section, "cursor.hovered-scale", cursorScale));
        styleBuilder.setCursorShadowed(section.getBoolean("cursor.shadow", defaults.isCursorShadowed()));
        styleBuilder.setCursorOffsetX(getFloat(section, "cursor.offset-x", defaultOffsetX));
        styleBuilder.setCursorOffsetY(getFloat(section, "cursor.offset-y", defaultOffsetY));
        styleBuilder.setCursorMenuMargin(getFloat(section, "cursor.menu-margin", defaults.getCursorMenuMargin()));
        styleBuilder.setCursorScreenMargin(getFloat(section, "cursor.screen-margin", defaults.getCursorScreenMargin()));

        long clickDelay = section.getLong("cursor.click-delay", defaults.getClickDelay());
        if (clickDelay < 0) {
            Log.warnFromFile(callerName, "Invalid hologram click delay: ", clickDelay, " - using the default one...");
        } else {
            styleBuilder.setClickDelay(clickDelay);
        }
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
