package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.IslandPrivilegePagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MenuIslandPrivileges extends AbstractPagedMenu<
        MenuIslandPrivileges.View, MenuIslandPrivileges.Args, MenuIslandPrivileges.IslandPrivilegeInfo> {

    private final List<MenuIslandPrivileges.IslandPrivilegeInfo> islandPrivileges;
    private final String noRolePermission;
    private final String exactRolePermission;
    private final String higherRolePermission;

    private MenuIslandPrivileges(MenuParseResult<View> parseResult, List<IslandPrivilegeInfo> islandPrivileges,
                                 String noRolePermission, String exactRolePermission, String higherRolePermission) {
        super(MenuIdentifiers.MENU_ISLAND_PRIVILEGES, parseResult, false);
        this.islandPrivileges = islandPrivileges;
        this.noRolePermission = noRolePermission;
        this.exactRolePermission = exactRolePermission;
        this.higherRolePermission = higherRolePermission;
    }

    public String getNoRolePermission() {
        return noRolePermission;
    }

    public String getExactRolePermission() {
        return exactRolePermission;
    }

    public String getHigherRolePermission() {
        return higherRolePermission;
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, Args args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    public void refreshViews(Island island) {
        refreshViews(view -> view.island.equals(island));
    }

    public void refreshViews(Island island, Object permissionHolder) {
        refreshViews(view -> view.island.equals(island) && view.permissionHolder.equals(permissionHolder));
    }

    @Nullable
    public static MenuIslandPrivileges createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("permissions.yml",
                MenuIslandPrivileges::convertOldGUI, new IslandPrivilegePagedObjectButton.Builder());

        if (menuParseResult == null)
            return null;

        YamlConfiguration cfg = menuParseResult.getConfig();

        String noRolePermission = cfg.getString("messages.no-role-permission", "");
        String exactRolePermission = cfg.getString("messages.exact-role-permission", "");
        String higherRolePermission = cfg.getString("messages.higher-role-permission", "");

        List<MenuIslandPrivileges.IslandPrivilegeInfo> islandPrivileges = new LinkedList<>();

        int position = 0;

        ConfigurationSection permissionsSection = cfg.getConfigurationSection("permissions");
        if (permissionsSection != null) {
            for (String key : permissionsSection.getKeys(false)) {
                if (permissionsSection.getBoolean(key + ".display-menu", true)) {
                    String permission = key.toLowerCase(Locale.ENGLISH);
                    try {
                        updatePermissionInternal(islandPrivileges, IslandPrivilege.getByName(permission), cfg, position++);
                    } catch (NullPointerException error) {
                        Log.warnFromFile("permissions.yml", "The island-privilege '",
                                permission, "' is not a valid privilege, skipping...");
                    }
                }
            }
        }

        return new MenuIslandPrivileges(menuParseResult, islandPrivileges, noRolePermission,
                exactRolePermission, higherRolePermission);
    }

    public void updatePermission(IslandPrivilege islandPrivilege) {
        File file = new File(plugin.getDataFolder(), "menus/permissions.yml");
        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        int position = 0;

        for (String key : cfg.getConfigurationSection("permissions").getKeys(false)) {
            if (islandPrivilege.getName().equalsIgnoreCase(key))
                break;

            position++;
        }

        updatePermissionInternal(islandPrivileges, islandPrivilege, cfg, position);
    }

    private static void updatePermissionInternal(List<IslandPrivilegeInfo> islandPrivileges,
                                                 IslandPrivilege islandPrivilege, YamlConfiguration cfg, int position) {
        islandPrivileges.removeIf(islandPrivilegeInfo -> islandPrivilegeInfo.getIslandPrivilege() == islandPrivilege);

        TemplateItem enabledIslandPrivilegeItem = null;
        TemplateItem disabledIslandPrivilegeItem = null;
        TemplateItem rolePrivilegeItem = null;
        GameSound accessSound = null;
        GameSound noAccessSound = null;
        List<String> accessCommands = null;
        List<String> noAccessCommands = null;

        ConfigurationSection itemPrivilegeSection = cfg.getConfigurationSection("permissions." +
                islandPrivilege.getName().toLowerCase(Locale.ENGLISH));

        if (itemPrivilegeSection != null) {
            enabledIslandPrivilegeItem = MenuParserImpl.getInstance().getItemStack("permissions.yml",
                    itemPrivilegeSection.getConfigurationSection("permission-enabled"));
            disabledIslandPrivilegeItem = MenuParserImpl.getInstance().getItemStack("permissions.yml",
                    itemPrivilegeSection.getConfigurationSection("permission-disabled"));
            rolePrivilegeItem = MenuParserImpl.getInstance().getItemStack("permissions.yml",
                    itemPrivilegeSection.getConfigurationSection("role-permission"));
            accessSound = MenuParserImpl.getInstance().getSound(itemPrivilegeSection.getConfigurationSection("has-access.sound"));
            noAccessSound = MenuParserImpl.getInstance().getSound(itemPrivilegeSection.getConfigurationSection("no-access.sound"));
            accessCommands = itemPrivilegeSection.getStringList("has-access.commands");
            noAccessCommands = itemPrivilegeSection.getStringList("no-access.commands");
        }

        islandPrivileges.add(new MenuIslandPrivileges.IslandPrivilegeInfo(islandPrivilege, enabledIslandPrivilegeItem,
                disabledIslandPrivilegeItem, rolePrivilegeItem, accessSound, noAccessSound, accessCommands,
                noAccessCommands, position));
        Collections.sort(islandPrivileges);
    }

    public static class Args implements ViewArgs {

        private final Island island;
        private final Object permissionHolder;

        public Args(Island island, Object permissionHolder) {
            this.island = island;
            this.permissionHolder = permissionHolder;
        }

    }

    public class View extends AbstractPagedMenuView<MenuIslandPrivileges.View, MenuIslandPrivileges.Args, IslandPrivilegeInfo> {

        private final Island island;
        private final Object permissionHolder;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, MenuIslandPrivileges.Args> menu, Args args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.island;
            this.permissionHolder = args.permissionHolder;
        }

        public Island getIsland() {
            return island;
        }

        public Object getPermissionHolder() {
            return permissionHolder;
        }

        @Override
        protected List<IslandPrivilegeInfo> requestObjects() {
            return Collections.unmodifiableList(islandPrivileges);
        }

    }

    public static class IslandPrivilegeInfo implements Comparable<MenuIslandPrivileges.IslandPrivilegeInfo> {

        private final IslandPrivilege islandPrivilege;
        private final TemplateItem enabledIslandPrivilegeItem;
        private final TemplateItem disabledIslandPrivilegeItem;
        private final TemplateItem roleIslandPrivilegeItem;
        private final GameSound accessSound;
        private final GameSound noAccessSound;
        private final List<String> accessCommands;
        private final List<String> noAccessCommands;
        private final int position;

        public IslandPrivilegeInfo(IslandPrivilege islandPrivilege, TemplateItem enabledIslandPrivilegeItem,
                                   TemplateItem disabledIslandPrivilegeItem, TemplateItem roleIslandPrivilegeItem,
                                   GameSound accessSound, GameSound noAccessSound, List<String> accessCommands,
                                   List<String> noAccessCommands, int position) {
            this.islandPrivilege = islandPrivilege;
            this.enabledIslandPrivilegeItem = enabledIslandPrivilegeItem;
            this.disabledIslandPrivilegeItem = disabledIslandPrivilegeItem;
            this.roleIslandPrivilegeItem = roleIslandPrivilegeItem;
            this.accessSound = accessSound;
            this.noAccessSound = noAccessSound;
            this.accessCommands = accessCommands == null ? Collections.emptyList() : accessCommands;
            this.noAccessCommands = noAccessCommands == null ? Collections.emptyList() : noAccessCommands;
            this.position = position;
        }

        public IslandPrivilege getIslandPrivilege() {
            return islandPrivilege;
        }

        public ItemBuilder getEnabledIslandPrivilegeItem() {
            return enabledIslandPrivilegeItem.getBuilder();
        }

        public ItemBuilder getDisabledIslandPrivilegeItem() {
            return disabledIslandPrivilegeItem.getBuilder();
        }

        @Nullable
        public ItemBuilder getRoleIslandPrivilegeItem() {
            return roleIslandPrivilegeItem == null ? null : roleIslandPrivilegeItem.getBuilder();
        }

        @Nullable
        public GameSound getAccessSound() {
            return accessSound;
        }

        @Nullable
        public GameSound getNoAccessSound() {
            return noAccessSound;
        }

        public List<String> getAccessCommands() {
            return accessCommands;
        }

        public List<String> getNoAccessCommands() {
            return noAccessCommands;
        }

        @Override
        public int compareTo(@NotNull MenuIslandPrivileges.IslandPrivilegeInfo other) {
            return Integer.compare(position, other.position);
        }

    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/permissions-gui.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("permissions-gui.title"));

        int size = cfg.getInt("permissions-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("permissions-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("permissions-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("permissions-gui"), newMenu,
                patternChars, slotsChar, AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++], AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("permissions", cfg.getConfigurationSection("permissions-gui.permissions"));
        newMenu.set("sounds", null);
        newMenu.set("commands", null);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
