package com.bgsoftware.superiorskyblock.commands.arguments;

import com.bgsoftware.superiorskyblock.core.LazyReference;

import java.util.HashMap;
import java.util.Map;

public class ArgumentsMap {

    private final Map<String, Entry> arguments = new HashMap<>();

    public void setArgument(String identifier, String input, Object value) {
        Entry existing = this.arguments.get(identifier);
        if (existing != null)
            throw new IllegalArgumentException("Identifier already exists: " + identifier);

        this.arguments.put(identifier, new Entry(value, input));
    }

    public Object getArgumentValue(String identifier) {
        Entry entry = this.arguments.get(identifier);
        Object value = entry == null ? null : entry.value;
        if (value instanceof LazyReference)
            value = ((LazyReference<?>) value).get();
        return value;
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
