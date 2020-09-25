package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.BigDecimalFormatted;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CmdAdminAddBonus implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addbonus");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addbonus";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addbonus <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <worth/level> <" +
                Locale.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_ADD_BONUS.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[2]);
        List<Island> islands = new ArrayList<>();

        if(args[2].equalsIgnoreCase("*")) {
            islands.addAll(plugin.getGrid().getIslands());
        }

        else{
            Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

            if (island == null) {
                if (args[2].equalsIgnoreCase(sender.getName()))
                    Locale.INVALID_ISLAND.send(sender);
                else if (targetPlayer == null)
                    Locale.INVALID_ISLAND_OTHER_NAME.send(sender, StringUtils.stripColors(args[2]));
                else
                    Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
                return;
            }

            islands.add(island);
        }

        boolean isWorthBonus = !args[3].equalsIgnoreCase("level");

        BigDecimal bonus;

        try{
            bonus = BigDecimalFormatted.of(args[4]);
        }catch(NumberFormatException ex){
            Locale.INVALID_AMOUNT.send(sender);
            return;
        }

        Executor.data(() -> islands.forEach(island -> {
            if(isWorthBonus)
                island.setBonusWorth(island.getBonusWorth().add(bonus));
            else
                island.setBonusLevel(island.getBonusLevel().add(bonus));
        }));

        Locale.BONUS_SET_SUCCESS.send(sender, bonus.toString());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null) {
                    if (player.getName().toLowerCase().contains(args[2].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().contains(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }
        else if(args.length == 4){
            list.addAll(Stream.of("worth", "level").filter(arg -> arg.contains(args[3].toLowerCase())).collect(Collectors.toSet()));
        }

        return list;
    }
}
