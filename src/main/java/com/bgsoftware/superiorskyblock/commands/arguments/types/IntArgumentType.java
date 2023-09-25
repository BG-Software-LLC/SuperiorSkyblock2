package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;

public class IntArgumentType implements CommandArgumentType<Integer, CommandContext> {

    public static final IntArgumentType PAGE = new IntArgumentType(Message.INVALID_PAGE);
    public static final IntArgumentType LIMIT = new IntArgumentType(Message.INVALID_LIMIT);
    public static final IntArgumentType SIZE = new IntArgumentType(Message.INVALID_SIZE);

    private final Message invalidMessage;

    private IntArgumentType(Message invalidMessage) {
        this.invalidMessage = invalidMessage;
    }

    @Override
    public Integer parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        int i;

        try {
            i = Integer.parseInt(argument);
        } catch (IllegalArgumentException ex) {
            this.invalidMessage.send(context.getDispatcher());
            throw new CommandSyntaxException("Invalid argument: " + argument);
        }

        return i;
    }

}
