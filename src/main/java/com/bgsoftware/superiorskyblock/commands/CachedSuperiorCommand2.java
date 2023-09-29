package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand2;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;

import java.util.List;
import java.util.Locale;

public class CachedSuperiorCommand2 implements SuperiorCommand2 {

    private final SuperiorCommand2 delegate;

    private List<CommandArgument<?>> cachedArguments = null;

    public CachedSuperiorCommand2(SuperiorCommand2 delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<String> getAliases() {
        return this.delegate.getAliases();
    }

    @Override
    public String getPermission() {
        return this.delegate.getPermission();
    }

    @Override
    public String getDescription(Locale locale) {
        return this.delegate.getDescription(locale);
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return this.delegate.canBeExecutedByConsole();
    }

    @Override
    public boolean displayCommand() {
        return this.delegate.displayCommand();
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        if (this.cachedArguments == null) {
            this.cachedArguments = this.delegate.getArguments();
        }

        return this.cachedArguments;
    }

    @Override
    public void execute(SuperiorSkyblock plugin, CommandContext context) throws CommandSyntaxException {
        this.delegate.execute(plugin, context);
    }

}
