
package com.bgsoftware.superiorskyblock.core.messages.component.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBarsService;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.bgsoftware.superiorskyblock.core.messages.component.EmptyMessageComponent;
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

    private final MessageContent content;
    private final BossBar.Color color;
    private final BossBar.Style style;
    private final int ticksToRun;

    public static IMessageComponent of(@Nullable String message, BossBar.Color color, BossBar.Style style, int ticks) {
        return ticks <= 0 || Text.isBlank(message) ? EmptyMessageComponent.getInstance() : new BossBarComponent(message, color, style, ticks);
    }

    private BossBarComponent(String content, BossBar.Color color, BossBar.Style style, int ticks) {
        this.content = MessageContent.parse(content);
        this.color = color;
        this.style = style;
        this.ticksToRun = ticks;
    }

    @Override
    public Type getType() {
        return Type.BOSS_BAR;
    }

    @Override
    public String getMessage() {
        return this.content.getContent(null).orElse("");
    }

    @Override
    public String getMessage(Object... args) {
        return this.content.getContent(null, args).orElse("");
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        if (sender instanceof Player) {
            this.content.getContent((Player) sender, args).ifPresent(message ->
                    bossBarsService.get().createBossBar((Player) sender, message, this.color, this.style, this.ticksToRun));
        }
    }

}
