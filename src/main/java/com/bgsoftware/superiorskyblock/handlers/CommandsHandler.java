package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.handlers.CommandsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CmdAccept;
import com.bgsoftware.superiorskyblock.commands.CmdAdmin;
import com.bgsoftware.superiorskyblock.commands.CmdBalance;
import com.bgsoftware.superiorskyblock.commands.CmdBan;
import com.bgsoftware.superiorskyblock.commands.CmdBank;
import com.bgsoftware.superiorskyblock.commands.CmdBiome;
import com.bgsoftware.superiorskyblock.commands.CmdBorder;
import com.bgsoftware.superiorskyblock.commands.CmdChest;
import com.bgsoftware.superiorskyblock.commands.CmdClose;
import com.bgsoftware.superiorskyblock.commands.CmdCoop;
import com.bgsoftware.superiorskyblock.commands.CmdCoops;
import com.bgsoftware.superiorskyblock.commands.CmdCounts;
import com.bgsoftware.superiorskyblock.commands.CmdCreate;
import com.bgsoftware.superiorskyblock.commands.CmdDelWarp;
import com.bgsoftware.superiorskyblock.commands.CmdDemote;
import com.bgsoftware.superiorskyblock.commands.CmdDeposit;
import com.bgsoftware.superiorskyblock.commands.CmdDisband;
import com.bgsoftware.superiorskyblock.commands.CmdExpel;
import com.bgsoftware.superiorskyblock.commands.CmdFly;
import com.bgsoftware.superiorskyblock.commands.CmdHelp;
import com.bgsoftware.superiorskyblock.commands.CmdInvite;
import com.bgsoftware.superiorskyblock.commands.CmdKick;
import com.bgsoftware.superiorskyblock.commands.CmdLang;
import com.bgsoftware.superiorskyblock.commands.CmdLeave;
import com.bgsoftware.superiorskyblock.commands.CmdMembers;
import com.bgsoftware.superiorskyblock.commands.CmdMission;
import com.bgsoftware.superiorskyblock.commands.CmdMissions;
import com.bgsoftware.superiorskyblock.commands.CmdName;
import com.bgsoftware.superiorskyblock.commands.CmdOpen;
import com.bgsoftware.superiorskyblock.commands.CmdPanel;
import com.bgsoftware.superiorskyblock.commands.CmdPardon;
import com.bgsoftware.superiorskyblock.commands.CmdPermissions;
import com.bgsoftware.superiorskyblock.commands.CmdPromote;
import com.bgsoftware.superiorskyblock.commands.CmdRankup;
import com.bgsoftware.superiorskyblock.commands.CmdRate;
import com.bgsoftware.superiorskyblock.commands.CmdRatings;
import com.bgsoftware.superiorskyblock.commands.CmdRecalc;
import com.bgsoftware.superiorskyblock.commands.CmdSetDiscord;
import com.bgsoftware.superiorskyblock.commands.CmdSetPaypal;
import com.bgsoftware.superiorskyblock.commands.CmdSetRole;
import com.bgsoftware.superiorskyblock.commands.CmdSetTeleport;
import com.bgsoftware.superiorskyblock.commands.CmdSetWarp;
import com.bgsoftware.superiorskyblock.commands.CmdSettings;
import com.bgsoftware.superiorskyblock.commands.CmdShow;
import com.bgsoftware.superiorskyblock.commands.CmdTeam;
import com.bgsoftware.superiorskyblock.commands.CmdTeamChat;
import com.bgsoftware.superiorskyblock.commands.CmdTeleport;
import com.bgsoftware.superiorskyblock.commands.CmdToggle;
import com.bgsoftware.superiorskyblock.commands.CmdTop;
import com.bgsoftware.superiorskyblock.commands.CmdTransfer;
import com.bgsoftware.superiorskyblock.commands.CmdUncoop;
import com.bgsoftware.superiorskyblock.commands.CmdUpgrade;
import com.bgsoftware.superiorskyblock.commands.CmdValue;
import com.bgsoftware.superiorskyblock.commands.CmdValues;
import com.bgsoftware.superiorskyblock.commands.CmdVisit;
import com.bgsoftware.superiorskyblock.commands.CmdVisitors;
import com.bgsoftware.superiorskyblock.commands.CmdWarp;
import com.bgsoftware.superiorskyblock.commands.CmdWarps;
import com.bgsoftware.superiorskyblock.commands.CmdWithdraw;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class CommandsHandler extends AbstractHandler implements CommandsManager {

    private final Registry<String, SuperiorCommand> subCommands = Registry.createLinkedRegistry();
    private final Registry<String, SuperiorCommand> aliasesToCommand = Registry.createRegistry();
    private final Registry<UUID, Registry<String, Long>> commandsCooldown = Registry.createRegistry();

    private CmdAdmin adminCommand = null;
    private String label = null;

    public CommandsHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
    }

    @Override
    public void loadData() {
        String islandCommand = plugin.getSettings().islandCommand;
        label = islandCommand.split(",")[0];

        PluginCommand pluginCommand = new PluginCommand(label);

        String[] commandSections = islandCommand.split(",");

        if(commandSections.length > 1){
            pluginCommand.setAliases(Arrays.asList(Arrays.copyOfRange(commandSections, 1, commandSections.length)));
        }

        plugin.getNMSAdapter().registerCommand(pluginCommand);

        registerCommand(new CmdAccept(), false);
        registerCommand((adminCommand = new CmdAdmin(this)), false);
        registerCommand(new CmdBalance(), false);
        registerCommand(new CmdBan(), false);
        registerCommand(new CmdBank(), false);
        registerCommand(new CmdBiome(), false);
        registerCommand(new CmdBorder(), false);
        registerCommand(new CmdChest(), false);
        registerCommand(new CmdClose(), false);
        registerCommand(new CmdCoop(), false);
        registerCommand(new CmdCoops(), false);
        registerCommand(new CmdCounts(), false);
        registerCommand(new CmdCreate(), false);
        registerCommand(new CmdDelWarp(), false);
        registerCommand(new CmdDemote(), false);
        registerCommand(new CmdDeposit(), false);
        registerCommand(new CmdDisband(), false);
        registerCommand(new CmdExpel(), false);
        registerCommand(new CmdFly(), false);
        registerCommand(new CmdHelp(), false);
        registerCommand(new CmdInvite(), false);
        registerCommand(new CmdKick(), false);
        registerCommand(new CmdLang(), false);
        registerCommand(new CmdLeave(), false);
        registerCommand(new CmdMembers(), false);
        registerCommand(new CmdMission(), false);
        registerCommand(new CmdMissions(), false);
        registerCommand(new CmdName(), false);
        registerCommand(new CmdOpen(), false);
        registerCommand(new CmdPanel(), false);
        registerCommand(new CmdPardon(), false);
        registerCommand(new CmdPermissions(), false);
        registerCommand(new CmdPromote(), false);
        registerCommand(new CmdRankup(), false);
        registerCommand(new CmdRate(), false);
        registerCommand(new CmdRatings(), false);
        registerCommand(new CmdRecalc(), false);
        registerCommand(new CmdSetDiscord(), false);
        registerCommand(new CmdSetPaypal(), false);
        registerCommand(new CmdSetRole(), false);
        registerCommand(new CmdSetTeleport(), false);
        registerCommand(new CmdSettings(), false);
        registerCommand(new CmdSetWarp(), false);
        registerCommand(new CmdShow(), false);
        registerCommand(new CmdTeam(), false);
        registerCommand(new CmdTeamChat(), false);
        registerCommand(new CmdTeleport(), false);
        registerCommand(new CmdToggle(), false);
        registerCommand(new CmdTop(), false);
        registerCommand(new CmdTransfer(), false);
        registerCommand(new CmdUncoop(), false);
        registerCommand(new CmdUpgrade(), false);
        registerCommand(new CmdValue(), false);
        registerCommand(new CmdValues(), false);
        registerCommand(new CmdVisit(), false);
        registerCommand(new CmdVisitors(), false);
        registerCommand(new CmdWarp(), false);
        registerCommand(new CmdWarps(), false);
        registerCommand(new CmdWithdraw(), false);

        loadCommands();
    }

    @Override
    public void registerCommand(SuperiorCommand superiorCommand) {
        Preconditions.checkNotNull(superiorCommand, "superiorCommand parameter cannot be null.");
        registerCommand(superiorCommand, true);
    }

    @Override
    public void registerAdminCommand(SuperiorCommand superiorCommand) {
        Preconditions.checkNotNull(superiorCommand, "superiorCommand parameter cannot be null.");
        adminCommand.registerCommand(superiorCommand, true);
    }

    @Override
    public List<SuperiorCommand> getSubCommands() {
        return Collections.unmodifiableList(new ArrayList<>(subCommands.values()));
    }

    @Override
    public List<SuperiorCommand> getAdminSubCommands() {
        return adminCommand.getSubCommands();
    }

    public String getLabel(){
        return label;
    }

    private SuperiorCommand getCommand(String label){
        label = label.toLowerCase();
        return subCommands.get(label, aliasesToCommand.get(label));
    }

    private void registerCommand(SuperiorCommand superiorCommand, boolean sort){
        List<String> aliases = new ArrayList<>(superiorCommand.getAliases());
        String label = aliases.get(0).toLowerCase();
        aliases.addAll(plugin.getSettings().commandAliases.getOrDefault(label, new ArrayList<>()));

        if(subCommands.containsKey(label)){
            subCommands.remove(label);
            aliasesToCommand.values().removeIf(sC -> sC.getAliases().get(0).equals(aliases.get(0)));
        }
        subCommands.add(label, superiorCommand);

        for(int i = 1; i < aliases.size(); i++){
            aliasesToCommand.add(aliases.get(i).toLowerCase(), superiorCommand);
        }

        if(sort){
            List<SuperiorCommand> superiorCommands = new ArrayList<>(subCommands.values());
            superiorCommands.sort(Comparator.comparing(o -> o.getAliases().get(0)));
            subCommands.clear();
            superiorCommands.forEach(s -> subCommands.add(s.getAliases().get(0), s));
        }
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

    private class PluginCommand extends BukkitCommand{

        PluginCommand(String islandCommandLabel){
            super(islandCommandLabel);
        }

        @Override
        public boolean execute(CommandSender sender, String label, String[] args) {
            java.util.Locale locale = LocaleUtils.getLocale(sender);

            if(args.length > 0){
                SuperiorCommand command = getCommand(args[0]);
                if(command != null){
                    if(!(sender instanceof Player) && !command.canBeExecutedByConsole()){
                        Locale.sendMessage(sender, "&cCan be executed only by players!", true);
                        return false;
                    }

                    if(!command.getPermission().isEmpty() && !sender.hasPermission(command.getPermission())) {
                        Locale.NO_COMMAND_PERMISSION.send(sender, locale);
                        return false;
                    }

                    if(args.length < command.getMinArgs() || args.length > command.getMaxArgs()){
                        Locale.COMMAND_USAGE.send(sender, locale, getLabel() + " " + command.getUsage(locale));
                        return false;
                    }

                    String commandLabel = command.getAliases().get(0);

                    if(sender instanceof Player && plugin.getSettings().commandsCooldown.containsKey(commandLabel)) {
                        UUID uuid = ((Player) sender).getUniqueId();

                        long timeToExecute = commandsCooldown.containsKey(uuid) && commandsCooldown.get(uuid).containsKey(commandLabel) ?
                                commandsCooldown.get(uuid).get(commandLabel) : -1;

                        long timeNow = System.currentTimeMillis();

                        if(timeNow < timeToExecute){
                            Locale.COMMAND_COOLDOWN_FORMAT.send(sender, locale, StringUtils.formatTime(locale, timeToExecute - timeNow));
                            return false;
                        }

                        if(!commandsCooldown.containsKey(uuid)){
                            commandsCooldown.add(uuid, Registry.createRegistry());
                        }

                        Registry<String, Long> timedCommands = commandsCooldown.get(uuid);
                        timedCommands.add(commandLabel, timeNow + plugin.getSettings().commandsCooldown.get(commandLabel).getKey());
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
                SuperiorCommand command = getCommand(args[0]);
                if(command != null){
                    return command.getPermission() != null && !sender.hasPermission(command.getPermission()) ?
                            new ArrayList<>() : command.tabComplete(plugin, sender, args);
                }
            }

            List<String> list = new ArrayList<>();

            for(SuperiorCommand subCommand : getSubCommands()) {
                if (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission())) {
                    List<String> aliases = new ArrayList<>(subCommand.getAliases());
                    aliases.addAll(plugin.getSettings().commandAliases.getOrDefault(aliases.get(0).toLowerCase(), new ArrayList<>()));
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
