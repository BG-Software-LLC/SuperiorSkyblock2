package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.ProvidersManager;
import com.bgsoftware.superiorskyblock.api.hooks.AFKProvider;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.hooks.WorldsProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.AsyncProvider;
import com.bgsoftware.superiorskyblock.hooks.types.defaults.AsyncProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.types.defaults.BlocksProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.types.BlocksProvider_WildStacker;
import com.bgsoftware.superiorskyblock.hooks.types.CoreProtectHook;
import com.bgsoftware.superiorskyblock.hooks.types.EconomyProvider_Vault;
import com.bgsoftware.superiorskyblock.hooks.PermissionsProvider;
import com.bgsoftware.superiorskyblock.hooks.types.PermissionsProvider_LuckPerms;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.hooks.BlocksProvider;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.hooks.PricesProvider;
import com.bgsoftware.superiorskyblock.api.hooks.VanishProvider;
import com.bgsoftware.superiorskyblock.hooks.types.defaults.EconomyProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.types.defaults.PermissionsProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.types.defaults.WorldsProvider_Default;
import com.bgsoftware.superiorskyblock.listeners.PaperListener;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class ProvidersHandler extends AbstractHandler implements ProvidersManager {

    private final BigDecimal INVALID_WORTH = BigDecimal.valueOf(-1);
    private final BigDecimal MAX_DOUBLE = BigDecimal.valueOf(Double.MAX_VALUE);

    private SpawnersProvider spawnersProvider = new BlocksProvider_Default();
    private EconomyProvider economyProvider = new EconomyProvider_Default();
    private EconomyProvider bankEconomyProvider = new EconomyProvider_Default();
    private PermissionsProvider permissionsProvider = new PermissionsProvider_Default();
    private PricesProvider pricesProvider = itemStack -> INVALID_WORTH;
    private VanishProvider vanishProvider = player -> false;
    private AsyncProvider asyncProvider = new AsyncProvider_Default();
    private WorldsProvider worldsProvider;

    private final List<AFKProvider> AFKProvidersList = new ArrayList<>();

    public ProvidersHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
        this.worldsProvider = new WorldsProvider_Default(plugin);
    }

    @Override
    public void loadData(){
        Executor.sync(() -> {
            if(Bukkit.getPluginManager().isPluginEnabled("CoreProtect"))
                runSafe(() -> CoreProtectHook.register(plugin));

            if(this.spawnersProvider == null || spawnersProvider instanceof BlocksProvider) {
                String spawnersProvider = plugin.getSettings().spawnersProvider;
                boolean auto = spawnersProvider.equalsIgnoreCase("Auto");

                if (Bukkit.getPluginManager().isPluginEnabled("WildStacker") && (auto || spawnersProvider.equalsIgnoreCase("WildStacker"))) {
                    runSafe(() -> setSpawnersProvider(new BlocksProvider_WildStacker()));
                }
            }

            if(Bukkit.getPluginManager().isPluginEnabled("LuckPerms") && PermissionsProvider_LuckPerms.isCompatible()) {
                runSafe(() -> this.permissionsProvider = new PermissionsProvider_LuckPerms());
            }

            if(hasPaperAsyncSupport()){
                try {
                    asyncProvider = (AsyncProvider) Class.forName("com.bgsoftware.superiorskyblock.hooks.AsyncProvider_Paper").newInstance();
                    // Only added in versions 1.13+ of paper, so it can be here
                    Bukkit.getPluginManager().registerEvents(new PaperListener(plugin), plugin);
                    SuperiorSkyblockPlugin.log("Detected PaperSpigot - Using async chunk-loading support with PaperMC.");
                }catch (Exception ex){
                    SuperiorSkyblockPlugin.log("Detected PaperSpigot but failed to load async chunk-loading support...");
                    ex.printStackTrace();
                }
            }

            if(Bukkit.getPluginManager().isPluginEnabled("Vault")){
                boolean alreadyCheckedForVault = false;
                if(economyProvider instanceof EconomyProvider_Default && EconomyProvider_Vault.isCompatible()) {
                    setEconomyProvider(new EconomyProvider_Vault());
                    alreadyCheckedForVault = true;
                }
                if(bankEconomyProvider instanceof EconomyProvider_Default && (alreadyCheckedForVault || EconomyProvider_Vault.isCompatible())) {
                    setBankEconomyProvider(economyProvider);
                }
            }

            PlaceholderHook.register(plugin);
        });
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
        Preconditions.checkNotNull(spawnersProvider, "spawnersProvider parameter cannot be null.");
        this.spawnersProvider = spawnersProvider;
    }

    @Override
    public void setEconomyProvider(EconomyProvider economyProvider) {
        Preconditions.checkNotNull(economyProvider, "economyProvider parameter cannot be null.");
        this.economyProvider = economyProvider;
    }

    @Override
    public void setWorldsProvider(WorldsProvider worldsProvider) {
        Preconditions.checkNotNull(worldsProvider, "worldsProvider parameter cannot be null.");
        this.worldsProvider = worldsProvider;
    }

    @Override
    public void setBankEconomyProvider(EconomyProvider bankEconomyProvider) {
        Preconditions.checkNotNull(bankEconomyProvider, "bankEconomyProvider parameter cannot be null.");
        this.bankEconomyProvider = bankEconomyProvider;
    }

    @Override
    public void addAFKProvider(AFKProvider afkProvider) {
        Preconditions.checkNotNull(afkProvider, "afkProvider parameter cannot be null.");
        AFKProvidersList.add(afkProvider);
    }

    @Override
    public void setVanishProvider(VanishProvider vanishProvider) {
        Preconditions.checkNotNull(vanishProvider, "vanishProvider parameter cannot be null.");
        this.vanishProvider = vanishProvider;
    }

    public Pair<Integer, String> getSpawner(Location location){
        return spawnersProvider.getSpawner(location);
    }

    public Key getSpawnerKey(ItemStack itemStack){
        return spawnersProvider != null ? Key.of(Materials.SPAWNER.toBukkitType() + ":" + spawnersProvider.getSpawnerType(itemStack)) : Key.of(itemStack);
    }

    public Set<Pair<com.bgsoftware.superiorskyblock.api.key.Key, Integer>> getBlocks(ChunkPosition chunkPosition){
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

    public BigDecimal getBalance(SuperiorPlayer superiorPlayer){
        return economyProvider.getBalance(superiorPlayer);
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

    public boolean hasCustomWorldsSupport(){
        return !(worldsProvider instanceof WorldsProvider_Default);
    }

    public BigDecimal getBalanceForBanks(SuperiorPlayer superiorPlayer){
        return bankEconomyProvider.getBalance(superiorPlayer);
    }

    public String depositMoneyForBanks(SuperiorPlayer superiorPlayer, BigDecimal amount){
        while(amount.compareTo(MAX_DOUBLE) > 0){
            String error = bankEconomyProvider.depositMoney(superiorPlayer, Double.MAX_VALUE);
            if(error != null && !error.isEmpty())
                return error;

            amount = amount.subtract(MAX_DOUBLE);
        }

        return bankEconomyProvider.depositMoney(superiorPlayer, amount.doubleValue());
    }

    public String withdrawMoneyForBanks(SuperiorPlayer superiorPlayer, BigDecimal amount){
        while(amount.compareTo(MAX_DOUBLE) > 0){
            String error = withdrawMoneyForBanks(superiorPlayer, Double.MAX_VALUE);
            if(error != null && !error.isEmpty())
                return error;

            amount = amount.subtract(MAX_DOUBLE);
        }

        return withdrawMoneyForBanks(superiorPlayer, amount.doubleValue());
    }

    public String withdrawMoneyForBanks(SuperiorPlayer superiorPlayer, double amount){
        return bankEconomyProvider.withdrawMoney(superiorPlayer, amount);
    }

    public boolean isAFK(Player player){
        return AFKProvidersList.stream().anyMatch(afkProvider -> afkProvider.isAFK(player));
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
