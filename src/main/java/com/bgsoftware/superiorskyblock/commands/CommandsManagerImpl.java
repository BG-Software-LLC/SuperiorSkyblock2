package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.handlers.CommandsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.io.JarFiles;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
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
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CommandsManagerImpl extends Manager implements CommandsManager {

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

    @Nullable
    @Override
    public SuperiorCommand getAdminCommand(String commandLabel) {
        return adminCommandsMap.getCommand(commandLabel);
    }

    @Override
    public void dispatchSubCommand(CommandSender sender, String subCommand) {
        dispatchSubCommand(sender, subCommand, null);
    }

    @Override
    public void dispatchSubCommand(CommandSender sender, String subCommand, @Nullable String args) {
        // We first check that the sub command is enabled.
        if (getCommand(subCommand) == null) {
            Bukkit.dispatchCommand(sender, this.label + " " + subCommand + (args == null ? "" : " " + args));
            return;
        }

        String[] argsSplit = args == null ? null : args.split(" ");
        String[] commandArguments;

        if (argsSplit == null || (argsSplit.length == 1 && argsSplit[0].isEmpty())) {
            commandArguments = new String[1];
            commandArguments[0] = subCommand;
        } else {
            commandArguments = new String[argsSplit.length + 1];
            commandArguments[0] = subCommand;
            System.arraycopy(argsSplit, 0, commandArguments, 1, argsSplit.length);
        }

        pluginCommand.execute(sender, "", commandArguments);
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
            if (!file.getName().endsWith(".jar"))
                continue;

            try {
                //noinspection deprecation
                Class<?> commandClass = JarFiles.getClass(file.toURL(), SuperiorCommand.class, plugin.getPluginClassLoader()).getRight();

                if (commandClass == null)
                    continue;

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
                if (!constructor.isAccessible())
                    constructor.setAccessible(true);

                return (SuperiorCommand) constructor.newInstance();
            }
        }

        throw new IllegalArgumentException("Class " + clazz + " has no valid constructors.");
    }

    private class PluginCommand extends BukkitCommand {

        PluginCommand(String islandCommandLabel) {
            super(islandCommandLabel);
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            java.util.Locale locale = PlayerLocales.getLocale(sender);

            String executedSubCommand = null;

            if (args.length > 0) {
                executedSubCommand = args[0];

                Log.debug(Debug.EXECUTE_COMMAND, sender.getName(), executedSubCommand);

                SuperiorCommand command = playerCommandsMap.getCommand(executedSubCommand);
                if (command != null) {
                    if (!(sender instanceof Player) && !command.canBeExecutedByConsole()) {
                        Message.CUSTOM.send(sender, "&cCan be executed only by players!", true);
                        return false;
                    }

                    if (!command.getPermission().isEmpty() && !sender.hasPermission(command.getPermission())) {
                        Log.debugResult(Debug.EXECUTE_COMMAND, "Return Missing Permission", command.getPermission());
                        Message.NO_COMMAND_PERMISSION.send(sender, locale);
                        return false;
                    }

                    if (args.length < command.getMinArgs() || args.length > command.getMaxArgs()) {
                        Log.debugResult(Debug.EXECUTE_COMMAND, "Return Incorrect Usage", command.getUsage(locale));
                        Message.COMMAND_USAGE.send(sender, locale, getLabel() + " " + command.getUsage(locale));
                        return false;
                    }

                    if (sender instanceof Player) {
                        UUID uuid = ((Player) sender).getUniqueId();
                        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(uuid);
                        if (!superiorPlayer.hasPermission("superior.admin.bypass.cooldowns")) {
                            Pair<Integer, String> commandCooldown = getCooldown(command);
                            if (commandCooldown != null) {
                                String commandLabel = command.getAliases().get(0);

                                Map<String, Long> playerCooldowns = commandsCooldown.get(uuid);
                                long timeNow = System.currentTimeMillis();

                                if (playerCooldowns != null) {
                                    Long timeToExecute = playerCooldowns.get(commandLabel);
                                    if (timeToExecute != null) {
                                        if (timeNow < timeToExecute) {
                                            String formattedTime = Formatters.TIME_FORMATTER.format(Duration.ofMillis(timeToExecute - timeNow), locale);
                                            Log.debugResult(Debug.EXECUTE_COMMAND, "Return Cooldown", formattedTime);
                                            Message.COMMAND_COOLDOWN_FORMAT.send(sender, locale, formattedTime);
                                            return false;
                                        }
                                    }
                                }

                                commandsCooldown.computeIfAbsent(uuid, u -> new HashMap<>()).put(commandLabel,
                                        timeNow + commandCooldown.getKey());
                            }
                        }
                    }

                    command.execute(plugin, sender, args);
                    return false;
                }
            }

            if (sender instanceof Player) {
                SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

                if (superiorPlayer != null) {
                    Island island = superiorPlayer.getIsland();

                    String subCommandToExecute;
                    if (args.length != 0) {
                        subCommandToExecute = "help";
                    } else if (island == null) {
                        subCommandToExecute = "create";
                    } else if (superiorPlayer.hasToggledPanel()) {
                        subCommandToExecute = "panel";
                    } else {
                        subCommandToExecute = "tp";
                    }

                    // We don't want to end up in an infinite loop
                    if (!subCommandToExecute.equalsIgnoreCase(executedSubCommand)) {
                        dispatchSubCommand(sender, subCommandToExecute);
                    }

                    return false;
                }
            }

            Message.NO_COMMAND_PERMISSION.send(sender, locale);

            return false;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String label, String[] args) {
            if (args.length > 0) {
                SuperiorCommand command = playerCommandsMap.getCommand(args[0]);
                if (command != null) {
                    return command.getPermission() != null && !sender.hasPermission(command.getPermission()) ?
                            Collections.emptyList() : command.tabComplete(plugin, sender, args);
                }
            }

            List<String> list = new LinkedList<>();

            for (SuperiorCommand subCommand : getSubCommands()) {
                if (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission())) {
                    List<String> aliases = new LinkedList<>(subCommand.getAliases());
                    aliases.addAll(plugin.getSettings().getCommandAliases().getOrDefault(aliases.get(0).toLowerCase(Locale.ENGLISH), Collections.emptyList()));
                    for (String _aliases : aliases) {
                        if (_aliases.contains(args[0].toLowerCase(Locale.ENGLISH))) {
                            list.add(_aliases);
                        }
                    }
                }
            }

            return list;
        }

    }

    @Nullable
    private Pair<Integer, String> getCooldown(SuperiorCommand command) {
        for (String alias : command.getAliases()) {
            Pair<Integer, String> commandCooldown = plugin.getSettings().getCommandsCooldown().get(alias);
            if (commandCooldown != null)
                return commandCooldown;
        }

        return null;
    }

}
