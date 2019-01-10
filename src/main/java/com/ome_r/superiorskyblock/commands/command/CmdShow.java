package com.ome_r.superiorskyblock.commands.command;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.commands.ICommand;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.island.IslandPermission;
import com.ome_r.superiorskyblock.wrappers.WrappedLocation;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CmdShow implements ICommand {

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
        return "island show [player-name]";
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
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer targetPlayer;

        if(args.length == 1){
            if(!(sender instanceof Player)){
                Locale.sendMessage(sender, "&cYou must specify a player's name.");
                return;
            }

            targetPlayer = WrappedPlayer.of(sender);
        }
        else{
            targetPlayer = WrappedPlayer.of(args[1]);

            if(targetPlayer == null){
                Locale.INVALID_PLAYER.send(sender, args[1]);
                return;
            }
        }

        Island island = targetPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(sender);
            return;
        }

        StringBuilder infoMessage = new StringBuilder();

        if(!Locale.ISLAND_INFO_HEADER.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_HEADER.getMessage()).append("\n");
        if(!Locale.ISLAND_INFO_OWNER.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_OWNER.getMessage(island.getOwner().getName())).append("\n");
        if(!Locale.ISLAND_INFO_LOCATION.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_LOCATION.getMessage(WrappedLocation.of(island.getCenter()))).append("\n");
        if(!Locale.ISLAND_INFO_BANK.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_BANK.getMessage(island.getMoneyInBank())).append("\n");
        if(!Locale.ISLAND_INFO_WORTH.isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_WORTH.getMessage(island.getWorth())).append("\n");
        if(!Locale.ISLAND_INFO_DISCORD.isEmpty() && island.hasPermission(sender, IslandPermission.DISCORD_SHOW))
            infoMessage.append(Locale.ISLAND_INFO_DISCORD.getMessage(island.getDiscord())).append("\n");
        if(!Locale.ISLAND_INFO_PAYPAL.isEmpty() && island.hasPermission(sender, IslandPermission.PAYPAL_SHOW))
            infoMessage.append(Locale.ISLAND_INFO_PAYPAL.getMessage(island.getPaypal())).append("\n");

        List<UUID> members = island.getMembers();
        StringBuilder adminsString = new StringBuilder(), modsString = new StringBuilder(),
                membersString = new StringBuilder();

        if(!Locale.ISLAND_INFO_PLAYER_LINE.isEmpty()) {
            for (UUID uuid : members) {
                WrappedPlayer wrappedPlayer = WrappedPlayer.of(uuid);
                switch (wrappedPlayer.getIslandRole()) {
                    case ADMIN:
                        adminsString.append(Locale.ISLAND_INFO_PLAYER_LINE.getMessage(WrappedPlayer.of(uuid).getName())).append("\n");
                        break;
                    case MODERATOR:
                        adminsString.append(Locale.ISLAND_INFO_PLAYER_LINE.getMessage(WrappedPlayer.of(uuid).getName())).append("\n");
                        break;
                    case MEMBER:
                        adminsString.append(Locale.ISLAND_INFO_PLAYER_LINE.getMessage(WrappedPlayer.of(uuid).getName())).append("\n");
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

        Locale.sendMessage(sender, infoMessage.toString());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
