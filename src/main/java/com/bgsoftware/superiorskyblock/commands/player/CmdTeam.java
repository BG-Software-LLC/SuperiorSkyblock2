package com.bgsoftware.superiorskyblock.commands.player;

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
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        BukkitExecutor.async(() -> {
            java.util.Locale locale = PlayerLocales.getLocale(sender);
            StringBuilder infoMessage = new StringBuilder();

            if (!Message.ISLAND_TEAM_STATUS_HEADER.isEmpty(locale)) {
                infoMessage.append(Message.ISLAND_TEAM_STATUS_HEADER.getMessage(locale, island.getOwner().getName(),
                        island.getIslandMembers(true).size(), island.getTeamLimit())).append("\n");
            }

            List<SuperiorPlayer> members = island.getIslandMembers(true);

            if (!Message.ISLAND_TEAM_STATUS_ROLES.isEmpty(locale)) {
                Map<PlayerRole, StringBuilder> rolesStrings = new HashMap<>();
                plugin.getRoles().getRoles().stream().filter(PlayerRole::isRoleLadder)
                        .forEach(playerRole -> rolesStrings.put(playerRole, new StringBuilder()));
                rolesStrings.put(SPlayerRole.lastRole(), new StringBuilder());

                String onlineStatus = Message.ISLAND_TEAM_STATUS_ONLINE.getMessage(locale),
                        offlineStatus = Message.ISLAND_TEAM_STATUS_OFFLINE.getMessage(locale);

                members.forEach(islandMember -> {
                    PlayerRole playerRole = islandMember.getPlayerRole();
                    long time = islandMember.getLastTimeStatus() == -1 ? -1 : ((System.currentTimeMillis() / 1000) - islandMember.getLastTimeStatus());
                    boolean onlinePlayer = islandMember.isOnline() && islandMember.isShownAsOnline();
                    rolesStrings.get(playerRole).append(Message.ISLAND_TEAM_STATUS_ROLES.getMessage(locale, playerRole,
                            islandMember.getName(), onlinePlayer ? onlineStatus : offlineStatus,
                            Formatters.TIME_FORMATTER.format(Duration.ofSeconds(time), locale))).append("\n");
                });

                rolesStrings.keySet().stream()
                        .sorted(Collections.reverseOrder(Comparator.comparingInt(PlayerRole::getWeight)))
                        .forEach(playerRole -> infoMessage.append(rolesStrings.get(playerRole)));
            }

            if (!Message.ISLAND_TEAM_STATUS_FOOTER.isEmpty(locale))
                infoMessage.append(Message.ISLAND_TEAM_STATUS_FOOTER.getMessage(locale));

            Message.CUSTOM.send(sender, infoMessage.toString(), false);
        });
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getPlayerIslandsExceptSender(plugin, sender, args[1],
                plugin.getSettings().isTabCompleteHideVanished()) : Collections.emptyList();
    }

}
