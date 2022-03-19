package com.bgsoftware.superiorskyblock.lang.component;

import org.bukkit.command.CommandSender;

public final class EmptyMessageComponent implements IMessageComponent {

    private static final EmptyMessageComponent INSTANCE = new EmptyMessageComponent();

    public static EmptyMessageComponent getInstance() {
        return INSTANCE;
    }

    private EmptyMessageComponent() {

    }

    @Override
    public String getMessage() {
        return "";
    }

    @Override
    public void sendMessage(CommandSender sender, Object... objects) {
        // Do nothing.
    }

}
