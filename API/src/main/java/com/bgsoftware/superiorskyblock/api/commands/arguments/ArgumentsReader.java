package com.bgsoftware.superiorskyblock.api.commands.arguments;

import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;

public class ArgumentsReader {

    private final String[] args;
    private int cursor;

    public ArgumentsReader(String[] args) {
        this.args = args;
    }

    public String read() throws CommandSyntaxException {
        if (this.cursor >= this.args.length)
            throw new CommandSyntaxException("Out of bounds argument: " + this.cursor);

        return this.args[this.cursor++];
    }

    public boolean hasNext() {
        return this.cursor < this.args.length;
    }

    public int getCursor() {
        return this.cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

}
