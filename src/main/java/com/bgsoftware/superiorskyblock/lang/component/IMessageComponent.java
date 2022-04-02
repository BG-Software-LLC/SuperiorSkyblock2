package com.bgsoftware.superiorskyblock.lang.component;

import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Optional;

public interface IMessageComponent {

    String getMessage();

    void sendMessage(CommandSender sender, Object... objects);

    default boolean isEmpty() {
        return this instanceof EmptyMessageComponent;
    }

    static Optional<String> replaceArgs(String msg, Object... objects) {
        if (msg == null || msg.isEmpty())
            return Optional.empty();

        for (int i = 0; i < objects.length; i++) {
            String objectString = objects[i] instanceof BigDecimal ?
                    StringUtils.format((BigDecimal) objects[i]) : objects[i].toString();
            msg = msg.replace("{" + i + "}", objectString);
        }

        return msg.isEmpty() ? Optional.empty() : Optional.of(msg);
    }

}
