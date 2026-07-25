package com.bgsoftware.superiorskyblock.core.messages.component;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBarsService;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BossBarComponent implements IMessageComponent {

    private static final LazyReference<BossBarsService> bossBarsService = new LazyReference<BossBarsService>() {
        @Override
        protected BossBarsService create() {
            return SuperiorSkyblockPlugin.getPlugin().getServices().getService(BossBarsService.class);
        }
    };

    private final MessageContent name;
    private final BossBar.Color color;
    private final BossBar.Style style;
    private final double duration;

    public static IMessageComponent of(@Nullable String name, BossBar.Color color, BossBar.Style style, int duration) {
        return duration <= 0 || Text.isBlank(name) ? EmptyMessageComponent.getInstance() : new BossBarComponent(name, color, style, duration);
    }

    private BossBarComponent(String name, BossBar.Color color, BossBar.Style style, double duration) {
        this.name = MessageContent.parse(name);
        this.color = color;
        this.style = style;
        this.duration = duration;
    }

    @Override
    public Type getType() {
        return Type.BOSS_BAR;
    }

    @Override
    public String getMessage() {
        return this.name.getContent(null).orElse("");
    }

    @Override
    public String getMessage(Object... args) {
        return this.name.getContent(null, args).orElse("");
    }

    @Override
    public void sendMessage(CommandSender sender, Object... args) {
        if (!(sender instanceof Player)) {
            return;
        }

        Player player = (Player) sender;
        this.name.getContent(player, args).ifPresent(message ->
                bossBarsService.get().createBossBar(player, message, this.color, this.style, this.duration));
    }
}
