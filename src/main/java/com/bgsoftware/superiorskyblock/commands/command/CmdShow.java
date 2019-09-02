package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.StringUtil;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class CmdShow implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("show", "info");
    }

    @Override
    public String getPermission() {
        return "superior.island.show";
    }

    @Override
    public String getUsage() {
        return "island show [player-name/island-name]";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_SHOW.getMessage();
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
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, args[1]);
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
        if(!Locale.ISLAND_INFO_LOCATION.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_LOCATION.getMessage(SBlockPosition.of(island.getCenter()))).append("\n");
        if(!Locale.ISLAND_INFO_RATE.isEmpty()) {
            StringBuilder rateMessage = new StringBuilder();
            double rating = island.getTotalRating();
            if(rating >= 1)
                rateMessage.append(Locale.ISLAND_INFO_RATE_ONE_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
            if(rating >= 2)
                rateMessage.append(Locale.ISLAND_INFO_RATE_TWO_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
            if(rating >= 3)
                rateMessage.append(Locale.ISLAND_INFO_RATE_THREE_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
            if(rating >= 4)
                rateMessage.append(Locale.ISLAND_INFO_RATE_FOUR_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
            if(rating >= 5)
                rateMessage.append(Locale.ISLAND_INFO_RATE_FIVE_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
            for(int i = 5; i > rating; i--)
                rateMessage.append(Locale.ISLAND_INFO_RATE_EMPTY_SYMBOL.getMessage());
            infoMessage.append(Locale.ISLAND_INFO_RATE.getMessage(rateMessage.toString(), StringUtil.format(rating), island.getRatingAmount())).append("\n");
        }
        if(!Locale.ISLAND_INFO_BANK.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_BANK.getMessage(island.getMoneyInBankAsBigDecimal())).append("\n");
        if(!Locale.ISLAND_INFO_WORTH.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_WORTH.getMessage(island.getWorthAsBigDecimal())).append("\n");
        if(!Locale.ISLAND_INFO_DISCORD.isEmpty() && island.hasPermission(sender, IslandPermission.DISCORD_SHOW))
            infoMessage.append(Locale.ISLAND_INFO_DISCORD.getMessage(island.getDiscord())).append("\n");
        if(!Locale.ISLAND_INFO_PAYPAL.isEmpty() && island.hasPermission(sender, IslandPermission.PAYPAL_SHOW))
            infoMessage.append(Locale.ISLAND_INFO_PAYPAL.getMessage(island.getPaypal())).append("\n");

        List<UUID> members = island.getMembers();
        StringBuilder adminsString = new StringBuilder(), modsString = new StringBuilder(),
                membersString = new StringBuilder();

        if(!Locale.ISLAND_INFO_PLAYER_LINE.isEmpty()) {
            for (UUID uuid : members) {
                SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(uuid);
                switch (superiorPlayer.getIslandRole()) {
                    case ADMIN:
                        adminsString.append(Locale.ISLAND_INFO_PLAYER_LINE.getMessage(SSuperiorPlayer.of(uuid).getName())).append("\n");
                        break;
                    case MODERATOR:
                        modsString.append(Locale.ISLAND_INFO_PLAYER_LINE.getMessage(SSuperiorPlayer.of(uuid).getName())).append("\n");
                        break;
                    case MEMBER:
                        membersString.append(Locale.ISLAND_INFO_PLAYER_LINE.getMessage(SSuperiorPlayer.of(uuid).getName())).append("\n");
                        break;
                }
            }
        }

        if(!Locale.ISLAND_INFO_ADMINS.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_ADMINS.getMessage(adminsString));
        if(!Locale.ISLAND_INFO_MODS.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_MODS.getMessage(modsString));
        if(!Locale.ISLAND_INFO_MEMBERS.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_MEMBERS.getMessage(membersString));

        if(!Locale.ISLAND_INFO_FOOTER.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_FOOTER.getMessage());

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
                if (onlinePlayer.getIsland() != null && (superiorPlayer == null || island == null ||
                        !island.getOwner().getUniqueId().equals(player.getUniqueId()))) {
                    if (player.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(player.getName());
                    if (onlinePlayer.getIsland() != null && onlinePlayer.getIsland().getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(onlinePlayer.getIsland().getName());
                }
            }
        }

        return list;
    }
}
