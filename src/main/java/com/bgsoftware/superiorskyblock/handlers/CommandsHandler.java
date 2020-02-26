package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.handlers.CommandsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CmdAccept;
import com.bgsoftware.superiorskyblock.commands.CmdAdmin;
import com.bgsoftware.superiorskyblock.commands.CmdBan;
import com.bgsoftware.superiorskyblock.commands.CmdBiome;
import com.bgsoftware.superiorskyblock.commands.CmdBorder;
import com.bgsoftware.superiorskyblock.commands.CmdClose;
import com.bgsoftware.superiorskyblock.commands.CmdCoop;
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
import com.bgsoftware.superiorskyblock.commands.CmdVisit;
import com.bgsoftware.superiorskyblock.commands.CmdVisitors;
import com.bgsoftware.superiorskyblock.commands.CmdWarp;
import com.bgsoftware.superiorskyblock.commands.CmdWarps;
import com.bgsoftware.superiorskyblock.commands.CmdWithdraw;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CommandsHandler extends BukkitCommand implements CommandsManager {

    private final SuperiorSkyblockPlugin plugin;

    private final Map<String, SuperiorCommand> subCommands = new LinkedHashMap<>();
    private final Map<String, SuperiorCommand> aliasesToCommand = new HashMap<>();
    private final CmdAdmin adminCommand;

    private final Map<UUID, Map<String, Long>> commandsCooldown = new HashMap<>();

    public CommandsHandler(SuperiorSkyblockPlugin plugin, String islandCommand){
        super(islandCommand.split(",")[0]);

        String[] commandSections = islandCommand.split(",");

        if(commandSections.length > 1){
            super.setAliases(Arrays.asList(Arrays.copyOfRange(commandSections, 1, commandSections.length)));
        }

        this.plugin = plugin;

        registerCommand(new CmdAccept());
        registerCommand((adminCommand = new CmdAdmin(this)));
        registerCommand(new CmdBan());
        registerCommand(new CmdBiome());
        registerCommand(new CmdBorder());
        registerCommand(new CmdClose());
        registerCommand(new CmdCoop());
        registerCommand(new CmdCreate());
        registerCommand(new CmdDelWarp());
        registerCommand(new CmdDemote());
        registerCommand(new CmdDeposit());
        registerCommand(new CmdDisband());
        registerCommand(new CmdExpel());
        registerCommand(new CmdFly());
        registerCommand(new CmdHelp());
        registerCommand(new CmdInvite());
        registerCommand(new CmdKick());
        registerCommand(new CmdLang());
        registerCommand(new CmdLeave());
        registerCommand(new CmdMembers());
        registerCommand(new CmdMission());
        registerCommand(new CmdMissions());
        registerCommand(new CmdName());
        registerCommand(new CmdOpen());
        registerCommand(new CmdPanel());
        registerCommand(new CmdPardon());
        registerCommand(new CmdPermissions());
        registerCommand(new CmdPromote());
        registerCommand(new CmdRankup());
        registerCommand(new CmdRate());
        registerCommand(new CmdRatings());
        registerCommand(new CmdRecalc());
        registerCommand(new CmdSetDiscord());
        registerCommand(new CmdSetPaypal());
        registerCommand(new CmdSetRole());
        registerCommand(new CmdSetTeleport());
        registerCommand(new CmdSettings());
        registerCommand(new CmdSetWarp());
        registerCommand(new CmdShow());
        registerCommand(new CmdTeam());
        registerCommand(new CmdTeamChat());
        registerCommand(new CmdTeleport());
        registerCommand(new CmdToggle());
        registerCommand(new CmdTop());
        registerCommand(new CmdTransfer());
        registerCommand(new CmdUncoop());
        registerCommand(new CmdUpgrade());
        registerCommand(new CmdValue());
        registerCommand(new CmdVisit());
        registerCommand(new CmdVisitors());
        registerCommand(new CmdWarp());
        registerCommand(new CmdWarps());
        registerCommand(new CmdWithdraw());
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        java.util.Locale locale = LocaleUtils.getLocale(sender);

        if(args.length > 0){
            SuperiorCommand command = getCommand(args[0]);
            if(command != null){
                if(!(sender instanceof Player) && !command.canBeExecutedByConsole()){
                    Locale.sendMessage(sender, "&cCan be executed only by players!");
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

    @Override
    public void registerCommand(SuperiorCommand superiorCommand) {
        List<String> aliases = superiorCommand.getAliases();
        subCommands.put(aliases.get(0).toLowerCase(), superiorCommand);
        for(int i = 1; i < aliases.size(); i++){
            aliasesToCommand.put(aliases.get(i).toLowerCase(), superiorCommand);
        }
    }

    @Override
    public void registerAdminCommand(SuperiorCommand superiorCommand) {
        adminCommand.registerCommand(superiorCommand);
    }

    @Override
    public List<SuperiorCommand> getSubCommands() {
        return Collections.unmodifiableList(new ArrayList<>(subCommands.values()));
    }

    @Override
    public List<SuperiorCommand> getAdminSubCommands() {
        return adminCommand.getSubCommands();
    }

    private SuperiorCommand getCommand(String label){
        label = label.toLowerCase();
        return subCommands.getOrDefault(label, aliasesToCommand.get(label));
    }


}
