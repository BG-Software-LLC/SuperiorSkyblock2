package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.common.shopsbridge.ShopsProvider;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.ProvidersManager;
import com.bgsoftware.superiorskyblock.api.hooks.AFKProvider;
import com.bgsoftware.superiorskyblock.api.hooks.ChunksProvider;
import com.bgsoftware.superiorskyblock.api.hooks.EconomyProvider;
import com.bgsoftware.superiorskyblock.api.hooks.EntitiesProvider;
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
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ChunkPosition;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.types.SpawnerKey;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.external.async.AsyncProvider;
import com.bgsoftware.superiorskyblock.external.async.AsyncProvider_Default;
import com.bgsoftware.superiorskyblock.external.chunks.ChunksProvider_Default;
import com.bgsoftware.superiorskyblock.external.economy.EconomyProvider_Default;
import com.bgsoftware.superiorskyblock.external.menus.MenusProvider_Default;
import com.bgsoftware.superiorskyblock.external.permissions.PermissionsProvider_Default;
import com.bgsoftware.superiorskyblock.external.placeholders.PlaceholdersProvider;
import com.bgsoftware.superiorskyblock.external.prices.PricesProvider_Default;
import com.bgsoftware.superiorskyblock.external.prices.PricesProvider_ShopsBridgeWrapper;
import com.bgsoftware.superiorskyblock.external.spawners.SpawnersProvider_AutoDetect;
import com.bgsoftware.superiorskyblock.external.spawners.SpawnersProvider_Default;
import com.bgsoftware.superiorskyblock.external.stackedblocks.StackedBlocksProvider_AutoDetect;
import com.bgsoftware.superiorskyblock.external.stackedblocks.StackedBlocksProvider_Default;
import com.bgsoftware.superiorskyblock.external.worlds.WorldsProvider_Default;
import com.bgsoftware.superiorskyblock.service.placeholders.PlaceholdersServiceImpl;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ProvidersManagerImpl extends Manager implements ProvidersManager {

    private static final BigDecimal MAX_DOUBLE = BigDecimal.valueOf(Double.MAX_VALUE);

    private final List<AFKProvider> AFKProvidersList = new LinkedList<>();
    private List<Runnable> pricesLoadCallbacks = new LinkedList<>();
    private SpawnersProvider spawnersProvider = new SpawnersProvider_Default();
    private StackedBlocksProvider stackedBlocksProvider = new StackedBlocksProvider_Default();
    private List<EntitiesProvider> entitiesProviders = new LinkedList<>();
    private EconomyProvider economyProvider = new EconomyProvider_Default();
    private EconomyProvider bankEconomyProvider = new EconomyProvider_Default();
    private PermissionsProvider permissionsProvider = new PermissionsProvider_Default();
    private PricesProvider pricesProvider = new PricesProvider_Default();
    private VanishProvider vanishProvider = player -> false;
    private AsyncProvider asyncProvider = new AsyncProvider_Default();
    private WorldsProvider worldsProvider;
    private ChunksProvider chunksProvider = new ChunksProvider_Default();
    private MenusProvider menusProvider;
    private boolean listenToSpawnerChanges = true;

    private final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
        @Override
        protected PlaceholdersService create() {
            return plugin.getServices().getService(PlaceholdersService.class);
        }
    };

    private final List<ISkinsListener> skinsListeners = new LinkedList<>();
    private final List<IStackedBlocksListener> stackedBlocksListeners = new LinkedList<>();
    private final List<IWorldsListener> worldsListeners = new LinkedList<>();

    public ProvidersManagerImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        this.worldsProvider = new WorldsProvider_Default(plugin);
        this.menusProvider = new MenusProvider_Default(plugin);
    }

    @Override
    public void loadData() {
        BukkitExecutor.sync(() -> {
            registerGeneralHooks();
            registerSpawnersProvider();
            registerStackedBlocksProvider();
            registerEntitiesProvider();
            registerPermissionsProvider();
            registerPricesProvider();
            registerVanishProvider();
            registerAFKProvider();
            registerAsyncProvider();
            registerEconomyProviders();
            registerPlaceholdersProvider();
            registerChunksProvider();
        });
        // We try to forcefully load prices after a second the server has enabled.
        BukkitExecutor.sync(this::forcePricesLoad, 60L);
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
    public List<EntitiesProvider> getEntitiesProviders() {
        return Collections.unmodifiableList(this.entitiesProviders);
    }

    @Override
    public void addEntitiesProvider(EntitiesProvider entitiesProvider) {
        Preconditions.checkNotNull(entitiesProvider, "entitiesProvider parameter cannot be null.");
        this.entitiesProviders.add(entitiesProvider);
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
    public ChunksProvider getChunksProvider() {
        return chunksProvider;
    }

    @Override
    public void setChunksProvider(ChunksProvider chunksProvider) {
        Preconditions.checkNotNull(chunksProvider, "chunksProvider parameter cannot be null.");
        this.chunksProvider = chunksProvider;
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
        this.pricesProvider.getWhenPricesAreReady().whenComplete((result, error) -> this.forcePricesLoad());
    }

    public void forcePricesLoad() {
        if (this.pricesLoadCallbacks != null) {
            this.pricesLoadCallbacks.forEach(Runnable::run);
            this.pricesLoadCallbacks = null;
            // After we loaded all the price callbacks, we want to sort the top islands.
            SortingType.values().forEach(plugin.getGrid()::forceSortIslands);
        }
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

    public void addPricesLoadCallback(Runnable callback) {
        if (this.pricesLoadCallbacks == null) {
            callback.run();
        } else {
            this.pricesLoadCallbacks.add(callback);
        }
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
        String type = spawnersProvider.getSpawnerType(itemStack);
        return type == null ? SpawnerKey.GLOBAL_KEY : Keys.ofSpawner(type);
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
        if (canRegisterHook("JetsMinions"))
            registerHook("JetsMinionsHook");

        if (canRegisterHook("SkinsRestorer")) {
            String version = Bukkit.getPluginManager().getPlugin("SkinsRestorer").getDescription().getVersion();
            if (version.startsWith("14")) {
                registerHook("SkinsRestorer14Hook");
            } else if (version.startsWith("15")) {
                registerHook("SkinsRestorer15Hook");
            } else {
                registerHook("SkinsRestorerHook");
            }
        }

        if (canRegisterHook("ChangeSkin"))
            registerHook("ChangeSkinHook");

        if (canRegisterHook("Slimefun"))
            registerHook("SlimefunHook");

        if (canRegisterHook("CoreProtect"))
            registerHook("CoreProtectHook");

        if (canRegisterHook("SlimeWorldManager"))
            registerHook("SlimeWorldManagerHook");

        if (canRegisterHook("ProtocolLib"))
            registerHook("ProtocolLibHook");

        if (Bukkit.getPluginManager().isPluginEnabled("Oraxen"))
            registerHook("OraxenHook");

        if (Bukkit.getPluginManager().isPluginEnabled("ItemsAdder"))
            registerHook("ItemsAdderHook");
    }

    private void registerSpawnersProvider() {
        if (!(spawnersProvider instanceof SpawnersProvider_AutoDetect))
            return;

        String configSpawnersProvider = plugin.getSettings().getSpawnersProvider();
        boolean auto = configSpawnersProvider.equalsIgnoreCase("Auto");

        Optional<SpawnersProvider> spawnersProvider = Optional.empty();

        if (canRegisterHook("MergedSpawner") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("MergedSpawner"))) {
            spawnersProvider = createInstance("spawners.SpawnersProvider_MergedSpawner");
            listenToSpawnerChanges = false;
        } else if (canRegisterHook("AdvancedSpawners") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("AdvancedSpawners"))) {
            spawnersProvider = createInstance("spawners.SpawnersProvider_AdvancedSpawners");
        } else if (canRegisterHook("WildStacker") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("WildStacker"))) {
            spawnersProvider = createInstance("spawners.SpawnersProvider_WildStacker");
        } else if (canRegisterHook("SilkSpawners") &&
                Bukkit.getPluginManager().getPlugin("SilkSpawners").getDescription().getAuthors().contains("CandC_9_12") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("SilkSpawners"))) {
            spawnersProvider = createInstance("spawners.SpawnersProvider_SilkSpawners");
        } else if (canRegisterHook("PvpingSpawners") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("PvpingSpawners"))) {
            spawnersProvider = createInstance("spawners.SpawnersProvider_PvpingSpawners");
        } else if (canRegisterHook("EpicSpawners") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("EpicSpawners"))) {
            String version = Bukkit.getPluginManager().getPlugin("EpicSpawners").getDescription().getVersion();
            if (version.startsWith("8")) {
                spawnersProvider = createInstance("spawners.SpawnersProvider_EpicSpawners8");
            } else if (version.startsWith("7")) {
                spawnersProvider = createInstance("spawners.SpawnersProvider_EpicSpawners7");
            } else {
                spawnersProvider = createInstance("spawners.SpawnersProvider_EpicSpawners6");
            }
        } else if (canRegisterHook("UltimateStacker") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("UltimateStacker"))) {
            if (Bukkit.getPluginManager().getPlugin("UltimateStacker").getDescription().getVersion().startsWith("3")) {
                spawnersProvider = createInstance("spawners.SpawnersProvider_UltimateStacker3");
            } else {
                spawnersProvider = createInstance("spawners.SpawnersProvider_UltimateStacker");
            }
            listenToSpawnerChanges = false;
        } else if (canRegisterHook("RoseStacker") &&
                (auto || configSpawnersProvider.equalsIgnoreCase("RoseStacker"))) {
            spawnersProvider = createInstance("spawners.SpawnersProvider_RoseStacker");
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

        if (canRegisterHook("WildStacker") &&
                (auto || configStackedBlocksProvider.equalsIgnoreCase("WildStacker"))) {
            stackedBlocksProvider = createInstance("stackedblocks.StackedBlocksProvider_WildStacker");
        } else if (canRegisterHook("RoseStacker") &&
                (auto || configStackedBlocksProvider.equalsIgnoreCase("RoseStacker"))) {
            stackedBlocksProvider = createInstance("stackedblocks.StackedBlocksProvider_RoseStacker");
        }

        stackedBlocksProvider.ifPresent(this::setStackedBlocksProvider);
    }

    private void registerEntitiesProvider() {
        if (canRegisterHook("WildStacker")) {
            Optional<EntitiesProvider> entitiesProvider = createInstance("entities.EntitiesProvider_WildStacker");
            entitiesProvider.ifPresent(this::addEntitiesProvider);
        }
        if (canRegisterHook("RoseStacker")) {
            Optional<EntitiesProvider> entitiesProvider = createInstance("entities.EntitiesProvider_RoseStacker");
            entitiesProvider.ifPresent(this::addEntitiesProvider);
        }
    }

    private void registerPermissionsProvider() {
        Optional<PermissionsProvider> permissionsProvider = Optional.empty();

        if (canRegisterHook("LuckPerms")) {
            permissionsProvider = createInstance("permissions.PermissionsProvider_LuckPerms");
        }

        permissionsProvider.ifPresent(this::setPermissionsProvider);
    }

    private void registerPricesProvider() {
        ShopsProvider.SHOPGUIPLUS.createInstance(plugin)
                .map(shopsBridge -> new PricesProvider_ShopsBridgeWrapper(plugin, ShopsProvider.SHOPGUIPLUS, shopsBridge))
                .ifPresent(this::setPricesProvider);
    }

    private void registerVanishProvider() {
        Optional<VanishProvider> vanishProvider = Optional.empty();

        if (canRegisterHook("VanishNoPacket")) {
            vanishProvider = createInstance("vanish.VanishProvider_VanishNoPacket");
        } else if (canRegisterHook("SuperVanish") ||
                canRegisterHook("PremiumVanish")) {
            vanishProvider = createInstance("vanish.VanishProvider_SuperVanish");
        } else if (canRegisterHook("Essentials")) {
            vanishProvider = createInstance("vanish.VanishProvider_Essentials");
        } else if (canRegisterHook("CMI")) {
            vanishProvider = createInstance("vanish.VanishProvider_CMI");
        }

        vanishProvider.ifPresent(this::setVanishProvider);
    }

    private void registerAFKProvider() {
        if (canRegisterHook("CMI")) {
            Optional<AFKProvider> afkProvider = createInstance("afk.AFKProvider_CMI");
            afkProvider.ifPresent(this::addAFKProvider);
        }
        if (canRegisterHook("Essentials")) {
            Optional<AFKProvider> afkProvider = createInstance("afk.AFKProvider_Essentials");
            afkProvider.ifPresent(this::addAFKProvider);
        }
    }

    private void registerAsyncProvider() {
        if (hasPaperAsyncSupport()) {
            Optional<AsyncProvider> asyncProviderOptional = createInstance("async.AsyncProvider_Paper");
            asyncProviderOptional.ifPresent(asyncProvider -> {
                this.asyncProvider = asyncProvider;
            });
        }
    }

    private void registerEconomyProviders() {
        if (canRegisterHook("Vault")) {
            if (this.economyProvider instanceof EconomyProvider_Default ||
                    this.bankEconomyProvider instanceof EconomyProvider_Default) {
                Optional<EconomyProvider> economyProviderOptional = createInstance("economy.EconomyProvider_Vault");
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

        if (canRegisterHook("MVdWPlaceholderAPI")) {
            Optional<PlaceholdersProvider> placeholdersProvider = createInstance("placeholders.PlaceholdersProvider_MVdWPlaceholderAPI");
            placeholdersProvider.ifPresent(placeholdersProviders::add);
        }
        if (canRegisterHook("PlaceholderAPI")) {
            Optional<PlaceholdersProvider> placeholdersProvider = createInstance("placeholders.PlaceholdersProvider_PlaceholderAPI");
            placeholdersProvider.ifPresent(placeholdersProviders::add);
        }

        ((PlaceholdersServiceImpl) this.placeholdersService.get()).register(placeholdersProviders);
    }

    private void registerChunksProvider() {
        if (hasPaperAsyncSupport()) {
            Optional<ChunksProvider> chunksProviderOptional = createInstance("chunks.ChunksProvider_Paper");
            chunksProviderOptional.ifPresent(chunksProvider -> {
                try {
                    setChunksProvider(chunksProvider);
                    Log.info("Detected PaperSpigot - Using async chunk-loading support with PaperMC.");
                } catch (Exception error) {
                    Log.error(error, "Detected PaperSpigot but failed to load async chunk-loading support due to an unexpected error:");
                }
            });
        }
    }

    private void registerHook(String className) {
        try {
            Class<?> clazz = Class.forName("com.bgsoftware.superiorskyblock.external." + className);

            if (!isHookCompatible(clazz))
                return;

            Method registerMethod = clazz.getMethod("register", SuperiorSkyblockPlugin.class);
            registerMethod.invoke(null, plugin);
        } catch (Throwable error) {
            Log.error(error, "An unexpected error occurred while registering hook ", className, ":");
        }
    }

    private static boolean hasPaperAsyncSupport() {
        return new ReflectMethod<>(World.class, "getChunkAtAsync", int.class, int.class).isValid();
    }

    private <T> Optional<T> createInstance(String className) {
        try {
            Class<?> clazz = Class.forName("com.bgsoftware.superiorskyblock.external." + className);

            if (!isHookCompatible(clazz))
                return Optional.empty();

            try {
                Constructor<?> constructor = clazz.getConstructor(SuperiorSkyblockPlugin.class);
                // noinspection unchecked
                return Optional.of((T) constructor.newInstance(plugin));
            } catch (NoSuchMethodException error) {
                // noinspection unchecked
                return Optional.of((T) clazz.newInstance());
            }
        } catch (ClassNotFoundException ignored) {
            return Optional.empty();
        } catch (Exception error) {
            Log.entering("ENTER", className);
            Log.error(error, "An unexpected error occurred while creating hook instance:");
            return Optional.empty();
        }
    }

    private boolean canRegisterHook(String pluginName) {
        return Bukkit.getPluginManager().isPluginEnabled(pluginName) && isHookEnabled(pluginName);
    }

    private boolean isHookEnabled(String pluginName) {
        return !plugin.getSettings().getDisabledHooks().contains(pluginName.toLowerCase(Locale.ENGLISH));
    }

    private boolean isHookCompatible(Class<?> clazz) {
        ReflectMethod<Boolean> compatibleMethod = new ReflectMethod<>(clazz, "isCompatible");
        return !compatibleMethod.isValid() || compatibleMethod.invoke(null);
    }

}
