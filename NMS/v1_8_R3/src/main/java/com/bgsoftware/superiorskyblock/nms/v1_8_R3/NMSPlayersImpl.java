package com.bgsoftware.superiorskyblock.nms.v1_8_R3;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.nms.NMSPlayers;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.bgsoftware.superiorskyblock.service.bossbar.EmptyBossBar;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityItem;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftItem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Optional;

public class NMSPlayersImpl implements NMSPlayers {

    private final SuperiorSkyblockPlugin plugin;

    public NMSPlayersImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void clearInventory(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline() || offlinePlayer instanceof Player) {
            Player player = offlinePlayer instanceof Player ? (Player) offlinePlayer : offlinePlayer.getPlayer();
            player.getInventory().clear();
            player.getEnderChest().clear();
            return;
        }

        GameProfile profile = new GameProfile(offlinePlayer.getUniqueId(), Optional.ofNullable(offlinePlayer.getName()).orElse(""));

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.getWorldServer(0);
        EntityPlayer entity = new EntityPlayer(server, worldServer, profile, new PlayerInteractManager(worldServer));
        Player targetPlayer = entity.getBukkitEntity();

        targetPlayer.loadData();

        clearInventory(targetPlayer);

        //Setting the entity to the spawn location
        Location spawnLocation = plugin.getGrid().getSpawnIsland().getCenter(Dimensions.NORMAL);
        entity.world = ((CraftWorld) spawnLocation.getWorld()).getHandle();
        entity.setPositionRotation(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ(), spawnLocation.getYaw(), spawnLocation.getPitch());

        targetPlayer.saveData();
    }

    @Override
    public void setSkinTexture(SuperiorPlayer superiorPlayer) {
        superiorPlayer.runIfOnline(player -> {
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            Optional<Property> optional = entityPlayer.getProfile().getProperties().get("textures").stream().findFirst();
            optional.ifPresent(property -> setSkinTexture(superiorPlayer, property));
        });
    }

    @Override
    public void setSkinTexture(SuperiorPlayer superiorPlayer, Property property) {
        superiorPlayer.setTextureValue(property.getValue());
    }

    @Override
    public void sendActionBar(Player player, String message) {
        PacketPlayOutChat packetPlayOutChat = new PacketPlayOutChat(CraftChatMessage.fromString(message)[0], (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutChat);
    }

    @Override
    public BossBar createBossBar(Player player, String message, BossBar.Color color, double ticksToRun) {
        return EmptyBossBar.getInstance();
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        PlayerConnection playerConnection = ((CraftPlayer) player).getHandle().playerConnection;

        PacketPlayOutTitle times;
        if (title != null) {
            times = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, CraftChatMessage.fromString(title)[0]);
            playerConnection.sendPacket(times);
        }

        if (subtitle != null) {
            times = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, CraftChatMessage.fromString(subtitle)[0]);
            playerConnection.sendPacket(times);
        }

        times = new PacketPlayOutTitle(fadeIn, duration, fadeOut);
        playerConnection.sendPacket(times);
    }

    @Override
    public boolean wasThrownByPlayer(org.bukkit.entity.Item item, SuperiorPlayer superiorPlayer) {
        Entity entity = ((CraftItem) item).getHandle();
        return entity instanceof EntityItem && superiorPlayer.getName().equals(((EntityItem) entity).n());
    }

    @Nullable
    @Override
    public Locale getPlayerLocale(Player player) {
        try {
            return PlayerLocales.getLocale(player.spigot().getLocale());
        } catch (IllegalArgumentException error) {
            return null;
        }
    }

}
