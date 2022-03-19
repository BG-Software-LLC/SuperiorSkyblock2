package com.bgsoftware.superiorskyblock.lang.component.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.lang.component.EmptyMessageComponent;
import com.bgsoftware.superiorskyblock.lang.component.IMessageComponent;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public final class ActionBarComponent implements IMessageComponent {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final String message;

    public static IMessageComponent of(@Nullable String message) {
        return StringUtils.isBlank(message) ? EmptyMessageComponent.getInstance() : new ActionBarComponent(message);
    }

    private ActionBarComponent(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public void sendMessage(CommandSender sender, Object... objects) {
        IMessageComponent.replaceArgs(this.message, objects).ifPresent(message ->
                plugin.getNMSPlayers().sendActionBar((Player) sender, message));
    }

}
