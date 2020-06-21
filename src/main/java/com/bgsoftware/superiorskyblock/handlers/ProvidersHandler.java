package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.ProvidersManager;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_EpicSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_PvpingSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_SilkSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_WildStacker;
import com.bgsoftware.superiorskyblock.hooks.SlimefunHook;
import com.bgsoftware.superiorskyblock.hooks.ChangeSkinHook;
import com.bgsoftware.superiorskyblock.hooks.JetsMinionsHook;
import com.bgsoftware.superiorskyblock.hooks.LeaderHeadsHook;
import com.bgsoftware.superiorskyblock.hooks.PermissionsProvider;
import com.bgsoftware.superiorskyblock.hooks.PermissionsProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.PermissionsProvider_LuckPerms;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_MergedSpawner;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.hooks.PricesProvider;
import com.bgsoftware.superiorskyblock.hooks.PricesProvider_ShopGUIPlus;
import com.bgsoftware.superiorskyblock.hooks.SkinsRestorerHook;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;

public final class ProvidersHandler implements ProvidersManager {

    private final BigDecimal INVALID_WORTH = BigDecimal.valueOf(-1);

    private SpawnersProvider spawnersProvider;
    private PermissionsProvider permissionsProvider;
    private PricesProvider pricesProvider;

    public ProvidersHandler(SuperiorSkyblockPlugin plugin){
        Executor.sync(() -> {
            if(Bukkit.getPluginManager().isPluginEnabled("LeaderHeads"))
                LeaderHeadsHook.register();

            if(Bukkit.getPluginManager().isPluginEnabled("JetsMinions"))
                JetsMinionsHook.register(plugin);

            if(Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer"))
                SkinsRestorerHook.register(plugin);

            if(Bukkit.getPluginManager().isPluginEnabled("ChangeSkin"))
                ChangeSkinHook.register(plugin);

            if(Bukkit.getPluginManager().isPluginEnabled("Slimefun"))
                SlimefunHook.register(plugin);

            if(this.spawnersProvider == null || spawnersProvider instanceof BlocksProvider) {
                String spawnersProvider = plugin.getSettings().spawnersProvider;

                if (Bukkit.getPluginManager().isPluginEnabled("MergedSpawner") &&
                        (spawnersProvider.equalsIgnoreCase("MergedSpawner") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                    setSpawnersProvider(new BlocksProvider_MergedSpawner());
                } else if (Bukkit.getPluginManager().isPluginEnabled("WildStacker") &&
                        (spawnersProvider.equalsIgnoreCase("WildStacker") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                    setSpawnersProvider(new BlocksProvider_WildStacker());
                } else if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") &&
                        Bukkit.getPluginManager().getPlugin("SilkSpawners").getDescription().getAuthors().contains("CandC_9_12") &&
                        (spawnersProvider.equalsIgnoreCase("SilkSpawners") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                    setSpawnersProvider(new BlocksProvider_SilkSpawners());
                } else if (Bukkit.getPluginManager().isPluginEnabled("PvpingSpawners") &&
                        (spawnersProvider.equalsIgnoreCase("PvpingSpawners") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                    setSpawnersProvider(new BlocksProvider_PvpingSpawners());
                } else if (Bukkit.getPluginManager().isPluginEnabled("EpicSpawners") &&
                        (spawnersProvider.equalsIgnoreCase("EpicSpawners") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                    setSpawnersProvider(new BlocksProvider_EpicSpawners());
                } else {
                    setSpawnersProvider(new BlocksProvider_Default());
                }
            }

            if(Bukkit.getPluginManager().isPluginEnabled("LuckPerms"))
                permissionsProvider = new PermissionsProvider_LuckPerms();
            else
                permissionsProvider = new PermissionsProvider_Default();

            if(Bukkit.getPluginManager().isPluginEnabled("ShopGUIPlus"))
                pricesProvider = new PricesProvider_ShopGUIPlus();
            else
                pricesProvider = itemStack -> INVALID_WORTH;
        });

        PlaceholderHook.register(plugin);
    }

    @Override
    public void setSpawnersProvider(SpawnersProvider spawnersProvider){
        Preconditions.checkArgument(spawnersProvider != null, "SpawnersProvider cannot be null.");
        this.spawnersProvider = spawnersProvider;
    }

    public Pair<Integer, String> getSpawner(Location location){
        return spawnersProvider.getSpawner(location);
    }

    public Key getSpawnerKey(ItemStack itemStack){
        return spawnersProvider != null ? Key.of(Materials.SPAWNER.toBukkitType() + ":" + spawnersProvider.getSpawnerType(itemStack)) : Key.of(itemStack);
    }

    public Pair<Integer, ItemStack> getBlock(Location location){
        return spawnersProvider instanceof BlocksProvider ? ((BlocksProvider) spawnersProvider).getBlock(location) : null;
    }

    public boolean isWildStacker(){
        return spawnersProvider instanceof BlocksProvider_WildStacker;
    }

    public boolean hasPermission(Player player, String permission){
        return permissionsProvider.hasPermission(player, permission.toLowerCase());
    }

    public BigDecimal getPrice(Key key){
        return pricesProvider.getPrice(key);
    }

}
