package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Collections;
import java.util.List;

public class PercentageArgumentType implements CommandArgumentType<PercentageArgumentType.Result> {

    public static final PercentageArgumentType AMOUNT = new PercentageArgumentType(Message.INVALID_AMOUNT);

    private final Message invalidMessage;

    private PercentageArgumentType(Message invalidMessage) {
        this.invalidMessage = invalidMessage;
    }

    @Override
    public Result parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        boolean isPercentage = argument.endsWith("%");
        if (isPercentage)
            argument = argument.substring(0, argument.length() - 1);

        int i;

        try {
            i = Integer.parseInt(argument);
        } catch (IllegalArgumentException ex) {
            this.invalidMessage.send(context.getDispatcher(), argument);
            throw new CommandSyntaxException("Invalid argument: " + argument);
        }

        if (isPercentage && (i < 0 || i > 100)) {
            Message.INVALID_PERCENTAGE.send(context.getDispatcher());
            throw new CommandSyntaxException("Invalid percentage: " + argument);
        }

        return new Result(i, isPercentage);
    }

    @Override
    public List<String> getSuggestions(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        reader.read(); // skip argument
        return Collections.emptyList();
    }

    public static class Result {

        private final int value;
        private final boolean isPercentage;

        private Result(int value, boolean isPercentage) {
            this.value = value;
            this.isPercentage = isPercentage;
        }

        public int getValue() {
            return this.value;
        }

        public boolean isPercentage() {
            return this.isPercentage;
        }

    }

}
