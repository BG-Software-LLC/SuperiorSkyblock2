package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.handlers.CommandsManager;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.io.FileClassLoader;
import com.bgsoftware.superiorskyblock.core.io.Files;
import com.bgsoftware.superiorskyblock.core.io.JarFiles;
import com.bgsoftware.superiorskyblock.core.io.loader.FilesLookup;
import com.bgsoftware.superiorskyblock.core.io.loader.FilesLookupFactory;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("UnstableApiUsage")
public class CommandsManagerImpl extends Manager implements CommandsManager {

    private static volatile Set<String> DANGEROUS_COMMANDS_CACHE = Collections.emptySet();

    private final Map<UUID, Map<String, Long>> commandsCooldown = new ConcurrentHashMap<>();
    private final Map<String, Cache<UUID, DangerousCommandRequest>> dangerousCommandsRequests = new ConcurrentHashMap<>();

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
        // Pending requests of dangerous commands should not survive reloads.
        cancelAllDangerousRequests();

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

        if (pendingCommands != null) {
            pendingCommands.add(() -> registerCommand(superiorCommand));
            return;
        }

        playerCommandsMap.registerCommand(superiorCommand);
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
        adminCommandsMap.registerCommand(superiorCommand);
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

    public static CompletableFuture<Boolean> dispatchCommand(CommandSender sender, String command) {
        if (!BukkitExecutor.isFolia())
            return CompletableFuture.completedFuture(Bukkit.dispatchCommand(sender, command));
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return BukkitExecutor.submit(player, () -> player.isOnline() && Bukkit.dispatchCommand(player, command));
        }
        return BukkitExecutor.submit(() -> Bukkit.dispatchCommand(sender, command));
    }

    @Override
    public void dispatchSubCommand(CommandSender sender, String subCommand) {
        dispatchSubCommand(sender, subCommand, null);
    }

    @Override
    public void dispatchSubCommand(CommandSender sender, String subCommand, @Nullable String args) {
        // We first check that the sub command is enabled.
        if (getCommand(subCommand) == null) {
            dispatchCommand(sender, this.label + " " + subCommand + (args == null ? "" : " " + args));
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

    public static void registerListeners(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, CommandsManagerImpl::onSettingsUpdate);
        // Admin commands can be registered after settings are loaded (e.g. by modules).
        dispatcher.registerCallback(PluginEventType.COMMANDS_UPDATE_EVENT, CommandsManagerImpl::onSettingsUpdate);
    }

    @Override
    public boolean isDangerousCommand(SuperiorCommand command) {
        return DANGEROUS_COMMANDS_CACHE.contains(getCommandLabel(command));
    }

    @Override
    public void submitDangerousRequest(Player player, SuperiorCommand command, String[] args) {
        String commandLabel = getCommandLabel(command);
        String executedCommand = "/" + this.label + " " + String.join(" ", args);

        // Overrides any previous request of the player for this command, and resets its expiry time.
        dangerousCommandsRequests.computeIfAbsent(commandLabel, l -> CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build()
        ).put(player.getUniqueId(), new DangerousCommandRequest(player.getUniqueId(), player.getName(),
                executedCommand, args.clone(), System.currentTimeMillis()));

        Message.DANGEROUS_COMMAND_REQUEST_SENT.send(player, executedCommand);
        Message.DANGEROUS_COMMAND_REQUEST_CONSOLE.send(Bukkit.getConsoleSender(), player.getName(),
                executedCommand, this.label + " admin " + commandLabel);
    }

    @Override
    public boolean hasPendingDangerousRequests(SuperiorCommand command) {
        Cache<UUID, DangerousCommandRequest> requests = dangerousCommandsRequests.get(getCommandLabel(command));
        if (requests == null)
            return false;

        requests.cleanUp();
        return requests.size() > 0;
    }

    @Override
    public void approveDangerousRequests(CommandSender sender, SuperiorCommand command) {
        Cache<UUID, DangerousCommandRequest> requests = dangerousCommandsRequests.get(getCommandLabel(command));
        if (requests == null)
            return;

        List<DangerousCommandRequest> requestsToExecute = new LinkedList<>(requests.asMap().values());
        // We clear the requests before executing them, so they cannot be executed twice.
        requestsToExecute.removeIf(request -> !requests.asMap().remove(request.playerUUID, request));

        requestsToExecute.sort(Comparator.comparingLong(request -> request.creationTime));
        CompletableFuture<Void> requestsCompletion = CompletableFuture.completedFuture(null);

        for (DangerousCommandRequest request : requestsToExecute) {
            Player player = Bukkit.getPlayer(request.playerUUID);

            if (player == null || !player.isOnline()) {
                Message.DANGEROUS_COMMAND_PLAYER_OFFLINE.send(sender, request.playerName, request.executedCommand);
                continue;
            }

            Runnable executeRequest = () -> {
                if (!player.isOnline() || Bukkit.getPlayer(request.playerUUID) != player) {
                    Message.DANGEROUS_COMMAND_PLAYER_OFFLINE.send(sender, request.playerName, request.executedCommand);
                    return;
                }

                if (!CommandsHelper.hasCommandAccess(command, player)) {
                    Message.DANGEROUS_COMMAND_NO_ACCESS.send(sender, request.playerName, request.executedCommand);
                    return;
                }

                Message.DANGEROUS_COMMAND_APPROVED.send(player, request.executedCommand);

                try {
                    command.execute(plugin, player, request.args);
                } catch (Throwable error) {
                    Log.error(error, "An unexpected error occurred while executing approved command ",
                            request.executedCommand, " of ", request.playerName, ":");
                }
            };
            if (BukkitExecutor.isFolia()) {
                requestsCompletion = requestsCompletion.thenCompose(ignored ->
                        BukkitExecutor.<Void>submit(player, () -> {
                            executeRequest.run();
                            return null;
                        })).handle((ignored, error) -> {
                    if (error != null) {
                        if (!player.isOnline() || Bukkit.getPlayer(request.playerUUID) != player) {
                            Message.DANGEROUS_COMMAND_PLAYER_OFFLINE.send(sender, request.playerName, request.executedCommand);
                        } else {
                            Log.error(error, "An unexpected error occurred while scheduling approved command ",
                                    request.executedCommand, " of ", request.playerName, ":");
                        }
                    }
                    return null;
                });
            } else {
                executeRequest.run();
            }
        }
    }

    @Override
    public void cancelDangerousRequests(UUID playerUUID) {
        for (Cache<UUID, DangerousCommandRequest> requests : dangerousCommandsRequests.values()) {
            DangerousCommandRequest request = requests.asMap().remove(playerUUID);
            if (request != null) {
                Message.DANGEROUS_COMMAND_PLAYER_QUIT.send(Bukkit.getConsoleSender(), request.playerName, request.executedCommand);
            }
        }
    }

    @Override
    public void cancelAllDangerousRequests() {
        for (Cache<UUID, DangerousCommandRequest> requests : dangerousCommandsRequests.values()) {
            for (DangerousCommandRequest request : requests.asMap().values()) {
                Player player = Bukkit.getPlayer(request.playerUUID);
                if (player != null)
                    Message.DANGEROUS_COMMAND_CANCELLED.send(player, request.executedCommand);
            }
        }

        dangerousCommandsRequests.clear();
    }

    private static void onSettingsUpdate() {
        Set<String> dangerousCommands = new HashSet<>();
        SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
        plugin.getSettings().getDangerousCommands().forEach(commandLabel -> {
            SuperiorCommand superiorCommand = plugin.getCommands().getAdminCommand(commandLabel);
            if (superiorCommand != null)
                dangerousCommands.add(getCommandLabel(superiorCommand));
        });
        DANGEROUS_COMMANDS_CACHE = Collections.unmodifiableSet(dangerousCommands);
    }

    private static String getCommandLabel(SuperiorCommand command) {
        return command.getAliases().get(0).toLowerCase(Locale.ENGLISH);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void loadCommands() {
        File commandsFolder = new File(plugin.getDataFolder(), "commands");

        if (!commandsFolder.exists()) {
            commandsFolder.mkdirs();
            return;
        }

        List<File> folderFiles = Files.listFolderFiles(commandsFolder, false, file -> file.getName().endsWith(".jar"));
        if (folderFiles.isEmpty())
            return;

        try (FilesLookup filesLookup = FilesLookupFactory.getInstance().lookupFolder(commandsFolder)) {
            for (File file : folderFiles) {
                try {
                    String fileName = file.getName();
                    file = filesLookup.getFile(fileName);

                    FileClassLoader classLoader = new FileClassLoader(file, plugin.getPluginClassLoader(),
                            plugin.getNMSAlgorithms().getClassProcessor());

                    //noinspection deprecation
                    Class<?> commandClass = JarFiles.getClass(file.toURL(), SuperiorCommand.class, classLoader).getLeft();

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
            if (plugin.getTaskScheduler().isFolia()) {
                if (!plugin.isReady()) {
                    sender.sendMessage("SuperiorSkyblock is still starting. Please try again in a moment.");
                    return false;
                }
                if (sender instanceof Player && !plugin.getTaskScheduler().isOwned((Player) sender)) {
                    String[] arguments = args.clone();
                    plugin.getTaskScheduler().entity((Player) sender, () -> execute(sender, label, arguments), null, 0L, 0L);
                    return false;
                }
            }
            java.util.Locale locale = PlayerLocales.getLocale(sender);

            String executedSubCommand;

            if (args.length > 0) {
                executedSubCommand = args[0];

                Log.debug(Debug.EXECUTE_COMMAND, sender.getName(), executedSubCommand);

                SuperiorCommand command = playerCommandsMap.getCommand(executedSubCommand);
                if (command != null) {
                    if (!(sender instanceof Player) && !command.canBeExecutedByConsole()) {
                        Message.CUSTOM.send(sender, "&cCan be executed only by players!", true);
                        return false;
                    }

                    if (!CommandsHelper.hasCommandAccess(command, sender)) {
                        Log.debugResult(Debug.EXECUTE_COMMAND, "Return Missing Permission", command.getPermission());

                        if (!plugin.getSettings().isHelpOnNoPermission())
                            Message.NO_COMMAND_PERMISSION.send(sender, locale, command.getPermission());
                        else if (!"help".equalsIgnoreCase(executedSubCommand))
                            dispatchSubCommand(sender, "help");

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

                                commandsCooldown.computeIfAbsent(uuid, u -> new ConcurrentHashMap<>()).put(commandLabel,
                                        timeNow + commandCooldown.getKey());
                            }
                        }
                    }

                    command.execute(plugin, sender, args);
                    return false;
                }

                if (!plugin.getSettings().isHelpOnInvalidCommand())
                    Message.INVALID_COMMAND.send(sender, locale, executedSubCommand);
                else if (!"help".equalsIgnoreCase(executedSubCommand))
                    dispatchSubCommand(sender, "help");

                return false;
            }

            if (sender instanceof Player) {
                SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

                if (superiorPlayer != null) {
                    String subCommandToExecute;

                    if (!superiorPlayer.hasIsland())
                        subCommandToExecute = "create";
                    else if (superiorPlayer.hasToggledPanel())
                        subCommandToExecute = "panel";
                    else
                        subCommandToExecute = "tp";

                    dispatchSubCommand(sender, subCommandToExecute);
                    return false;
                }
            }

            return false;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String label, String[] args) {
            if (plugin.getTaskScheduler().isFolia() && !plugin.isReady())
                return Collections.emptyList();
            if (args.length > 0) {
                SuperiorCommand command = playerCommandsMap.getCommand(args[0]);
                if (command != null) {
                    return CommandsHelper.shouldDisplayCommandForPlayer(command, sender) ?
                            command.tabComplete(plugin, sender, args) : Collections.emptyList();
                }
            }

            List<String> list = new LinkedList<>();

            for (SuperiorCommand subCommand : getSubCommands()) {
                if (CommandsHelper.shouldDisplayCommandForPlayer(subCommand, sender)) {
                    List<String> aliases = new LinkedList<>(subCommand.getAliases());
                    aliases.addAll(plugin.getSettings().getCommandAliases().getOrDefault(aliases.get(0).toLowerCase(Locale.ENGLISH), Collections.emptyList()));
                    for (String alias : aliases) {
                        if (alias.contains(args[0].toLowerCase(Locale.ENGLISH))) {
                            list.add(alias);
                        }
                    }
                }
            }

            return list;
        }

    }

    private static class DangerousCommandRequest {

        private final UUID playerUUID;
        private final String playerName;
        private final String executedCommand;
        private final String[] args;
        private final long creationTime;

        DangerousCommandRequest(UUID playerUUID, String playerName, String executedCommand, String[] args, long creationTime) {
            this.playerUUID = playerUUID;
            this.playerName = playerName;
            this.executedCommand = executedCommand;
            this.args = args;
            this.creationTime = creationTime;
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
