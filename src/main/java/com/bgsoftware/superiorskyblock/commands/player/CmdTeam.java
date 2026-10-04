package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.top.SortingComparators;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CmdTeam implements ISuperiorCommand {

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
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_TEAM.getMessage(locale);
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
                CommandArguments.getIsland(plugin, sender, args[1])).getIsland();

        if (island == null)
            return;

        SuperiorPlayer superiorPlayer = sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null;
        Locale locale = superiorPlayer != null ? superiorPlayer.getUserLocale() : PlayerLocales.getDefaultLocale();

        List<SuperiorPlayer> members = island.getIslandMembers(true);

        BukkitExecutor.ensureAsync(() -> doShowTeamStatus(superiorPlayer, locale, island, members));
    }

    private static void doShowTeamStatus(@Nullable SuperiorPlayer superiorPlayer, Locale locale, Island island, List<SuperiorPlayer> members) {
        Message.ISLAND_TEAM_STATUS_HEADER.sendPlayerOrConsole(superiorPlayer,
                island.getOwner().getName(),
                members.size(),
                island.getTeamLimit()
        );

        if (!Message.ISLAND_TEAM_STATUS_ROLES.isEmpty(locale)) {
            String onlineStatus = Message.ISLAND_TEAM_STATUS_ONLINE.getMessage(locale);
            String offlineStatus = Message.ISLAND_TEAM_STATUS_OFFLINE.getMessage(locale);

            members.stream()
                    .sorted(Collections.reverseOrder(SortingComparators.ISLAND_ROLES_COMPARATOR))
                    .forEach(islandMember -> {
                        PlayerRole playerRole = islandMember.getPlayerRole();
                        long time = islandMember.getLastTimeStatus() == -1 ? -1 : ((System.currentTimeMillis() / 1000) - islandMember.getLastTimeStatus());
                        boolean onlinePlayer = islandMember.isOnline() && islandMember.isShownAsOnline();
                        Message.ISLAND_TEAM_STATUS_ROLES.sendPlayerOrConsole(superiorPlayer,
                                playerRole,
                                islandMember.getName(),
                                onlinePlayer ? onlineStatus : offlineStatus,
                                Formatters.TIME_FORMATTER.format(Duration.ofSeconds(time), locale)
                        );
                    });
        }

        Message.ISLAND_TEAM_STATUS_FOOTER.sendPlayerOrConsole(superiorPlayer);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getPlayerIslandsExceptSender(plugin, sender, args[1],
                plugin.getSettings().isTabCompleteHideVanished()) : Collections.emptyList();
    }

}
