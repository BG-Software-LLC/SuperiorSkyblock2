package com.bgsoftware.superiorskyblock.core.zmenu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.IslandCreationLoader;
import com.bgsoftware.superiorskyblock.core.zmenu.loader.IslandSettingsLoader;
import fr.maxlego08.menu.api.ButtonManager;
import fr.maxlego08.menu.api.InventoryManager;
import fr.maxlego08.menu.exceptions.InventoryException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class ZMenuManager implements Listener {

    private final SuperiorSkyblockPlugin plugin;
    private final InventoryManager inventoryManager;
    private final ButtonManager buttonManager;
    private final Map<Player, PlayerCache> caches = new HashMap<>();

    public ZMenuManager(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.inventoryManager = getProvider(InventoryManager.class);
        this.buttonManager = getProvider(ButtonManager.class);
    }

    private <T> T getProvider(Class<T> classz) {
        RegisteredServiceProvider<T> provider = Bukkit.getServer().getServicesManager().getRegistration(classz);
        return provider == null ? null : provider.getProvider() != null ? provider.getProvider() : null;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        this.caches.remove(event.getPlayer());
    }

    public PlayerCache getCache(Player player) {
        return this.caches.computeIfAbsent(player, PlayerCache::new);
    }

    public void registerButtons() {

        this.buttonManager.register(new IslandCreationLoader(this.plugin));
        this.buttonManager.register(new IslandSettingsLoader(this.plugin));
    }

    public void loadInventories() {

        File folder = new File(plugin.getDataFolder(), "inventories");
        if (!folder.exists()) {
            folder.mkdirs();

        }
        // Save inventories files
        List<String> strings = Arrays.asList("island-creation", "settings");
        strings.forEach(inventoryName -> {
            if (!new File(plugin.getDataFolder(), "inventories/" + inventoryName + ".yml").exists()) {
                this.plugin.saveResource("inventories/" + inventoryName + ".yml", false);
            }
        });

        this.inventoryManager.deleteInventories(this.plugin);

        this.files(folder, file -> {
            try {
                this.inventoryManager.loadInventory(this.plugin, file);
            } catch (InventoryException exception) {
                exception.printStackTrace();
            }
        });
    }

    private void files(File folder, Consumer<File> consumer) {
        try (Stream<Path> s = Files.walk(Paths.get(folder.getPath()))) {
            s.skip(1).map(Path::toFile).filter(File::isFile).filter(e -> e.getName().endsWith(".yml")).forEach(consumer);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void cache(SuperiorPlayer superiorPlayer, Consumer<PlayerCache> consumer) {
        this.cache(superiorPlayer.asPlayer(), consumer);
    }

    public void cache(Player player, Consumer<PlayerCache> consumer) {
        consumer.accept(getCache(player));
    }

    public void openInventory(SuperiorPlayer superiorPlayer, String inventoryName, Consumer<PlayerCache> consumer) {
        this.openInventory(superiorPlayer.asPlayer(), inventoryName, consumer);
    }

    public void openInventory(Player player, String inventoryName, Consumer<PlayerCache> consumer) {
        this.cache(player, consumer);
        this.inventoryManager.openInventory(player, plugin, inventoryName);
    }

    public void openInventory(SuperiorPlayer superiorPlayer, String inventoryName) {
        this.openInventory(superiorPlayer.asPlayer(), inventoryName);
    }

    public void openInventory(Player player, String inventoryName) {
        this.inventoryManager.openInventory(player, plugin, inventoryName);
    }

    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }
}
