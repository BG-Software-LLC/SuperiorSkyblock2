package com.bgsoftware.superiorskyblock.core.menu.parser;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.parser.MenuParseException;
import com.bgsoftware.superiorskyblock.api.menu.parser.MenuParser;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.io.Resources;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuSlotsMap;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.layout.PagedDialogMenuLayoutImpl;
import com.bgsoftware.superiorskyblock.core.menu.layout.PagedInventoryMenuLayoutImpl;
import com.bgsoftware.superiorskyblock.core.menu.layout.RegularDialogMenuLayoutImpl;
import com.bgsoftware.superiorskyblock.core.menu.layout.RegularInventoryMenuLayoutImpl;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryType;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class MenuParserImpl implements MenuParser {

    private static final MenuParserImpl INSTANCE = new MenuParserImpl();

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static MenuParserImpl getInstance() {
        return INSTANCE;
    }

    private MenuParserImpl() {
    }

    @Override
    public <V extends MenuView<V, ?>> MenuParseResult<V> parseMenu(String callerName, YamlConfiguration cfg) throws MenuParseException {
        String menuType = cfg.getString("type", "CHEST");
        boolean isDialog = menuType.equalsIgnoreCase("DIALOG");

        AbstractMenuLayout.Builder<V> menuLayoutBuilder = isDialog ? new RegularDialogMenuLayoutImpl.Builder<>() :
                new RegularInventoryMenuLayoutImpl.Builder<>();

        menuLayoutBuilder.setTitle(Formatters.COLOR_FORMATTER.format(cfg.getString("title", "")));

        boolean previousMoveAllowed = cfg.getBoolean("previous-menu", true);
        boolean skipOneItem = cfg.getBoolean("skip-one-item", false);
        GameSound openingSound = MenuParserUtils.getSound(cfg.getConfigurationSection("open-sound"));

        MenuSlotsMap menuSlotsMap;
        if (isDialog) {
            RegularDialogMenuLayoutImpl.Builder<V> dialogMenuLayoutBuilder = (RegularDialogMenuLayoutImpl.Builder<V>) menuLayoutBuilder;
            menuSlotsMap = DialogMenuParser.parseRegularMenuPatternInternal(callerName, cfg, dialogMenuLayoutBuilder);
        } else {
            RegularInventoryMenuLayoutImpl.Builder<V> inventoryMenuLayoutBuilder = (RegularInventoryMenuLayoutImpl.Builder<V>) menuLayoutBuilder;
            inventoryMenuLayoutBuilder.setInventoryType(MenuParserUtils.getMinecraftEnum(InventoryType.class, menuType));
            menuSlotsMap = InventoryMenuParser.parseRegularMenuPatternInternal(callerName, cfg, inventoryMenuLayoutBuilder);
        }

        return new MenuParseResult<>(menuLayoutBuilder, openingSound, previousMoveAllowed, skipOneItem, menuSlotsMap, cfg);
    }

    @Override
    public <V extends PagedMenuView<V, ?, E>, E> MenuParseResult<V> parseMenu(
            String callerName, YamlConfiguration cfg, PagedMenuTemplateButton.Builder<V, E> pagedButtonBuilder) throws MenuParseException {
        String menuType = cfg.getString("type", "CHEST");
        boolean isDialog = menuType.equalsIgnoreCase("DIALOG");

        AbstractMenuLayout.Builder<V> menuLayoutBuilder = isDialog ? new PagedDialogMenuLayoutImpl.Builder<>() :
                new PagedInventoryMenuLayoutImpl.Builder<>();

        menuLayoutBuilder.setTitle(Formatters.COLOR_FORMATTER.format(cfg.getString("title", "")));

        boolean previousMoveAllowed = cfg.getBoolean("previous-menu", true);
        boolean skipOneItem = cfg.getBoolean("skip-one-item", false);
        GameSound openingSound = MenuParserUtils.getSound(cfg.getConfigurationSection("open-sound"));

        MenuSlotsMap menuSlotsMap;
        if (isDialog) {
            PagedDialogMenuLayoutImpl.Builder<V, E> dialogMenuLayoutBuilder = (PagedDialogMenuLayoutImpl.Builder<V, E>) menuLayoutBuilder;

            menuSlotsMap = DialogMenuParser.parsePagedMenuPatternInternal(callerName, cfg, dialogMenuLayoutBuilder);
        } else {
            PagedInventoryMenuLayoutImpl.Builder<V, E> inventoryMenuLayoutBuilder = (PagedInventoryMenuLayoutImpl.Builder<V, E>) menuLayoutBuilder;

            inventoryMenuLayoutBuilder.setInventoryType(MenuParserUtils.getMinecraftEnum(InventoryType.class, menuType));

            menuSlotsMap = InventoryMenuParser.parsePagedMenuPatternInternal(callerName, cfg, inventoryMenuLayoutBuilder);

            inventoryMenuLayoutBuilder.setPreviousPageSlots(parseButtonSlots(cfg, "previous-page", menuSlotsMap));
            inventoryMenuLayoutBuilder.setCurrentPageSlots(parseButtonSlots(cfg, "current-page", menuSlotsMap));
            inventoryMenuLayoutBuilder.setNextPageSlots(parseButtonSlots(cfg, "next-page", menuSlotsMap));
            inventoryMenuLayoutBuilder.setPagedObjectSlots(parseButtonSlots(cfg, "slots", menuSlotsMap), pagedButtonBuilder);

            if (cfg.isList("custom-order"))
                inventoryMenuLayoutBuilder.setCustomLayoutOrder(cfg.getIntegerList("custom-order"));
        }

        return new MenuParseResult<>(menuLayoutBuilder, openingSound, previousMoveAllowed, skipOneItem, menuSlotsMap, cfg);
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

    public List<Integer> parseButtonSlots(ConfigurationSection section, String key, MenuSlotsMap menuSlotsMap) {
        return !section.isString(key) ? Collections.emptyList() : menuSlotsMap.getSlots(section.getString(key));
    }

//    private static <V extends MenuView<V, ?>> Map<String, MenuTemplateButton<V>> buildDialogButtonTemplates(YamlConfiguration cfg) {
//        ConfigurationSection dialogSection = cfg.getConfigurationSection("dialog");
//        if (dialogSection == null)
//            return Collections.emptyMap();
//
//        ConfigurationSection buttonsSection = dialogSection.getConfigurationSection("buttons");
//        if (buttonsSection == null)
//            return Collections.emptyMap();
//
//        Map<String, MenuTemplateButton<V>> templates = new LinkedHashMap<>();
//        for (String buttonId : buttonsSection.getKeys(false)) {
//            // Static action types (open_url, suggest_command) have no server callback — skip template
//            if (buttonsSection.isConfigurationSection(buttonId)) {
//                ConfigurationSection btnSection = buttonsSection.getConfigurationSection(buttonId);
//                String actionStr = btnSection.getString("action", "custom").toLowerCase(Locale.ENGLISH);
//                if ("open_url".equals(actionStr) || "suggest_command".equals(actionStr))
//                    continue;
//            }
//            DummyButton.Builder<V> btnBuilder = new DummyButton.Builder<>();
//            btnBuilder.setClickCommands(cfg.getStringList("commands." + buttonId));
//            templates.put(buttonId, btnBuilder.build());
//        }
//        return templates;
//    }
//
//    @Nullable
//    private DialogButtonMenuData parseDialogSection(String fileName, YamlConfiguration cfg) {
//        ConfigurationSection dialogSection = cfg.getConfigurationSection("dialog");
//        if (dialogSection == null)
//            return null;
//
//        ConfigurationSection inputsSection = dialogSection.getConfigurationSection("inputs");
//        if (inputsSection != null) {
//            for (String inputKey : inputsSection.getKeys(false)) {
//                ConfigurationSection inputSection = inputsSection.getConfigurationSection(inputKey);
//                if (inputSection == null)
//                    continue;
//                DialogInputData inputData = parseInputEntry(inputKey, inputSection);
//                if (inputData != null)
//                    builder.addInput(inputData);
//            }
//        }
//
//        ConfigurationSection buttonsSection = dialogSection.getConfigurationSection("buttons");
//        if (buttonsSection != null) {
//            for (String charStr : buttonsSection.getKeys(false)) {
//                if (buttonsSection.isConfigurationSection(charStr)) {
//                    ConfigurationSection btnSection = buttonsSection.getConfigurationSection(charStr);
//                    String label = Formatters.COLOR_FORMATTER.format(btnSection.getString("label", charStr));
//                    String actionStr = btnSection.getString("action", "custom").toLowerCase(Locale.ENGLISH);
//                    DialogButtonData.ActionType actionType = parseButtonActionType(actionStr);
//                    String actionParam = null;
//                    if (actionType == DialogButtonData.ActionType.OPEN_URL)
//                        actionParam = btnSection.getString("url");
//                    else if (actionType == DialogButtonData.ActionType.SUGGEST_COMMAND)
//                        actionParam = btnSection.getString("command");
//                    builder.button(charStr, label, actionType, actionParam);
//                } else {
//                    // backward compat: string value = label, CUSTOM action type
//                    String label = Formatters.COLOR_FORMATTER.format(buttonsSection.getString(charStr, charStr));
//                    builder.button(charStr, label);
//                }
//            }
//        }
//
//        return builder.build();
//    }
//
//    private static DialogButtonData.ActionType parseButtonActionType(String actionStr) {
//        switch (actionStr) {
//            case "run_command":
//                return DialogButtonData.ActionType.RUN_COMMAND;
//            case "open_url":
//                return DialogButtonData.ActionType.OPEN_URL;
//            case "suggest_command":
//                return DialogButtonData.ActionType.SUGGEST_COMMAND;
//            default:
//                return DialogButtonData.ActionType.CUSTOM;
//        }
//    }
//
//    @Nullable
//    private static DialogInputData parseInputEntry(String key, ConfigurationSection section) {
//        String typeStr = section.getString("type", "text").toLowerCase(Locale.ENGLISH);
//        String label = Formatters.COLOR_FORMATTER.format(section.getString("label", key));
//        int width = section.getInt("width", 200);
//        boolean labelVisible = section.getBoolean("label-visible", true);
//
//        switch (typeStr) {
//            case "text": {
//                DialogInputData.Builder b = DialogInputData.text(key, label)
//                        .width(width)
//                        .labelVisible(labelVisible)
//                        .textInitial(section.getString("initial", ""))
//                        .maxLength(section.getInt("max-length", 100));
//                if (section.isSet("multiline")) {
//                    b.multiline(true);
//                    ConfigurationSection ml = section.getConfigurationSection("multiline");
//                    if (ml != null) {
//                        if (ml.isSet("max-lines")) b.multilineMaxLines(ml.getInt("max-lines"));
//                        if (ml.isSet("height")) b.multilineHeight(ml.getInt("height"));
//                    }
//                }
//                return b.build();
//            }
//            case "boolean": {
//                return DialogInputData.bool(key, label)
//                        .boolInitial(section.getBoolean("initial", false))
//                        .onTrue(section.getString("on-true", "true"))
//                        .onFalse(section.getString("on-false", "false"))
//                        .build();
//            }
//            case "number_range": {
//                float start = (float) section.getDouble("start", 0);
//                float end = (float) section.getDouble("end", 100);
//                DialogInputData.Builder b = DialogInputData.numberRange(key, label, start, end)
//                        .width(width)
//                        .labelFormat(section.getString("label-format", "options.generic_value"));
//                if (section.isSet("initial")) b.rangeInitial((float) section.getDouble("initial"));
//                if (section.isSet("step")) b.rangeStep((float) section.getDouble("step"));
//                return b.build();
//            }
//            case "single_option": {
//                DialogInputData.Builder b = DialogInputData.singleOption(key, label)
//                        .width(width)
//                        .labelVisible(labelVisible);
//                ConfigurationSection optionsSection = section.getConfigurationSection("options");
//                if (optionsSection != null) {
//                    for (String optId : optionsSection.getKeys(false)) {
//                        ConfigurationSection optSection = optionsSection.getConfigurationSection(optId);
//                        if (optSection != null) {
//                            String display = optSection.isSet("display")
//                                    ? Formatters.COLOR_FORMATTER.format(optSection.getString("display"))
//                                    : null;
//                            boolean initial = optSection.getBoolean("initial", false);
//                            b.option(new DialogInputData.OptionEntry(optId, display, initial));
//                        } else {
//                            b.option(new DialogInputData.OptionEntry(optId, null, false));
//                        }
//                    }
//                }
//                return b.build();
//            }
//            default:
//                Log.warnFromFile("dialog.inputs." + key + ".type", "Unknown input type '", typeStr, "', skipping");
//                return null;
//        }
//    }

    public interface IMenuConverter {

        boolean convert(SuperiorSkyblockPlugin plugin, YamlConfiguration cfg);

    }

}
