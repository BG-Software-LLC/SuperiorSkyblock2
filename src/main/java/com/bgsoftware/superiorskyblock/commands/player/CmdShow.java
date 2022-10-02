package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
        Island island = args.length == 1 ? CommandArguments.getIslandWhereStanding(plugin, sender).getIsland() :
                CommandArguments.getIsland(plugin, sender, args[1]).getIsland();

        if (island == null)
            return;

        java.util.Locale locale = PlayerLocales.getLocale(sender);

        StringBuilder infoMessage = new StringBuilder();

        if (!Message.ISLAND_INFO_HEADER.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_HEADER.getMessage(locale)).append("\n");
        if (!Message.ISLAND_INFO_OWNER.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_OWNER.getMessage(locale, island.getOwner().getName())).append("\n");
        if (!Message.ISLAND_INFO_NAME.isEmpty(locale) && !island.getName().isEmpty())
            infoMessage.append(Message.ISLAND_INFO_NAME.getMessage(locale, island.getName())).append("\n");
        if (!Message.ISLAND_INFO_LOCATION.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_LOCATION.getMessage(locale, Formatters.LOCATION_FORMATTER.format(
                    island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld())))).append("\n");
        if (!Message.ISLAND_INFO_CREATION_TIME.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_CREATION_TIME.getMessage(locale, island.getCreationTimeDate())).append("\n");
        if (!Message.ISLAND_INFO_RATE.isEmpty(locale)) {
            double rating = island.getTotalRating();
            infoMessage.append(Message.ISLAND_INFO_RATE.getMessage(locale, Formatters.RATING_FORMATTER.format(rating, locale),
                    Formatters.NUMBER_FORMATTER.format(rating), island.getRatingAmount())).append("\n");
        }
        if (BuiltinModules.BANK.isEnabled()) {
            if (!Message.ISLAND_INFO_BANK.isEmpty(locale))
                infoMessage.append(Message.ISLAND_INFO_BANK.getMessage(locale, island.getIslandBank().getBalance())).append("\n");
        }
        if (!Message.ISLAND_INFO_WORTH.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_WORTH.getMessage(locale, island.getWorth())).append("\n");
        if (!Message.ISLAND_INFO_LEVEL.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_LEVEL.getMessage(locale, island.getIslandLevel())).append("\n");
        if (!Message.ISLAND_INFO_DISCORD.isEmpty(locale) && island.hasPermission(sender, IslandPrivileges.DISCORD_SHOW))
            infoMessage.append(Message.ISLAND_INFO_DISCORD.getMessage(locale, island.getDiscord())).append("\n");
        if (!Message.ISLAND_INFO_PAYPAL.isEmpty(locale) && island.hasPermission(sender, IslandPrivileges.PAYPAL_SHOW))
            infoMessage.append(Message.ISLAND_INFO_PAYPAL.getMessage(locale, island.getPaypal())).append("\n");
        if (!Message.ISLAND_INFO_VISITORS_COUNT.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_VISITORS_COUNT.getMessage(locale, island.getUniqueVisitorsWithTimes().size())).append("\n");

        if (!Message.ISLAND_INFO_ROLES.isEmpty(locale)) {
            Map<PlayerRole, StringBuilder> rolesStrings = new HashMap<>();
            plugin.getRoles().getRoles().stream().filter(playerRole -> playerRole.isRoleLadder() && !playerRole.isLastRole())
                    .forEach(playerRole -> rolesStrings.put(playerRole, new StringBuilder()));

            List<SuperiorPlayer> members = island.getIslandMembers(false);

            if (!Message.ISLAND_INFO_PLAYER_LINE.isEmpty(locale)) {
                members.forEach(superiorPlayer -> {
                    try {
                        rolesStrings.get(superiorPlayer.getPlayerRole())
                                .append(Message.ISLAND_INFO_PLAYER_LINE.getMessage(locale, superiorPlayer.getName())).append("\n");
                    } catch (NullPointerException ex) {
                        Log.warn("It seems like ", superiorPlayer.getName(), " isn't part of the island of "
                                , island.getOwner().getName(), ".");
                    }
                });
            }

            rolesStrings.keySet().stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(PlayerRole::getWeight)))
                    .forEach(playerRole ->
                            infoMessage.append(Message.ISLAND_INFO_ROLES.getMessage(locale, playerRole, rolesStrings.get(playerRole))));
        }

        if (!Message.ISLAND_INFO_FOOTER.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_FOOTER.getMessage(locale));

        Message.CUSTOM.send(sender, infoMessage.toString(), false);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getPlayerIslandsExceptSender(plugin, sender, args[1],
                plugin.getSettings().isTabCompleteHideVanished()) : Collections.emptyList();
    }

}
