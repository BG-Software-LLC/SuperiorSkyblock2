package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeBlockLimits;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeCropGrowth;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeEntityLimits;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeIslandEffects;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeMobDrops;
import com.bgsoftware.superiorskyblock.module.upgrades.type.UpgradeTypeSpawnerRates;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CmdAdminShow implements IAdminIslandCommand {

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
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SHOW.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, Island island, String[] args) {
        java.util.Locale locale = PlayerLocales.getLocale(sender);
        long lastTime = island.getLastTimeUpdate();

        StringBuilder infoMessage = new StringBuilder();

        if (!Message.ISLAND_INFO_HEADER.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_HEADER.getMessage(locale)).append("\n");

        // Island owner
        if (!Message.ISLAND_INFO_OWNER.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_OWNER.getMessage(locale, island.getOwner().getName())).append("\n");

        // Island name
        if (!Message.ISLAND_INFO_NAME.isEmpty(locale) && !island.getName().isEmpty())
            infoMessage.append(Message.ISLAND_INFO_NAME.getMessage(locale, island.getName())).append("\n");

        // Island location
        if (!Message.ISLAND_INFO_LOCATION.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_LOCATION.getMessage(locale, Formatters.LOCATION_FORMATTER.format(
                    island.getCenter(World.Environment.NORMAL)))).append("\n");

        // Island last time updated
        if (lastTime != -1) {
            if (!Message.ISLAND_INFO_LAST_TIME_UPDATED.isEmpty(locale)) {

                infoMessage.append(Message.ISLAND_INFO_LAST_TIME_UPDATED.getMessage(locale, Formatters.TIME_FORMATTER.format(
                        Duration.ofMillis(System.currentTimeMillis() - (lastTime * 1000)), locale))).append("\n");
            }
        } else {
            if (!Message.ISLAND_INFO_LAST_TIME_UPDATED_CURRENTLY_ACTIVE.isEmpty(locale)) {
                infoMessage.append(Message.ISLAND_INFO_LAST_TIME_UPDATED_CURRENTLY_ACTIVE.getMessage(locale)).append("\n");
            }
        }

        // Island rate
        if (!Message.ISLAND_INFO_RATE.isEmpty(locale)) {
            double rating = island.getTotalRating();
            infoMessage.append(Message.ISLAND_INFO_RATE.getMessage(locale, Formatters.RATING_FORMATTER.format(rating, locale),
                    Formatters.NUMBER_FORMATTER.format(rating), island.getRatingAmount())).append("\n");
        }

        if (BuiltinModules.BANK.isEnabled()) {
            // Island balance
            if (!Message.ISLAND_INFO_BANK.isEmpty(locale))
                infoMessage.append(Message.ISLAND_INFO_BANK.getMessage(locale, island.getIslandBank().getBalance())).append("\n");
        }

        // Island bonus worth
        if (!Message.ISLAND_INFO_BONUS.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_BONUS.getMessage(locale, island.getBonusWorth())).append("\n");

        // Island bonus level
        if (!Message.ISLAND_INFO_BONUS_LEVEL.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_BONUS_LEVEL.getMessage(locale, island.getBonusLevel())).append("\n");

        // Island worth
        if (!Message.ISLAND_INFO_WORTH.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_WORTH.getMessage(locale, island.getWorth())).append("\n");

        // Island level
        if (!Message.ISLAND_INFO_LEVEL.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_LEVEL.getMessage(locale, island.getIslandLevel())).append("\n");

        // Island discord
        if (!Message.ISLAND_INFO_DISCORD.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_DISCORD.getMessage(locale, island.getDiscord())).append("\n");

        // Island paypal
        if (!Message.ISLAND_INFO_PAYPAL.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_PAYPAL.getMessage(locale, island.getPaypal())).append("\n");

        boolean upgradesModule = BuiltinModules.UPGRADES.isEnabled();

        if (upgradesModule) {
            // Island upgrades
            if (!Message.ISLAND_INFO_ADMIN_UPGRADES.isEmpty(locale) && !Message.ISLAND_INFO_ADMIN_UPGRADE_LINE.isEmpty(locale)) {
                StringBuilder upgradesString = new StringBuilder();
                for (Upgrade upgrade : plugin.getUpgrades().getUpgrades()) {
                    upgradesString.append(Message.ISLAND_INFO_ADMIN_UPGRADE_LINE.getMessage(locale, upgrade.getName(), island.getUpgradeLevel(upgrade).getLevel())).append("\n");
                }
                infoMessage.append(Message.ISLAND_INFO_ADMIN_UPGRADES.getMessage(locale, upgradesString));
            }
        }

        // Island admin size
        if (!Message.ISLAND_INFO_ADMIN_SIZE.isEmpty(locale)) {
            infoMessage.append(Message.ISLAND_INFO_ADMIN_SIZE.getMessage(locale, island.getIslandSize()));
            if (island.getIslandSizeRaw() != island.getIslandSize())
                infoMessage.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        // Island team limit
        if (!Message.ISLAND_INFO_ADMIN_TEAM_LIMIT.isEmpty(locale)) {
            infoMessage.append(Message.ISLAND_INFO_ADMIN_TEAM_LIMIT.getMessage(locale, island.getTeamLimit()));
            if (island.getTeamLimitRaw() != island.getTeamLimit())
                infoMessage.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        // Island warps limit
        if (!Message.ISLAND_INFO_ADMIN_WARPS_LIMIT.isEmpty(locale)) {
            infoMessage.append(Message.ISLAND_INFO_ADMIN_WARPS_LIMIT.getMessage(locale, island.getWarpsLimit()));
            if (island.getWarpsLimitRaw() != island.getWarpsLimit())
                infoMessage.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        // Island coop limit
        if (plugin.getSettings().isCoopMembers() && !Message.ISLAND_INFO_ADMIN_COOP_LIMIT.isEmpty(locale)) {
            infoMessage.append(Message.ISLAND_INFO_ADMIN_COOP_LIMIT.getMessage(locale, island.getCoopLimit()));
            if (island.getCoopLimitRaw() != island.getCoopLimit())
                infoMessage.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        // Island bank limit
        if (!Message.ISLAND_INFO_ADMIN_BANK_LIMIT.isEmpty(locale)) {
            infoMessage.append(Message.ISLAND_INFO_ADMIN_BANK_LIMIT.getMessage(locale,
                    Formatters.NUMBER_FORMATTER.format(island.getBankLimit())));
            if (!island.getBankLimitRaw().equals(island.getBankLimit()))
                infoMessage.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            infoMessage.append("\n");
        }

        if (upgradesModule) {
            // Island spawners multiplier
            if (!Message.ISLAND_INFO_ADMIN_SPAWNERS_MULTIPLIER.isEmpty(locale) &&
                    BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeSpawnerRates.class)) {
                infoMessage.append(Message.ISLAND_INFO_ADMIN_SPAWNERS_MULTIPLIER.getMessage(locale, island.getSpawnerRatesMultiplier()));
                if (island.getSpawnerRatesRaw() != island.getSpawnerRatesMultiplier())
                    infoMessage.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
                infoMessage.append("\n");
            }

            // Island drops multiplier
            if (!Message.ISLAND_INFO_ADMIN_DROPS_MULTIPLIER.isEmpty(locale) &&
                    BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeMobDrops.class)) {
                infoMessage.append(Message.ISLAND_INFO_ADMIN_DROPS_MULTIPLIER.getMessage(locale, island.getMobDropsMultiplier()));
                if (island.getMobDropsRaw() != island.getMobDropsMultiplier())
                    infoMessage.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
                infoMessage.append("\n");
            }

            // Island crops multiplier
            if (!Message.ISLAND_INFO_ADMIN_CROPS_MULTIPLIER.isEmpty(locale) &&
                    BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class)) {
                infoMessage.append(Message.ISLAND_INFO_ADMIN_CROPS_MULTIPLIER.getMessage(locale, island.getCropGrowthMultiplier()));
                if (island.getCropGrowthRaw() != island.getCropGrowthMultiplier())
                    infoMessage.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
                infoMessage.append("\n");
            }

            // Island entity limits
            if (!Message.ISLAND_INFO_ADMIN_ENTITIES_LIMITS.isEmpty(locale) &&
                    !Message.ISLAND_INFO_ADMIN_ENTITIES_LIMITS_LINE.isEmpty(locale) &&
                    BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class)) {
                StringBuilder entitiesString = new StringBuilder();
                for (Map.Entry<Key, Integer> entry : island.getEntitiesLimitsAsKeys().entrySet()) {
                    entitiesString.append(Message.ISLAND_INFO_ADMIN_ENTITIES_LIMITS_LINE.getMessage(locale,
                            Formatters.CAPITALIZED_FORMATTER.format(entry.getKey().toString()), entry.getValue()));
                    if (!island.getCustomEntitiesLimits().containsKey(entry.getKey()))
                        entitiesString.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
                    entitiesString.append("\n");
                }
                infoMessage.append(Message.ISLAND_INFO_ADMIN_ENTITIES_LIMITS.getMessage(locale, entitiesString));
            }

            // Island block limits
            if (!Message.ISLAND_INFO_ADMIN_BLOCKS_LIMITS.isEmpty(locale) &&
                    !Message.ISLAND_INFO_ADMIN_BLOCKS_LIMITS_LINE.isEmpty(locale) &&
                    BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeBlockLimits.class)) {
                StringBuilder blocksString = new StringBuilder();
                for (Map.Entry<Key, Integer> entry : island.getBlocksLimits().entrySet()) {
                    blocksString.append(Message.ISLAND_INFO_ADMIN_BLOCKS_LIMITS_LINE.getMessage(locale,
                            Formatters.CAPITALIZED_FORMATTER.format(entry.getKey().toString()), entry.getValue()));
                    if (!island.getCustomBlocksLimits().containsKey(entry.getKey()))
                        blocksString.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
                    blocksString.append("\n");
                }
                infoMessage.append(Message.ISLAND_INFO_ADMIN_BLOCKS_LIMITS.getMessage(locale, blocksString));
            }
        }

        if (BuiltinModules.GENERATORS.isEnabled()) {
            // Island generator rates
            if (!Message.ISLAND_INFO_ADMIN_GENERATOR_RATES.isEmpty(locale) && !Message.ISLAND_INFO_ADMIN_GENERATOR_RATES_LINE.isEmpty(locale)) {
                for (World.Environment environment : World.Environment.values()) {
                    Map<Key, Integer> customGeneratorValues = island.getCustomGeneratorAmounts(environment);
                    StringBuilder generatorString = new StringBuilder();
                    for (Map.Entry<String, Integer> entry : island.getGeneratorPercentages(environment).entrySet()) {
                        Key key = Keys.ofMaterialAndData(entry.getKey());
                        generatorString.append(Message.ISLAND_INFO_ADMIN_GENERATOR_RATES_LINE.getMessage(locale,
                                Formatters.CAPITALIZED_FORMATTER.format(entry.getKey()),
                                Formatters.NUMBER_FORMATTER.format(IslandUtils.getGeneratorPercentageDecimal(island, key, environment)),
                                island.getGeneratorAmount(key, environment))
                        );
                        if (!customGeneratorValues.containsKey(key))
                            generatorString.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
                        generatorString.append("\n");
                    }
                    infoMessage.append(Message.ISLAND_INFO_ADMIN_GENERATOR_RATES.getMessage(locale, generatorString,
                            Formatters.CAPITALIZED_FORMATTER.format(environment.name())));
                }
            }
        }

        if (upgradesModule) {
            // Island effects
            if (!Message.ISLAND_INFO_ADMIN_ISLAND_EFFECTS.isEmpty(locale) &&
                    !Message.ISLAND_INFO_ADMIN_ISLAND_EFFECTS_LINE.isEmpty(locale) &&
                    BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeIslandEffects.class)) {
                StringBuilder blocksString = new StringBuilder();
                for (Map.Entry<PotionEffectType, Integer> entry : island.getPotionEffects().entrySet()) {
                    blocksString.append(Message.ISLAND_INFO_ADMIN_ISLAND_EFFECTS_LINE.getMessage(locale,
                            Formatters.CAPITALIZED_FORMATTER.format(entry.getKey().getName()), entry.getValue())).append("\n");
                }
                infoMessage.append(Message.ISLAND_INFO_ADMIN_ISLAND_EFFECTS.getMessage(locale, blocksString));
            }
        }

        // Island entity limits
        if (!Message.ISLAND_INFO_ADMIN_ROLE_LIMITS.isEmpty(locale) && !Message.ISLAND_INFO_ADMIN_ROLE_LIMITS_LINE.isEmpty(locale)) {
            StringBuilder entitiesString = new StringBuilder();
            for (Map.Entry<PlayerRole, Integer> entry : island.getRoleLimits().entrySet()) {
                entitiesString.append(Message.ISLAND_INFO_ADMIN_ROLE_LIMITS_LINE.getMessage(locale, entry.getKey(), entry.getValue()));
                if (!island.getCustomRoleLimits().containsKey(entry.getKey()))
                    entitiesString.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
                entitiesString.append("\n");
            }
            infoMessage.append(Message.ISLAND_INFO_ADMIN_ROLE_LIMITS.getMessage(locale, entitiesString));
        }

        // Island members
        if (!Message.ISLAND_INFO_ROLES.isEmpty(locale)) {
            Map<PlayerRole, StringBuilder> rolesStrings = new HashMap<>();

            List<SuperiorPlayer> members = island.getIslandMembers(false);

            if (!Message.ISLAND_INFO_PLAYER_LINE.isEmpty(locale)) {
                members.forEach(superiorPlayer -> rolesStrings.computeIfAbsent(superiorPlayer.getPlayerRole(), role -> new StringBuilder())
                        .append(Message.ISLAND_INFO_PLAYER_LINE.getMessage(locale, superiorPlayer.getName())).append("\n"));
            }

            rolesStrings.keySet().stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(PlayerRole::getWeight)))
                    .forEach(playerRole ->
                            infoMessage.append(Message.ISLAND_INFO_ROLES.getMessage(locale, playerRole, rolesStrings.get(playerRole))));
        }

        if (!Message.ISLAND_INFO_FOOTER.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_FOOTER.getMessage(locale));

        Message.CUSTOM.send(sender, infoMessage.toString(), false);
    }

}
