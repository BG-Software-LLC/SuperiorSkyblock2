package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.ProvidersManager;
import com.bgsoftware.superiorskyblock.api.hooks.AFKProvider;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.hooks.MenusProvider;
import com.bgsoftware.superiorskyblock.api.hooks.PermissionsProvider;
import com.bgsoftware.superiorskyblock.api.hooks.PricesProvider;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersSnapshotProvider;
import com.bgsoftware.superiorskyblock.api.hooks.StackedBlocksProvider;
import com.bgsoftware.superiorskyblock.api.hooks.StackedBlocksSnapshotProvider;
import com.bgsoftware.superiorskyblock.api.hooks.VanishProvider;
import com.bgsoftware.superiorskyblock.api.hooks.WorldsProvider;
import com.bgsoftware.superiorskyblock.api.hooks.listener.ISkinsListener;
import com.bgsoftware.superiorskyblock.api.hooks.listener.IStackedBlocksListener;
import com.bgsoftware.superiorskyblock.api.hooks.listener.IWorldsListener;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handler.AbstractHandler;
import com.bgsoftware.superiorskyblock.hooks.provider.AsyncProvider;
import com.bgsoftware.superiorskyblock.hooks.provider.AsyncProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.EconomyProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.MenusProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.PermissionsProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.PlaceholdersProvider;
import com.bgsoftware.superiorskyblock.hooks.provider.PricesProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.SpawnersProvider_AutoDetect;
import com.bgsoftware.superiorskyblock.hooks.provider.SpawnersProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.StackedBlocksProvider_AutoDetect;
import com.bgsoftware.superiorskyblock.hooks.provider.StackedBlocksProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.provider.WorldsProvider_Default;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.listeners.PaperListener;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.world.chunks.ChunkPosition;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class ProvidersHandler extends AbstractHandler implements ProvidersManager {

    private static final BigDecimal MAX_DOUBLE = BigDecimal.valueOf(Double.MAX_VALUE);

    private final List<AFKProvider> AFKProvidersList = new ArrayList<>();
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
    private boolean listenToSpawnerChanges = true;

    private final List<ISkinsListener> skinsListeners = new ArrayList<>();
    private final List<IStackedBlocksListener> stackedBlocksListeners = new ArrayList<>();
    private final List<IWorldsListener> worldsListeners = new ArrayList<>();

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
            registerPlaceholdersProvider();
        });
    }

    @Override
    public SpawnersProvider getSpawnersProvider() {
        return this.spawnersProvider;
    }

    @Override
    public void setSpawnersProvider(SpawnersProvider spawnersProvider) {
        Preconditions.checkNotNull(spawnersProvider, "spawnersProvider parameter cannot be null.");
        this.spawnersProvider = spawnersProvider;
    }

    @Override
    public StackedBlocksProvider getStackedBlocksProvider() {
        return this.stackedBlocksProvider;
    }

    @Override
    public void setStackedBlocksProvider(StackedBlocksProvider stackedBlocksProvider) {
        Preconditions.checkNotNull(stackedBlocksProvider, "stackedBlocksProvider parameter cannot be null.");
        this.stackedBlocksProvider = stackedBlocksProvider;
    }

    @Override
    public EconomyProvider getEconomyProvider() {
        return this.economyProvider;
    }

    @Override
    public void setEconomyProvider(EconomyProvider economyProvider) {
        Preconditions.checkNotNull(economyProvider, "economyProvider parameter cannot be null.");
        this.economyProvider = economyProvider;
    }

    @Override
    public WorldsProvider getWorldsProvider() {
        return this.worldsProvider;
    }

    @Override
    public void setWorldsProvider(WorldsProvider worldsProvider) {
        Preconditions.checkNotNull(worldsProvider, "worldsProvider parameter cannot be null.");
        this.worldsProvider = worldsProvider;
    }

    @Override
    public EconomyProvider getBankEconomyProvider() {
        return this.bankEconomyProvider;
    }

    @Override
    public void setBankEconomyProvider(EconomyProvider bankEconomyProvider) {
        Preconditions.checkNotNull(bankEconomyProvider, "bankEconomyProvider parameter cannot be null.");
        this.bankEconomyProvider = bankEconomyProvider;
    }

    @Override
    public List<AFKProvider> getAFKProviders() {
        return Collections.unmodifiableList(this.AFKProvidersList);
    }

    @Override
    public void addAFKProvider(AFKProvider afkProvider) {
        Preconditions.checkNotNull(afkProvider, "afkProvider parameter cannot be null.");
        AFKProvidersList.add(afkProvider);
    }

    @Override
    public MenusProvider getMenusProvider() {
        return this.menusProvider;
    }

    @Override
    public void setMenusProvider(MenusProvider menusProvider) {
        Preconditions.checkNotNull(menusProvider, "menusProvider parameter cannot be null.");
        this.menusProvider = menusProvider;
    }

    @Override
    public PermissionsProvider getPermissionsProvider() {
        return permissionsProvider;
    }

    @Override
    public void setPermissionsProvider(PermissionsProvider permissionsProvider) {
        this.permissionsProvider = permissionsProvider;
    }

    @Override
    public PricesProvider getPricesProvider() {
        return pricesProvider;
    }

    @Override
    public void setPricesProvider(PricesProvider pricesProvider) {
        this.pricesProvider = pricesProvider;
    }

    @Override
    public VanishProvider getVanishProvider() {
        return vanishProvider;
    }

    @Override
    public void setVanishProvider(VanishProvider vanishProvider) {
        this.vanishProvider = vanishProvider;
    }

    @Override
    public void registerSkinsListener(ISkinsListener skinsListener) {
        this.skinsListeners.add(skinsListener);
    }

    @Override
    public void unregisterSkinsListener(ISkinsListener skinsListener) {
        this.skinsListeners.remove(skinsListener);
    }

    public boolean notifySkinsListeners(SuperiorPlayer superiorPlayer) {
        this.skinsListeners.forEach(skinsListener -> skinsListener.setSkinTexture(superiorPlayer));
        return !this.skinsListeners.isEmpty();
    }

    @Override
    public void registerStackedBlocksListener(IStackedBlocksListener stackedBlocksListener) {
        this.stackedBlocksListeners.add(stackedBlocksListener);
    }

    @Override
    public void unregisterStackedBlocksListener(IStackedBlocksListener stackedBlocksListener) {
        this.stackedBlocksListeners.remove(stackedBlocksListener);
    }

    public void notifyStackedBlocksListeners(OfflinePlayer offlinePlayer, Block block,
                                             IStackedBlocksListener.Action action) {
        this.stackedBlocksListeners.forEach(stackedBlocksListener ->
                stackedBlocksListener.recordBlockAction(offlinePlayer, block, action));
    }

    @Override
    public void registerWorldsListener(IWorldsListener worldsListener) {
        this.worldsListeners.add(worldsListener);
    }

    @Override
    public void unregisterWorldsListener(IWorldsListener worldsListener) {
        this.worldsListeners.remove(worldsListener);
    }

    public void runWorldsListeners(String worldName) {
        this.worldsListeners.forEach(worldsListener -> worldsListener.loadWorld(worldName));
    }

    public Key getSpawnerKey(ItemStack itemStack) {
        return Key.of(Materials.SPAWNER.toBukkitType() + "", spawnersProvider.getSpawnerType(itemStack) + "");
    }

    public boolean hasSnapshotsSupport() {
        return spawnersProvider instanceof SpawnersSnapshotProvider ||
                stackedBlocksProvider instanceof StackedBlocksSnapshotProvider;
    }

    public void takeSnapshots(Chunk chunk) {
        if (spawnersProvider instanceof SpawnersSnapshotProvider) {
            ((SpawnersSnapshotProvider) spawnersProvider).takeSnapshot(chunk);
        }
        if (stackedBlocksProvider instanceof StackedBlocksSnapshotProvider) {
            ((StackedBlocksSnapshotProvider) stackedBlocksProvider).takeSnapshot(chunk);
        }
    }

    public void releaseSnapshots(ChunkPosition chunkPosition) {
        if (spawnersProvider instanceof SpawnersSnapshotProvider) {
            ((SpawnersSnapshotProvider) spawnersProvider).releaseSnapshot(
                    chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ());
        }
        if (stackedBlocksProvider instanceof StackedBlocksSnapshotProvider) {
            ((StackedBlocksSnapshotProvider) stackedBlocksProvider).releaseSnapshot(
                    chunkPosition.getWorld(), chunkPosition.getX(), chunkPosition.getZ());
        }
    }

    public AsyncProvider getAsyncProvider() {
        return asyncProvider;
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
            EconomyProvider.EconomyResult result = economyProvider.withdrawMoney(superiorPlayer, Double.MAX_VALUE);
            if (result.hasFailed())
                return result;

            amount = amount.subtract(MAX_DOUBLE);
        }

        return economyProvider.withdrawMoney(superiorPlayer, amount.doubleValue());
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
            EconomyProvider.EconomyResult result = bankEconomyProvider.withdrawMoney(superiorPlayer, Double.MAX_VALUE);
            if (result.hasFailed())
                return result;

            amount = amount.subtract(MAX_DOUBLE);
        }

        return bankEconomyProvider.withdrawMoney(superiorPlayer, amount.doubleValue());
    }

    public boolean hasCustomWorldsSupport() {
        return !(worldsProvider instanceof WorldsProvider_Default);
    }

    public boolean isAFK(Player player) {
        return AFKProvidersList.stream().anyMatch(afkProvider -> afkProvider.isAFK(player));
    }

    public boolean shouldListenToSpawnerChanges() {
        return listenToSpawnerChanges;
    }

    private void registerGeneralHooks() {
        if (Bukkit.getPluginManager().isPluginEnabled("LeaderHeads"))
            registerHook("LeaderHeadsHook");

        if (Bukkit.getPluginManager().isPluginEnabled("JetsMinions"))
            registerHook("JetsMinionsHook");

        if (Bukkit.getPluginManager().isPluginEnabled("SkinsRestorer"))
            registerHook("SkinsRestorerHook");

        if (Bukkit.getPluginManager().isPluginEnabled("ChangeSkin"))
            registerHook("ChangeSkinHook");

        if (Bukkit.getPluginManager().isPluginEnabled("Slimefun"))
            registerHook("SlimefunHook");

        if (Bukkit.getPluginManager().isPluginEnabled("CoreProtect"))
            registerHook("CoreProtectHook");

        if (Bukkit.getPluginManager().isPluginEnabled("SlimeWorldManager"))
            registerHook("SlimeWorldManagerHook");
    }

    private void registerSpawnersProvider() {
        if (!(spawnersProvider instanceof SpawnersProvider_AutoDetect))
            return;

        String configSpawnersProvider = plugin.getSettings().getSpawnersProvider();
        boolean auto = configSpawnersProvider.equalsIgnoreCase("Auto");

        Optional<SpawnersProvider> spawnersProvider = Optional.empty();

        if (Bukkit.getPluginManager().isPluginEnabled("MergedSpawner") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("MergedSpawner"))) {
            spawnersProvider = createInstance("SpawnersProvider_MergedSpawner");
            listenToSpawnerChanges = false;
        } else if (Bukkit.getPluginManager().isPluginEnabled("AdvancedSpawners") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("AdvancedSpawners"))) {
            spawnersProvider = createInstance("SpawnersProvider_AdvancedSpawners");
        } else if (Bukkit.getPluginManager().isPluginEnabled("WildStacker") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("WildStacker"))) {
            spawnersProvider = createInstance("SpawnersProvider_WildStacker");
        } else if (Bukkit.getPluginManager().isPluginEnabled("SilkSpawners") &&
                Bukkit.getPluginManager().getPlugin("SilkSpawners").getDescription().getAuthors().contains("CandC_9_12") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("SilkSpawners"))) {
            spawnersProvider = createInstance("SpawnersProvider_SilkSpawners");
        } else if (Bukkit.getPluginManager().isPluginEnabled("PvpingSpawners") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("PvpingSpawners"))) {
            spawnersProvider = createInstance("SpawnersProvider_PvpingSpawners");
        } else if (Bukkit.getPluginManager().isPluginEnabled("EpicSpawners") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("EpicSpawners"))) {
            if (Bukkit.getPluginManager().getPlugin("EpicSpawners").getDescription().getVersion().startsWith("7")) {
                spawnersProvider = createInstance("SpawnersProvider_EpicSpawners7");
            } else {
                spawnersProvider = createInstance("SpawnersProvider_EpicSpawners6");
            }
        } else if (Bukkit.getPluginManager().isPluginEnabled("UltimateStacker") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("UltimateStacker"))) {
            spawnersProvider = createInstance("SpawnersProvider_UltimateStacker");
            listenToSpawnerChanges = false;
        } else if (Bukkit.getPluginManager().isPluginEnabled("RoseStacker") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("RoseStacker"))) {
            spawnersProvider = createInstance("SpawnersProvider_RoseStacker");
            listenToSpawnerChanges = false;
        }

        spawnersProvider.ifPresent(this::setSpawnersProvider);
    }

    private void registerStackedBlocksProvider() {
        if (!(stackedBlocksProvider instanceof StackedBlocksProvider_AutoDetect))
            return;

        String configStackedBlocksProvider = plugin.getSettings().getStackedBlocksProvider();
        boolean auto = configStackedBlocksProvider.equalsIgnoreCase("Auto");

        Optional<StackedBlocksProvider> stackedBlocksProvider = Optional.empty();

        if (Bukkit.getPluginManager().isPluginEnabled("WildStacker") &&
                (auto || configStackedBlocksProvider.equalsIgnoreCase("WildStacker"))) {
            stackedBlocksProvider = createInstance("StackedBlocksProvider_WildStacker");
        } else if (Bukkit.getPluginManager().isPluginEnabled("RoseStacker") &&
                (auto || configStackedBlocksProvider.equalsIgnoreCase("RoseStacker"))) {
            stackedBlocksProvider = createInstance("StackedBlocksProvider_RoseStacker");
        }

        stackedBlocksProvider.ifPresent(this::setStackedBlocksProvider);
    }

    private void registerPermissionsProvider() {
        Optional<PermissionsProvider> permissionsProvider = Optional.empty();

        if (Bukkit.getPluginManager().isPluginEnabled("LuckPerms")) {
            permissionsProvider = createInstance("PermissionsProvider_LuckPerms");
        }

        permissionsProvider.ifPresent(this::setPermissionsProvider);
    }

    private void registerPricesProvider() {
        Optional<PricesProvider> pricesProvider = Optional.empty();

        if (Bukkit.getPluginManager().isPluginEnabled("ShopGUIPlus")) {
            pricesProvider = createInstance("PricesProvider_ShopGUIPlus");
        }

        pricesProvider.ifPresent(this::setPricesProvider);
    }

    private void registerVanishProvider() {
        Optional<VanishProvider> vanishProvider = Optional.empty();

        if (Bukkit.getPluginManager().isPluginEnabled("VanishNoPacket")) {
            vanishProvider = createInstance("VanishProvider_VanishNoPacket");
        } else if (Bukkit.getPluginManager().isPluginEnabled("SuperVanish") ||
                Bukkit.getPluginManager().isPluginEnabled("PremiumVanish")) {
            vanishProvider = createInstance("VanishProvider_SuperVanish");
        } else if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            vanishProvider = createInstance("VanishProvider_Essentials");
        } else if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            vanishProvider = createInstance("VanishProvider_CMI");
        }

        vanishProvider.ifPresent(this::setVanishProvider);
    }

    private void registerAFKProvider() {
        if (Bukkit.getPluginManager().isPluginEnabled("CMI")) {
            Optional<AFKProvider> afkProvider = createInstance("AFKProvider_CMI");
            afkProvider.ifPresent(this::addAFKProvider);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
            Optional<AFKProvider> afkProvider = createInstance("AFKProvider_Essentials");
            afkProvider.ifPresent(this::addAFKProvider);
        }
    }

    private void registerAsyncProvider() {
        if (hasPaperAsyncSupport()) {
            try {
                Optional<AsyncProvider> asyncProviderOptional = createInstance("AsyncProvider_Paper");
                asyncProviderOptional.ifPresent(asyncProvider -> {
                    this.asyncProvider = asyncProvider;
                    // Only added in versions 1.13+ of paper, so it can be here
                    Bukkit.getPluginManager().registerEvents(new PaperListener(plugin), plugin);
                    SuperiorSkyblockPlugin.log("Detected PaperSpigot - Using async chunk-loading support with PaperMC.");
                });
            } catch (Exception ex) {
                SuperiorSkyblockPlugin.log("Detected PaperSpigot but failed to load async chunk-loading support...");
                ex.printStackTrace();
                PluginDebugger.debug(ex);
            }
        }
    }

    private void registerEconomyProviders() {
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            if (this.economyProvider instanceof EconomyProvider_Default ||
                    this.bankEconomyProvider instanceof EconomyProvider_Default) {
                Optional<EconomyProvider> economyProviderOptional = createInstance("EconomyProvider_Vault");
                economyProviderOptional.ifPresent(economyProvider -> {
                    if (this.economyProvider instanceof EconomyProvider_Default)
                        setEconomyProvider(economyProvider);
                    if (this.bankEconomyProvider instanceof EconomyProvider_Default)
                        setBankEconomyProvider(economyProvider);
                });
            }
        }
    }

    private void registerPlaceholdersProvider() {
        List<PlaceholdersProvider> placeholdersProviders = new ArrayList<>();

        if (Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
            Optional<PlaceholdersProvider> placeholdersProvider = createInstance("PlaceholdersProvider_MVdWPlaceholderAPI");
            placeholdersProvider.ifPresent(placeholdersProviders::add);
        }
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            Optional<PlaceholdersProvider> placeholdersProvider = createInstance("PlaceholdersProvider_PlaceholderAPI");
            placeholdersProvider.ifPresent(placeholdersProviders::add);
        }

        PlaceholderHook.register(placeholdersProviders);
    }

    private void registerHook(String className) {
        try {
            Class<?> clazz = Class.forName("com.bgsoftware.superiorskyblock.hooks.support." + className);
            Method registerMethod = clazz.getMethod("register", SuperiorSkyblockPlugin.class);
            registerMethod.invoke(null, plugin);
        } catch (Exception ignored) {
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

    private <T> Optional<T> createInstance(String className) {
        try {
            Class<?> clazz = Class.forName("com.bgsoftware.superiorskyblock.hooks.provider." + className);
            try {
                Method compatibleMethod = clazz.getDeclaredMethod("isCompatible");
                if (!(boolean) compatibleMethod.invoke(null))
                    return Optional.empty();
            } catch (Exception ignored) {
            }

            try {
                Constructor<?> constructor = clazz.getConstructor(SuperiorSkyblockPlugin.class);
                // noinspection unchecked
                return Optional.of((T) constructor.newInstance(plugin));
            } catch (Exception error) {
                // noinspection unchecked
                return Optional.of((T) clazz.newInstance());
            }
        } catch (ClassNotFoundException ignored) {
            return Optional.empty();
        } catch (Exception error) {
            error.printStackTrace();
            PluginDebugger.debug(error);
            return Optional.empty();
        }
    }

}
