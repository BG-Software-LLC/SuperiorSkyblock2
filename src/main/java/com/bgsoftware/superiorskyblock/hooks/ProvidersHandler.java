package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.ProvidersManager;
import com.bgsoftware.superiorskyblock.api.hooks.AFKProvider;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.hooks.MenusProvider;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersSnapshotProvider;
import com.bgsoftware.superiorskyblock.api.hooks.StackedBlocksProvider;
import com.bgsoftware.superiorskyblock.api.hooks.StackedBlocksSnapshotProvider;
import com.bgsoftware.superiorskyblock.api.hooks.WorldsProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handler.AbstractHandler;
import com.bgsoftware.superiorskyblock.hooks.provider.AFKProvider_CMI;
import com.bgsoftware.superiorskyblock.hooks.provider.AFKProvider_Essentials;
import com.bgsoftware.superiorskyblock.hooks.provider.AsyncProvider;
import com.bgsoftware.superiorskyblock.hooks.provider.AsyncProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.MenusProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.PricesProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.support.ChangeSkinHook;
import com.bgsoftware.superiorskyblock.hooks.support.CoreProtectHook;
import com.bgsoftware.superiorskyblock.hooks.provider.EconomyProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.EconomyProvider_Vault;
import com.bgsoftware.superiorskyblock.hooks.support.JetsMinionsHook;
import com.bgsoftware.superiorskyblock.hooks.support.LeaderHeadsHook;
import com.bgsoftware.superiorskyblock.hooks.provider.PermissionsProvider;
import com.bgsoftware.superiorskyblock.hooks.provider.PermissionsProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.PermissionsProvider_LuckPerms;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import com.bgsoftware.superiorskyblock.hooks.provider.PricesProvider;
import com.bgsoftware.superiorskyblock.hooks.provider.PricesProvider_ShopGUIPlus;
import com.bgsoftware.superiorskyblock.hooks.support.SWMHook;
import com.bgsoftware.superiorskyblock.hooks.support.SkinsRestorerHook;
import com.bgsoftware.superiorskyblock.hooks.support.SlimefunHook;
import com.bgsoftware.superiorskyblock.hooks.provider.SpawnersProvider_AdvancedSpawners;
import com.bgsoftware.superiorskyblock.hooks.provider.SpawnersProvider_AutoDetect;
import com.bgsoftware.superiorskyblock.hooks.provider.SpawnersProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.SpawnersProvider_EpicSpawners;
import com.bgsoftware.superiorskyblock.hooks.provider.SpawnersProvider_MergedSpawner;
import com.bgsoftware.superiorskyblock.hooks.provider.SpawnersProvider_PvpingSpawners;
import com.bgsoftware.superiorskyblock.hooks.provider.SpawnersProvider_RoseStacker;
import com.bgsoftware.superiorskyblock.hooks.provider.SpawnersProvider_SilkSpawners;
import com.bgsoftware.superiorskyblock.hooks.provider.SpawnersProvider_UltimateStacker;
import com.bgsoftware.superiorskyblock.hooks.provider.SpawnersProvider_WildStacker;
import com.bgsoftware.superiorskyblock.hooks.provider.StackedBlocksProvider_AutoDetect;
import com.bgsoftware.superiorskyblock.hooks.provider.StackedBlocksProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.StackedBlocksProvider_RoseStacker;
import com.bgsoftware.superiorskyblock.hooks.provider.StackedBlocksProvider_WildStacker;
import com.bgsoftware.superiorskyblock.hooks.provider.VanishProvider;
import com.bgsoftware.superiorskyblock.hooks.provider.VanishProvider_CMI;
import com.bgsoftware.superiorskyblock.hooks.provider.VanishProvider_Essentials;
import com.bgsoftware.superiorskyblock.hooks.provider.VanishProvider_SuperVanish;
import com.bgsoftware.superiorskyblock.hooks.provider.VanishProvider_VanishNoPacket;
import com.bgsoftware.superiorskyblock.hooks.provider.WorldsProvider_Default;
import com.bgsoftware.superiorskyblock.listeners.PaperListener;
import com.bgsoftware.superiorskyblock.utils.chunks.ChunkPosition;
import com.bgsoftware.superiorskyblock.key.Key;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class ProvidersHandler extends AbstractHandler implements ProvidersManager {

    private final BigDecimal MAX_DOUBLE = BigDecimal.valueOf(Double.MAX_VALUE);

    private SpawnersProvider spawnersProvider = new SpawnersProvider_Default();
    private StackedBlocksProvider stackedBlocksProvider = new StackedBlocksProvider_Default();
    private EconomyProvider economyProvider = new EconomyProvider_Default();
    private EconomyProvider bankEconomyProvider = new EconomyProvider_Default();
    private PermissionsProvider permissionsProvider = new PermissionsProvider_Default();
    private PricesProvider pricesProvider = new PricesProvider_Default();
    private VanishProvider vanishProvider = player -> false;
    private AsyncProvider asyncProvider = new AsyncProvider_Default();
    private WorldsProvider worldsProvider;
    private MenusProvider menusProvider;

    private final List<AFKProvider> AFKProvidersList = new ArrayList<>();

    private boolean listenToSpawnerChanges = true;

    public ProvidersHandler(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        this.worldsProvider = new WorldsProvider_Default(plugin);
        this.menusProvider = new MenusProvider_Default(plugin);
    }

    @Override
    public void loadData() {
        Executor.sync(() -> {
            registerGeneralHooks();
            registerSpawnersProvider();
            registerStackedBlocksProvider();
            registerPermissionsProvider();
            registerPricesProvider();
            registerVanishProvider();
            registerAFKProvider();
            registerAsyncProvider();
            registerEconomyProviders();
        });
    }

    @Override
    public void setSpawnersProvider(SpawnersProvider spawnersProvider) {
        Preconditions.checkNotNull(spawnersProvider, "spawnersProvider parameter cannot be null.");
        this.spawnersProvider = spawnersProvider;
    }

    @Override
    public SpawnersProvider getSpawnersProvider() {
        return this.spawnersProvider;
    }

    @Override
    public void setStackedBlocksProvider(StackedBlocksProvider stackedBlocksProvider) {
        Preconditions.checkNotNull(stackedBlocksProvider, "stackedBlocksProvider parameter cannot be null.");
        this.stackedBlocksProvider = stackedBlocksProvider;
    }

    @Override
    public StackedBlocksProvider getStackedBlocksProvider() {
        return this.stackedBlocksProvider;
    }

    @Override
    public void setEconomyProvider(EconomyProvider economyProvider) {
        Preconditions.checkNotNull(economyProvider, "economyProvider parameter cannot be null.");
        this.economyProvider = economyProvider;
    }

    @Override
    public EconomyProvider getEconomyProvider() {
        return this.economyProvider;
    }

    @Override
    public void setWorldsProvider(WorldsProvider worldsProvider) {
        Preconditions.checkNotNull(worldsProvider, "worldsProvider parameter cannot be null.");
        this.worldsProvider = worldsProvider;
    }

    @Override
    public WorldsProvider getWorldsProvider() {
        return this.worldsProvider;
    }

    @Override
    public void setBankEconomyProvider(EconomyProvider bankEconomyProvider) {
        Preconditions.checkNotNull(bankEconomyProvider, "bankEconomyProvider parameter cannot be null.");
        this.bankEconomyProvider = bankEconomyProvider;
    }

    @Override
    public EconomyProvider getBankEconomyProvider() {
        return this.bankEconomyProvider;
    }

    @Override
    public void addAFKProvider(AFKProvider afkProvider) {
        Preconditions.checkNotNull(afkProvider, "afkProvider parameter cannot be null.");
        AFKProvidersList.add(afkProvider);
    }

    @Override
    public List<AFKProvider> getAFKProviders() {
        return Collections.unmodifiableList(this.AFKProvidersList);
    }

    @Override
    public void setMenusProvider(MenusProvider menusProvider) {
        Preconditions.checkNotNull(menusProvider, "menusProvider parameter cannot be null.");
        this.menusProvider = menusProvider;
    }

    @Override
    public MenusProvider getMenusProvider() {
        return this.menusProvider;
    }

    public Pair<Integer, String> getSpawner(Location location) {
        return spawnersProvider.getSpawner(location);
    }

    public Key getSpawnerKey(ItemStack itemStack) {
        return Key.of(Materials.SPAWNER.toBukkitType() + "", spawnersProvider.getSpawnerType(itemStack) + "");
    }

    public Collection<Pair<com.bgsoftware.superiorskyblock.api.key.Key, Integer>> getBlocks(ChunkPosition chunkPosition) {
        return stackedBlocksProvider.getBlocks(chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ());
    }

    public boolean hasSnapshotsSupport() {
        return spawnersProvider instanceof SpawnersSnapshotProvider ||
                stackedBlocksProvider instanceof StackedBlocksSnapshotProvider;
    }

    public void takeSnapshots(Chunk chunk){
        if(spawnersProvider instanceof SpawnersSnapshotProvider){
            ((SpawnersSnapshotProvider) spawnersProvider).takeSnapshot(chunk);
        }
        if(stackedBlocksProvider instanceof StackedBlocksSnapshotProvider){
            ((StackedBlocksSnapshotProvider) stackedBlocksProvider).takeSnapshot(chunk);
        }
    }

    public void releaseSnapshots(ChunkPosition chunkPosition){
        if(spawnersProvider instanceof SpawnersSnapshotProvider){
            ((SpawnersSnapshotProvider) spawnersProvider).releaseSnapshot(
                    chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ());
        }
        if(stackedBlocksProvider instanceof StackedBlocksSnapshotProvider){
            ((StackedBlocksSnapshotProvider) stackedBlocksProvider).releaseSnapshot(
                    chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ());
        }
    }

    public boolean hasPermission(Player player, String permission) {
        return permissionsProvider.hasPermission(player, permission.toLowerCase());
    }

    public BigDecimal getPrice(Key key) {
        return pricesProvider.getPrice(key.getSubKey().isEmpty() ? Key.of(key.getGlobalKey(), "0") : key);
    }

    public Key getPriceBlockKey(Key key) {
        return pricesProvider.getBlockKey(key);
    }

    public boolean isVanished(Player player) {
        return vanishProvider.isVanished(player);
    }

    public void loadChunk(ChunkPosition chunkPosition, Consumer<Chunk> chunkResult) {
        asyncProvider.loadChunk(chunkPosition, chunkResult);
    }

    public void teleport(Entity entity, Location location) {
        asyncProvider.teleport(entity, location);
    }

    public void teleport(Entity entity, Location location, Consumer<Boolean> teleportResult) {
        asyncProvider.teleport(entity, location, teleportResult);
    }

    public BigDecimal getBalance(SuperiorPlayer superiorPlayer) {
        return economyProvider.getBalance(superiorPlayer);
    }

    public EconomyProvider.EconomyResult depositMoney(SuperiorPlayer superiorPlayer, BigDecimal amount) {
        while (amount.compareTo(MAX_DOUBLE) > 0) {
            EconomyProvider.EconomyResult result = economyProvider.depositMoney(superiorPlayer, Double.MAX_VALUE);
            if (result.hasFailed())
                return result;

            amount = amount.subtract(MAX_DOUBLE);
        }

        return economyProvider.depositMoney(superiorPlayer, amount.doubleValue());
    }

    public EconomyProvider.EconomyResult withdrawMoney(SuperiorPlayer superiorPlayer, BigDecimal amount) {
        while (amount.compareTo(MAX_DOUBLE) > 0) {
            EconomyProvider.EconomyResult result = withdrawMoney(superiorPlayer, Double.MAX_VALUE);
            if (result.hasFailed())
                return result;

            amount = amount.subtract(MAX_DOUBLE);
        }

        return withdrawMoney(superiorPlayer, amount.doubleValue());
    }

    public EconomyProvider.EconomyResult withdrawMoney(SuperiorPlayer superiorPlayer, double amount) {
        return economyProvider.withdrawMoney(superiorPlayer, amount);
    }

    public void prepareWorlds() {
        worldsProvider.prepareWorlds();
    }

    public World getIslandsWorld(Island island, World.Environment environment) {
        return worldsProvider.getIslandsWorld(island, environment);
    }

    public boolean isIslandsWorld(World world) {
        return worldsProvider.isIslandsWorld(world);
    }

    public Location getNextLocation(Location previousLocation, int islandsHeight, int maxIslandSize, UUID islandOwner, UUID islandUUID) {
        return worldsProvider.getNextLocation(previousLocation, islandsHeight, maxIslandSize, islandOwner, islandUUID);
    }

    public void finishIslandCreation(Location islandLocation, UUID islandOwner, UUID islandUUID) {
        worldsProvider.finishIslandCreation(islandLocation, islandOwner, islandUUID);
    }

    public void prepareTeleport(Island island, Location location, Runnable finishCallback) {
        worldsProvider.prepareTeleport(island, location, finishCallback);
    }

    public boolean isNormalEnabled() {
        return worldsProvider.isNormalEnabled();
    }

    public boolean isNormalUnlocked() {
        return worldsProvider.isNormalUnlocked();
    }

    public boolean isNetherEnabled() {
        return worldsProvider.isNetherEnabled();
    }

    public boolean isNetherUnlocked() {
        return worldsProvider.isNetherUnlocked();
    }

    public boolean isEndEnabled() {
        return worldsProvider.isEndEnabled();
    }

    public boolean isEndUnlocked() {
        return worldsProvider.isEndUnlocked();
    }

    public boolean hasCustomWorldsSupport() {
        return !(worldsProvider instanceof WorldsProvider_Default);
    }

    public BigDecimal getBalanceForBanks(SuperiorPlayer superiorPlayer) {
        return bankEconomyProvider.getBalance(superiorPlayer);
    }

    public EconomyProvider.EconomyResult depositMoneyForBanks(SuperiorPlayer superiorPlayer, BigDecimal amount) {
        while (amount.compareTo(MAX_DOUBLE) > 0) {
            EconomyProvider.EconomyResult result = bankEconomyProvider.depositMoney(superiorPlayer, Double.MAX_VALUE);
            if (result.hasFailed())
                return result;

            amount = amount.subtract(MAX_DOUBLE);
        }

        return bankEconomyProvider.depositMoney(superiorPlayer, amount.doubleValue());
    }

    public EconomyProvider.EconomyResult withdrawMoneyForBanks(SuperiorPlayer superiorPlayer, BigDecimal amount) {
        while (amount.compareTo(MAX_DOUBLE) > 0) {
            EconomyProvider.EconomyResult result = withdrawMoneyForBanks(superiorPlayer, Double.MAX_VALUE);
            if (result.hasFailed())
                return result;

            amount = amount.subtract(MAX_DOUBLE);
        }

        return withdrawMoneyForBanks(superiorPlayer, amount.doubleValue());
    }

    public EconomyProvider.EconomyResult withdrawMoneyForBanks(SuperiorPlayer superiorPlayer, double amount) {
        return bankEconomyProvider.withdrawMoney(superiorPlayer, amount);
    }

    public boolean isAFK(Player player) {
        return AFKProvidersList.stream().anyMatch(afkProvider -> afkProvider.isAFK(player));
    }

    public boolean shouldListenToSpawnerChanges() {
        return listenToSpawnerChanges;
    }

    private void registerGeneralHooks() {
        if (Bukkit.getPluginManager().isPluginEnabled("LeaderHeads"))
            runSafe(LeaderHeadsHook::register);

        if (Bukkit.getPluginManager().isPluginEnabled("JetsMinions"))
            runSafe(() -> JetsMinionsHook.register(plugin));

        if (Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer"))
            runSafe(() -> SkinsRestorerHook.register(plugin));

        if (Bukkit.getPluginManager().isPluginEnabled("ChangeSkin"))
            runSafe(() -> ChangeSkinHook.register(plugin));

        if (Bukkit.getPluginManager().isPluginEnabled("Slimefun"))
            runSafe(() -> SlimefunHook.register(plugin));

        if (Bukkit.getPluginManager().isPluginEnabled("CoreProtect"))
            runSafe(() -> CoreProtectHook.register(plugin));

        if (Bukkit.getPluginManager().isPluginEnabled("SlimeWorldManager"))
            runSafe(SWMHook::register);

        PlaceholderHook.register(plugin);
    }

    private void registerSpawnersProvider() {
        if (!(spawnersProvider instanceof SpawnersProvider_AutoDetect))
            return;

        String spawnersProvider = plugin.getSettings().getSpawnersProvider();
        boolean auto = spawnersProvider.equalsIgnoreCase("Auto");

        if (Bukkit.getPluginManager().isPluginEnabled("MergedSpawner") &&
                (auto || spawnersProvider.equalsIgnoreCase("MergedSpawner"))) {
            runSafe(() -> setSpawnersProvider(new SpawnersProvider_MergedSpawner()));
            listenToSpawnerChanges = false;
        } else if (Bukkit.getPluginManager().isPluginEnabled("AdvancedSpawners") &&
                (auto || spawnersProvider.equalsIgnoreCase("AdvancedSpawners"))) {
            runSafe(() -> setSpawnersProvider(new SpawnersProvider_AdvancedSpawners()));
        } else if (Bukkit.getPluginManager().isPluginEnabled("WildStacker") &&
                (auto || spawnersProvider.equalsIgnoreCase("WildStacker"))) {
            runSafe(() -> setSpawnersProvider(new SpawnersProvider_WildStacker()));
        } else if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") &&
                Bukkit.getPluginManager().getPlugin("SilkSpawners").getDescription().getAuthors().contains("CandC_9_12") &&
                (auto || spawnersProvider.equalsIgnoreCase("SilkSpawners"))) {
            runSafe(() -> setSpawnersProvider(new SpawnersProvider_SilkSpawners()));
        } else if (Bukkit.getPluginManager().isPluginEnabled("PvpingSpawners") &&
                (auto || spawnersProvider.equalsIgnoreCase("PvpingSpawners"))) {
            runSafe(() -> setSpawnersProvider(new SpawnersProvider_PvpingSpawners()));
        } else if (Bukkit.getPluginManager().isPluginEnabled("EpicSpawners") &&
                (auto || spawnersProvider.equalsIgnoreCase("EpicSpawners"))) {
            if (Bukkit.getPluginManager().getPlugin("EpicSpawners").getDescription().getVersion().startsWith("7")) {
                try {
                    SpawnersProvider epicSpawnersProvider = (SpawnersProvider)
                            Class.forName("com.bgsoftware.superiorskyblock.hooks.BlocksProvider_EpicSpawners7").newInstance();
                    runSafe(() -> setSpawnersProvider(epicSpawnersProvider));
                } catch (Exception ignored) {
                }
            } else {
                runSafe(() -> setSpawnersProvider(new SpawnersProvider_EpicSpawners()));
            }
        } else if (Bukkit.getPluginManager().isPluginEnabled("UltimateStacker") &&
                (auto || spawnersProvider.equalsIgnoreCase("UltimateStacker"))) {
            runSafe(() -> setSpawnersProvider(new SpawnersProvider_UltimateStacker()));
            listenToSpawnerChanges = false;
        } else if (Bukkit.getPluginManager().isPluginEnabled("RoseStacker") &&
                (auto || spawnersProvider.equalsIgnoreCase("RoseStacker"))) {
            runSafe(() -> setSpawnersProvider(new SpawnersProvider_RoseStacker()));
            listenToSpawnerChanges = false;
        }
    }

    private void registerStackedBlocksProvider() {
        if (!(stackedBlocksProvider instanceof StackedBlocksProvider_AutoDetect))
            return;

        String stackedBlocksProvider = plugin.getSettings().getStackedBlocksProvider();
        boolean auto = stackedBlocksProvider.equalsIgnoreCase("Auto");

        if (Bukkit.getPluginManager().isPluginEnabled("WildStacker") &&
                (auto || stackedBlocksProvider.equalsIgnoreCase("WildStacker"))) {
            runSafe(() -> setStackedBlocksProvider(new StackedBlocksProvider_WildStacker()));
        } else if (Bukkit.getPluginManager().isPluginEnabled("RoseStacker") &&
                (auto || stackedBlocksProvider.equalsIgnoreCase("RoseStacker"))) {
            runSafe(() -> setStackedBlocksProvider(new StackedBlocksProvider_RoseStacker()));
        }
    }

    private void registerPermissionsProvider() {
        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms") && PermissionsProvider_LuckPerms.isCompatible()) {
            runSafe(() -> this.permissionsProvider = new PermissionsProvider_LuckPerms());
        }
    }

    private void registerPricesProvider() {
        if (Bukkit.getPluginManager().isPluginEnabled("ShopGUIPlus")) {
            runSafe(() -> pricesProvider = new PricesProvider_ShopGUIPlus());
        }
    }

    private void registerVanishProvider() {
        if (Bukkit.getPluginManager().isPluginEnabled("VanishNoPacket")) {
            runSafe(() -> vanishProvider = new VanishProvider_VanishNoPacket(plugin));
        } else if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish") ||
                Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
            runSafe(() -> vanishProvider = new VanishProvider_SuperVanish(plugin));
        } else if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            runSafe(() -> vanishProvider = new VanishProvider_Essentials(plugin));
        } else if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            runSafe(() -> vanishProvider = new VanishProvider_CMI(plugin));
        }
    }

    private void registerAFKProvider() {
        if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            runSafe(() -> addAFKProvider(new AFKProvider_CMI()));
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            runSafe(() -> addAFKProvider(new AFKProvider_Essentials()));
        }
    }

    private void registerAsyncProvider(){
        if (hasPaperAsyncSupport()) {
            try {
                asyncProvider = (AsyncProvider) Class.forName("com.bgsoftware.superiorskyblock.hooks.AsyncProvider_Paper").newInstance();
                // Only added in versions 1.13+ of paper, so it can be here
                Bukkit.getPluginManager().registerEvents(new PaperListener(plugin), plugin);
                SuperiorSkyblockPlugin.log("Detected PaperSpigot - Using async chunk-loading support with PaperMC.");
            } catch (Exception ex) {
                SuperiorSkyblockPlugin.log("Detected PaperSpigot but failed to load async chunk-loading support...");
                ex.printStackTrace();
            }
        }
    }

    private void registerEconomyProviders(){
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            boolean alreadyCheckedForVault = false;
            if (economyProvider instanceof EconomyProvider_Default && EconomyProvider_Vault.isCompatible()) {
                setEconomyProvider(new EconomyProvider_Vault());
                alreadyCheckedForVault = true;
            }
            if (bankEconomyProvider instanceof EconomyProvider_Default && (alreadyCheckedForVault || EconomyProvider_Vault.isCompatible())) {
                setBankEconomyProvider(economyProvider);
            }
        }
    }

    private static void runSafe(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private static boolean hasPaperAsyncSupport() {
        try {
            //noinspection JavaReflectionMemberAccess
            World.class.getMethod("getChunkAtAsync", int.class, int.class);
            return true;
        } catch (Throwable ex) {
            return false;
        }
    }

}
