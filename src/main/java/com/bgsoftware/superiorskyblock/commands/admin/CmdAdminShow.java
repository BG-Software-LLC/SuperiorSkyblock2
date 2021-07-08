package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.LocaleUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class CmdAdminShow implements IAdminIslandCommand {

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
    public boolean supportMultipleIslands() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, Island island, String[] args) {
        java.util.Locale locale = LocaleUtils.getLocale(sender);
        long lastTime = island.getLastTimeUpdate();

        StringBuilder infoMessage = new StringBuilder();

        if(!Locale.ISLAND_INFO_HEADER.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_HEADER.getMessage(locale)).append("\n");

        // Island owner
        if(!Locale.ISLAND_INFO_OWNER.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_OWNER.getMessage(locale, island.getOwner().getName())).append("\n");

        // Island name
        if(!Locale.ISLAND_INFO_NAME.isEmpty(locale) && !island.getName().isEmpty())
            infoMessage.append(Locale.ISLAND_INFO_NAME.getMessage(locale, island.getName())).append("\n");

        // Island location
        if(!Locale.ISLAND_INFO_LOCATION.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_LOCATION.getMessage(locale, SBlockPosition.of(island.getCenter(World.Environment.NORMAL)))).append("\n");

        // Island last time updated
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

        // Island rate
        if(!Locale.ISLAND_INFO_RATE.isEmpty(locale)) {
            double rating = island.getTotalRating();
            infoMessage.append(Locale.ISLAND_INFO_RATE.getMessage(locale, StringUtils.formatRating(locale, rating), StringUtils.format(rating), island.getRatingAmount())).append("\n");
        }

        // Island balance
        if(!Locale.ISLAND_INFO_BANK.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_BANK.getMessage(locale, island.getIslandBank().getBalance())).append("\n");

        // Island bonus worth
        if(!Locale.ISLAND_INFO_BONUS.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_BONUS.getMessage(locale, island.getBonusWorth())).append("\n");

        // Island bonus level
        if(!Locale.ISLAND_INFO_BONUS_LEVEL.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_BONUS_LEVEL.getMessage(locale, island.getBonusLevel())).append("\n");

        // Island worth
        if(!Locale.ISLAND_INFO_WORTH.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_WORTH.getMessage(locale, island.getWorth())).append("\n");

        // Island discord
        if(!Locale.ISLAND_INFO_DISCORD.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_DISCORD.getMessage(locale, island.getDiscord())).append("\n");

        // Island paypal
        if(!Locale.ISLAND_INFO_PAYPAL.isEmpty(locale))
            infoMessage.append(Locale.ISLAND_INFO_PAYPAL.getMessage(locale, island.getPaypal())).append("\n");

        // Island upgrades
        if(!Locale.ISLAND_INFO_ADMIN_UPGRADES.isEmpty(locale) && !Locale.ISLAND_INFO_ADMIN_UPGRADE_LINE.isEmpty(locale)){
            StringBuilder upgradesString = new StringBuilder();
            for(Upgrade upgrade : plugin.getUpgrades().getUpgrades()){
                upgradesString.append(Locale.ISLAND_INFO_ADMIN_UPGRADE_LINE.getMessage(locale, upgrade.getName(), island.getUpgradeLevel(upgrade).getLevel())).append("\n");
            }
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_UPGRADES.getMessage(locale, upgradesString));
        }

        // Island admin size
        if(!Locale.ISLAND_INFO_ADMIN_SIZE.isEmpty(locale)) {
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_SIZE.getMessage(locale, island.getIslandSize()));
            if(island.getIslandSizeRaw() != island.getIslandSize())
                infoMessage.append(" ").append(Locale.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        // Island team limit
        if(!Locale.ISLAND_INFO_ADMIN_TEAM_LIMIT.isEmpty(locale)) {
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_TEAM_LIMIT.getMessage(locale, island.getTeamLimit()));
            if(island.getTeamLimitRaw() != island.getTeamLimit())
                infoMessage.append(" ").append(Locale.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        // Island warps limit
        if(!Locale.ISLAND_INFO_ADMIN_WARPS_LIMIT.isEmpty(locale)) {
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_WARPS_LIMIT.getMessage(locale, island.getWarpsLimit()));
            if(island.getWarpsLimitRaw() != island.getWarpsLimit())
                infoMessage.append(" ").append(Locale.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        // Island coop limit
        if(!Locale.ISLAND_INFO_ADMIN_COOP_LIMIT.isEmpty(locale)) {
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_COOP_LIMIT.getMessage(locale, island.getCoopLimit()));
            if(island.getCoopLimitRaw() != island.getCoopLimit())
                infoMessage.append(" ").append(Locale.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        // Island bank limit
        if(!Locale.ISLAND_INFO_ADMIN_BANK_LIMIT.isEmpty(locale)) {
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_BANK_LIMIT.getMessage(locale, StringUtils.format(island.getBankLimit())));
            if(!island.getBankLimitRaw().equals(island.getBankLimit()))
                infoMessage.append(" ").append(Locale.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        // Island spawners multiplier
        if(!Locale.ISLAND_INFO_ADMIN_SPAWNERS_MULTIPLIER.isEmpty(locale)) {
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_SPAWNERS_MULTIPLIER.getMessage(locale, island.getSpawnerRatesMultiplier()));
            if(island.getSpawnerRatesRaw() != island.getSpawnerRatesMultiplier())
                infoMessage.append(" ").append(Locale.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        // Island drops multiplier
        if(!Locale.ISLAND_INFO_ADMIN_DROPS_MULTIPLIER.isEmpty(locale)) {
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_DROPS_MULTIPLIER.getMessage(locale, island.getMobDropsMultiplier()));
            if(island.getMobDropsRaw() != island.getMobDropsMultiplier())
                infoMessage.append(" ").append(Locale.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        // Island crops multiplier
        if(!Locale.ISLAND_INFO_ADMIN_CROPS_MULTIPLIER.isEmpty(locale)) {
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_CROPS_MULTIPLIER.getMessage(locale, island.getCropGrowthMultiplier()));
            if(island.getCropGrowthRaw() != island.getCropGrowthMultiplier())
                infoMessage.append(" ").append(Locale.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        // Island entity limits
        if(!Locale.ISLAND_INFO_ADMIN_ENTITIES_LIMITS.isEmpty(locale) && !Locale.ISLAND_INFO_ADMIN_ENTITIES_LIMITS_LINE.isEmpty(locale)){
            StringBuilder entitiesString = new StringBuilder();
            for(Map.Entry<com.bgsoftware.superiorskyblock.api.key.Key, Integer> entry : island.getEntitiesLimitsAsKeys().entrySet()){
                entitiesString.append(Locale.ISLAND_INFO_ADMIN_ENTITIES_LIMITS_LINE.getMessage(locale, StringUtils.format(entry.getKey().toString()), entry.getValue()));
                if(!island.getCustomEntitiesLimits().containsKey(entry.getKey()))
                    entitiesString.append(" ").append(Locale.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
                entitiesString.append("\n");
            }
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_ENTITIES_LIMITS.getMessage(locale, entitiesString));
        }

        // Island block limits
        if(!Locale.ISLAND_INFO_ADMIN_BLOCKS_LIMITS.isEmpty(locale) && !Locale.ISLAND_INFO_ADMIN_BLOCKS_LIMITS_LINE.isEmpty(locale)){
            StringBuilder blocksString = new StringBuilder();
            for(Map.Entry<com.bgsoftware.superiorskyblock.api.key.Key, Integer> entry : island.getBlocksLimits().entrySet()){
                blocksString.append(Locale.ISLAND_INFO_ADMIN_BLOCKS_LIMITS_LINE.getMessage(locale, StringUtils.format(entry.getKey().toString()), entry.getValue()));
                if(!island.getCustomBlocksLimits().containsKey(entry.getKey()))
                    blocksString.append(" ").append(Locale.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
                blocksString.append("\n");
            }
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_BLOCKS_LIMITS.getMessage(locale, blocksString));
        }

        // Island generator rates
        if(!Locale.ISLAND_INFO_ADMIN_GENERATOR_RATES.isEmpty(locale) && !Locale.ISLAND_INFO_ADMIN_GENERATOR_RATES_LINE.isEmpty(locale)){
            for(World.Environment environment : World.Environment.values()) {
                StringBuilder generatorString = new StringBuilder();
                for (Map.Entry<String, Integer> entry : island.getGeneratorPercentages(environment).entrySet()) {
                    Key key = Key.of(entry.getKey());
                    generatorString.append(Locale.ISLAND_INFO_ADMIN_GENERATOR_RATES_LINE.getMessage(locale,
                            StringUtils.format(entry.getKey()),
                            StringUtils.format(IslandUtils.getGeneratorPercentageDecimal(island, key, environment)),
                            island.getGeneratorAmount(key, environment))
                    );
                    if (!island.getCustomGeneratorAmounts(environment).containsKey(key))
                        generatorString.append(" ").append(Locale.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
                    generatorString.append("\n");
                }
                infoMessage.append(Locale.ISLAND_INFO_ADMIN_GENERATOR_RATES.getMessage(locale, generatorString, StringUtils.format(environment.name())));
            }
        }

        // Island effects
        if(!Locale.ISLAND_INFO_ADMIN_ISLAND_EFFECTS.isEmpty(locale) && !Locale.ISLAND_INFO_ADMIN_ISLAND_EFFECTS_LINE.isEmpty(locale)){
            StringBuilder blocksString = new StringBuilder();
            for(Map.Entry<PotionEffectType, Integer> entry : island.getPotionEffects().entrySet()){
                blocksString.append(Locale.ISLAND_INFO_ADMIN_ISLAND_EFFECTS_LINE.getMessage(locale, StringUtils.format(entry.getKey().getName()), entry.getValue())).append("\n");
            }
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_ISLAND_EFFECTS.getMessage(locale, blocksString));
        }

        // Island entity limits
        if(!Locale.ISLAND_INFO_ADMIN_ROLE_LIMITS.isEmpty(locale) && !Locale.ISLAND_INFO_ADMIN_ROLE_LIMITS_LINE.isEmpty(locale)){
            StringBuilder entitiesString = new StringBuilder();
            for(Map.Entry<PlayerRole, Integer> entry : island.getRoleLimits().entrySet()){
                entitiesString.append(Locale.ISLAND_INFO_ADMIN_ROLE_LIMITS_LINE.getMessage(locale, entry.getKey(), entry.getValue()));
                if(!island.getCustomRoleLimits().containsKey(entry.getKey()))
                    entitiesString.append(" ").append(Locale.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
                entitiesString.append("\n");
            }
            infoMessage.append(Locale.ISLAND_INFO_ADMIN_ROLE_LIMITS.getMessage(locale, entitiesString));
        }

        // Island members
        if(!Locale.ISLAND_INFO_ROLES.isEmpty(locale)) {
            Registry<PlayerRole, StringBuilder> rolesStrings = Registry.createRegistry();

            List<SuperiorPlayer> members = island.getIslandMembers(false);

            if (!Locale.ISLAND_INFO_PLAYER_LINE.isEmpty(locale)) {
                members.forEach(superiorPlayer -> rolesStrings.computeIfAbsent(superiorPlayer.getPlayerRole(), role -> new StringBuilder())
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

        Locale.sendMessage(sender, infoMessage.toString(), false);
    }

}
