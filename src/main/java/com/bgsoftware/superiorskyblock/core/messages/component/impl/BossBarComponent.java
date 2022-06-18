
package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;

public class BossBarComponent implements IMessageComponent {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final String message;
    private final BossBar.Color color;
    private final int ticksToRun;

    public static IMessageComponent of(@Nullable String message, BossBar.Color color, int ticks) {
        return ticks <= 0 || Strings.isBlank(message) ? EmptyMessageComponent.getInstance() : new BossBarComponent(message, color, ticks);
    }

    private BossBarComponent(String message, BossBar.Color color, int ticks) {
        this.message = message;
        this.color = color;
        this.ticksToRun = ticks;
    }

    @Override
    public Type getType() {
        return Type.BOSS_BAR;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        if (sender instanceof Player) {
            Message.replaceArgs(this.message, args).ifPresent(message -> {
                plugin.getServices().getBossBarsService().createBossBar((Player) sender, message, this.color, this.ticksToRun);
            });
        }
    }

}
