package com.bgsoftware.superiorskyblock.module.warps;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.modules.ModuleLogger;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
import com.bgsoftware.superiorskyblock.module.warps.commands.CmdAdminAddWarpsLimit;
import com.bgsoftware.superiorskyblock.module.warps.commands.CmdAdminDelWarp;
import com.bgsoftware.superiorskyblock.module.warps.commands.CmdAdminSetWarpsLimit;
import com.bgsoftware.superiorskyblock.module.warps.commands.CmdDelWarp;
import com.bgsoftware.superiorskyblock.module.warps.commands.CmdSetWarp;
import com.bgsoftware.superiorskyblock.module.warps.commands.CmdWarp;
import com.bgsoftware.superiorskyblock.module.warps.commands.CmdWarps;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WarpsModule extends BuiltinModule<WarpsModule.Configuration> {

    public WarpsModule() {
        super("warps");
    }

    @Override
    protected boolean onConfigCreate(SuperiorSkyblockPlugin plugin, CommentedConfiguration config, boolean firstTime) {
        File oldConfigFile = new File(plugin.getDataFolder(), "config.yml");
        if (!oldConfigFile.exists()) {
            return false;
        }

        CommentedConfiguration oldConfig = CommentedConfiguration.loadConfiguration(oldConfigFile);
        boolean updatedConfig = false;

        if (oldConfig.isString("global-warps-order")) {
            config.set("menus.global-warps-order", oldConfig.getString("global-warps-order"));
            oldConfig.set("global-warps-order", null);
            updatedConfig = true;
        }
        if (oldConfig.isString("sign-warp-line")) {
            config.set("signs.create-line", oldConfig.getString("sign-warp-line"));
            oldConfig.set("sign-warp-line", null);
            updatedConfig = true;
        }
        if (oldConfig.isList("sign-warp")) {
            config.set("signs.active-lines", oldConfig.getStringList("sign-warp"));
            config.set("signs.inactive-lines", oldConfig.getStringList("sign-warp"));
            oldConfig.set("sign-warp", null);
            updatedConfig = true;
        }
        if (oldConfig.isLong("warps-warmup")) {
            config.set("teleport-warmup", oldConfig.getLong("warps-warmup"));
            oldConfig.set("warps-warmup", null);
            updatedConfig = true;
        }
        if (oldConfig.isBoolean("warp-categories")) {
            config.set("categories.enabled", oldConfig.getBoolean("warp-categories"));
            oldConfig.set("warp-categories", null);
            updatedConfig = true;
        }
        if (oldConfig.isDouble("charge-on-warp")) {
            config.set("charge-on-teleport", oldConfig.getDouble("charge-on-warp"));
            oldConfig.set("charge-on-warp", null);
            updatedConfig = true;
        }
        if (oldConfig.isBoolean("public-warps")) {
            config.set("private-by-default", !oldConfig.getBoolean("public-warps"));
            oldConfig.set("public-warps", null);
            updatedConfig = true;
        }
        if (oldConfig.isBoolean("delete-unsafe-warps")) {
            config.set("delete-unsafe", oldConfig.getBoolean("delete-unsafe-warps"));
            oldConfig.set("delete-unsafe-warps", null);
            updatedConfig = true;
        }

        if (updatedConfig) {
            config.set("names.max-length", 255);

            try {
                oldConfig.save(oldConfigFile);
            } catch (Exception error) {
                this.logger().e("An error occurred while saving config file: ", error);
            }
        }

        return updatedConfig;
    }

    @Override
    protected void onEnable(SuperiorSkyblockPlugin plugin) {
        // Do nothing.
    }

    @Override
    protected void onDisable(SuperiorSkyblockPlugin plugin) {
        // Do nothing.
    }

    @Override
    protected void loadData(SuperiorSkyblockPlugin plugin) {
        // Do nothing.
    }

    @Override
    protected Listener[] getModuleListeners(SuperiorSkyblockPlugin plugin) {
        return new Listener[0];
    }

    @Override
    protected SuperiorCommand[] getSuperiorCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdDelWarp(), new CmdSetWarp(), new CmdWarp(), new CmdWarps()};
    }

    @Override
    protected SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[]{new CmdAdminAddWarpsLimit(), new CmdAdminDelWarp(), new CmdAdminSetWarpsLimit()};
    }

    @Override
    protected Configuration createConfigFile(CommentedConfiguration config) {
        return new Configuration(config, this.logger());
    }

    public static class Configuration implements IModuleConfiguration {

        private final boolean enabled;
        private final boolean deleteUnsafe;
        private final boolean privateByDefault;
        private final double chargeOnTeleport;
        private final long teleportWarmup;
        private final String menusGlobalWarpsOrder;
        private final boolean menusWarpManageEnabled;
        private final boolean menusWarpCategoryManageEnabled;
        private final boolean categoriesEnabled;
        private final String categoriesDefaultName;
        private final int namesMinLength;
        private final int namesMaxLength;
        private final List<String> namesBlacklist;
        private final boolean signsEnabled;
        private final String signsCreateLine;
        private final List<String> signsActiveLines;
        private final List<String> signsInactiveLines;

        Configuration(CommentedConfiguration config, ModuleLogger logger) {
            this.enabled = config.getBoolean("enabled", true);
            this.deleteUnsafe = config.getBoolean("delete-unsafe", true);
            this.privateByDefault = config.getBoolean("private-by-default", false);
            this.chargeOnTeleport = config.getDouble("charge-on-teleport", 0D);
            this.teleportWarmup = config.getLong("teleport-warmup", 0);
            String menusGlobalWarpsOrder = config.getString("menus.global-warps-order", "WORTH").toUpperCase(Locale.ENGLISH);
            if (SortingType.getByName(menusGlobalWarpsOrder) == null) {
                logger.w("Invalid menu islands order sorting type '" + menusGlobalWarpsOrder + "', using 'WORTH' instead.");
                menusGlobalWarpsOrder = "WORTH";
            }
            this.menusGlobalWarpsOrder = menusGlobalWarpsOrder;
            this.menusWarpManageEnabled = config.getBoolean("menus.warp-manage-enabled", true);
            this.menusWarpCategoryManageEnabled = config.getBoolean("menus.warp-category-manage-enabled", true);
            this.namesMinLength = config.getInt("names.min-length", 1);
            this.namesMaxLength = config.getInt("names.max-length", 16);
            this.namesBlacklist = Collections.unmodifiableList(config.getStringList("names.blacklist").stream()
                    .map(name -> name.toLowerCase(Locale.ENGLISH)).collect(Collectors.toList()));
            this.categoriesEnabled = config.getBoolean("categories.enabled", true);
            String categoriesDefaultName = config.getString("categories.default-name");
            if (!isValidDefaultWarpCategoryName(categoriesDefaultName, this.namesMinLength, this.namesMaxLength)) {
                logger.w("Invalid default warp category name '" + categoriesDefaultName + "', using 'Default' instead.");
                categoriesDefaultName = "Default";
            }
            this.categoriesDefaultName = categoriesDefaultName;
            this.signsEnabled = config.getBoolean("signs.enabled", true);
            this.signsCreateLine = config.getString("signs.create-line", "[IslandWarp]");
            this.signsActiveLines = formatSignLines(config.getStringList("signs.active-lines"));
            this.signsInactiveLines = formatSignLines(config.getStringList("signs.inactive-lines"));

        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }

        public boolean isDeleteUnsafe() {
            return this.deleteUnsafe;
        }

        public boolean isPrivateByDefault() {
            return this.privateByDefault;
        }

        public double getChargeOnTeleport() {
            return this.chargeOnTeleport;
        }

        public long getTeleportWarmup() {
            return this.teleportWarmup;
        }

        public String getMenusGlobalWarpsOrder() {
            return this.menusGlobalWarpsOrder;
        }

        public boolean isMenusWarpManageEnabled() {
            return this.menusWarpManageEnabled;
        }

        public boolean isMenusWarpCategoryManageEnabled() {
            return this.menusWarpCategoryManageEnabled;
        }

        public boolean isCategoriesEnabled() {
            return this.categoriesEnabled;
        }

        public String getCategoriesDefaultName() {
            return this.categoriesDefaultName;
        }

        public int getNamesMinLength() {
            return this.namesMinLength;
        }

        public int getNamesMaxLength() {
            return this.namesMaxLength;
        }

        public List<String> getNamesBlacklist() {
            return this.namesBlacklist;
        }

        public boolean isSignsEnabled() {
            return this.signsEnabled;
        }

        public String getSignsCreateLine() {
            return this.signsCreateLine;
        }

        public List<String> getSignsActiveLines() {
            return this.signsActiveLines;
        }

        public List<String> getSignsInactiveLines() {
            return this.signsInactiveLines;
        }

    }

    private static List<String> formatSignLines(List<String> lines) {
        if (lines.size() > 4) {
            lines.subList(4, lines.size()).clear();
        }

        List<String> formattedLines = Formatters.formatList(lines, Formatters.COLOR_FORMATTER);

        while (formattedLines.size() < 4) {
            formattedLines.add("");
        }

        return Collections.unmodifiableList(formattedLines);
    }

    private static boolean isValidDefaultWarpCategoryName(String name, int minLength, int maxLength) {
        return !Text.isBlank(name) && !name.contains(" ") && name.length() >= minLength && name.length() <= maxLength;
    }

}
