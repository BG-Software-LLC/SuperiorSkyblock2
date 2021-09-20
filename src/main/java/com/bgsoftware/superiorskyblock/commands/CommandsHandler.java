package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.handlers.CommandsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handler.AbstractHandler;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class CommandsHandler extends AbstractHandler implements CommandsManager {

    private final Map<UUID, Map<String, Long>> commandsCooldown = new HashMap<>();

    private final CommandsMap playerCommandsMap;
    private final CommandsMap adminCommandsMap;

    private Set<Runnable> pendingCommands = new HashSet<>();

    private PluginCommand pluginCommand;
    private String label = null;

    public CommandsHandler(SuperiorSkyblockPlugin plugin, CommandsMap playerCommandsMap, CommandsMap adminCommandsMap){
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

        if(commandSections.length > 1){
            pluginCommand.setAliases(Arrays.asList(Arrays.copyOfRange(commandSections, 1, commandSections.length)));
        }

        plugin.getNMSAlgorithms().registerCommand(pluginCommand);

        playerCommandsMap.loadDefaultCommands();
        adminCommandsMap.loadDefaultCommands();

        loadCommands();

        if(this.pendingCommands != null) {
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
        if(pendingCommands != null){
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
        return playerCommandsMap.getSubCommands();
    }

    @Nullable
    @Override
    public SuperiorCommand getCommand(String commandLabel) {
        return playerCommandsMap.getCommand(commandLabel);
    }

    @Override
    public List<SuperiorCommand> getAdminSubCommands() {
        return adminCommandsMap.getSubCommands();
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
        String[] commandArguments = new String[argsSplit.length + 1];
        commandArguments[0] = subCommand;
        System.arraycopy(argsSplit, 0, commandArguments, 1, argsSplit.length);
        pluginCommand.execute(sender, "", commandArguments);
    }

    public String getLabel(){
        return label;
    }

    public void registerCommand(SuperiorCommand superiorCommand, boolean sort){
        if(pendingCommands != null){
            pendingCommands.add(() -> registerCommand(superiorCommand, sort));
            return;
        }

        playerCommandsMap.registerCommand(superiorCommand, sort);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "ConstantConditions"})
    private void loadCommands(){
        File commandsFolder = new File(plugin.getDataFolder(), "commands");

        if(!commandsFolder.exists()){
            commandsFolder.mkdirs();
            return;
        }

        for(File file : commandsFolder.listFiles()){
            if(!file.getName().endsWith(".jar"))
                continue;

            try {
                //noinspection deprecation
                Optional<Class<?>> commandClass = FileUtils.getClasses(file.toURL(), SuperiorCommand.class).stream().findFirst();

                if(!commandClass.isPresent())
                    continue;

                SuperiorCommand superiorCommand = createInstance(commandClass.get());

                if(file.getName().toLowerCase().contains("admin")) {
                    registerAdminCommand(superiorCommand);
                    SuperiorSkyblockPlugin.log("Successfully loaded external admin command: " + file.getName().split("\\.")[0]);
                }
                else {
                    registerCommand(superiorCommand);
                    SuperiorSkyblockPlugin.log("Successfully loaded external command: " + file.getName().split("\\.")[0]);
                }

            }catch(Exception ex){
                ex.printStackTrace();
            }
        }

    }

    private SuperiorCommand createInstance(Class<?> clazz) throws Exception{
        Preconditions.checkArgument(SuperiorCommand.class.isAssignableFrom(clazz), "Class " + clazz + " is not a SuperiorCommand.");

        for(Constructor<?> constructor : clazz.getConstructors()){
            if(constructor.getParameterCount() == 0) {
                if(!constructor.isAccessible())
                    constructor.setAccessible(true);

                return (SuperiorCommand) constructor.newInstance();
            }
        }

        throw new IllegalArgumentException("Class " + clazz + " has no valid constructors.");
    }

    private class PluginCommand extends BukkitCommand {

        PluginCommand(String islandCommandLabel){
            super(islandCommandLabel);
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            java.util.Locale locale = LocaleUtils.getLocale(sender);

            if(args.length > 0){
                SuperiorCommand command = playerCommandsMap.getCommand(args[0]);
                if(command != null){
                    if(!(sender instanceof Player) && !command.canBeExecutedByConsole()){
                        Locale.sendMessage(sender, "&cCan be executed only by players!", true);
                        return false;
                    }

                    if(!command.getPermission().isEmpty() && !sender.hasPermission(command.getPermission())) {
                        SuperiorSkyblockPlugin.debug("Action: Execute Command, Player: " + sender.getName() + ", Command: " + args[0] + ", Missing Permission: " + command.getPermission());
                        Locale.NO_COMMAND_PERMISSION.send(sender, locale);
                        return false;
                    }

                    if(args.length < command.getMinArgs() || args.length > command.getMaxArgs()){
                        Locale.COMMAND_USAGE.send(sender, locale, getLabel() + " " + command.getUsage(locale));
                        return false;
                    }

                    String commandLabel = command.getAliases().get(0);

                    if(sender instanceof Player && plugin.getSettings().getCommandsCooldown().containsKey(commandLabel)) {
                        UUID uuid = ((Player) sender).getUniqueId();

                        long timeToExecute = commandsCooldown.containsKey(uuid) && commandsCooldown.get(uuid).containsKey(commandLabel) ?
                                commandsCooldown.get(uuid).get(commandLabel) : -1;

                        long timeNow = System.currentTimeMillis();

                        if(timeNow < timeToExecute){
                            Locale.COMMAND_COOLDOWN_FORMAT.send(sender, locale,
                                    StringUtils.formatTime(locale, timeToExecute - timeNow, TimeUnit.MILLISECONDS));
                            return false;
                        }

                        if(!commandsCooldown.containsKey(uuid)){
                            commandsCooldown.put(uuid, new HashMap<>());
                        }

                        commandsCooldown.get(uuid).put(commandLabel,
                                timeNow + plugin.getSettings().getCommandsCooldown().get(commandLabel).getKey());
                    }

                    command.execute(plugin, sender, args);
                    return false;
                }
            }

            if(sender instanceof Player){
                SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

                if(superiorPlayer != null){
                    Island island = superiorPlayer.getIsland();

                    if(args.length != 0){
                        Bukkit.dispatchCommand(sender, label + " help");
                    }
                    else if(island == null){
                        Bukkit.dispatchCommand(sender, label + " create");
                    }
                    else if(superiorPlayer.hasToggledPanel()){
                        Bukkit.dispatchCommand(sender, label + " panel");
                    }
                    else{
                        Bukkit.dispatchCommand(sender, label + " tp");
                    }

                    return false;
                }
            }

            Locale.NO_COMMAND_PERMISSION.send(sender, locale);

            return false;
        }


        @Override
        public List<String> tabComplete(CommandSender sender, String label, String[] args) {
            if(args.length > 0){
                SuperiorCommand command = playerCommandsMap.getCommand(args[0]);
                if(command != null){
                    return command.getPermission() != null && !sender.hasPermission(command.getPermission()) ?
                            new ArrayList<>() : command.tabComplete(plugin, sender, args);
                }
            }

            List<String> list = new ArrayList<>();

            for(SuperiorCommand subCommand : getSubCommands()) {
                if (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission())) {
                    List<String> aliases = new ArrayList<>(subCommand.getAliases());
                    aliases.addAll(plugin.getSettings().getCommandAliases().getOrDefault(aliases.get(0).toLowerCase(), new ArrayList<>()));
                    for (String _aliases : aliases) {
                        if (_aliases.contains(args[0].toLowerCase())) {
                            list.add(_aliases);
                        }
                    }
                }
            }

            return list;
        }

    }

}
