package com.bgsoftware.superiorskyblock.menu.impl.internal;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.lang.PlayerLocales;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.google.common.collect.Maps;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SuperiorMenuCustom extends SuperiorMenu<SuperiorMenuCustom> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Map<String, RegularMenuPattern<SuperiorMenuCustom>> customMenus = Maps.newHashMap();

    private final String fileName;

    private SuperiorMenuCustom(RegularMenuPattern<SuperiorMenuCustom> menuPattern, SuperiorPlayer superiorPlayer,
                               String fileName) {
        super(menuPattern, superiorPlayer);
        this.fileName = fileName;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, fileName, previousMenu);
    }

    public static void createMenu(File file) {
        String fileName = file.getName();

        RegularMenuPattern.Builder<SuperiorMenuCustom> patternBuilder = new RegularMenuPattern.Builder<>();

        Pair<MenuPatternSlots, CommentedConfiguration> menuLoadResult = FileUtils.loadMenu(patternBuilder,
                fileName, true, null);

        if (menuLoadResult == null)
            return;

        CommentedConfiguration cfg = menuLoadResult.getValue();

        if (cfg.contains("command")) {
            ConfigurationSection commandsSection = cfg.getConfigurationSection("command");

            if (commandsSection == null) {
                SuperiorSkyblockPlugin.log("&c[" + fileName + "] Custom menu doesn't have it's command section configured correctly, skipping...");
                return;
            }

            List<String> aliases = Arrays.asList(commandsSection.getString("aliases", "").split(", "));
            String permission = commandsSection.getString("permission", "");
            Map<Locale, String> descriptions = new HashMap<>();
            if (commandsSection.contains("description")) {
                for (String locale : commandsSection.getConfigurationSection("description").getKeys(false))
                    descriptions.put(PlayerLocales.getLocale(locale), commandsSection.getString("description." + locale));
            }
            boolean displayCommand = commandsSection.getBoolean("display-command", false);

            plugin.getCommands().registerCommand(new CustomMenuCommand(fileName, aliases, permission, descriptions, displayCommand));
        }

        customMenus.put(fileName.toLowerCase(), patternBuilder.build());
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, String fileName, ISuperiorMenu previousMenu) {
        RegularMenuPattern<SuperiorMenuCustom> menuPattern = customMenus.get(fileName.toLowerCase());
        if (menuPattern != null)
            new SuperiorMenuCustom(menuPattern, superiorPlayer, fileName).open(previousMenu);
    }

    public static boolean isValidMenu(String menuName) {
        return customMenus.containsKey(menuName.toLowerCase());
    }

    public static Set<String> getCustomMenus() {
        return Collections.unmodifiableSet(customMenus.keySet());
    }

    public static void resetMenus() {
        customMenus.clear();
    }

    private static final class CustomMenuCommand implements ISuperiorCommand {

        private final String fileName;
        private final List<String> aliases;
        private final String permission;
        private final Map<Locale, String> descriptions;
        private final boolean displayCommand;

        public CustomMenuCommand(String fileName, List<String> aliases, String permission, Map<Locale, String> descriptions, boolean displayCommand) {
            this.fileName = fileName;
            this.aliases = aliases;
            this.permission = permission;
            this.descriptions = descriptions;
            this.displayCommand = displayCommand;
        }

        @Override
        public List<String> getAliases() {
            return aliases;
        }

        @Override
        public String getPermission() {
            return permission;
        }

        @Override
        public String getUsage(Locale locale) {
            return aliases.get(0);
        }

        @Override
        public String getDescription(Locale locale) {
            return descriptions.getOrDefault(locale, "");
        }

        @Override
        public int getMinArgs() {
            return 1;
        }

        @Override
        public int getMaxArgs() {
            return 1;
        }

        @Override
        public boolean canBeExecutedByConsole() {
            return false;
        }

        @Override
        public boolean displayCommand() {
            return displayCommand;
        }

        @Override
        public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
            SuperiorMenuCustom.openInventory(plugin.getPlayers().getSuperiorPlayer(sender), fileName, null);
        }

        @Override
        public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
            return new ArrayList<>();
        }

    }

}
