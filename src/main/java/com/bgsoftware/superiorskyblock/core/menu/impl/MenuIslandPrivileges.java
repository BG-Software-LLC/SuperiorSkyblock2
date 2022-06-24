package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.IslandPrivilegePagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.PagedMenuPattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MenuIslandPrivileges extends PagedSuperiorMenu<MenuIslandPrivileges,
        MenuIslandPrivileges.IslandPrivilegeInfo> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static PagedMenuPattern<MenuIslandPrivileges, IslandPrivilegeInfo> menuPattern;

    private static final List<IslandPrivilegeInfo> islandPrivileges = new LinkedList<>();

    public static String noRolePermission = "";
    public static String exactRolePermission = "";
    public static String higherRolePermission = "";

    private final Island island;
    private final Object permissionHolder;

    private MenuIslandPrivileges(SuperiorPlayer superiorPlayer, Island island, Object permissionHolder) {
        super(menuPattern, superiorPlayer);
        this.island = island;
        this.permissionHolder = permissionHolder;
        if (permissionHolder instanceof SuperiorPlayer)
            updateTargetPlayer((SuperiorPlayer) permissionHolder);
    }

    public Island getTargetIsland() {
        return island;
    }

    public Object getPermissionHolder() {
        return permissionHolder;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, island, permissionHolder);
    }

    @Override
    protected List<IslandPrivilegeInfo> requestObjects() {
        return Collections.unmodifiableList(islandPrivileges);
    }

    public static void init() {
        menuPattern = null;
        islandPrivileges.clear();

        PagedMenuPattern.Builder<MenuIslandPrivileges, IslandPrivilegeInfo> patternBuilder =
                new PagedMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "permissions.yml",
                MenuIslandPrivileges::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        noRolePermission = cfg.getString("messages.no-role-permission", "");
        exactRolePermission = cfg.getString("messages.exact-role-permission", "");
        higherRolePermission = cfg.getString("messages.higher-role-permission", "");

        islandPrivileges.clear();
        int position = 0;

        ConfigurationSection permissionsSection = cfg.getConfigurationSection("permissions");
        if (permissionsSection != null) {
            for (String key : permissionsSection.getKeys(false)) {
                if (permissionsSection.getBoolean(key + ".display-menu", true)) {
                    String permission = key.toLowerCase(Locale.ENGLISH);
                    try {
                        updatePermission(IslandPrivilege.getByName(permission), cfg, position++);
                    } catch (NullPointerException error) {
                        SuperiorSkyblockPlugin.log("&cThe island-privilege '" + permission + "' is not a valid privilege, skipping...");
                    }
                }
            }
        }

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .setPagedObjectSlots(getSlots(cfg, "slots", menuPatternSlots),
                        new IslandPrivilegePagedObjectButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island, Object permissionHolder) {
        new MenuIslandPrivileges(superiorPlayer, island, permissionHolder).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        SuperiorMenu.refreshMenus(MenuIslandPrivileges.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    public static void refreshMenus(Island island, Object permissionHolder) {
        SuperiorMenu.refreshMenus(MenuIslandPrivileges.class, superiorMenu -> superiorMenu.island.equals(island) &&
                superiorMenu.permissionHolder.equals(permissionHolder));
    }

    public static void updatePermission(IslandPrivilege islandPrivilege) {
        File file = new File(plugin.getDataFolder(), "menus/permissions.yml");
        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);
        int position = 0;

        for (String key : cfg.getConfigurationSection("permissions").getKeys(false)) {
            if (islandPrivilege.getName().equalsIgnoreCase(key))
                break;

            position++;
        }

        updatePermission(islandPrivilege, cfg, position);
    }

    public static void updatePermission(IslandPrivilege islandPrivilege, YamlConfiguration cfg, int position) {
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
            enabledIslandPrivilegeItem = MenuParser.getItemStack("permissions.yml",
                    itemPrivilegeSection.getConfigurationSection("permission-enabled"));
            disabledIslandPrivilegeItem = MenuParser.getItemStack("permissions.yml",
                    itemPrivilegeSection.getConfigurationSection("permission-disabled"));
            rolePrivilegeItem = MenuParser.getItemStack("permissions.yml",
                    itemPrivilegeSection.getConfigurationSection("role-permission"));
            accessSound = MenuParser.getSound(itemPrivilegeSection.getConfigurationSection("has-access.sound"));
            noAccessSound = MenuParser.getSound(itemPrivilegeSection.getConfigurationSection("no-access.sound"));
            accessCommands = itemPrivilegeSection.getStringList("has-access.commands");
            noAccessCommands = itemPrivilegeSection.getStringList("no-access.commands");
        }

        islandPrivileges.add(new IslandPrivilegeInfo(islandPrivilege, enabledIslandPrivilegeItem,
                disabledIslandPrivilegeItem, rolePrivilegeItem, accessSound, noAccessSound, accessCommands,
                noAccessCommands, position));
        Collections.sort(islandPrivileges);
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

        char slotsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("permissions-gui"), newMenu,
                patternChars, slotsChar, SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++], SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("permissions", cfg.getConfigurationSection("permissions-gui.permissions"));
        newMenu.set("sounds", null);
        newMenu.set("commands", null);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

    public static class IslandPrivilegeInfo implements Comparable<IslandPrivilegeInfo> {

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
        public int compareTo(@NotNull IslandPrivilegeInfo other) {
            return Integer.compare(position, other.position);
        }

    }

}