package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.ProvidersManager;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.hooks.WorldsProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.AsyncProvider;
import com.bgsoftware.superiorskyblock.hooks.AsyncProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_AdvancedSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_EpicSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_PvpingSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_SilkSpawners;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_UltimateStacker;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider_WildStacker;
import com.bgsoftware.superiorskyblock.hooks.EconomyProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.EconomyProvider_Vault;
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
import com.bgsoftware.superiorskyblock.hooks.VanishProvider;
import com.bgsoftware.superiorskyblock.hooks.VanishProvider_CMI;
import com.bgsoftware.superiorskyblock.hooks.VanishProvider_Essentials;
import com.bgsoftware.superiorskyblock.hooks.VanishProvider_SuperVanish;
import com.bgsoftware.superiorskyblock.hooks.VanishProvider_VanishNoPacket;
import com.bgsoftware.superiorskyblock.hooks.WorldsProvider_Default;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class ProvidersHandler extends AbstractHandler implements ProvidersManager {

    private final BigDecimal INVALID_WORTH = BigDecimal.valueOf(-1);
    private final BigDecimal MAX_DOUBLE = BigDecimal.valueOf(Double.MAX_VALUE);

    private SpawnersProvider spawnersProvider = new BlocksProvider_Default();
    private EconomyProvider economyProvider = new EconomyProvider_Default();
    private PermissionsProvider permissionsProvider = new PermissionsProvider_Default();
    private PricesProvider pricesProvider = itemStack -> INVALID_WORTH;
    private VanishProvider vanishProvider = player -> false;
    private AsyncProvider asyncProvider = new AsyncProvider_Default();
    private WorldsProvider worldsProvider;

    public ProvidersHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
        this.worldsProvider = new WorldsProvider_Default(plugin);
    }

    @Override
    public void loadData(){
        Executor.sync(() -> {
            if(Bukkit.getPluginManager().isPluginEnabled("LeaderHeads"))
                runSafe(LeaderHeadsHook::register);

            if(Bukkit.getPluginManager().isPluginEnabled("JetsMinions"))
                runSafe(() -> JetsMinionsHook.register(plugin));

            if(Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer"))
                runSafe(() -> SkinsRestorerHook.register(plugin));

            if(Bukkit.getPluginManager().isPluginEnabled("ChangeSkin"))
                runSafe(() -> ChangeSkinHook.register(plugin));

            if(Bukkit.getPluginManager().isPluginEnabled("Slimefun"))
                runSafe(() -> SlimefunHook.register(plugin));

            if(this.spawnersProvider == null || spawnersProvider instanceof BlocksProvider) {
                String spawnersProvider = plugin.getSettings().spawnersProvider;

                if (Bukkit.getPluginManager().isPluginEnabled("MergedSpawner") &&
                        (spawnersProvider.equalsIgnoreCase("MergedSpawner") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                    runSafe(() -> setSpawnersProvider(new BlocksProvider_MergedSpawner()));
                } else if (Bukkit.getPluginManager().isPluginEnabled("AdvancedSpawners") &&
                        (spawnersProvider.equalsIgnoreCase("AdvancedSpawners") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                    runSafe(() -> setSpawnersProvider(new BlocksProvider_AdvancedSpawners()));
                } else if (Bukkit.getPluginManager().isPluginEnabled("WildStacker") &&
                        (spawnersProvider.equalsIgnoreCase("WildStacker") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                    runSafe(() -> setSpawnersProvider(new BlocksProvider_WildStacker()));
                } else if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") &&
                        Bukkit.getPluginManager().getPlugin("SilkSpawners").getDescription().getAuthors().contains("CandC_9_12") &&
                        (spawnersProvider.equalsIgnoreCase("SilkSpawners") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                    runSafe(() -> setSpawnersProvider(new BlocksProvider_SilkSpawners()));
                } else if (Bukkit.getPluginManager().isPluginEnabled("PvpingSpawners") &&
                        (spawnersProvider.equalsIgnoreCase("PvpingSpawners") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                    runSafe(() -> setSpawnersProvider(new BlocksProvider_PvpingSpawners()));
                } else if (Bukkit.getPluginManager().isPluginEnabled("EpicSpawners") &&
                        (spawnersProvider.equalsIgnoreCase("EpicSpawners") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                    runSafe(() -> setSpawnersProvider(new BlocksProvider_EpicSpawners()));
                } else if (Bukkit.getPluginManager().isPluginEnabled("UltimateStacker") &&
                        (spawnersProvider.equalsIgnoreCase("UltimateStacker") || spawnersProvider.equalsIgnoreCase("Auto"))) {
                    runSafe(() -> setSpawnersProvider(new BlocksProvider_UltimateStacker()));
                }
            }

            if(Bukkit.getPluginManager().isPluginEnabled("LuckPerms"))
                runSafe(() -> permissionsProvider = new PermissionsProvider_LuckPerms());

            if(Bukkit.getPluginManager().isPluginEnabled("ShopGUIPlus"))
                runSafe(() -> pricesProvider = new PricesProvider_ShopGUIPlus());

            if(Bukkit.getPluginManager().isPluginEnabled("VanishNoPacket"))
                runSafe(() -> vanishProvider = new VanishProvider_VanishNoPacket());
            else if(Bukkit.getPluginManager().isPluginEnabled("SuperVanish") ||
                    Bukkit.getPluginManager().isPluginEnabled("PremiumVanish"))
                runSafe(() -> vanishProvider = new VanishProvider_SuperVanish());
            else if(Bukkit.getPluginManager().isPluginEnabled("Essentials"))
                runSafe(() -> vanishProvider = new VanishProvider_Essentials());
            else if(Bukkit.getPluginManager().isPluginEnabled("CMI"))
                runSafe(() -> vanishProvider = new VanishProvider_CMI());

            if(hasPaperAsyncSupport()){
                try {
                    asyncProvider = (AsyncProvider) Class.forName("com.bgsoftware.superiorskyblock.hooks.AsyncProvider_Paper").newInstance();
                    SuperiorSkyblockPlugin.log("Detected PaperSpigot - Using async chunk-loading support with PaperMC.");
                }catch (Exception ex){
                    SuperiorSkyblockPlugin.log("Detected PaperSpigot but failed to load async chunk-loading support...");
                    ex.printStackTrace();
                }
            }

            try{
                setEconomyProvider(new EconomyProvider_Vault());
            }catch (Exception ignored){}
        });

        PlaceholderHook.register(plugin);
    }

    private void runSafe(Runnable runnable){
        try{
            runnable.run();
        }catch (Throwable ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void setSpawnersProvider(SpawnersProvider spawnersProvider){
        Preconditions.checkArgument(spawnersProvider != null, "SpawnersProvider cannot be null.");
        this.spawnersProvider = spawnersProvider;
    }

    @Override
    public void setEconomyProvider(EconomyProvider economyProvider) {
        Preconditions.checkArgument(economyProvider != null, "EconomyProvider cannot be null.");
        this.economyProvider = economyProvider;
    }

    @Override
    public void setWorldsProvider(WorldsProvider worldsProvider) {
        this.worldsProvider = worldsProvider;
    }

    public Pair<Integer, String> getSpawner(Location location){
        return spawnersProvider.getSpawner(location);
    }

    public Key getSpawnerKey(ItemStack itemStack){
        return spawnersProvider != null ? Key.of(Materials.SPAWNER.toBukkitType() + ":" + spawnersProvider.getSpawnerType(itemStack)) : Key.of(itemStack);
    }

    public Set<Pair<Integer, com.bgsoftware.superiorskyblock.api.key.Key>> getBlocks(ChunkPosition chunkPosition){
        return spawnersProvider instanceof BlocksProvider ? ((BlocksProvider) spawnersProvider).getBlocks(chunkPosition) : Collections.emptySet();
    }

    public boolean isWildStacker(){
        return spawnersProvider instanceof BlocksProvider_WildStacker;
    }

    public boolean hasPermission(Player player, String permission){
        return permissionsProvider.hasPermission(player, permission.toLowerCase());
    }

    public BigDecimal getPrice(Key key){
        return pricesProvider.getPrice(key.getSubKey().isEmpty() ? Key.of(key.getGlobalKey() + ":0") : key);
    }

    public boolean isVanished(Player player){
        return vanishProvider.isVanished(player);
    }

    public void loadChunk(ChunkPosition chunkPosition, Consumer<Chunk> chunkResult){
        asyncProvider.loadChunk(chunkPosition, chunkResult);
    }

    public void teleport(Entity entity, Location location){
        asyncProvider.teleport(entity, location);
    }

    public void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult){
        asyncProvider.teleport(entity, location, teleportResult);
    }

    public boolean hasEconomySupport(){
        return economyProvider.isEnabled();
    }

    public double getMoneyInBank(SuperiorPlayer superiorPlayer){
        return economyProvider.getMoneyInBank(superiorPlayer);
    }

    public String depositMoney(SuperiorPlayer superiorPlayer, BigDecimal amount){
        while(amount.compareTo(MAX_DOUBLE) > 0){
            String error = economyProvider.depositMoney(superiorPlayer, Double.MAX_VALUE);
            if(error != null && !error.isEmpty())
                return error;

            amount = amount.subtract(MAX_DOUBLE);
        }

        return economyProvider.depositMoney(superiorPlayer, amount.doubleValue());
    }

    public String withdrawMoney(SuperiorPlayer superiorPlayer, BigDecimal amount){
        while(amount.compareTo(MAX_DOUBLE) > 0){
            String error = withdrawMoney(superiorPlayer, Double.MAX_VALUE);
            if(error != null && !error.isEmpty())
                return error;

            amount = amount.subtract(MAX_DOUBLE);
        }

        return withdrawMoney(superiorPlayer, amount.doubleValue());
    }

    public String withdrawMoney(SuperiorPlayer superiorPlayer, double amount){
        return economyProvider.withdrawMoney(superiorPlayer, amount);
    }

    public void prepareWorlds(){
        worldsProvider.prepareWorlds();
    }

    public World getIslandsWorld(Island island, World.Environment environment){
        return worldsProvider.getIslandsWorld(island, environment);
    }

    public boolean isIslandsWorld(World world){
        return worldsProvider.isIslandsWorld(world);
    }

    public Location getNextLocation(Location previousLocation, int islandsHeight, int maxIslandSize, UUID islandOwner, UUID islandUUID){
        return worldsProvider.getNextLocation(previousLocation, islandsHeight, maxIslandSize, islandOwner, islandUUID);
    }

    public void finishIslandCreation(Location islandLocation, UUID islandOwner, UUID islandUUID){
        worldsProvider.finishIslandCreation(islandLocation, islandOwner, islandUUID);
    }

    public void prepareTeleport(Island island, Location location, Runnable finishCallback){
        worldsProvider.prepareTeleport(island, location, finishCallback);
    }

    public boolean isNetherEnabled(){
        return worldsProvider.isNetherEnabled();
    }

    public boolean isNetherUnlocked(){
        return worldsProvider.isNetherUnlocked();
    }

    public boolean isEndEnabled(){
        return worldsProvider.isEndEnabled();
    }

    public boolean isEndUnlocked(){
        return worldsProvider.isEndUnlocked();
    }

    private static boolean hasPaperAsyncSupport(){
        try{
            //noinspection JavaReflectionMemberAccess
            World.class.getMethod("getChunkAtAsync", int.class, int.class);
            return true;
        }catch (Throwable ex){
            return false;
        }
    }

}
