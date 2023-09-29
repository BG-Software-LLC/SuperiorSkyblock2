package com.bgsoftware.superiorskyblock.commands.arguments;

import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommandArgumentsBuilder {

    private final Map<String, CommandArgument<?>> arguments = new LinkedHashMap<>();

    public CommandArgumentsBuilder() {

    }

    public CommandArgumentsBuilder add(CommandArgument<?> argument) {
        String argumentName = argument.getIdentifier().toLowerCase(Locale.ENGLISH);

        CommandArgument<?> current = this.arguments.get(argumentName);
        if (current != null)
            throw new IllegalArgumentException("Argument " + argument.getIdentifier() + " already exists");

        this.arguments.put(argumentName, argument);

        return this;
    }

    public List<CommandArgument<?>> build() {
        return this.arguments.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(new LinkedList<>(this.arguments.values()));
    }

}
