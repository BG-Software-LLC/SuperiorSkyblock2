package com.bgsoftware.superiorskyblock.core.database.cache;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.container.value.Value;
import com.bgsoftware.superiorskyblock.island.privilege.PlayerPrivilegeNode;
import com.bgsoftware.superiorskyblock.core.key.KeyMapImpl;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CachedIslandInfo {

    private static final BigDecimal SYNCED_BANK_LIMIT_VALUE = BigDecimal.valueOf(-2);
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public final Location[] islandHomes = new Location[World.Environment.values().length];
    public final List<SuperiorPlayer> members = new ArrayList<>();
    public final List<SuperiorPlayer> bannedPlayers = new ArrayList<>();
    public final Map<SuperiorPlayer, PlayerPrivilegeNode> playerPermissions = new HashMap<>();
    public final Map<IslandPrivilege, PlayerRole> rolePermissions = new HashMap<>();
    public final Map<String, Integer> upgrades = new HashMap<>();
    public final KeyMap<Value<Integer>> blockLimits = KeyMapImpl.createHashMap();
    public final Map<UUID, Rating> ratings = new HashMap<>();
    public final Map<Mission<?>, Integer> completedMissions = new HashMap<>();
    public final Map<IslandFlag, Byte> islandFlags = new HashMap<>();
    public final KeyMap<Value<Integer>>[] cobbleGeneratorValues = new KeyMap[World.Environment.values().length];
    public final List<SIsland.UniqueVisitor> uniqueVisitors = new ArrayList<>();
    public final KeyMap<Value<Integer>> entityLimits = KeyMapImpl.createHashMap();
    public final Map<PotionEffectType, Value<Integer>> islandEffects = new HashMap<>();
    public final List<ItemStack[]> islandChests = new ArrayList<>(plugin.getSettings().getIslandChests().getDefaultPages());
    public final Map<PlayerRole, Value<Integer>> roleLimits = new HashMap<>();
    public final Location[] visitorHomes = new Location[World.Environment.values().length];
    public Value<Integer> islandSize = Value.syncedFixed(-1);
    public Value<Integer> warpsLimit = Value.syncedFixed(-1);
    public Value<Integer> teamLimit = Value.syncedFixed(-1);
    public Value<Integer> coopLimit = Value.syncedFixed(-1);
    public Value<Double> cropGrowth = Value.syncedFixed(-1D);
    public Value<Double> spawnerRates = Value.syncedFixed(-1D);
    public Value<Double> mobDrops = Value.syncedFixed(-1D);
    public Value<BigDecimal> bankLimit = Value.syncedFixed(SYNCED_BANK_LIMIT_VALUE);
    public BigDecimal balance = BigDecimal.ZERO;
    public long lastInterestTime = 0L;
    public List<CachedWarpInfo> cachedWarpInfoList = new ArrayList<>();
    public List<CachedWarpCategoryInfo> cachedWarpCategoryInfoList = new ArrayList<>();
    public List<BankTransaction> bankTransactions = new ArrayList<>();
    public byte[] persistentData = new byte[0];

    public CachedIslandInfo() {

    }

}
