package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class SoundComponent implements IMessageComponent {

    private final GameSound soundWrapper;

    public static IMessageComponent of(@Nullable GameSound soundWrapper) {
        return GameSound.isEmpty(soundWrapper) ? EmptyMessageComponent.getInstance() : new SoundComponent(soundWrapper);
    }

    private SoundComponent(GameSound soundWrapper) {
        this.soundWrapper = soundWrapper;
    }


    @Override
    public Type getType() {
        return Type.SOUND;
    }

    @Override
    public String getMessage() {
        return "";
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        if (sender instanceof Player)
            soundWrapper.playSound((Player) sender);
    }

}
