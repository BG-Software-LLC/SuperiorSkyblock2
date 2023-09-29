package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;

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

}
