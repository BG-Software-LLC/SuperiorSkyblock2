package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.utils.BigDecimalFormatted;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminBonus implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("bonus");
    }

    @Override
    public String getPermission() {
        return "superior.admin.bonus";
    }

    @Override
    public String getUsage() {
        return "island admin bonus <player-name> <amount>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_BONUS.getMessage();
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

        BigDecimal bonusWorth;

        try{
            bonusWorth = BigDecimalFormatted.of(args[3]);
        }catch(NumberFormatException ex){
            Locale.INVALID_AMOUNT.send(sender);
            return;
        }

        island.setBonusWorth(bonusWorth);

        Locale.BONUS_SET_SUCCESS.send(sender, bonusWorth.toString());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                if(!player.equals(sender) && player.getName().toLowerCase().startsWith(args[2].toLowerCase()) &&
                        SSuperiorPlayer.of(player).getIsland() != null){
                    list.add(player.getName());
                }
            }
        }

        return list;
    }
}
