package com.bgsoftware.superiorskyblock.commands.arguments;

import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Locale;

public class CommandArgumentImpl<E> implements CommandArgument<E> {

    private final String identifier;
    private final Object[] displayNameComponents;
    private final boolean isOptional;
    private final CommandArgumentType<E> argumentType;

    public CommandArgumentImpl(String identifier, Object[] displayNameComponents, boolean isOptional, CommandArgumentType<E> argumentType) {
        this.identifier = identifier;
        this.displayNameComponents = displayNameComponents;
        this.isOptional = isOptional;
        this.argumentType = argumentType;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getDisplayName(Locale locale) {
        StringBuilder displayName = new StringBuilder();
        for (Object component : this.displayNameComponents) {
            if (component instanceof Message) {
                displayName.append("/").append(((Message) component).getMessage(locale));
            } else {
                displayName.append("/").append(component);
            }
        }
        return displayName.length() == 0 ? "" : displayName.substring(1);
    }

    @Override
    public boolean isOptional() {
        return this.isOptional;
    }

    @Override
    public CommandArgumentType<E> getType() {
        return this.argumentType;
    }

}
