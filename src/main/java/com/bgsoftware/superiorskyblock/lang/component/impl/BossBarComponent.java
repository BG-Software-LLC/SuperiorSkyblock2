
package com.bgsoftware.superiorskyblock.lang.component.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.bossbar.BossBarTask;
import com.bgsoftware.superiorskyblock.lang.component.EmptyMessageComponent;
import com.bgsoftware.superiorskyblock.lang.component.IMessageComponent;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

public final class BossBarComponent implements IMessageComponent {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final String message;
    private final BossBar.Color color;
    private final int millisecondsToRun;

    public static IMessageComponent of(@Nullable String message, BossBar.Color color, int ticks) {
        return ticks <= 0 || Strings.isBlank(message) ? EmptyMessageComponent.getInstance() : new BossBarComponent(message, color, ticks);
    }

    private BossBarComponent(String message, BossBar.Color color, int ticks) {
        this.message = message;
        this.color = color;
        this.millisecondsToRun = ticks * 50;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public void sendMessage(CommandSender sender, Object... objects) {
        if (sender instanceof Player) {
            IMessageComponent.replaceArgs(this.message, objects).ifPresent(message -> {
                BossBar bossBar = plugin.getNMSPlayers().createBossBar((Player) sender, message, this.color);
                BossBarTask.of(bossBar, this.millisecondsToRun, TimeUnit.MILLISECONDS);
            });
        }
    }

}
