package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.core.formatting.IFormatter;

import java.util.stream.Stream;

public class CommaFormatter implements IFormatter<Stream<String>> {

    private static final CommaFormatter INSTANCE = new CommaFormatter();

    public static CommaFormatter getInstance() {
        return INSTANCE;
    }

    private CommaFormatter() {
    }

    @Override
    public String format(Stream<String> values) {
        StringBuilder stringBuilder = new StringBuilder();
        values.forEach(value -> stringBuilder.append(", ").append(value));
        return stringBuilder.substring(2);
    }

}
