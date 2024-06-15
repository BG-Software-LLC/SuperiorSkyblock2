package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.common.collections.Maps;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class BaseCommand {

    private final List<String> aliases = Collections.unmodifiableList(aliases());
    private final String permission = permission();
    private final Map<Locale, CommandDescription> commandDescription = Maps.newArrayMap();

    public final List<String> getAliases() {
        return this.aliases;
    }

    protected abstract List<String> aliases();

    public final String getPermission() {
        return this.permission;
    }

    protected abstract String permission();

    public final String getUsage(Locale locale) {
        return this.commandDescription.computeIfAbsent(locale, CommandDescription::new).usage;
    }

    protected abstract String usage(Locale locale);

    public final String getDescription(Locale locale) {
        return this.commandDescription.computeIfAbsent(locale, CommandDescription::new).description;
    }

    protected abstract String description(Locale locale);

    private class CommandDescription {

        private final String usage;
        private final String description;

        CommandDescription(Locale locale) {
            this.usage = BaseCommand.this.usage(locale);
            this.description = BaseCommand.this.description(locale);
        }

    }

}
