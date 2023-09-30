package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CmdTeam implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("team", "showteam", "online");
    }

    @Override
    public String getPermission() {
        return "superior.island.team";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_TEAM.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("island", IslandArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        Island island = context.getOptionalArgument("island", Island.class).orElseGet(() -> {
            if (!(dispatcher instanceof Player)) {
                Message.CUSTOM.send(dispatcher, "&cYou must specify a player's name.", true);
                return null;
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(dispatcher);
            return superiorPlayer.getIsland();
        });

        if (island == null) {
            Message.INVALID_ISLAND.send(dispatcher);
            return;
        }

        BukkitExecutor.async(() -> {
            java.util.Locale locale = PlayerLocales.getLocale(dispatcher);
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

            Message.CUSTOM.send(dispatcher, infoMessage.toString(), false);
        });
    }

}
