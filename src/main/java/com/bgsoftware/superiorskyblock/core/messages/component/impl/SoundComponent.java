package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SoundComponent implements IMessageComponent {

    private final GameSound gameSound;

    public static IMessageComponent of(@Nullable GameSound gameSound) {
        return GameSoundImpl.isEmpty(gameSound) ? EmptyMessageComponent.getInstance() : new SoundComponent(gameSound);
    }

    private SoundComponent(GameSound gameSound) {
        this.gameSound = gameSound;
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
            GameSoundImpl.playSound((Player) sender, gameSound);
    }

}
