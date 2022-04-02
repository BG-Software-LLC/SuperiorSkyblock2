package com.bgsoftware.superiorskyblock.database.cache;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.permissions.PlayerPermissionNode;
import com.bgsoftware.superiorskyblock.key.dataset.KeyMapImpl;
import com.bgsoftware.superiorskyblock.upgrade.UpgradeValue;
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

public final class CachedIslandInfo {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public final Location[] islandHomes = new Location[World.Environment.values().length];
    public final List<SuperiorPlayer> members = new ArrayList<>();
    public final List<SuperiorPlayer> bannedPlayers = new ArrayList<>();
    public final Map<SuperiorPlayer, PlayerPermissionNode> playerPermissions = new HashMap<>();
    public final Map<IslandPrivilege, PlayerRole> rolePermissions = new HashMap<>();
    public final Map<String, Integer> upgrades = new HashMap<>();
    public final KeyMap<UpgradeValue<Integer>> blockLimits = KeyMapImpl.createHashMap();
    public final Map<UUID, Rating> ratings = new HashMap<>();
    public final Map<Mission<?>, Integer> completedMissions = new HashMap<>();
    public final Map<IslandFlag, Byte> islandFlags = new HashMap<>();
    public final KeyMap<UpgradeValue<Integer>>[] cobbleGeneratorValues = new KeyMap[World.Environment.values().length];
    public final List<Pair<SuperiorPlayer, Long>> uniqueVisitors = new ArrayList<>();
    public final KeyMap<UpgradeValue<Integer>> entityLimits = KeyMapImpl.createHashMap();
    public final Map<PotionEffectType, UpgradeValue<Integer>> islandEffects = new HashMap<>();
    public final List<ItemStack[]> islandChests = new ArrayList<>(plugin.getSettings().getIslandChests().getDefaultPages());
    public final Map<PlayerRole, UpgradeValue<Integer>> roleLimits = new HashMap<>();
    public final Location[] visitorHomes = new Location[World.Environment.values().length];
    public UpgradeValue<Integer> islandSize = UpgradeValue.NEGATIVE;
    public UpgradeValue<Integer> warpsLimit = UpgradeValue.NEGATIVE;
    public UpgradeValue<Integer> teamLimit = UpgradeValue.NEGATIVE;
    public UpgradeValue<Integer> coopLimit = UpgradeValue.NEGATIVE;
    public UpgradeValue<Double> cropGrowth = UpgradeValue.NEGATIVE_DOUBLE;
    public UpgradeValue<Double> spawnerRates = UpgradeValue.NEGATIVE_DOUBLE;
    public UpgradeValue<Double> mobDrops = UpgradeValue.NEGATIVE_DOUBLE;
    public UpgradeValue<BigDecimal> bankLimit = new UpgradeValue<>(new BigDecimal(-2), true);
    public BigDecimal balance = BigDecimal.ZERO;
    public long lastInterestTime = 0L;
    public List<CachedWarpInfo> cachedWarpInfoList = new ArrayList<>();
    public List<CachedWarpCategoryInfo> cachedWarpCategoryInfoList = new ArrayList<>();
    public List<BankTransaction> bankTransactions = new ArrayList<>();

    public CachedIslandInfo() {

    }

}
