package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class CmdTeam implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("team", "showteam", "online");
    }

    @Override
    public String getPermission() {
        return "superior.island.team";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "team [" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_TEAM.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = null;
        Island island;

        if(args.length == 1){
            if(!(sender instanceof Player)){
                Locale.sendMessage(sender, "&cYou must specify a player's name.");
                return;
            }

            island = SSuperiorPlayer.of(sender).getIsland();
        }
        else{
            targetPlayer = SSuperiorPlayer.of(args[1]);
            island = targetPlayer == null ? plugin.getGrid().getIsland(args[1]) : targetPlayer.getIsland();
        }

        if(island == null){
            if(args.length == 1 || args[1].equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if(targetPlayer == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, StringUtils.stripColors(args[1]));
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        Executor.async(() -> {
            java.util.Locale locale = LocaleUtils.getLocale(sender);
            StringBuilder infoMessage = new StringBuilder();

            if(!Locale.ISLAND_TEAM_STATUS_HEADER.isEmpty(locale))
                infoMessage.append(Locale.ISLAND_TEAM_STATUS_HEADER.getMessage(locale, island.getOwner().getName(),
                        island.getIslandMembers(true).size(), island.getTeamLimit())).append("\n");

            List<SuperiorPlayer> members = island.getIslandMembers(true);
            members.sort(SortingComparators.ISLAND_MEMBERS_COMPARATOR);

            if(!Locale.ISLAND_TEAM_STATUS_ROLES.isEmpty(locale)){
                Registry<PlayerRole, StringBuilder> rolesStrings = Registry.createRegistry();
                plugin.getPlayers().getRoles().stream().filter(PlayerRole::isRoleLadder)
                        .forEach(playerRole -> rolesStrings.add(playerRole, new StringBuilder()));
                rolesStrings.add(SPlayerRole.lastRole(), new StringBuilder());

                String onlineStatus = Locale.ISLAND_TEAM_STATUS_ONLINE.getMessage(locale),
                        offlineStatus = Locale.ISLAND_TEAM_STATUS_OFFLINE.getMessage(locale);

                members.forEach(islandMember -> {
                    PlayerRole playerRole = islandMember.getPlayerRole();
                    long time = islandMember.getLastTimeStatus() == -1 ? -1 : (System.currentTimeMillis() / 1000) - islandMember.getLastTimeStatus();
                    rolesStrings.get(playerRole).append(Locale.ISLAND_TEAM_STATUS_ROLES.getMessage(locale, playerRole,
                            islandMember.getName(), islandMember.isOnline() ? onlineStatus : offlineStatus, getTime(time))).append("\n");
                });

                rolesStrings.keys().stream()
                        .sorted(Collections.reverseOrder(Comparator.comparingInt(PlayerRole::getWeight)))
                        .forEach(playerRole -> infoMessage.append(rolesStrings.get(playerRole)));

                rolesStrings.delete();
            }

            if(!Locale.ISLAND_TEAM_STATUS_FOOTER.isEmpty(locale))
                infoMessage.append(Locale.ISLAND_TEAM_STATUS_FOOTER.getMessage(locale));

            Locale.sendMessage(sender, infoMessage.toString());
        });
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = sender instanceof Player ? SSuperiorPlayer.of(sender) : null;
        Island island = superiorPlayer == null ? null : superiorPlayer.getIsland();
        List<String> list = new ArrayList<>();

        if(args.length == 2){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null && (superiorPlayer == null || island == null ||
                        !island.getOwner().getUniqueId().equals(player.getUniqueId()))) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }

        return list;
    }

    private String getTime(long timeLeft){
        String time = "";

        if(timeLeft == -1)
            return time;

        if(timeLeft >= 3600) {
            if (timeLeft / 3600 == 1)
                time += "1 hour, ";
            else time += (timeLeft / 3600) + " hours, ";
            timeLeft %= 3600;
        }

        if(timeLeft >= 60){
            if (timeLeft / 60 == 1)
                time += "1 minute, ";
            else time += (timeLeft / 60) + " minutes, ";
            timeLeft %= 60;
        }

        if(timeLeft != 0) {
            if (timeLeft == 1)
                time += timeLeft + " second";
            else time += timeLeft + " seconds";
            return time;
        }

        return time.substring(0, time.length() - 2);
    }

}
