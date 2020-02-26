package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CmdShow implements ISuperiorCommand {

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
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_SHOW.getMessage(locale);
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

            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);

            Island locationIsland = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());

            island = locationIsland == null || locationIsland.isSpawn() ? superiorPlayer.getIsland() : locationIsland;
        }
        else{
            targetPlayer = SSuperiorPlayer.of(args[1]);
            island = targetPlayer == null ? plugin.getGrid().getIsland(args[1]) : targetPlayer.getIsland();
        }

        if(island == null){
            if(args.length == 1 || args[1].equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if(targetPlayer == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, args[1]);
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        java.util.Locale locale = LocaleUtils.getLocale(sender);

        StringBuilder infoMessage = new StringBuilder();

        if(!Locale.ISLAND_INFO_HEADER.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_HEADER.getMessage(locale)).append("\n");
        if(!Locale.ISLAND_INFO_OWNER.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_OWNER.getMessage(locale, island.getOwner().getName())).append("\n");
        if(!Locale.ISLAND_INFO_NAME.isEmpty(locale) && !island.getName().isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_NAME.getMessage(locale, island.getName())).append("\n");
        if(!Locale.ISLAND_INFO_LOCATION.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_LOCATION.getMessage(locale, SBlockPosition.of(island.getCenter(World.Environment.NORMAL)))).append("\n");
        if(!Locale.ISLAND_INFO_RATE.isEmpty(locale)) {
            double rating = island.getTotalRating();
            infoMessage.append(Locale.ISLAND_INFO_RATE.getMessage(locale, StringUtils.formatRating(locale, rating), StringUtils.format(rating), island.getRatingAmount())).append("\n");
        }
        if(!Locale.ISLAND_INFO_BANK.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_BANK.getMessage(locale, island.getMoneyInBank())).append("\n");
        if(!Locale.ISLAND_INFO_WORTH.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_WORTH.getMessage(locale, island.getWorth())).append("\n");
        if(!Locale.ISLAND_INFO_DISCORD.isEmpty(locale) && island.hasPermission(sender, IslandPermission.DISCORD_SHOW))
            infoMessage.append(Locale.ISLAND_INFO_DISCORD.getMessage(locale, island.getDiscord())).append("\n");
        if(!Locale.ISLAND_INFO_PAYPAL.isEmpty(locale) && island.hasPermission(sender, IslandPermission.PAYPAL_SHOW))
            infoMessage.append(Locale.ISLAND_INFO_PAYPAL.getMessage(locale, island.getPaypal())).append("\n");
        if(!Locale.ISLAND_INFO_VISITORS_COUNT.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_VISITORS_COUNT.getMessage(locale, island.getUniqueVisitors().size())).append("\n");

        if(!Locale.ISLAND_INFO_ROLES.isEmpty(locale)) {
            Map<PlayerRole, StringBuilder> rolesStrings = new HashMap<>();
            plugin.getPlayers().getRoles().stream().filter(playerRole -> playerRole.isRoleLadder() && !playerRole.isLastRole())
                    .forEach(playerRole -> rolesStrings.put(playerRole, new StringBuilder()));

            List<SuperiorPlayer> members = island.getIslandMembers(false);

            if (!Locale.ISLAND_INFO_PLAYER_LINE.isEmpty(locale)) {
                members.forEach(superiorPlayer -> {
                    try{
                        rolesStrings.get(superiorPlayer.getPlayerRole())
                                .append(Locale.ISLAND_INFO_PLAYER_LINE.getMessage(locale, superiorPlayer.getName())).append("\n");
                    }catch(NullPointerException ex){
                        SuperiorSkyblockPlugin.log("[Warn] It seems like " + superiorPlayer.getName() + " isn't part of the island of " + island.getOwner().getName() + ".");
                    }
                });
            }

            rolesStrings.keySet().stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(PlayerRole::getWeight)))
                    .forEach(playerRole ->
                    infoMessage.append(Locale.ISLAND_INFO_ROLES.getMessage(locale, playerRole, rolesStrings.get(playerRole))));
        }

        if(!Locale.ISLAND_INFO_FOOTER.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_FOOTER.getMessage(locale));

        Locale.sendMessage(sender, infoMessage.toString());
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
}
