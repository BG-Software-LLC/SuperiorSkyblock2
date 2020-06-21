package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class CmdAdminShow implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("show", "info");
    }

    @Override
    public String getPermission() {
        return "superior.admin.show";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin show <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SHOW.getMessage(locale);
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
        java.util.Locale locale = LocaleUtils.getLocale(sender);

        if (island == null) {
            if (args[2].equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if (targetPlayer == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, StringUtils.stripColors(args[2]));
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        long lastTime = island.getLastTimeUpdate();

        StringBuilder infoMessage = new StringBuilder();

        if(!Locale.ISLAND_INFO_HEADER.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_HEADER.getMessage(locale)).append("\n");
        if(!Locale.ISLAND_INFO_OWNER.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_OWNER.getMessage(locale, island.getOwner().getName())).append("\n");
        if(!Locale.ISLAND_INFO_NAME.isEmpty(locale) && !island.getName().isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_NAME.getMessage(locale, island.getName())).append("\n");
        if(!Locale.ISLAND_INFO_LOCATION.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_LOCATION.getMessage(locale, SBlockPosition.of(island.getCenter(World.Environment.NORMAL)))).append("\n");
        if(lastTime != -1){
            if(!Locale.ISLAND_INFO_LAST_TIME_UPDATED.isEmpty(locale)){
                infoMessage.append(Locale.ISLAND_INFO_LAST_TIME_UPDATED.getMessage(locale, StringUtils.formatTime(locale, System.currentTimeMillis() - (lastTime * 1000)))).append("\n");
            }
        }
        else{
            if(!Locale.ISLAND_INFO_LAST_TIME_UPDATED_CURRENTLY_ACTIVE.isEmpty(locale)){
                infoMessage.append(Locale.ISLAND_INFO_LAST_TIME_UPDATED_CURRENTLY_ACTIVE.getMessage(locale)).append("\n");
            }
        }
        if(!Locale.ISLAND_INFO_RATE.isEmpty(locale)) {
            double rating = island.getTotalRating();
            infoMessage.append(Locale.ISLAND_INFO_RATE.getMessage(locale, StringUtils.formatRating(locale, rating), StringUtils.format(rating), island.getRatingAmount())).append("\n");
        }
        if(!Locale.ISLAND_INFO_BANK.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_BANK.getMessage(locale, island.getMoneyInBank())).append("\n");
        if(!Locale.ISLAND_INFO_BONUS.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_BONUS.getMessage(locale, island.getBonusWorth())).append("\n");
        if(!Locale.ISLAND_INFO_BONUS_LEVEL.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_BONUS_LEVEL.getMessage(locale, island.getBonusLevel())).append("\n");
        if(!Locale.ISLAND_INFO_WORTH.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_WORTH.getMessage(locale, island.getWorth())).append("\n");
        if(!Locale.ISLAND_INFO_DISCORD.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_DISCORD.getMessage(locale, island.getDiscord())).append("\n");
        if(!Locale.ISLAND_INFO_PAYPAL.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_PAYPAL.getMessage(locale, island.getPaypal())).append("\n");

        if(!Locale.ISLAND_INFO_ADMIN_UPGRADES.isEmpty(locale) && !Locale.ISLAND_INFO_ADMIN_UPGRADE_LINE.isEmpty(locale)){
            StringBuilder upgradesString = new StringBuilder();
            for(Upgrade upgrade : plugin.getUpgrades().getUpgrades()){
                upgradesString.append(Locale.ISLAND_INFO_ADMIN_UPGRADE_LINE.getMessage(locale, upgrade.getName(), island.getUpgradeLevel(upgrade).getLevel())).append("\n");
            }
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_UPGRADES.getMessage(locale, upgradesString));
        }

        if(!Locale.ISLAND_INFO_ADMIN_SIZE.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_SIZE.getMessage(locale, island.getIslandSize())).append("\n");
        if(!Locale.ISLAND_INFO_ADMIN_TEAM_LIMIT.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_TEAM_LIMIT.getMessage(locale, island.getTeamLimit())).append("\n");
        if(!Locale.ISLAND_INFO_ADMIN_COOP_LIMIT.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_COOP_LIMIT.getMessage(locale, island.getCoopLimit())).append("\n");
        if(!Locale.ISLAND_INFO_ADMIN_SPAWNERS_MULTIPLIER.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_SPAWNERS_MULTIPLIER.getMessage(locale, island.getSpawnerRatesMultiplier())).append("\n");
        if(!Locale.ISLAND_INFO_ADMIN_DROPS_MULTIPLIER.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_DROPS_MULTIPLIER.getMessage(locale, island.getMobDropsMultiplier())).append("\n");
        if(!Locale.ISLAND_INFO_ADMIN_CROPS_MULTIPLIER.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_CROPS_MULTIPLIER.getMessage(locale, island.getCropGrowthMultiplier())).append("\n");

        if(!Locale.ISLAND_INFO_ADMIN_ENTITIES_LIMITS.isEmpty(locale) && !Locale.ISLAND_INFO_ADMIN_ENTITIES_LIMITS_LINE.isEmpty(locale)){
            StringBuilder entitiesString = new StringBuilder();
            for(Map.Entry<EntityType, Integer> entry : island.getEntitiesLimits().entrySet()){
                entitiesString.append(Locale.ISLAND_INFO_ADMIN_ENTITIES_LIMITS_LINE.getMessage(locale, StringUtils.format(entry.getKey().name()), entry.getValue())).append("\n");
            }
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_ENTITIES_LIMITS.getMessage(locale, entitiesString));
        }

        if(!Locale.ISLAND_INFO_ADMIN_BLOCKS_LIMITS.isEmpty(locale) && !Locale.ISLAND_INFO_ADMIN_BLOCKS_LIMITS_LINE.isEmpty(locale)){
            StringBuilder blocksString = new StringBuilder();
            for(Map.Entry<Key, Integer> entry : island.getBlocksLimits().entrySet()){
                blocksString.append(Locale.ISLAND_INFO_ADMIN_BLOCKS_LIMITS_LINE.getMessage(locale, StringUtils.format(entry.getKey().toString()), entry.getValue())).append("\n");
            }
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_BLOCKS_LIMITS.getMessage(locale, blocksString));
        }

        if(!Locale.ISLAND_INFO_ADMIN_GENERATOR_RATES.isEmpty(locale) && !Locale.ISLAND_INFO_ADMIN_GENERATOR_RATES_LINE.isEmpty(locale)){
            StringBuilder generatorString = new StringBuilder();
            for(Map.Entry<String, Integer> entry : island.getGeneratorPercentages().entrySet()){
                generatorString.append(Locale.ISLAND_INFO_ADMIN_GENERATOR_RATES_LINE.getMessage(locale, StringUtils.format(entry.getKey()),
                        StringUtils.format(((SIsland) island).getGeneratorPercentageDecimal(Key.of(entry.getKey()))), island.getGeneratorAmount(Key.of(entry.getKey())))).append("\n");
            }
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_GENERATOR_RATES.getMessage(locale, generatorString));
        }

        if(!Locale.ISLAND_INFO_ROLES.isEmpty(locale)) {
            Registry<PlayerRole, StringBuilder> rolesStrings = Registry.createRegistry();
            plugin.getPlayers().getRoles().stream().filter(playerRole -> playerRole.isRoleLadder() && !playerRole.isLastRole())
                    .forEach(playerRole -> rolesStrings.add(playerRole, new StringBuilder()));

            List<SuperiorPlayer> members = island.getIslandMembers(false);

            if (!Locale.ISLAND_INFO_PLAYER_LINE.isEmpty(locale)) {
                members.forEach(superiorPlayer -> rolesStrings.get(superiorPlayer.getPlayerRole())
                        .append(Locale.ISLAND_INFO_PLAYER_LINE.getMessage(locale, superiorPlayer.getName())).append("\n"));
            }

            rolesStrings.keys().stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(PlayerRole::getWeight)))
                    .forEach(playerRole ->
                    infoMessage.append(Locale.ISLAND_INFO_ROLES.getMessage(locale, playerRole, rolesStrings.get(playerRole))));

            rolesStrings.delete();
        }

        if(!Locale.ISLAND_INFO_FOOTER.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_FOOTER.getMessage(locale));

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
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }

        return list;
    }
}
