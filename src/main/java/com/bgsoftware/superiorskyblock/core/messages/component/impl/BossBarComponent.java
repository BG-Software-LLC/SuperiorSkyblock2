
package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBarsService;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BossBarComponent implements IMessageComponent {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<BossBarsService> bossBarsService = new LazyReference<BossBarsService>() {
        @Override
        protected BossBarsService create() {
            return plugin.getServices().getService(BossBarsService.class);
        }
    };

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
                bossBarsService.get().createBossBar((Player) sender, message, this.color, this.ticksToRun);
            });
        }
    }

}
