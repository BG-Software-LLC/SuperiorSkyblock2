package com.bgsoftware.superiorskyblock.core.menu.impl.internal;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.collections.ArrayMap;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.view.BaseMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.EmptyViewArgs;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MenuCustom extends AbstractMenu<BaseMenuView, EmptyViewArgs> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<MessagesService> messagesService = new LazyReference<MessagesService>() {
        @Override
        protected MessagesService create() {
            return plugin.getServices().getService(MessagesService.class);
        }
    };

    private MenuCustom(MenuParseResult<BaseMenuView> parseResult, String fileName, @Nullable CommandArgs commandArgs) {
        super(MenuIdentifiers.MENU_CUSTOM_PREFIX + fileName, parseResult);
        if (commandArgs != null)
            plugin.getCommands().registerCommand(new CustomMenuCommand(commandArgs));
    }

    @Override
    protected BaseMenuView createViewInternal(SuperiorPlayer superiorPlayer, EmptyViewArgs unused,
                                              @Nullable MenuView<?, ?> previousMenuView) {
        return new BaseMenuView(superiorPlayer, previousMenuView, this);
    }

    public static MenuCustom createInstance(File menuFile) {
        String fileName = menuFile.getName();

        MenuParseResult<BaseMenuView> menuParseResult = MenuParserImpl.getInstance().loadCustomMenu(fileName, null);

        if (menuParseResult == null)
            return null;

        YamlConfiguration cfg = menuParseResult.getConfig();
        CommandArgs args = null;

        if (cfg.isConfigurationSection("command")) {
            ConfigurationSection commandsSection = cfg.getConfigurationSection("command");

            if (commandsSection == null) {
                Log.warnFromFile(fileName, "Custom menu doesn't have it's command section configured correctly, skipping...");
                return null;
            }

            List<String> aliases = commandsSection.isList("aliases") ?
                    commandsSection.getStringList("aliases") :
                    Arrays.asList(commandsSection.getString("aliases", "").split(", "));
            String permission = commandsSection.getString("permission", "");
            Map<Locale, IMessageComponent> descriptions = new ArrayMap<>();
            if (commandsSection.isConfigurationSection("description")) {
                ConfigurationSection description = commandsSection.getConfigurationSection("description");
                for (String locale : description.getKeys(false)) {
                    descriptions.put(PlayerLocales.getLocale(locale),
                            messagesService.get().parseComponent(cfg, description.getCurrentPath() + "." + locale));
                }
            }
            boolean displayCommand = commandsSection.getBoolean("display-command", false);
            boolean requireIsland = commandsSection.getBoolean("require-island", false);

            args = new CommandArgs(aliases, permission, descriptions, displayCommand, requireIsland);
        }

        return new MenuCustom(menuParseResult, fileName, args);
    }

    private static class CommandArgs {

        private final List<String> aliases;
        private final String permission;
        private final Map<Locale, IMessageComponent> descriptions;
        private final boolean displayCommand;
        private final boolean requireIsland;

        CommandArgs(List<String> aliases, String permission, Map<Locale, IMessageComponent> descriptions, boolean displayCommand, boolean requireIsland) {
            this.aliases = aliases;
            this.permission = permission;
            this.descriptions = descriptions;
            this.displayCommand = displayCommand;
            this.requireIsland = requireIsland;
        }

    }

    private class CustomMenuCommand implements ISuperiorCommand {

        private final CommandArgs args;

        public CustomMenuCommand(CommandArgs args) {
            this.args = args;
        }

        @Override
        public List<String> getAliases() {
            return this.args.aliases;
        }

        @Override
        public String getPermission() {
            return this.args.permission;
        }

        @Override
        public String getUsage(Locale locale) {
            return this.args.aliases.get(0);
        }

        @Override
        public String getDescription(Locale locale) {
            return this.args.descriptions.getOrDefault(locale, EmptyMessageComponent.getInstance()).getMessage();
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
            return this.args.displayCommand;
        }

        @Override
        public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

            if (this.args.requireIsland && !superiorPlayer.hasIsland()) {
                Message.INVALID_ISLAND.send(superiorPlayer);
                return;
            }

            MenuCustom.this.createView(superiorPlayer, EmptyViewArgs.INSTANCE);
        }

        @Override
        public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
            return Collections.emptyList();
        }

    }

}
