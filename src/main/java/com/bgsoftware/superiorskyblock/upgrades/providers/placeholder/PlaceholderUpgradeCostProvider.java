package com.bgsoftware.superiorskyblock.upgrades.providers.placeholder;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeCostProvider;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;
import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.math.BigDecimal;

public class PlaceholderUpgradeCostProvider implements UpgradeCostProvider {
    @Override
    public String getName() {
        return "placeholder";
    }

    @Override
    public BigDecimal getBalance(SuperiorPlayer superiorPlayer, UpgradeCost upgradeCost) {
        Preconditions.checkArgument(upgradeCost instanceof PlaceholderUpgradeCost, "Placeholder Upgrade Cost provider only accepts PlaceholderUpgradeCost type");

        // Determine what type of placeholder api are we using
        String placeholder = ((PlaceholderUpgradeCost) upgradeCost).getPlaceholder();

        String response;

        // We have PAPI
        if (StringUtils.startsWith(placeholder, "%")) {
            Preconditions.checkArgument(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"), "Using PlaceholderAPI placeholder in upgrades, but plugin not found");
            response = PlaceholderAPI.setPlaceholders(superiorPlayer.asOfflinePlayer(), placeholder);

            // We have MvdwAPI
        } else if (StringUtils.startsWith(placeholder, "{")) {
            Preconditions.checkArgument(Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI"), "Using MVdWPlaceholderAPI placeholder in upgrades, but plugin not found");
            response = be.maximvdw.placeholderapi.PlaceholderAPI.replacePlaceholders(superiorPlayer.asOfflinePlayer(), placeholder);
        } else {
            throw new IllegalStateException("Failed to find a placeholder parser that starts with " + placeholder.charAt(0));
        }

        try {
            return new BigDecimal(response);
        } catch (Throwable throwable) {
            throw new IllegalStateException("Failed to parse placeholder parse response: " + response + " for placeholder: " + placeholder + " as number...");
        }
    }

    @Override
    public void take(SuperiorPlayer superiorPlayer, UpgradeCost upgradeCost) {
        Preconditions.checkArgument(upgradeCost instanceof PlaceholderUpgradeCost, "Placeholder Upgrade Cost provider only accepts PlaceholderUpgradeCost type");

        String takeCommand = ((PlaceholderUpgradeCost) upgradeCost).getTakeCommand();
        if (takeCommand.startsWith("/"))
            takeCommand = takeCommand.substring(1);

        takeCommand = StringUtils
                .replace(takeCommand, "%amount%", upgradeCost.getValue().toPlainString());
        takeCommand = StringUtils
                .replace(takeCommand, "%player%", superiorPlayer.getName());

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), takeCommand);
    }

    @Override
    public Pair<UpgradeCost, String> createCost(ConfigurationSection configurationSection) {
        if (!configurationSection.contains("price"))
            return new Pair<>(null, "Missing price value");

        if (!configurationSection.contains("placeholder"))
            return new Pair<>(null, "Missing placeholder value");

        if (!configurationSection.contains("take-command"))
            return new Pair<>(null, "Missing take-command value");

        return new Pair<>(
                new PlaceholderUpgradeCost(
                    new BigDecimal(configurationSection.get("price").toString()),
                    configurationSection.get("placeholder").toString().trim(),
                        configurationSection.get("take-command").toString(),
                        this
                ),
                null
        );
    }
}
