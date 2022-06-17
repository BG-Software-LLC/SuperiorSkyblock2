package com.bgsoftware.superiorskyblock.core.messages.component;

import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import org.bukkit.command.CommandSender;

public class EmptyMessageComponent implements IMessageComponent {

    private static final EmptyMessageComponent INSTANCE = new EmptyMessageComponent();

    public static EmptyMessageComponent getInstance() {
        return INSTANCE;
    }

    private EmptyMessageComponent() {

    }

    @Override
    public Type getType() {
        return Type.EMPTY;
    }

    @Override
    public String getMessage() {
        return "";
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        // Do nothing.
    }

}
