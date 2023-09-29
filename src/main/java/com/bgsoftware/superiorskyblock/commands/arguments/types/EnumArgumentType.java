package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.Locale;

public class EnumArgumentType<E extends Enum<E>> implements CommandArgumentType<E> {

    public static final EnumArgumentType<BorderColor> BORDER_COLOR = new EnumArgumentType<>(BorderColor.class, Message.INVALID_BORDER_COLOR);
    public static final EnumArgumentType<World.Environment> WORLD_ENVIRONMENT = new EnumArgumentType<>(World.Environment.class, Message.INVALID_ENVIRONMENT);
    public static final EnumArgumentType<Biome> BIOME = new EnumArgumentType<>(Biome.class, Message.INVALID_BIOME);
    public static final EnumArgumentType<Rating> RATING = new EnumArgumentType<>(Rating.class, Message.INVALID_RATE);

    private final Class<E> enumClazz;
    private final Message invalidMessage;

    private EnumArgumentType(Class<E> enumClazz, Message invalidMessage) {
        this.enumClazz = enumClazz;
        this.invalidMessage = invalidMessage;
    }

    @Override
    public E parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        E enumObject = EnumHelper.getEnum(this.enumClazz, argument.toUpperCase(Locale.ENGLISH));

        if (enumObject == null) {
            this.invalidMessage.send(context.getDispatcher(), argument);
            throw new CommandSyntaxException("Invalid border color");
        }

        return enumObject;
    }

}
