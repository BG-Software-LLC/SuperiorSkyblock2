package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.handlers.CommandsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentImpl;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.io.JarFiles;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CommandsManagerImpl extends Manager implements CommandsManager {

    private static final String[] EMPTY_ARGS = new String[0];

    private final Map<UUID, Map<String, Long>> commandsCooldown = new HashMap<>();

    private final CommandsMap playerCommandsMap;
    private final CommandsMap adminCommandsMap;

    private Set<Runnable> pendingCommands = new HashSet<>();

    private PluginCommand pluginCommand;
    private String label = null;

    public CommandsManagerImpl(SuperiorSkyblockPlugin plugin, CommandsMap playerCommandsMap, CommandsMap adminCommandsMap) {
        super(plugin);
        this.playerCommandsMap = playerCommandsMap;
        this.adminCommandsMap = adminCommandsMap;
    }

    @Override
    public void loadData() {
        String islandCommand = plugin.getSettings().getIslandCommand();
        label = islandCommand.split(",")[0];

        pluginCommand = new PluginCommand(label);

        String[] commandSections = islandCommand.split(",");

        if (commandSections.length > 1) {
            pluginCommand.setAliases(Arrays.asList(Arrays.copyOfRange(commandSections, 1, commandSections.length)));
        }

        plugin.getNMSAlgorithms().registerCommand(pluginCommand);

        playerCommandsMap.loadDefaultCommands();
        adminCommandsMap.loadDefaultCommands();

        loadCommands();

        if (this.pendingCommands != null) {
            Set<Runnable> pendingCommands = new HashSet<>(this.pendingCommands);
            this.pendingCommands = null;
            pendingCommands.forEach(Runnable::run);
        }
    }

    @Override
    public void registerCommand(SuperiorCommand superiorCommand) {
        Preconditions.checkNotNull(superiorCommand, "superiorCommand parameter cannot be null.");
        registerCommand(superiorCommand, true);
    }

    @Override
    public void unregisterCommand(SuperiorCommand superiorCommand) {
        playerCommandsMap.unregisterCommand(superiorCommand);
    }

    @Override
    public void registerAdminCommand(SuperiorCommand superiorCommand) {
        if (pendingCommands != null) {
            pendingCommands.add(() -> registerAdminCommand(superiorCommand));
            return;
        }

        Preconditions.checkNotNull(superiorCommand, "superiorCommand parameter cannot be null.");
        adminCommandsMap.registerCommand(superiorCommand, true);
    }

    @Override
    public void unregisterAdminCommand(SuperiorCommand superiorCommand) {
        Preconditions.checkNotNull(superiorCommand, "superiorCommand parameter cannot be null.");
        adminCommandsMap.unregisterCommand(superiorCommand);
    }

    @Override
    public List<SuperiorCommand> getSubCommands() {
        return getSubCommands(false);
    }

    @Override
    public List<SuperiorCommand> getSubCommands(boolean includeDisabled) {
        return playerCommandsMap.getSubCommands(includeDisabled);
    }

    @Nullable
    @Override
    public SuperiorCommand getCommand(String commandLabel) {
        return playerCommandsMap.getCommand(commandLabel);
    }

    @Override
    public List<SuperiorCommand> getAdminSubCommands() {
        return adminCommandsMap.getSubCommands(true);
    }

    public CommandsMap getAdminCommandsMap() {
        return this.adminCommandsMap;
    }

    @Nullable
    @Override
    public SuperiorCommand getAdminCommand(String commandLabel) {
        return adminCommandsMap.getCommand(commandLabel);
    }

    @Override
    public void dispatchSubCommand(CommandSender sender, String subCommand) {
        dispatchSubCommand(sender, subCommand, "");
    }

    @Override
    public void dispatchSubCommand(CommandSender sender, String subCommand, String args) {
        String[] argsSplit = args.split(" ");
        String[] commandArguments;

        if (argsSplit.length == 1 && argsSplit[0].isEmpty()) {
            commandArguments = new String[1];
            commandArguments[0] = subCommand;
        } else {
            commandArguments = new String[argsSplit.length + 1];
            commandArguments[0] = subCommand;
            System.arraycopy(argsSplit, 0, commandArguments, 1, argsSplit.length);
        }

        pluginCommand.execute(sender, "", commandArguments);
    }

    @Override
    public <E> CommandArgument<E> createArgument(String identifier, CommandArgumentType<E> argumentType, boolean isOptional, Object... displayNameComponents) {
        return new CommandArgumentImpl<>(identifier, displayNameComponents, isOptional, argumentType);
    }

    public String getLabel() {
        return label;
    }

    public void registerCommand(SuperiorCommand superiorCommand, boolean sort) {
        if (pendingCommands != null) {
            pendingCommands.add(() -> registerCommand(superiorCommand, sort));
            return;
        }

        playerCommandsMap.registerCommand(superiorCommand, sort);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadCommands() {
        File commandsFolder = new File(plugin.getDataFolder(), "commands");

        if (!commandsFolder.exists()) {
            commandsFolder.mkdirs();
            return;
        }

        for (File file : commandsFolder.listFiles()) {
            if (!file.getName().endsWith(".jar")) continue;

            try {
                //noinspection deprecation
                Class<?> commandClass = JarFiles.getClass(file.toURL(), SuperiorCommand.class, plugin.getPluginClassLoader()).getRight();

                if (commandClass == null) continue;

                SuperiorCommand superiorCommand = createInstance(commandClass);

                if (file.getName().toLowerCase(Locale.ENGLISH).contains("admin")) {
                    registerAdminCommand(superiorCommand);
                    Log.info("Successfully loaded external admin command: ", file.getName().split("\\.")[0]);
                } else {
                    registerCommand(superiorCommand);
                    Log.info("Successfully loaded external command: ", file.getName().split("\\.")[0]);
                }

            } catch (Exception error) {
                Log.error(error, "An unexpected error occurred while loading an external command ", file.getName(), ":");
            }
        }

    }

    private SuperiorCommand createInstance(Class<?> clazz) throws Exception {
        Preconditions.checkArgument(SuperiorCommand.class.isAssignableFrom(clazz), "Class " + clazz + " is not a SuperiorCommand.");

        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                if (!constructor.isAccessible()) constructor.setAccessible(true);

                return (SuperiorCommand) constructor.newInstance();
            }
        }

        throw new IllegalArgumentException("Class " + clazz + " has no valid constructors.");
    }

    private class PluginCommand extends BukkitCommand {

        private final SubCommandsHandler commandsHandler;

        PluginCommand(String islandCommandLabel) {
            super(islandCommandLabel);
            this.commandsHandler = new SubCommandsHandler(islandCommandLabel, CommandsManagerImpl.this.playerCommandsMap, this::handleUnknownCommand);
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            String subCommand = args.length == 0 ? null : args[0];
            ArgumentsReader commandArgsReader = new ArgumentsReader(args);
            commandArgsReader.setCursor(1);
            this.commandsHandler.execute(plugin, sender, subCommand, commandArgsReader);
            return false;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
            String subCommand = args.length == 0 ? null : args[0];
            ArgumentsReader commandArgsReader = new ArgumentsReader(args);
            commandArgsReader.setCursor(1);
            return this.commandsHandler.tabComplete(plugin, sender, subCommand, commandArgsReader);
        }

        private void handleUnknownCommand(SuperiorSkyblockPlugin plugin, CommandSender dispatcher, String subCommandName, ArgumentsReader reader) {
            if (dispatcher instanceof Player) {
                SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(dispatcher);

                if (superiorPlayer != null) {
                    Island island = superiorPlayer.getIsland();

                    if (reader.hasNext()) {
                        Bukkit.dispatchCommand(dispatcher, label + " help");
                    } else if (island == null) {
                        Bukkit.dispatchCommand(dispatcher, label + " create");
                    } else if (superiorPlayer.hasToggledPanel()) {
                        Bukkit.dispatchCommand(dispatcher, label + " panel");
                    } else {
                        Bukkit.dispatchCommand(dispatcher, label + " tp");
                    }

                    return;
                }
            }

            Locale locale = PlayerLocales.getLocale(dispatcher);
            Message.NO_COMMAND_PERMISSION.send(dispatcher, locale);
        }

        @Nullable
        private Pair<Integer, String> getCooldown(SuperiorCommand command) {
            for (String alias : command.getAliases()) {
                Pair<Integer, String> commandCooldown = plugin.getSettings().getCommandsCooldown().get(alias);
                if (commandCooldown != null) return commandCooldown;
            }

            return null;
        }

    }

}