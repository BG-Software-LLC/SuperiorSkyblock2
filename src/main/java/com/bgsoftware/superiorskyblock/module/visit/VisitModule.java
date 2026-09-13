package com.bgsoftware.superiorskyblock.module.visit;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.modules.ModuleLogger;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.module.BuiltinModule;
import com.bgsoftware.superiorskyblock.module.IModuleConfiguration;
import com.bgsoftware.superiorskyblock.module.visit.commands.CmdVisit;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class VisitModule extends BuiltinModule<VisitModule.Configuration> {

    public VisitModule() {
        super("visit");
    }

    @Override
    protected boolean onConfigCreate(SuperiorSkyblockPlugin plugin, CommentedConfiguration moduleConfig, boolean firstTime) {
        File mainConfigFile = new File(plugin.getDataFolder(), "config.yml");
        if (!mainConfigFile.exists()) {
            return false;
        }

        CommentedConfiguration mainConfig = CommentedConfiguration.loadConfiguration(mainConfigFile);
        boolean updatedConfig = false;

        if (mainConfig.isLong("visit-warmup")) {
            moduleConfig.set("teleport-warmup", mainConfig.getLong("visit-warmup"));
            mainConfig.set("visit-warmup", null);
            updatedConfig = true;
        }
        if (mainConfig.isBoolean("visitors-sign.required-for-visit")) {
            moduleConfig.set("signs.required-for-visit", mainConfig.getBoolean("visitors-sign.required-for-visit"));
            mainConfig.set("visitors-sign.required-for-visit", null);
            updatedConfig = true;
        }
        if (mainConfig.isString("visitors-sign.line")) {
            moduleConfig.set("signs.create-line", mainConfig.getString("visitors-sign.line"));
            mainConfig.set("visitors-sign.line", null);
            updatedConfig = true;
        }
        if (mainConfig.isString("visitors-sign.active")) {
            moduleConfig.set("signs.active-lines", mainConfig.getString("visitors-sign.active"));
            mainConfig.set("visitors-sign.active", null);
            updatedConfig = true;
        }
        if (mainConfig.isString("visitors-sign.inactive")) {
            moduleConfig.set("signs.inactive-line", mainConfig.getString("visitors-sign.inactive"));
            mainConfig.set("visitors-sign.inactive", null);
            updatedConfig = true;
        }
        if (mainConfig.isString("visitors-sign.description-line-format")) {
            moduleConfig.set("descriptions.line-format", mainConfig.getString("visitors-sign.description-line-format"));
            mainConfig.set("visitors-sign.description-line-format", null);
            updatedConfig = true;
        }

        File oldMenuFile = new File(plugin.getDataFolder(), "menus/global-warps.yml");
        if (oldMenuFile.exists()) {
            CommentedConfiguration oldMenu = CommentedConfiguration.loadConfiguration(oldMenuFile);

            // If global-warps menu was using visitor homes, we disable warps command and reassign its aliases to visit command.
            if (oldMenu.getBoolean("visitor-warps")) {
                List<String> disabledCommands = mainConfig.getStringList("disabled-commands");

                if (!disabledCommands.contains("warps")) {
                    disabledCommands.add("warps");
                    mainConfig.set("disabled-commands", disabledCommands);
                }

                List<String> commandAliases = mainConfig.getStringList("command-aliases.warps");

                if (!commandAliases.isEmpty()) {
                    mainConfig.set("command-aliases.warps", null);
                    commandAliases.add("warps");
                    mainConfig.set("command-aliases.visit", commandAliases);
                }

                updatedConfig = true;
            }
        }

        if (updatedConfig) {
            mainConfig.set("visitors-sign", null);

            try {
                mainConfig.save(mainConfigFile);
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
        return new SuperiorCommand[]{new CmdVisit()};
    }

    @Override
    protected SuperiorCommand[] getSuperiorAdminCommands(SuperiorSkyblockPlugin plugin) {
        return new SuperiorCommand[0];
    }

    @Override
    protected Configuration createConfigFile(CommentedConfiguration config) {
        return new Configuration(config, logger());
    }

    public static class Configuration implements IModuleConfiguration {

        private final boolean enabled;
        private final boolean onlyDefaultDimension;
        private final long teleportWarmup;
        private final boolean menusVisitIslandsEnabled;
        private final String menusVisitIslandsOrder;
        private final boolean signsRequiredForVisit;
        private final String signsCreateLine;
        private final String signsActiveLine;
        private final String signsInactiveLine;
        private final boolean descriptionsEnabled;
        private final String descriptionsLineFormat;

        Configuration(CommentedConfiguration config, ModuleLogger logger) {
            this.enabled = config.getBoolean("enabled", true);
            this.onlyDefaultDimension = config.getBoolean("only-default-dimension", true);
            this.teleportWarmup = config.getLong("teleport-warmup", 0);
            this.menusVisitIslandsEnabled = config.getBoolean("menus.visit-islands-enabled", true);
            String menusVisitIslandsOrder = config.getString("menus.visit-islands-order", "WORTH").toUpperCase(Locale.ENGLISH);
            if (SortingType.getByName(menusVisitIslandsOrder) == null) {
                logger.w("Invalid menu islands order sorting type '" + menusVisitIslandsOrder + "', using 'WORTH' instead.");
                menusVisitIslandsOrder = "WORTH";
            }
            this.menusVisitIslandsOrder = menusVisitIslandsOrder;
            this.signsRequiredForVisit = config.getBoolean("signs.required-for-visit", true);
            this.signsCreateLine = config.getString("signs.create-line", "[Welcome]");
            this.signsActiveLine = Formatters.COLOR_FORMATTER.format(config.getString("signs.active-line", "&a[Welcome]"));
            this.signsInactiveLine = Formatters.COLOR_FORMATTER.format(config.getString("signs.inactive-line", "&c[Welcome]"));
            this.descriptionsEnabled = config.getBoolean("descriptions.enabled", true);
            this.descriptionsLineFormat = Formatters.COLOR_FORMATTER.format(config.getString("descriptions.line-format", "&8 - {0}"));
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }

        public boolean isOnlyDefaultDimension() {
            return this.onlyDefaultDimension;
        }

        public long getTeleportWarmup() {
            return this.teleportWarmup;
        }

        public boolean isMenusVisitIslandsEnabled() {
            return this.menusVisitIslandsEnabled;
        }

        public String getMenusVisitIslandsOrder() {
            return this.menusVisitIslandsOrder;
        }

        public boolean isSignsRequiredForVisit() {
            return this.signsRequiredForVisit;
        }

        public String getSignsCreateLine() {
            return this.signsCreateLine;
        }

        public String getSignsActiveLine() {
            return this.signsActiveLine;
        }

        public String getSignsInactiveLine() {
            return this.signsInactiveLine;
        }

        public boolean isDescriptionsEnabled() {
            return this.descriptionsEnabled;
        }

        public String getDescriptionsLineFormat() {
            return this.descriptionsLineFormat;
        }

    }

}
