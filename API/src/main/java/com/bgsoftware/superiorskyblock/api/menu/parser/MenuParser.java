package com.bgsoftware.superiorskyblock.api.menu.parser;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.PagedMenuView;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

/**
 * Singleton class used to parse menus out of files.
 * You can get the instance of this parser by calling {@link #getInstance()}
 */
public interface MenuParser {

    /**
     * Parse a menu out of the provided config file.
     *
     * @param callerName The caller's name.
     *                   Used to log warnings when parsing the menu that were not critical.
     * @param config     The config to load the menu from.
     * @throws MenuParseException In case an error occurred while parsing the menu.
     */
    <V extends MenuView<V, ?>> ParseResult<V> parseMenu(String callerName, YamlConfiguration config) throws MenuParseException;

    /**
     * Parse a paged menu out of the provided config file.
     *
     * @param callerName         The caller's name.
     *                           Used to log warnings when parsing the menu that were not critical.
     * @param config             The config to load the menu from.
     * @param pagedButtonBuilder The builder to use to build the paged objects for the menu.
     * @throws MenuParseException In case an error occurred while parsing the menu.
     */
    <V extends PagedMenuView<V, ?, E>, E> ParseResult<V> parseMenu(
            String callerName, YamlConfiguration config, PagedMenuTemplateButton.Builder<V, E> pagedButtonBuilder) throws MenuParseException;

    /**
     * Get the instance of the parser.
     */
    static MenuParser getInstance() {
        return SuperiorSkyblockAPI.getMenus().getParser();
    }

    /**
     * Represents the result of the parsing.
     */
    interface ParseResult<V extends MenuView<V, ?>> {

        /**
         * Get the layout builder used by the parser.
         */
        MenuLayout.Builder<V> getLayoutBuilder();

        /**
         * Get the opening sound to play.
         */
        @Nullable
        GameSound getOpeningSound();

        /**
         * Get whether it is possible to open the previous opened menu after closing the current one.
         */
        boolean isPreviousMoveAllowed();

        /**
         * Get whether this menu should be skipped when it only contains one item.
         * This is only useful for menus that have their buttons open other menus.
         */
        boolean isSkipOneItem();

        /**
         * Get the slots in the layout for a char.
         *
         * @param ch The char to get slots for.
         */
        List<Integer> getSlotsForChar(char ch);

    }

}
