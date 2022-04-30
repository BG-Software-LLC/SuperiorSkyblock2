package com.bgsoftware.superiorskyblock.lang.component.impl;

import com.bgsoftware.superiorskyblock.lang.component.EmptyMessageComponent;
import com.bgsoftware.superiorskyblock.lang.component.IMessageComponent;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public final class SoundComponent implements IMessageComponent {

    private final SoundWrapper soundWrapper;

    public static IMessageComponent of(@Nullable SoundWrapper soundWrapper) {
        return SoundWrapper.isEmpty(soundWrapper) ? EmptyMessageComponent.getInstance() : new SoundComponent(soundWrapper);
    }

    private SoundComponent(SoundWrapper soundWrapper) {
        this.soundWrapper = soundWrapper;
    }

    @Override
    public String getMessage() {
        return "";
    }

    @Override
    public void sendMessage(CommandSender sender, Object... objects) {
        if (sender instanceof Player)
            soundWrapper.playSound((Player) sender);
    }

}
