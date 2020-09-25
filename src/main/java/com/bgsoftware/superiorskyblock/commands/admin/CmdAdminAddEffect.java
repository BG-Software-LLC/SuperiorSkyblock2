package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminAddEffect implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addeffect");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addeffect";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addeffect <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_EFFECT.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_LEVEL.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_ADD_EFFECT.getMessage(locale);
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

        PotionEffectType potionEffectType = PotionEffectType.getByName(args[3].toUpperCase());

        if(potionEffectType == null){
            Locale.INVALID_EFFECT.send(sender, args[3]);
            return;
        }

        int level;

        try{
            level = Integer.parseInt(args[4]);
        }catch(IllegalArgumentException ex){
            Locale.INVALID_LEVEL.send(sender, args[4]);
            return;
        }

        Executor.data(() -> islands.forEach(island -> island.setPotionEffect(potionEffectType, island.getPotionEffectLevel(potionEffectType) + level)));

        if(islands.size() > 1)
            Locale.CHANGED_ISLAND_EFFECT_LEVEL_ALL.send(sender, StringUtils.format(potionEffectType.getName()));
        else if(targetPlayer == null)
            Locale.CHANGED_ISLAND_EFFECT_LEVEL_NAME.send(sender, StringUtils.format(potionEffectType.getName()), islands.get(0).getName());
        else
            Locale.CHANGED_ISLAND_EFFECT_LEVEL.send(sender, StringUtils.format(potionEffectType.getName()), targetPlayer.getName());
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
            SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[2]);
            Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

            if(island != null){
                for(PotionEffectType potionEffectType : PotionEffectType.values()){
                    try {
                        if (potionEffectType != null && potionEffectType.getName().toLowerCase().contains(args[3].toLowerCase()))
                            list.add(potionEffectType.getName().toLowerCase());
                    }catch (Exception ignored){}
                }
            }
        }

        return list;
    }
}
