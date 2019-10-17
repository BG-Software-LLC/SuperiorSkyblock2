package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CmdAdminShow implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("show", "info");
    }

    @Override
    public String getPermission() {
        return "superior.admin.show";
    }

    @Override
    public String getUsage() {
        return "island admin show <player-name/island-name>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SHOW.getMessage();
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);
        Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

        if (island == null) {
            if (args[2].equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if (targetPlayer == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, args[2]);
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        StringBuilder infoMessage = new StringBuilder();

        if(!Locale.ISLAND_INFO_HEADER.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_HEADER.getMessage()).append("\n");
        if(!Locale.ISLAND_INFO_OWNER.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_OWNER.getMessage(island.getOwner().getName())).append("\n");
        if(!Locale.ISLAND_INFO_NAME.isEmpty() && !island.getName().isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_NAME.getMessage(island.getName())).append("\n");
        if(!Locale.ISLAND_INFO_ADMIN_SIZE.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_SIZE.getMessage(island.getIslandSize())).append("\n");
        if(!Locale.ISLAND_INFO_LOCATION.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_LOCATION.getMessage(SBlockPosition.of(island.getCenter()))).append("\n");
        if(!Locale.ISLAND_INFO_RATE.isEmpty()) {
            double rating = island.getTotalRating();
            infoMessage.append(Locale.ISLAND_INFO_RATE.getMessage(StringUtils.formatRating(rating), StringUtils.format(rating), island.getRatingAmount())).append("\n");
        }
        if(!Locale.ISLAND_INFO_BANK.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_BANK.getMessage(island.getMoneyInBankAsBigDecimal())).append("\n");
        if(!Locale.ISLAND_INFO_WORTH.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_WORTH.getMessage(island.getWorthAsBigDecimal())).append("\n");
        if(!Locale.ISLAND_INFO_DISCORD.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_DISCORD.getMessage(island.getDiscord())).append("\n");
        if(!Locale.ISLAND_INFO_PAYPAL.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_PAYPAL.getMessage(island.getPaypal())).append("\n");

        if(!Locale.ISLAND_INFO_ADMIN_UPGRADES.isEmpty() && !Locale.ISLAND_INFO_ADMIN_UPGRADE_LINE.isEmpty()){
            StringBuilder upgradesString = new StringBuilder();
            for(String upgrade : plugin.getUpgrades().getAllUpgrades()){
                upgradesString.append(Locale.ISLAND_INFO_ADMIN_UPGRADE_LINE.getMessage(upgrade, island.getUpgradeLevel(upgrade))).append("\n");
            }
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_UPGRADES.getMessage(upgradesString));
        }

        if(!Locale.ISLAND_INFO_ROLES.isEmpty()) {
            Map<PlayerRole, StringBuilder> rolesStrings = new HashMap<>();
            plugin.getPlayers().getRoles().stream().filter(playerRole -> playerRole.isRoleLadder() && !playerRole.isLastRole())
                    .forEach(playerRole -> rolesStrings.put(playerRole, new StringBuilder()));

            List<SuperiorPlayer> members = island.getIslandMembers(false);

            if (!Locale.ISLAND_INFO_PLAYER_LINE.isEmpty()) {
                members.forEach(superiorPlayer -> rolesStrings.get(superiorPlayer.getPlayerRole())
                        .append(Locale.ISLAND_INFO_PLAYER_LINE.getMessage(superiorPlayer.getName())).append("\n"));
            }

            rolesStrings.keySet().stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(PlayerRole::getWeight)))
                    .forEach(playerRole ->
                    infoMessage.append(Locale.ISLAND_INFO_ROLES.getMessage(playerRole, rolesStrings.get(playerRole))));
        }

        if(!Locale.ISLAND_INFO_FOOTER.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_FOOTER.getMessage());

        Locale.sendMessage(sender, infoMessage.toString());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }

        return list;
    }
}
