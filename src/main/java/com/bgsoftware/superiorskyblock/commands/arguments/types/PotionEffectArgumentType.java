package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.potion.PotionEffectType;

import java.util.Locale;

public class PotionEffectArgumentType implements CommandArgumentType<PotionEffectType> {

    public static final PotionEffectArgumentType INSTANCE = new PotionEffectArgumentType();

    @Override
    public PotionEffectType parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        PotionEffectType potionEffectType = PotionEffectType.getByName(argument.toUpperCase(Locale.ENGLISH));

        if (potionEffectType == null) {
            Message.INVALID_EFFECT.send(context.getDispatcher(), argument);
            throw new CommandSyntaxException("Invalid effect: " + argument);
        }

        return potionEffectType;
    }

}
