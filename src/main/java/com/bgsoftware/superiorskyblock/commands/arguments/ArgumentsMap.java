package com.bgsoftware.superiorskyblock.commands.arguments;

import java.util.HashMap;
import java.util.Map;

public class ArgumentsMap {

    private final Map<String, Entry> arguments = new HashMap<>();

    public Object getArgumentValue(String identifier) {
        Entry entry = this.arguments.get(identifier);
        return entry == null ? null : entry.value;
    }

    public String getArgument(String identifier) {
        Entry entry = this.arguments.get(identifier);
        return entry == null ? null : entry.argument;
    }

    public int size() {
        return this.arguments.size();
    }

    public static class Entry {

        public final Object value;
        public final String argument;

        public Entry(Object value, String argument) {
            this.value = value;
            this.argument = argument;
        }

    }

}
