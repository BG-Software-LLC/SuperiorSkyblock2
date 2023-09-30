package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class UpgradeArgumentType implements CommandArgumentType<Upgrade> {

    public static final UpgradeArgumentType INSTANCE = new UpgradeArgumentType();

    private UpgradeArgumentType() {

    }

    @Override
    public Upgrade parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        Upgrade upgrade = plugin.getUpgrades().getUpgrade(argument);

        if (upgrade == null) {
            Message.INVALID_UPGRADE.send(context.getDispatcher(), argument, Formatters.COMMA_FORMATTER.format(
                    plugin.getUpgrades().getUpgrades().stream().map(Upgrade::getName)));
            throw new CommandSyntaxException("Invalid upgrade: " + argument);
        }

        return upgrade;
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read().toLowerCase(Locale.ENGLISH);

        List<String> suggestions = new LinkedList<>();

        for (Upgrade upgrade : plugin.getUpgrades().getUpgrades()) {
            String upgradeName = upgrade.getName().toLowerCase(Locale.ENGLISH);
            if (upgradeName.contains(argument))
                suggestions.add(upgradeName);
        }

        return suggestions;
    }

}
