package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdTransfer implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("transfer", "leader", "leadership");
    }

    @Override
    public String getPermission() {
        return "superior.island.transfer";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "transfer <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_TRANSFER.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }


    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Pair<Island, SuperiorPlayer> arguments = CommandArguments.getSenderIsland(plugin, sender);

        Island island = arguments.getKey();

        if(island == null)
            return;

        SuperiorPlayer superiorPlayer = arguments.getValue();

        if (!superiorPlayer.getPlayerRole().isLastRole()) {
            Locale.NO_TRANSFER_PERMISSION.send(superiorPlayer);
            return;
        }

        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, superiorPlayer, args[1]);

        if(targetPlayer == null)
            return;

        if (!island.isMember(targetPlayer)) {
            Locale.PLAYER_NOT_INSIDE_ISLAND.send(sender);
            return;
        }

        if (island.getOwner().getUniqueId().equals(targetPlayer.getUniqueId())) {
            Locale.TRANSFER_ALREADY_LEADER.send(superiorPlayer);
            return;
        }

        if(island.transferIsland(targetPlayer))
            IslandUtils.sendMessage(island, Locale.TRANSFER_BROADCAST, new ArrayList<>(), targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island island = superiorPlayer.getIsland();
        return args.length == 2 && island != null && superiorPlayer.getPlayerRole().isLastRole() ?
                CommandTabCompletes.getIslandMembers(island, args[1]) : new ArrayList<>();
    }

}
