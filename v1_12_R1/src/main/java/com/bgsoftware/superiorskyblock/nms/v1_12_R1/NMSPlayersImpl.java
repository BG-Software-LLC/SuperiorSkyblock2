package com.bgsoftware.superiorskyblock.nms.v1_12_R1;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.nms.NMSPlayers;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityItem;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.PlayerInteractManager;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

public final class NMSPlayersImpl implements NMSPlayers {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public void clearInventory(OfflinePlayer offlinePlayer) {
        if (offlinePlayer.isOnline() || offlinePlayer instanceof Player) {
            Player player = offlinePlayer instanceof Player ? (Player) offlinePlayer : offlinePlayer.getPlayer();
            player.getInventory().clear();
            player.getEnderChest().clear();
            return;
        }

        GameProfile profile = new GameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName());

        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.getWorldServer(0);
        EntityPlayer entity = new EntityPlayer(server, worldServer, profile, new PlayerInteractManager(worldServer));
        Player targetPlayer = entity.getBukkitEntity();

        targetPlayer.loadData();

        clearInventory(targetPlayer);

        //Setting the entity to the spawn location
        Location spawnLocation = plugin.getGrid().getSpawnIsland().getCenter(org.bukkit.World.Environment.NORMAL);
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
        //noinspection deprecation
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut) {
        player.sendTitle(title, subtitle, fadeIn, duration, fadeOut);
    }

    @Override
    public boolean wasThrownByPlayer(org.bukkit.entity.Item item, Player player) {
        Entity entity = ((CraftItem) item).getHandle();
        return entity instanceof EntityItem && player.getName().equals(((EntityItem) entity).n());
    }

}
