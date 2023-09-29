package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.function.BiPredicate;

public class IntArgumentType implements CommandArgumentType<Integer> {

    public static final IntArgumentType PAGE = new IntArgumentType(Message.INVALID_PAGE, PageFilter.INSTANCE);
    public static final IntArgumentType LIMIT = new IntArgumentType(Message.INVALID_LIMIT, null);
    public static final IntArgumentType SIZE = new IntArgumentType(Message.INVALID_SIZE, null);
    public static final IntArgumentType AMOUNT = new IntArgumentType(Message.INVALID_AMOUNT, null);
    public static final IntArgumentType ROWS = new IntArgumentType(Message.INVALID_ROWS, RowsFilter.INSTANCE);
    public static final IntArgumentType INTERVAL = new IntArgumentType(Message.INVALID_INTERVAL, IntervalFilter.INSTANCE);
    public static final IntArgumentType LEVEL = new IntArgumentType(Message.INVALID_LEVEL, null);

    private final Message invalidMessage;
    @Nullable
    private final Filter filter;

    private IntArgumentType(Message invalidMessage, @Nullable Filter filter) {
        this.invalidMessage = invalidMessage;
        this.filter = filter;
    }

    @Override
    public Integer parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String argument = reader.read();

        int i;

        try {
            i = Integer.parseInt(argument);
        } catch (IllegalArgumentException ex) {
            this.invalidMessage.send(context.getDispatcher(), argument);
            throw new CommandSyntaxException("Invalid argument: " + argument);
        }

        if (this.filter != null && !this.filter.test(context, i)) {
            throw new CommandSyntaxException("Invalid argument: " + argument);
        }

        return i;
    }

    private interface Filter extends BiPredicate<CommandContext, Integer> {

    }

    private static class PageFilter implements Filter {

        private static final PageFilter INSTANCE = new PageFilter();

        @Override
        public boolean test(CommandContext context, Integer page) {
            if (page <= 0) {
                Message.INVALID_PAGE.send(context.getDispatcher(), page);
                return false;
            }

            return true;
        }
    }

    private static class RowsFilter implements Filter {

        private static final RowsFilter INSTANCE = new RowsFilter();

        @Override
        public boolean test(CommandContext context, Integer rows) {
            if (rows < 1 || rows > 6) {
                Message.INVALID_ROWS.send(context.getDispatcher(), context.getInputArgument("rows"));
                return false;
            }

            return true;
        }
    }

    private static class IntervalFilter implements Filter {

        private static final IntervalFilter INSTANCE = new IntervalFilter();

        @Override
        public boolean test(CommandContext context, Integer interval) {
            if (interval < 0) {
                Message.INVALID_INTERVAL.send(context.getDispatcher(), interval);
                return false;
            }

            return true;
        }
    }

}
