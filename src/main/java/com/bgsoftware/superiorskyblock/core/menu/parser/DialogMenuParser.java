package com.bgsoftware.superiorskyblock.core.menu.parser;

import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogBodyElement;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButton;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogMenuType;
import com.bgsoftware.superiorskyblock.api.menu.layout.DialogMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.MenuSlotsMap;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.core.menu.dialog.body.DialogBodyItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class DialogMenuParser {

    public static <V extends MenuView<V, ?>> MenuSlotsMap parseRegularMenuPatternInternal(
            String callerName, YamlConfiguration cfg, DialogMenuLayout.Builder<V> menuLayoutBuilder) {
        MenuSlotsMap menuSlotsMap = new MenuSlotsMap();

        menuLayoutBuilder.setDialogType(readDialogMenuType(cfg, callerName));
        menuLayoutBuilder.setCloseWithEscapeAllowed(cfg.getBoolean("can-close-with-escape", true));

        if (cfg.isString("body")) {
            readDialogBodyElement(callerName, cfg.getString("body")).ifPresent(menuLayoutBuilder::addBodyElement);
        } else if (cfg.isList("body")) {
            for (Object entry : cfg.getList("body")) {
                readDialogBodyElement(callerName, entry).ifPresent(menuLayoutBuilder::addBodyElement);
            }
        }

        if (cfg.isConfigurationSection("buttons")) {
            int slot = 0;
            for (String key : cfg.getConfigurationSection("buttons").getKeys(false)) {
                char buttonId = key.charAt(0);

                ConfigurationSection buttonSection = cfg.getConfigurationSection("buttons." + key);

                AbstractMenuTemplateButton.AbstractBuilder<V> buttonBuilder = new DummyButton.Builder<>();

                buttonBuilder.setButtonDialog(DialogButton.newBuilder()
                        .setLabel(Formatters.COLOR_FORMATTER.format(buttonSection.getString("label", key)))
                        .build());

                buttonBuilder.setClickCommands(cfg.getStringList("commands." + buttonId));
                buttonBuilder.setClickSound(MenuParserUtils.getSound(cfg.getConfigurationSection("sounds." + buttonId)));
                buttonBuilder.setRequiredPermission(cfg.getString("permissions." + buttonId + ".permission"));
                buttonBuilder.setLackPermissionsSound(MenuParserUtils.getSound(cfg.getConfigurationSection("permissions." + buttonId + ".no-access-sound")));

                menuLayoutBuilder.setButton(slot, buttonBuilder.build());

                menuSlotsMap.addSlot(buttonId, slot);

                ++slot;
            }
        }

        return menuSlotsMap;
    }

    public static <V extends PagedMenuView<V, ?, E>, E> MenuSlotsMap parsePagedMenuPatternInternal(
            String callerName, YamlConfiguration cfg, DialogMenuLayout.Builder<V> menuLayoutBuilder) {
        throw new UnsupportedOperationException("Dialog menus do not support paged-menus currently");
    }

    private static DialogMenuType readDialogMenuType(YamlConfiguration cfg, String callerName) {
        String dialogMenuTypeName = cfg.getString("dialog-type", "MULTI_ACTION").toUpperCase(Locale.ENGLISH);
        DialogMenuType dialogMenuType = EnumHelper.getEnum(DialogMenuType.class, dialogMenuTypeName);
        if (dialogMenuType == null) {
            Log.warnFromFile(callerName, "Unknown dialog-type '", dialogMenuTypeName, "', defaulting to MULTI_ACTION");
            dialogMenuType = DialogMenuType.MULTI_ACTION;
        }
        return dialogMenuType;
    }

    private static Optional<DialogBodyElement> readDialogBodyElement(String callerName, Object entry) {
        if (entry instanceof String) {
            return Optional.of(DialogBodyElement.fromText(Formatters.COLOR_FORMATTER.format((String) entry), null));
        } else if (entry instanceof Map) {
            MemoryConfiguration section = convertMapToConfigSection((Map<String, ?>) entry);
            if (section.isConfigurationSection("item")) {
                TemplateItem templateItem = MenuParserUtils.getItemStack(callerName, section.getConfigurationSection("item"));
                if (templateItem == null) {
                    Log.warnFromFile(callerName, "Invalid dialog body item in file, skipping...");
                    return Optional.empty();
                }
                return Optional.of(new DialogBodyItem(templateItem, DialogBodyElement.ItemConfig.create()
                        .setDescription(readDialogBodyElement(callerName, section.get("description")).orElse(null))
                        .setShowDecorations(section.getBoolean("show-decorations", true))
                        .setShowTooltip(section.getBoolean("show-tooltip", true))
                        .setWidth(section.getInt("width", DialogBodyElement.ItemConfig.DEFAULT_WIDTH))
                        .setHeight(section.getInt("height", DialogBodyElement.ItemConfig.DEFAULT_HEIGHT))
                ));
            } else {
                String text = Formatters.COLOR_FORMATTER.format(section.getString("text"));
                return Optional.of(DialogBodyElement.fromText(text, DialogBodyElement.TextConfig.create()
                        .setWidth(section.getInt("width", DialogBodyElement.TextConfig.DEFAULT_WIDTH))
                ));
            }
        } else {
            Log.warnFromFile(callerName, "Invalid dialog body entry: ", entry, " - skipping...");
            return Optional.empty();
        }
    }

    private static MemoryConfiguration convertMapToConfigSection(Map<String, ?> map) {
        MemoryConfiguration section = new MemoryConfiguration();
        for (Map.Entry<String, ?> e : map.entrySet()) {
            Object value = e.getValue();
            if (value instanceof Map)
                value = convertMapToConfigSection((Map<String, ?>) value);
            section.set(e.getKey(), value);
        }
        return section;
    }

}
