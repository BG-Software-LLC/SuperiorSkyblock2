package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.islands.SortingComparators;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.Locale;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
        Island island = (args.length == 1 ? CommandArguments.getSenderIsland(plugin, sender) :
                CommandArguments.getIsland(plugin, sender, args[1])).getKey();

        if(island == null)
            return;

        Executor.async(() -> {
            java.util.Locale locale = LocaleUtils.getLocale(sender);
            StringBuilder infoMessage = new StringBuilder();

            if(!Locale.ISLAND_TEAM_STATUS_HEADER.isEmpty(locale)) {
                infoMessage.append(Locale.ISLAND_TEAM_STATUS_HEADER.getMessage(locale, island.getOwner().getName(),
                        island.getIslandMembers(true).size(), island.getTeamLimit())).append("\n");
            }

            List<SuperiorPlayer> members = island.getIslandMembers(true);
            members.sort(SortingComparators.ISLAND_MEMBERS_COMPARATOR);

            if(!Locale.ISLAND_TEAM_STATUS_ROLES.isEmpty(locale)){
                Map<PlayerRole, StringBuilder> rolesStrings = new HashMap<>();
                plugin.getRoles().getRoles().stream().filter(PlayerRole::isRoleLadder)
                        .forEach(playerRole -> rolesStrings.put(playerRole, new StringBuilder()));
                rolesStrings.put(SPlayerRole.lastRole(), new StringBuilder());

                String onlineStatus = Locale.ISLAND_TEAM_STATUS_ONLINE.getMessage(locale),
                        offlineStatus = Locale.ISLAND_TEAM_STATUS_OFFLINE.getMessage(locale);

                members.forEach(islandMember -> {
                    PlayerRole playerRole = islandMember.getPlayerRole();
                    long time = islandMember.getLastTimeStatus() == -1 ? -1 : ((System.currentTimeMillis() / 1000) - islandMember.getLastTimeStatus());
                    boolean onlinePlayer = islandMember.isOnline() && islandMember.isShownAsOnline();
                    rolesStrings.get(playerRole).append(Locale.ISLAND_TEAM_STATUS_ROLES.getMessage(locale, playerRole,
                            islandMember.getName(), onlinePlayer ? onlineStatus : offlineStatus,
                            StringUtils.formatTime(locale, time, TimeUnit.SECONDS))).append("\n");
                });

                rolesStrings.keySet().stream()
                        .sorted(Collections.reverseOrder(Comparator.comparingInt(PlayerRole::getWeight)))
                        .forEach(playerRole -> infoMessage.append(rolesStrings.get(playerRole)));
            }

            if(!Locale.ISLAND_TEAM_STATUS_FOOTER.isEmpty(locale))
                infoMessage.append(Locale.ISLAND_TEAM_STATUS_FOOTER.getMessage(locale));

            Locale.sendMessage(sender, infoMessage.toString(), false);
        });
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getPlayerIslandsExceptSender(plugin, sender, args[1],
                plugin.getSettings().isTabCompleteHideVanished()) : new ArrayList<>();
    }

}
