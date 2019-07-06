package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetWarpsLimit implements ICommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setwarpslimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setwarpslimit";
    }

    @Override
    public String getUsage() {
        return "island admin setwarpslimit <island-member-name> <amount>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_WARPS_LIMIT.getMessage();
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return 4;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[2]);
            return;
        }

        Island island = targetPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        int amount;
        try {
            amount = Integer.valueOf(args[3]);
        } catch (Exception e) {
            Locale.INVALID_AMOUNT.send(sender);
            return;
        }

        if (amount < 0) {
            Locale.INVALID_AMOUNT.send(sender);
            return;
        }

        island.setWarpsLimit(amount);

        if (!(sender instanceof Player) || !island.isMember(SSuperiorPlayer.of((Player) sender)))
            Locale.WARP_COUNT_SET_OTHER.send(sender, args[2], amount);
        island.sendMessage(Locale.WARP_COUNT_SET.getMessage(amount));
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
