package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.command.*;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CommandsHandler extends BukkitCommand {

    private static CommandsHandler instance;

    private final SuperiorSkyblockPlugin plugin;

    private final Map<String, ICommand> subCommands = new LinkedHashMap<>();
    private final Map<String, ICommand> aliasesToCommand = new HashMap<>();

    private final Map<UUID, Map<String, Long>> commandsCooldown = new HashMap<>();

    public CommandsHandler(SuperiorSkyblockPlugin plugin, String islandCommand){
        super(islandCommand.split(",")[0]);
        String[] commandSections = islandCommand.split(",");

        if(commandSections.length > 1){
            for(int i = 1; i < commandSections.length; i++){
                super.getAliases().add(commandSections[i]);
            }
        }

        instance = this;
        this.plugin = plugin;

        register(new CmdAccept());
        register(new CmdAdmin());
        register(new CmdBan());
        register(new CmdBiome());
        register(new CmdBorder());
        register(new CmdClose());
        register(new CmdCoop());
        register(new CmdCreate());
        register(new CmdDelWarp());
        register(new CmdDemote());
        register(new CmdDeposit());
        register(new CmdDisband());
        register(new CmdExpel());
        register(new CmdFly());
        register(new CmdHelp());
        register(new CmdInvite());
        register(new CmdKick());
        register(new CmdLeave());
        register(new CmdMission());
        register(new CmdMissions());
        register(new CmdName());
        register(new CmdOpen());
        register(new CmdPanel());
        register(new CmdPardon());
        register(new CmdPermissions());
        register(new CmdPromote());
        register(new CmdRankup());
        register(new CmdRate());
        register(new CmdRatings());
        register(new CmdRecalc());
        register(new CmdSetDiscord());
        register(new CmdSetPaypal());
        register(new CmdSetRole());
        register(new CmdSetTeleport());
        register(new CmdSettings());
        register(new CmdSetWarp());
        register(new CmdShow());
        register(new CmdTeam());
        register(new CmdTeamChat());
        register(new CmdTeleport());
        register(new CmdToggle());
        register(new CmdTop());
        register(new CmdTransfer());
        register(new CmdUncoop());
        register(new CmdUpgrade());
        register(new CmdValue());
        register(new CmdVisit());
        register(new CmdWarp());
        register(new CmdWarps());
        register(new CmdWithdraw());
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(args.length > 0){
            ICommand command = getCommand(args[0]);
            if(command != null){
                if(!(sender instanceof Player) && !command.canBeExecutedByConsole()){
                    Locale.sendMessage(sender, "&cCan be executed only by players!");
                    return false;
                }

                if(!command.getPermission().isEmpty() && !sender.hasPermission(command.getPermission())) {
                    Locale.NO_COMMAND_PERMISSION.send(sender);
                    return false;
                }

                if(args.length < command.getMinArgs() || args.length > command.getMaxArgs()){
                    Locale.COMMAND_USAGE.send(sender, getLabel() + " " + command.getUsage());
                    return false;
                }

                String commandLabel = command.getAliases().get(0);

                if(sender instanceof Player && plugin.getSettings().commandsCooldown.containsKey(commandLabel)) {
                    UUID uuid = ((Player) sender).getUniqueId();

                    long timeToExecute = commandsCooldown.containsKey(uuid) && commandsCooldown.get(uuid).containsKey(commandLabel) ?
                            commandsCooldown.get(uuid).get(commandLabel) : -1;

                    long timeNow = System.currentTimeMillis();

                    if(timeNow < timeToExecute){
                        Locale.COMMAND_COOLDOWN_FORMAT.send(sender, StringUtils.formatTime(timeToExecute - timeNow));
                        return false;
                    }

                    if(!commandsCooldown.containsKey(uuid)){
                        commandsCooldown.put(uuid, new HashMap<>());
                    }

                    Map<String, Long> timedCommands = commandsCooldown.get(uuid);
                    timedCommands.put(commandLabel, timeNow + plugin.getSettings().commandsCooldown.get(commandLabel).getKey());
                }

                command.execute(plugin, sender, args);
                return false;
            }
        }

        if(sender instanceof Player){
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);

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

        Locale.NO_COMMAND_PERMISSION.send(sender);

        return false;
    }


    @Override
    public List<String> tabComplete(CommandSender sender, String label, String[] args) {
        if(args.length > 0){
            ICommand command = getCommand(args[0]);
            if(command != null){
                return command.getPermission() != null && !sender.hasPermission(command.getPermission()) ?
                        new ArrayList<>() : command.tabComplete(plugin, sender, args);
            }
        }

        List<String> list = new ArrayList<>();

        for(ICommand subCommand : getSubCommands()) {
            if (subCommand.getPermission() == null || sender.hasPermission(subCommand.getPermission())) {
                for (String aliases : subCommand.getAliases()) {
                    if (aliases.startsWith(args[0].toLowerCase())) {
                        list.add(aliases);
                        break;
                    }
                }
            }
        }

        return list;
    }

    private void register(ICommand cmd){
        List<String> aliases = cmd.getAliases();
        subCommands.put(aliases.get(0).toLowerCase(), cmd);
        for(int i = 1; i < aliases.size(); i++){
            aliasesToCommand.put(aliases.get(i).toLowerCase(), cmd);
        }
    }

    private ICommand getCommand(String label){
        label = label.toLowerCase();
        return subCommands.getOrDefault(label, aliasesToCommand.get(label));
    }

    public static List<ICommand> getSubCommands(){
        return new ArrayList<>(instance.subCommands.values());
    }

    public static String getCommandLabel(){
        return instance.getLabel();
    }


}
