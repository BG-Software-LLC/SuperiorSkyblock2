package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
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
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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
        if (!Message.ISLAND_INFO_LOCATION.isEmpty(locale)) {
            WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(island, plugin.getSettings().getWorlds().getDefaultWorldDimension());
            infoMessage.append(Message.ISLAND_INFO_LOCATION.getMessage(locale,
                    Formatters.BLOCK_POSITION_FORMATTER.format(island.getCenterPosition(), worldInfo))).append("\n");
        }

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
        if (!Message.ISLAND_INFO_DISCORD.isEmpty(locale) && !"None".equals(island.getDiscord())) {
            infoMessage.append(Message.ISLAND_INFO_DISCORD.getMessage(locale, island.getDiscord())).append("\n");
        }

        // Island paypal
        if (!Message.ISLAND_INFO_PAYPAL.isEmpty(locale) && !"None".equals(island.getPaypal())) {
            infoMessage.append(Message.ISLAND_INFO_PAYPAL.getMessage(locale, island.getPaypal())).append("\n");
        }

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
        collectIslandData(locale, infoMessage, island::getIslandSize, island::getIslandSizeRaw, Message.ISLAND_INFO_ADMIN_SIZE);

        // Island team limit
        collectIslandData(locale, infoMessage, island::getTeamLimit, island::getTeamLimitRaw, Message.ISLAND_INFO_ADMIN_TEAM_LIMIT);

        // Island warps limit
        collectIslandData(locale, infoMessage, island::getWarpsLimit, island::getWarpsLimitRaw, Message.ISLAND_INFO_ADMIN_WARPS_LIMIT);

        // Island coop limit
        if (plugin.getSettings().isCoopMembers())
            collectIslandData(locale, infoMessage, island::getCoopLimit, island::getCoopLimitRaw, Message.ISLAND_INFO_ADMIN_COOP_LIMIT);

        // Island bank limit
        collectIslandData(locale, infoMessage, island::getBankLimit, island::getBankLimitRaw, Message.ISLAND_INFO_ADMIN_BANK_LIMIT,
                Formatters.NUMBER_FORMATTER::format);

        if (upgradesModule) {
            // Island spawners multiplier
            if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeSpawnerRates.class)) {
                collectIslandData(locale, infoMessage, island::getSpawnerRatesMultiplier, island::getSpawnerRatesRaw,
                        Message.ISLAND_INFO_ADMIN_SPAWNERS_MULTIPLIER);
            }


            // Island drops multiplier
            if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeMobDrops.class)) {
                collectIslandData(locale, infoMessage, island::getMobDropsMultiplier, island::getMobDropsRaw,
                        Message.ISLAND_INFO_ADMIN_DROPS_MULTIPLIER);
            }

            // Island crops multiplier
            if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class)) {
                collectIslandData(locale, infoMessage, island::getCropGrowthMultiplier, island::getCropGrowthRaw,
                        Message.ISLAND_INFO_ADMIN_CROPS_MULTIPLIER);
            }

            // Island entity limits
            if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class)) {
                collectIslandData(locale, island::getEntitiesLimitsAsKeys, island::getCustomEntitiesLimits,
                        Message.ISLAND_INFO_ADMIN_ENTITIES_LIMITS, Message.ISLAND_INFO_ADMIN_ENTITIES_LIMITS_LINE)
                        .ifPresent(message -> infoMessage.append(Message.ISLAND_INFO_ADMIN_ENTITIES_LIMITS.getMessage(locale, message)));
            }

            // Island block limits
            if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeBlockLimits.class)) {
                collectIslandData(locale, island::getBlocksLimits, island::getCustomBlocksLimits,
                        Message.ISLAND_INFO_ADMIN_BLOCKS_LIMITS, Message.ISLAND_INFO_ADMIN_BLOCKS_LIMITS_LINE)
                        .ifPresent(message -> infoMessage.append(Message.ISLAND_INFO_ADMIN_BLOCKS_LIMITS.getMessage(locale, message)));
            }
        }

        if (BuiltinModules.GENERATORS.isEnabled()) {
            // Island generator rates
            if (!Message.ISLAND_INFO_ADMIN_GENERATOR_RATES.isEmpty(locale) && !Message.ISLAND_INFO_ADMIN_GENERATOR_RATES_LINE.isEmpty(locale)) {
                for (Dimension dimension : Dimension.values()) {
                    Map<Key, Integer> customGeneratorValues = island.getCustomGeneratorAmounts(dimension);
                    StringBuilder generatorString = new StringBuilder();
                    for (Map.Entry<String, Integer> entry : island.getGeneratorPercentages(dimension).entrySet()) {
                        Key key = Keys.ofMaterialAndData(entry.getKey());
                        generatorString.append(Message.ISLAND_INFO_ADMIN_GENERATOR_RATES_LINE.getMessage(locale,
                                Formatters.CAPITALIZED_FORMATTER.format(entry.getKey()),
                                Formatters.NUMBER_FORMATTER.format(IslandUtils.getGeneratorPercentageDecimal(island, key, dimension)),
                                island.getGeneratorAmount(key, dimension))
                        );
                        if (!customGeneratorValues.containsKey(key))
                            generatorString.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
                        generatorString.append("\n");
                    }
                    if (generatorString.length() > 0)
                        infoMessage.append(Message.ISLAND_INFO_ADMIN_GENERATOR_RATES.getMessage(locale, generatorString,
                                Formatters.CAPITALIZED_FORMATTER.format(dimension.getName())));
                }
            }
        }

        // Island effects
        if (upgradesModule && BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeIslandEffects.class)) {
            collectIslandData(locale, island::getPotionEffects, island::getCustomPotionEffects,
                    Message.ISLAND_INFO_ADMIN_ISLAND_EFFECTS, Message.ISLAND_INFO_ADMIN_ISLAND_EFFECTS_LINE, PotionEffectType::getName)
                    .ifPresent(message -> infoMessage.append(Message.ISLAND_INFO_ADMIN_ISLAND_EFFECTS.getMessage(locale, message)));
        }

        // Island role limits
        collectIslandData(locale, island::getRoleLimits, island::getCustomRoleLimits,
                Message.ISLAND_INFO_ADMIN_ROLE_LIMITS, Message.ISLAND_INFO_ADMIN_ROLE_LIMITS_LINE)
                .ifPresent(message -> infoMessage.append(Message.ISLAND_INFO_ADMIN_ROLE_LIMITS.getMessage(locale, message)));

        // Island members
        if (!Message.ISLAND_INFO_ROLES.isEmpty(locale)) {
            Map<PlayerRole, StringBuilder> rolesStrings = new LinkedHashMap<>();

            List<SuperiorPlayer> members = island.getIslandMembers(false);

            if (!Message.ISLAND_INFO_PLAYER_LINE.isEmpty(locale)) {
                members.forEach(superiorPlayer -> rolesStrings.computeIfAbsent(superiorPlayer.getPlayerRole(), role -> new StringBuilder())
                        .append(Message.ISLAND_INFO_PLAYER_LINE.getMessage(locale, superiorPlayer.getName())).append("\n"));
            }

            rolesStrings.keySet().stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(PlayerRole::getWeight)))
                    .forEach(playerRole -> {
                        StringBuilder players = rolesStrings.get(playerRole);
                        if (players != null && players.length() > 0)
                            infoMessage.append(Message.ISLAND_INFO_ROLES.getMessage(locale, playerRole, players));
                    });
        }

        if (!Message.ISLAND_INFO_FOOTER.isEmpty(locale))
            infoMessage.append(Message.ISLAND_INFO_FOOTER.getMessage(locale));

        Message.CUSTOM.send(sender, infoMessage.toString(), false);
    }

    private static <K, V> Optional<StringBuilder> collectIslandData(Locale locale,
                                                                    Supplier<Map<K, V>> dataFunction,
                                                                    Supplier<Map<K, V>> customDataFunction,
                                                                    Message dataMessage, Message dataLineMessage) {
        return collectIslandData(locale, dataFunction, customDataFunction, dataMessage, dataLineMessage, null);
    }

    private static <K, V> Optional<StringBuilder> collectIslandData(Locale locale,
                                                                    Supplier<Map<K, V>> dataFunction,
                                                                    Supplier<Map<K, V>> customDataFunction,
                                                                    Message dataMessage, Message dataLineMessage,
                                                                    @Nullable Function<K, String> formatter) {
        if (dataMessage.isEmpty(locale) || dataLineMessage.isEmpty(locale))
            return Optional.empty();

        StringBuilder dataValue = new StringBuilder();

        Map<K, V> islandData = dataFunction.get();
        Map<K, V> islandCustomData = customDataFunction.get();

        islandData.forEach((key, value) -> {
            String keyStringValue = formatter == null ? key.toString() : formatter.apply(key);
            dataValue.append(dataLineMessage.getMessage(locale, Formatters.CAPITALIZED_FORMATTER.format(keyStringValue), value));
            if (!islandCustomData.containsKey(key))
                dataValue.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            dataValue.append("\n");
        });

        return dataValue.length() > 0 ? Optional.of(dataValue) : Optional.empty();
    }

    private static <T> void collectIslandData(Locale locale, StringBuilder message,
                                              Supplier<T> dataFunction,
                                              Supplier<T> customDataFunction,
                                              Message dataLineMessage) {
        collectIslandData(locale, message, dataFunction, customDataFunction, dataLineMessage, null);
    }

    private static <T> void collectIslandData(Locale locale, StringBuilder message,
                                              Supplier<T> dataFunction,
                                              Supplier<T> customDataFunction,
                                              Message dataLineMessage,
                                              @Nullable Function<T, ?> formatter) {
        if (dataLineMessage.isEmpty(locale))
            return;

        T islandData = dataFunction.get();
        T islandCustomData = customDataFunction.get();

        Object formattedIslandData = formatter == null ? islandData : formatter.apply(islandData);
        message.append(dataLineMessage.getMessage(locale, formattedIslandData));
        if (!Objects.equals(islandData, islandCustomData))
            message.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
        message.append("\n");
    }

}
