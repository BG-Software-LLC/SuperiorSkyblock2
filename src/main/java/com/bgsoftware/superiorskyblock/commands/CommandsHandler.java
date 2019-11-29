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
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CommandsHandler extends BukkitCommand {

    private static CommandsHandler instance;

    private final SuperiorSkyblockPlugin plugin;

    private final List<ICommand> subCommands = new ArrayList<>();
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

        subCommands.add(new CmdAccept());
        subCommands.add(new CmdAdmin());
        subCommands.add(new CmdBan());
        subCommands.add(new CmdBiome());
        subCommands.add(new CmdBorder());
        subCommands.add(new CmdClose());
        subCommands.add(new CmdCoop());
        subCommands.add(new CmdCreate());
        subCommands.add(new CmdDelWarp());
        subCommands.add(new CmdDemote());
        subCommands.add(new CmdDeposit());
        subCommands.add(new CmdDisband());
        subCommands.add(new CmdExpel());
        subCommands.add(new CmdFly());
        subCommands.add(new CmdHelp());
        subCommands.add(new CmdInvite());
        subCommands.add(new CmdKick());
        subCommands.add(new CmdLeave());
        subCommands.add(new CmdMission());
        subCommands.add(new CmdMissions());
        subCommands.add(new CmdName());
        subCommands.add(new CmdOpen());
        subCommands.add(new CmdPanel());
        subCommands.add(new CmdPardon());
        subCommands.add(new CmdPermissions());
        subCommands.add(new CmdPromote());
        subCommands.add(new CmdRankup());
        subCommands.add(new CmdRate());
        subCommands.add(new CmdRatings());
        subCommands.add(new CmdRecalc());
        subCommands.add(new CmdSetDiscord());
        subCommands.add(new CmdSetPaypal());
        subCommands.add(new CmdSetRole());
        subCommands.add(new CmdSetTeleport());
        subCommands.add(new CmdSettings());
        subCommands.add(new CmdSetWarp());
        subCommands.add(new CmdShow());
        subCommands.add(new CmdTeam());
        subCommands.add(new CmdTeamChat());
        subCommands.add(new CmdTeleport());
        subCommands.add(new CmdToggle());
        subCommands.add(new CmdTop());
        subCommands.add(new CmdTransfer());
        subCommands.add(new CmdUncoop());
        subCommands.add(new CmdUpgrade());
        subCommands.add(new CmdValue());
        subCommands.add(new CmdVisit());
        subCommands.add(new CmdWarp());
        subCommands.add(new CmdWarps());
        subCommands.add(new CmdWithdraw());
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if(args.length > 0){
            for(ICommand subCommand : subCommands){
                if(subCommand.getAliases().contains(args[0].toLowerCase())){
                    if(!(sender instanceof Player) && !subCommand.canBeExecutedByConsole()){
                        Locale.sendMessage(sender, "&cCan be executed only by players!");
                        return false;
                    }

                    if(!subCommand.getPermission().isEmpty() && !sender.hasPermission(subCommand.getPermission())) {
                        Locale.NO_COMMAND_PERMISSION.send(sender);
                        return false;
                    }

                    if(args.length < subCommand.getMinArgs() || args.length > subCommand.getMaxArgs()){
                        Locale.COMMAND_USAGE.send(sender, getLabel() + " " + subCommand.getUsage());
                        return false;
                    }

                    String commandLabel = subCommand.getAliases().get(0);

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

                    subCommand.execute(plugin, sender, args);
                    return false;
                }
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
            for(ICommand subCommand : subCommands) {
                if (subCommand.getAliases().contains(args[0].toLowerCase())){
                    if(subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())){
                        return new ArrayList<>();
                    }
                    return subCommand.tabComplete(plugin, sender, args);
                }
            }
        }

        List<String> list = new ArrayList<>();

        for(ICommand subCommand : subCommands) {
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

    public static List<ICommand> getSubCommands(){
        return instance.subCommands;
    }

    public static String getCommandLabel(){
        return instance.getLabel();
    }


}
