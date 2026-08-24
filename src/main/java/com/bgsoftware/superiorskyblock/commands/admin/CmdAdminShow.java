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
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
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
import org.bukkit.entity.Player;
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
        return "admin show [" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SHOW.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        Island island = args.length == 2 ? CommandArguments.getIslandWhereStandingOrSenderIsland(plugin, sender).getIsland() :
                CommandArguments.getIsland(plugin, sender, args[2]).getIsland();

        if (island == null)
            return;

        BukkitExecutor.ensureAsync(() -> doShowIslandInfo(plugin, sender, island));
    }

    private static void doShowIslandInfo(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island) {
        SuperiorPlayer superiorPlayer = sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null;
        Locale locale = superiorPlayer != null ? superiorPlayer.getUserLocale() : PlayerLocales.getDefaultLocale();
        boolean isUpgradesModuleEnabled = BuiltinModules.UPGRADES.isEnabled();
        long lastTime = island.getLastTimeUpdate();

        Message.ISLAND_INFO_HEADER.sendPlayerOrConsole(superiorPlayer);
        Message.ISLAND_INFO_OWNER.sendPlayerOrConsole(superiorPlayer, island.getOwner().getName());
        Message.ISLAND_INFO_NAME.sendPlayerOrConsole(superiorPlayer, island.getName());

        WorldInfo worldInfo = plugin.getGrid().getIslandsWorldInfo(island, plugin.getSettings().getWorlds().getDefaultWorldDimension());
        Message.ISLAND_INFO_LOCATION.sendPlayerOrConsole(superiorPlayer,
                Formatters.BLOCK_POSITION_FORMATTER.format(island.getCenterPosition(), worldInfo));

        if (lastTime != -1) {
            Message.ISLAND_INFO_LAST_TIME_UPDATED.sendPlayerOrConsole(superiorPlayer,
                    Formatters.TIME_FORMATTER.format(Duration.ofMillis(System.currentTimeMillis() - (lastTime * 1000)), locale));
        } else {
            Message.ISLAND_INFO_LAST_TIME_UPDATED_CURRENTLY_ACTIVE.sendPlayerOrConsole(superiorPlayer);
        }

        double rating = island.getTotalRating();
        Message.ISLAND_INFO_RATE.sendPlayerOrConsole(superiorPlayer, Formatters.RATING_FORMATTER.format(rating, locale),
                Formatters.NUMBER_FORMATTER.format(rating), island.getRatingAmount());

        if (BuiltinModules.BANK.isEnabled()) {
            Message.ISLAND_INFO_BANK.sendPlayerOrConsole(superiorPlayer, island.getIslandBank().getBalance());
        }

        Message.ISLAND_INFO_BONUS.sendPlayerOrConsole(superiorPlayer, island.getBonusWorth());
        Message.ISLAND_INFO_BONUS_LEVEL.sendPlayerOrConsole(superiorPlayer, island.getBonusLevel());
        Message.ISLAND_INFO_WORTH.sendPlayerOrConsole(superiorPlayer, island.getWorth());
        Message.ISLAND_INFO_LEVEL.sendPlayerOrConsole(superiorPlayer, island.getIslandLevel());

        if (!IslandUtils.DEFAULT_NONE_VALUE.equals(island.getDiscord())) {
            Message.ISLAND_INFO_DISCORD.sendPlayerOrConsole(superiorPlayer, island.getDiscord());
        }

        if (!IslandUtils.DEFAULT_NONE_VALUE.equals(island.getPaypal())) {
            Message.ISLAND_INFO_PAYPAL.sendPlayerOrConsole(superiorPlayer, island.getPaypal());
        }

        if (isUpgradesModuleEnabled) {
            if (!Message.ISLAND_INFO_ADMIN_UPGRADES.isEmpty(locale) && !Message.ISLAND_INFO_ADMIN_UPGRADE_LINE.isEmpty(locale)) {
                StringBuilder upgradesString = new StringBuilder();
                for (Upgrade upgrade : plugin.getUpgrades().getUpgrades()) {
                    Text.appendWithLine(upgradesString, Message.ISLAND_INFO_ADMIN_UPGRADE_LINE.getMessage(
                            locale, upgrade.getName(), island.getUpgradeLevel(upgrade).getLevel()));
                }
                Message.ISLAND_INFO_ADMIN_UPGRADES.sendPlayerOrConsole(superiorPlayer, upgradesString);
            }
        }

        collectIslandData(superiorPlayer, locale, island::getIslandSize, island::getIslandSizeRaw, Message.ISLAND_INFO_ADMIN_SIZE);
        collectIslandData(superiorPlayer, locale, island::getTeamLimit, island::getTeamLimitRaw, Message.ISLAND_INFO_ADMIN_TEAM_LIMIT);
        collectIslandData(superiorPlayer, locale, island::getWarpsLimit, island::getWarpsLimitRaw, Message.ISLAND_INFO_ADMIN_WARPS_LIMIT);

        if (plugin.getSettings().isCoopMembers()) {
            collectIslandData(superiorPlayer, locale, island::getCoopLimit, island::getCoopLimitRaw, Message.ISLAND_INFO_ADMIN_COOP_LIMIT);
        }

        if (BuiltinModules.BANK.isEnabled()) {
            collectIslandData(superiorPlayer, locale, island::getBankLimit, island::getBankLimitRaw, Message.ISLAND_INFO_ADMIN_BANK_LIMIT,
                    Formatters.NUMBER_FORMATTER::format);
        }

        if (isUpgradesModuleEnabled) {
            if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeSpawnerRates.class)) {
                collectIslandData(superiorPlayer, locale, island::getSpawnerRatesMultiplier, island::getSpawnerRatesRaw,
                        Message.ISLAND_INFO_ADMIN_SPAWNERS_MULTIPLIER);
            }

            if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeMobDrops.class)) {
                collectIslandData(superiorPlayer, locale, island::getMobDropsMultiplier, island::getMobDropsRaw,
                        Message.ISLAND_INFO_ADMIN_DROPS_MULTIPLIER);
            }

            // Island crops multiplier
            if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeCropGrowth.class)) {
                collectIslandData(superiorPlayer, locale, island::getCropGrowthMultiplier, island::getCropGrowthRaw,
                        Message.ISLAND_INFO_ADMIN_CROPS_MULTIPLIER);
            }

            // Island entity limits
            if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeEntityLimits.class)) {
                collectIslandData(superiorPlayer, locale, island::getEntitiesLimitsAsKeys, island::getCustomEntitiesLimits,
                        Message.ISLAND_INFO_ADMIN_ENTITIES_LIMITS, Message.ISLAND_INFO_ADMIN_ENTITIES_LIMITS_LINE);
            }

            // Island block limits
            if (BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeBlockLimits.class)) {
                collectIslandData(superiorPlayer, locale, island::getBlocksLimits, island::getCustomBlocksLimits,
                        Message.ISLAND_INFO_ADMIN_BLOCKS_LIMITS, Message.ISLAND_INFO_ADMIN_BLOCKS_LIMITS_LINE);
            }
        }

        if (BuiltinModules.GENERATORS.isEnabled()) {
            // Island generator rates
            if (!Message.ISLAND_INFO_ADMIN_GENERATOR_RATES.isEmpty(locale) && !Message.ISLAND_INFO_ADMIN_GENERATOR_RATES_LINE.isEmpty(locale)) {
                for (Dimension dimension : Dimension.values()) {
                    StringBuilder islandDataMessage = new StringBuilder();

                    Map<Key, Integer> customGeneratorValues = island.getCustomGeneratorAmounts(dimension);
                    for (Map.Entry<String, Integer> entry : island.getGeneratorPercentages(dimension).entrySet()) {
                        StringBuilder lineDataMessage = new StringBuilder();

                        Key key = Keys.ofMaterialAndData(entry.getKey());
                        lineDataMessage.append(Message.ISLAND_INFO_ADMIN_GENERATOR_RATES_LINE.getMessage(locale,
                                Formatters.CAPITALIZED_FORMATTER.format(entry.getKey()),
                                Formatters.NUMBER_FORMATTER.format(IslandUtils.getGeneratorPercentageDecimal(island, key, dimension)),
                                island.getGeneratorAmount(key, dimension))
                        );

                        if (!customGeneratorValues.containsKey(key))
                            lineDataMessage.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));

                        Text.appendWithLine(islandDataMessage, lineDataMessage);
                    }

                    if (islandDataMessage.length() > 0) {
                        Message.ISLAND_INFO_ADMIN_GENERATOR_RATES.sendPlayerOrConsole(superiorPlayer, islandDataMessage,
                                Formatters.CAPITALIZED_FORMATTER.format(dimension.getName()));
                    }
                }
            }
        }

        if (isUpgradesModuleEnabled && BuiltinModules.UPGRADES.isUpgradeTypeEnabled(UpgradeTypeIslandEffects.class)) {
            collectIslandData(superiorPlayer, locale, island::getPotionEffects, island::getCustomPotionEffects,
                    Message.ISLAND_INFO_ADMIN_ISLAND_EFFECTS, Message.ISLAND_INFO_ADMIN_ISLAND_EFFECTS_LINE, PotionEffectType::getName);
        }

        collectIslandData(superiorPlayer, locale, island::getRoleLimits, island::getCustomRoleLimits,
                Message.ISLAND_INFO_ADMIN_ROLE_LIMITS, Message.ISLAND_INFO_ADMIN_ROLE_LIMITS_LINE);

        if (!Message.ISLAND_INFO_ROLES.isEmpty(locale)) {
            Map<PlayerRole, StringBuilder> rolesStrings = new LinkedHashMap<>();

            List<SuperiorPlayer> members = island.getIslandMembers(false);

            if (!Message.ISLAND_INFO_PLAYER_LINE.isEmpty(locale)) {
                members.forEach(islandMember -> {
                    Text.appendWithLine(rolesStrings.computeIfAbsent(islandMember.getPlayerRole(), role -> new StringBuilder()),
                            Message.ISLAND_INFO_PLAYER_LINE.getMessage(locale, islandMember.getName()));
                });
            }

            rolesStrings.keySet().stream()
                    .sorted(Collections.reverseOrder(Comparator.comparingInt(PlayerRole::getWeight)))
                    .forEach(playerRole -> {
                        StringBuilder players = rolesStrings.get(playerRole);
                        if (players != null && players.length() > 0)
                            Message.ISLAND_INFO_ROLES.sendPlayerOrConsole(superiorPlayer, playerRole, players);
                    });
        }

        Message.ISLAND_INFO_FOOTER.sendPlayerOrConsole(superiorPlayer);
    }

    private static <K, V> void collectIslandData(@Nullable SuperiorPlayer superiorPlayer, Locale locale,
                                                 Supplier<Map<K, V>> dataFunction,
                                                 Supplier<Map<K, V>> customDataFunction,
                                                 Message dataMessage, Message dataLineMessage) {
        collectIslandData(superiorPlayer, locale, dataFunction, customDataFunction, dataMessage, dataLineMessage, null);
    }

    private static <K, V> void collectIslandData(@Nullable SuperiorPlayer superiorPlayer, Locale locale,
                                                 Supplier<Map<K, V>> dataFunction,
                                                 Supplier<Map<K, V>> customDataFunction,
                                                 Message dataMessage, Message dataLineMessage,
                                                 @Nullable Function<K, String> formatter) {
        if (dataMessage.isEmpty(locale) || dataLineMessage.isEmpty(locale))
            return;

        Map<K, V> islandData = dataFunction.get();
        Map<K, V> islandCustomData = customDataFunction.get();

        if (islandData.isEmpty())
            return;

        StringBuilder islandDataMessage = new StringBuilder();

        islandData.forEach((key, value) -> {
            StringBuilder lineDataMessage = new StringBuilder();
            lineDataMessage.append(Formatters.CAPITALIZED_FORMATTER.format(formatter == null ? key.toString() : formatter.apply(key)));
            if (!islandCustomData.containsKey(key))
                lineDataMessage.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));
            Text.appendWithLine(islandDataMessage, dataLineMessage.getMessage(locale, lineDataMessage.toString(), value));
        });

        dataMessage.sendPlayerOrConsole(superiorPlayer, islandDataMessage);
    }

    private static <T> void collectIslandData(@Nullable SuperiorPlayer superiorPlayer, Locale locale,
                                              Supplier<T> dataFunction,
                                              Supplier<T> customDataFunction,
                                              Message dataLineMessage) {
        collectIslandData(superiorPlayer, locale, dataFunction, customDataFunction, dataLineMessage, null);
    }

    private static <T> void collectIslandData(@Nullable SuperiorPlayer superiorPlayer, Locale locale,
                                              Supplier<T> dataFunction,
                                              Supplier<T> customDataFunction,
                                              Message dataLineMessage,
                                              @Nullable Function<T, ?> formatter) {
        if (dataLineMessage.isEmpty(locale))
            return;

        T islandData = dataFunction.get();
        T islandCustomData = customDataFunction.get();

        StringBuilder islandDataMessage = new StringBuilder();
        islandDataMessage.append(formatter == null ? islandData : formatter.apply(islandData));
        if (!Objects.equals(islandData, islandCustomData))
            islandDataMessage.append(" ").append(Message.ISLAND_INFO_ADMIN_VALUE_SYNCED.getMessage(locale));

        dataLineMessage.sendPlayerOrConsole(superiorPlayer, islandDataMessage.toString());
    }

}
