package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Locale;

public class BorderColorArgumentType implements CommandArgumentType<BorderColor, CommandContext> {

    public static final BorderColorArgumentType INSTANCE = new BorderColorArgumentType();

    @Override
    public BorderColor parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        BorderColor borderColor = EnumHelper.getEnum(BorderColor.class, argument.toUpperCase(Locale.ENGLISH));

        if (borderColor == null) {
            Message.INVALID_BORDER_COLOR.send(context.getDispatcher(), argument);
            throw new CommandSyntaxException("Invalid border color");
        }

        return borderColor;
    }

}
