package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.collections.ArrayMap;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CmdShow implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("show", "info");
    }

    @Override
    public String getPermission() {
        return "superior.island.show";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "show [" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_SHOW.getMessage(locale);
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
        Island island = args.length == 1 ? CommandArguments.getIslandWhereStandingOrSenderIsland(plugin, sender).getIsland() :
                CommandArguments.getIsland(plugin, sender, args[1]).getIsland();

        if (island == null)
            return;

        BukkitExecutor.ensureAsync(() -> doShowIslandInfo(plugin, sender, island));
    }

    private static void doShowIslandInfo(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island) {
        SuperiorPlayer superiorPlayer = sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null;
        Locale locale = superiorPlayer != null ? superiorPlayer.getUserLocale() : PlayerLocales.getDefaultLocale();

        Message.ISLAND_INFO_HEADER.sendPlayerOrConsole(superiorPlayer);
        Message.ISLAND_INFO_OWNER.sendPlayerOrConsole(superiorPlayer, island.getOwner().getName());

        if (!island.getName().isEmpty()) {
            Message.ISLAND_INFO_NAME.sendPlayerOrConsole(superiorPlayer, island.getName());
        }

        WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(island, plugin.getSettings().getWorlds().getDefaultWorldDimension());
        Message.ISLAND_INFO_LOCATION.sendPlayerOrConsole(superiorPlayer,
                Formatters.BLOCK_POSITION_FORMATTER.format(island.getCenterPosition(), worldInfo));

        Message.ISLAND_INFO_CREATION_TIME.sendPlayerOrConsole(superiorPlayer, island.getCreationTimeDate());

        double rating = island.getTotalRating();
        Message.ISLAND_INFO_RATE.sendPlayerOrConsole(superiorPlayer, Formatters.RATING_FORMATTER.format(rating, locale),
                Formatters.NUMBER_FORMATTER.format(rating), island.getRatingAmount());

        if (BuiltinModules.BANK.isEnabled()) {
            Message.ISLAND_INFO_BANK.sendPlayerOrConsole(superiorPlayer, island.getIslandBank().getBalance());
        }

        Message.ISLAND_INFO_WORTH.sendPlayerOrConsole(superiorPlayer, island.getWorth());
        Message.ISLAND_INFO_LEVEL.sendPlayerOrConsole(superiorPlayer, island.getIslandLevel());

        if (!Message.ISLAND_INFO_DISCORD.isEmpty(locale) && !IslandUtils.DEFAULT_NONE_VALUE.equals(island.getDiscord()) &&
                island.hasPermission(sender, IslandPrivileges.DISCORD_SHOW)) {
            Message.ISLAND_INFO_DISCORD.sendPlayerOrConsole(superiorPlayer, island.getDiscord());
        }

        if (!Message.ISLAND_INFO_PAYPAL.isEmpty(locale) && !IslandUtils.DEFAULT_NONE_VALUE.equals(island.getPaypal()) &&
                island.hasPermission(sender, IslandPrivileges.PAYPAL_SHOW)) {
            Message.ISLAND_INFO_PAYPAL.sendPlayerOrConsole(superiorPlayer, island.getPaypal());
        }

        if (!Message.ISLAND_INFO_VISITORS_COUNT.isEmpty(locale)) {
            Message.ISLAND_INFO_VISITORS_COUNT.sendPlayerOrConsole(superiorPlayer,
                    island.getIslandVisitors(false).size(),
                    island.getUniqueVisitorsWithTimes().size()
            );
        }

        if (!Message.ISLAND_INFO_ROLES.isEmpty(locale)) {
            Map<PlayerRole, StringBuilder> rolesStrings = new ArrayMap<>();
            plugin.getRoles().getRoles().stream().filter(playerRole -> playerRole.isRoleLadder() && !playerRole.isLastRole())
                    .forEach(playerRole -> rolesStrings.put(playerRole, new StringBuilder()));

            List<SuperiorPlayer> members = island.getIslandMembers(false);

            if (!Message.ISLAND_INFO_PLAYER_LINE.isEmpty(locale)) {
                members.forEach(islandMember -> {
                    StringBuilder rolesString = rolesStrings.get(islandMember.getPlayerRole());

                    if(rolesString == null) {
                        Log.warn("It seems like ", islandMember.getName(), " isn't part of the island of "
                                , island.getOwner().getName(), ".");
                        return;
                    }

                    Text.appendWithLine(rolesString, Message.ISLAND_INFO_PLAYER_LINE.getMessage(locale, islandMember.getName()));
                });
            }

            rolesStrings.keySet().stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(PlayerRole::getWeight)))
                    .forEach(playerRole -> {
                        StringBuilder players = rolesStrings.get(playerRole);
                        if (players != null && players.length() > 0)
                            Message.ISLAND_INFO_ROLES.sendPlayerOrConsole(superiorPlayer, playerRole, players);
                    });
        }

        Message.ISLAND_INFO_FOOTER.sendPlayerOrConsole(superiorPlayer);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getPlayerIslandsExceptSender(plugin, sender, args[1],
                plugin.getSettings().isTabCompleteHideVanished()) : Collections.emptyList();
    }

}
