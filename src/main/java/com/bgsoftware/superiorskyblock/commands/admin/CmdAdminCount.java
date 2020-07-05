package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.menu.MenuCounts;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class CmdAdminCount implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("count");
    }

    @Override
    public String getPermission() {
        return "superior.admin.count";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin count <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> [" +
                Locale.COMMAND_ARGUMENT_MATERIAL.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_COUNT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
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

        if(args.length == 3){
            if(!(sender instanceof Player)){
                Locale.sendMessage(sender, "&cYou must be a player in order to open the counts menu.", true);
                return;
            }

            MenuCounts.openInventory(SSuperiorPlayer.of(sender), null, island);
            return;
        }

        String materialName = args[3].toUpperCase();

        if(materialName.equals("*")){
            StringBuilder materialsBuilder = new StringBuilder();

            java.util.Locale locale = LocaleUtils.getLocale(sender);

            if(!Locale.BLOCK_COUNTS_CHECK_MATERIAL.isEmpty(locale)){
                for(Map.Entry<com.bgsoftware.superiorskyblock.api.key.Key, Integer> entry : island.getBlockCounts().entrySet()){
                    materialsBuilder.append(", ").append(Locale.BLOCK_COUNTS_CHECK_MATERIAL.getMessage(locale, entry.getValue(), StringUtils.format(entry.getKey().toString())));
                }
            }

            if(materialsBuilder.length() == 0){
                Locale.BLOCK_COUNTS_CHECK_EMPTY.send(sender);
            }
            else {
                Locale.BLOCK_COUNTS_CHECK.send(sender, materialsBuilder.substring(1));
            }
        }

        else {
            try {
                Material.valueOf(materialName);
            } catch (Exception ex) {
                Locale.INVALID_MATERIAL.send(sender, args[3]);
                return;
            }

            int blockCount = island.getBlockCount(Key.of(materialName));

            if (blockCount > 1)
                materialName = materialName + "s";

            Locale.BLOCK_COUNT_CHECK.send(sender, blockCount, StringUtils.format(materialName));
        }
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
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }
        else if(args.length == 4){
            SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);
            Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

            if(island != null){
                String materialArgument = args[3].toLowerCase();
                for(Material material : Material.values()){
                    if(material.isBlock() && !material.name().startsWith("LEGACY_") && material.name().toLowerCase().startsWith(materialArgument))
                        list.add(material.name().toLowerCase());
                }
                if("*".startsWith(materialArgument))
                    list.add("*");
            }
        }

        return list;
    }
}
