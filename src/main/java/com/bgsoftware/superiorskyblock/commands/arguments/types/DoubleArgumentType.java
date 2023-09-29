package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.function.Function;

public class DoubleArgumentType implements CommandArgumentType<Double> {

    public static final DoubleArgumentType MULTIPLIER = new DoubleArgumentType(Message.INVALID_MULTIPLIER, MultiplierTransformer.INSTANCE);

    private final Message invalidMessage;
    @Nullable
    private final Transformer transformer;

    private DoubleArgumentType(Message invalidMessage, @Nullable Transformer transformer) {
        this.invalidMessage = invalidMessage;
        this.transformer = transformer;
    }

    @Override
    public Double parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        double i;

        try {
            i = Double.parseDouble(argument);
        } catch (IllegalArgumentException ex) {
            this.invalidMessage.send(context.getDispatcher(), argument);
            throw new CommandSyntaxException("Invalid argument: " + argument);
        }

        if (this.transformer != null)
            i = this.transformer.apply(i);

        return i;
    }

    private interface Transformer extends Function<Double, Double> {

    }

    private static class MultiplierTransformer implements Transformer {

        private static final MultiplierTransformer INSTANCE = new MultiplierTransformer();

        @Override
        public Double apply(Double multiplier) {
            return Math.round(multiplier * 100) / 100D;
        }
    }

}
