package com.bgsoftware.superiorskyblock.nms.v1_20_2;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.nms.NMSPlayers;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.bgsoftware.superiorskyblock.service.bossbar.BossBarTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;

public class NMSPlayersImpl implements NMSPlayers {

    private static final ReflectMethod<Locale> PLAYER_LOCALE = new ReflectMethod<>(ServerPlayer.class, "locale");

    private final SuperiorSkyblockPlugin plugin;

    public NMSPlayersImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void clearInventory(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline() || offlinePlayer instanceof Player) {
            Player player = offlinePlayer instanceof Player ? (Player) offlinePlayer : offlinePlayer.getPlayer();
            assert player != null;
            player.getInventory().clear();
            player.getEnderChest().clear();
            return;
        }

        GameProfile profile = new GameProfile(offlinePlayer.getUniqueId(), Optional.ofNullable(offlinePlayer.getName()).orElse(""));

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel serverLevel = server.getLevel(Level.OVERWORLD);

        if (serverLevel == null)
            return;

        ServerPlayer serverPlayer = new ServerPlayer(server, serverLevel, profile, ClientInformation.createDefault());
        Player targetPlayer = serverPlayer.getBukkitEntity();

        targetPlayer.loadData();

        clearInventory(targetPlayer);

        //Setting the entity to the spawn location
        Location spawnLocation = plugin.getGrid().getSpawnIsland().getCenter(org.bukkit.World.Environment.NORMAL);

        if (spawnLocation != null) {
            serverPlayer.setLevel(serverLevel);
            serverPlayer.absMoveTo(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(),
                    spawnLocation.getYaw(), spawnLocation.getPitch());
        }

        targetPlayer.saveData();
    }

    @Override
    public void setSkinTexture(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();

        if (player == null)
            return;

        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.getGameProfile().getProperties().get("textures").stream().findFirst()
                .ifPresent(property -> setSkinTexture(superiorPlayer, property));
    }

    @Override
    public void setSkinTexture(SuperiorPlayer superiorPlayer, Property property) {
        superiorPlayer.setTextureValue(property.value());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    @Override
    public BossBar createBossBar(Player player, String message, BossBar.Color color, double ticksToRun) {
        BossBarImpl bossBar = new BossBarImpl(message, BarColor.valueOf(color.name()), ticksToRun);
        bossBar.addPlayer(player);
        return bossBar;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, duration, fadeOut);
    }

    @Override
    public boolean wasThrownByPlayer(Item item, SuperiorPlayer superiorPlayer) {
        return superiorPlayer.getUniqueId().equals(item.getThrower());
    }

    @Nullable
    @Override
    public Locale getPlayerLocale(Player player) {
        if (PLAYER_LOCALE.isValid()) {
            return player.locale();
        } else try {
            //noinspection deprecation
            return PlayerLocales.getLocale(player.getLocale());
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

    private static class BossBarImpl implements BossBar {

        private final org.bukkit.boss.BossBar bossBar;
        private final BossBarTask bossBarTask;

        public BossBarImpl(String message, BarColor color, double ticksToRun) {
            bossBar = Bukkit.createBossBar(message, color, BarStyle.SOLID);
            this.bossBarTask = BossBarTask.create(this, ticksToRun);
        }

        @Override
        public void addPlayer(Player player) {
            this.bossBar.addPlayer(player);
            this.bossBarTask.registerTask(player);
        }

        @Override
        public void removeAll() {
            this.bossBar.removeAll();
            this.bossBar.getPlayers().forEach(this.bossBarTask::unregisterTask);
        }

        @Override
        public void setProgress(double progress) {
            this.bossBar.setProgress(progress);
        }

        @Override
        public double getProgress() {
            return this.bossBar.getProgress();
        }

    }

}
