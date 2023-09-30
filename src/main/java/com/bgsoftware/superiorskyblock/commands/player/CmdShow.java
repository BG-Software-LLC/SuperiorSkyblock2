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
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CmdShow implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("show", "info");
    }

    @Override
    public String getPermission() {
        return "superior.island.show";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_SHOW.getMessage(locale);
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
            Island locationIsland = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());
            return locationIsland == null || locationIsland.isSpawn() ? superiorPlayer.getIsland() : locationIsland;
        });

        if (island == null) {
            Message.INVALID_ISLAND.send(dispatcher);
            return;
        }

        java.util.Locale locale = PlayerLocales.getLocale(dispatcher);

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
        if (!Message.ISLAND_INFO_DISCORD.isEmpty(locale) && island.hasPermission(dispatcher, IslandPrivileges.DISCORD_SHOW))
            infoMessage.append(Message.ISLAND_INFO_DISCORD.getMessage(locale, island.getDiscord())).append("\n");
        if (!Message.ISLAND_INFO_PAYPAL.isEmpty(locale) && island.hasPermission(dispatcher, IslandPrivileges.PAYPAL_SHOW))
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

        Message.CUSTOM.send(dispatcher, infoMessage.toString(), false);
    }

}
