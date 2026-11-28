package com.bgsoftware.superiorskyblock.core.menu.parser;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.layout.InventoryMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.parser.MenuParseException;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.MenuSlotsMap;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BackButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public class InventoryMenuParser {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private InventoryMenuParser() {

    }

    public static <V extends MenuView<V, ?>> MenuSlotsMap parseRegularMenuPatternInternal(
            String callerName, YamlConfiguration cfg, InventoryMenuLayout.Builder<V> menuLayoutBuilder) {
        MenuSlotsMap menuSlotsMap = new MenuSlotsMap();

        List<String> pattern = cfg.getStringList("pattern");
        menuLayoutBuilder.setRowsCount(pattern.size());

        for (int row = 0; row < pattern.size() && row < 6; row++) {
            String patternLine = pattern.get(row).replace(" ", "");
            for (int i = 0; i < patternLine.length() && i < 9; i++) {
                int slot = row * 9 + i;

                char ch = patternLine.charAt(i);

                AbstractMenuTemplateButton.AbstractBuilder<V> buttonBuilder = new DummyButton.Builder<>();

                buttonBuilder.setButtonItem(MenuParserUtils.getItemStack(callerName, cfg.getConfigurationSection("items." + ch)));
                buttonBuilder.setClickCommands(cfg.getStringList("commands." + ch));
                buttonBuilder.setClickSound(MenuParserUtils.getSound(cfg.getConfigurationSection("sounds." + ch)));
                buttonBuilder.setRequiredPermission(cfg.getString("permissions." + ch + ".permission"));
                buttonBuilder.setLackPermissionsSound(MenuParserUtils.getSound(cfg.getConfigurationSection("permissions." + ch + ".no-access-sound")));

                menuLayoutBuilder.setButton(slot, buttonBuilder.build());

                menuSlotsMap.addSlot(ch, slot);
            }
        }

        return menuSlotsMap;
    }

    public static <V extends PagedMenuView<V, ?, E>, E> MenuSlotsMap parsePagedMenuPatternInternal(
            String callerName, YamlConfiguration cfg, InventoryMenuLayout.Builder<V> menuLayoutBuilder)
            throws MenuParseException {
        MenuSlotsMap menuSlotsMap = new MenuSlotsMap();

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

                buttonBuilder.setButtonItem(MenuParserUtils.getItemStackUnsafe(callerName, cfg.getConfigurationSection("items." + ch)));
                buttonBuilder.setClickCommands(cfg.getStringList("commands." + ch));
                buttonBuilder.setClickSound(MenuParserUtils.getSound(cfg.getConfigurationSection("sounds." + ch)));
                buttonBuilder.setRequiredPermission(cfg.getString("permissions." + ch + ".permission"));
                buttonBuilder.setLackPermissionsSound(MenuParserUtils.getSound(cfg.getConfigurationSection("permissions." + ch + ".no-access-sound")));

                menuLayoutBuilder.setButton(slot, buttonBuilder.build());

                menuSlotsMap.addSlot(ch, slot);
            }
        }

        if (plugin.getSettings().isOnlyBackButton() && !backButtonFound) {
            throw new MenuParseException("Menu doesn't have a back button, it's impossible to close it.");
        }

        return menuSlotsMap;
    }

}
