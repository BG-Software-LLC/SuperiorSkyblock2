package com.bgsoftware.superiorskyblock.nms.v1_17_R1;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.bossbar.BossBarTask;
import com.bgsoftware.superiorskyblock.lang.PlayerLocales;
import com.bgsoftware.superiorskyblock.nms.NMSPlayers;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.level.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Optional;

public final class NMSPlayersImpl implements NMSPlayers {

    private static final ReflectMethod<Locale> PLAYER_LOCALE = new ReflectMethod<>(Player.class, "locale");
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public void clearInventory(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline() || offlinePlayer instanceof Player) {
            Player player = offlinePlayer instanceof Player ? (Player) offlinePlayer : offlinePlayer.getPlayer();
            assert player != null;
            player.getInventory().clear();
            player.getEnderChest().clear();
            return;
        }

        GameProfile profile = new GameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName());

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.getWorldServer(World.f);
        assert worldServer != null;
        EntityPlayer entity = new EntityPlayer(server, worldServer, profile);
        Player targetPlayer = entity.getBukkitEntity();

        targetPlayer.loadData();

        clearInventory(targetPlayer);

        //Setting the entity to the spawn location
        Location spawnLocation = plugin.getGrid().getSpawnIsland().getCenter(org.bukkit.World.Environment.NORMAL);
        entity.t = ((CraftWorld) spawnLocation.getWorld()).getHandle();
        entity.setPositionRotation(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch());

        targetPlayer.saveData();
    }

    @Override
    public void setSkinTexture(SuperiorPlayer superiorPlayer) {
        Player player = superiorPlayer.asPlayer();
        if (player != null) {
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            Optional<Property> optional = entityPlayer.getProfile().getProperties().get("textures").stream().findFirst();
            optional.ifPresent(property -> setSkinTexture(superiorPlayer, property));
        }
    }

    @Override
    public void setSkinTexture(SuperiorPlayer superiorPlayer, Property property) {
        superiorPlayer.setTextureValue(property.getValue());
    }

    @Override
    public void sendActionBar(Player player, String message) {
        // noinspection deprecation
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    @Override
    public BossBar createBossBar(Player player, String message, BossBar.Color color, double ticksToRun) {
        BossBarImpl bossBar = new BossBarImpl(message, BarColor.valueOf(color.name()), ticksToRun);
        bossBar.addPlayer(player);
        return bossBar;
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, duration, fadeOut);
    }

    @Override
    public boolean wasThrownByPlayer(Item item, Player player) {
        Entity entity = ((CraftItem) item).getHandle();
        return entity instanceof EntityItem && player.getUniqueId().equals(((EntityItem) entity).getThrower());
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

    private static final class BossBarImpl implements BossBar {

        private final org.bukkit.boss.BossBar bossBar;
        private final BossBarTask bossBarTask;

        public BossBarImpl(String message, BarColor color, double ticksToRun) {
            bossBar = Bukkit.createBossBar(message, color, BarStyle.SOLID);
            this.bossBarTask = new BossBarTask(this, ticksToRun);
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
