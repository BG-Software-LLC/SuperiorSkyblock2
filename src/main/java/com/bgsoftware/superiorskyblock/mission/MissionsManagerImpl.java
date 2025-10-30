package com.bgsoftware.superiorskyblock.mission;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.MissionsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.IMissionsHolder;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.api.missions.store.UnifiedPayload;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Either;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.io.FileClassLoader;
import com.bgsoftware.superiorskyblock.core.io.Files;
import com.bgsoftware.superiorskyblock.core.io.JarFiles;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.mission.container.MissionsContainer;
import com.bgsoftware.superiorskyblock.mission.store.YamlMissionStore;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URLClassLoader;
import java.util.*;


public class MissionsManagerImpl extends Manager implements MissionsManager {

    private static final Object DATA_FOLDER_MUTEX = new Object();

    private final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
        @Override
        protected PlaceholdersService create() {
            return plugin.getServices().getService(PlaceholdersService.class);
        }
    };

    private final Map<String, FileClassLoader> missionTypesClassLoaders = new HashMap<>();
    private final MissionsContainer missionsContainer;

    public MissionsManagerImpl(SuperiorSkyblockPlugin plugin, MissionsContainer missionsContainer) {
        super(plugin);
        this.missionsContainer = missionsContainer;
    }

    @Override
    public void loadData() {
        if (!BuiltinModules.MISSIONS.isEnabled())
            return;

        BukkitExecutor.asyncTimer(this::saveMissionsData, 6000L);
    }

    public void clearData() {
        saveMissionsData();

        List<Mission<?>> missionsList = plugin.getMissions().getAllMissions();
        this.missionsContainer.clearMissionsData();

        for (Mission<?> mission : missionsList) {
            mission.unload();
        }

        this.missionTypesClassLoaders.forEach((missionJarName, classLoader) -> {
            try {
                classLoader.close();
            } catch (Exception error) {
                Log.error(error, "An error occurred while unloading mission jar ", missionJarName, ":");
            }
        });
        this.missionTypesClassLoaders.clear();

        System.gc();
    }

    @Override
    public Mission<?> getMission(String name) {
        Objects.requireNonNull(name, "name");
        return this.missionsContainer.getMission(name);
    }

    @Override
    public List<Mission<?>> getAllMissions() {
        return this.missionsContainer.getAllMissions();
    }

    @Override
    public List<Mission<?>> getPlayerMissions() {
        return this.missionsContainer.getPlayerMissions();
    }

    @Override
    public List<Mission<?>> getIslandMissions() {
        return this.missionsContainer.getIslandMissions();
    }

    @Nullable
    @Override
    public MissionCategory getMissionCategory(String name) {
        Objects.requireNonNull(name, "name");
        return this.missionsContainer.getMissionCategory(name);
    }

    @Override
    public List<MissionCategory> getMissionCategories() {
        return this.missionsContainer.getMissionCategories();
    }

    @Override
    public boolean hasCompleted(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Objects.requireNonNull(superiorPlayer, "superiorPlayer");
        Objects.requireNonNull(mission, "mission");
        Optional<MissionData> missionDataOptional = getMissionData(mission);
        if (!missionDataOptional.isPresent()) return false;

        MissionData missionData = missionDataOptional.get();
        Island playerIsland = superiorPlayer.getIsland();
        if (missionData.isIslandMission()) {
            if (playerIsland != null) return playerIsland.hasCompletedMission(mission);
        } else {
            return superiorPlayer.hasCompletedMission(mission);
        }
        return false;
    }

    @Override
    public boolean canComplete(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Objects.requireNonNull(superiorPlayer, "superiorPlayer");
        Objects.requireNonNull(mission, "mission");
        return canCompleteNoProgress(superiorPlayer, mission) && mission.getProgress(superiorPlayer) >= 1.0;
    }

    @Override
    public boolean canCompleteNoProgress(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Objects.requireNonNull(superiorPlayer, "superiorPlayer");
        Objects.requireNonNull(mission, "mission");
        return canCompleteAgain(superiorPlayer, mission) && hasAllRequiredMissions(superiorPlayer, mission) &&
                canPassAllChecks(superiorPlayer, mission);
    }

    @Override
    public boolean canCompleteAgain(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Objects.requireNonNull(superiorPlayer, "superiorPlayer");
        Objects.requireNonNull(mission, "mission");
        Optional<MissionData> missionDataOptional = getMissionData(mission);
        if (!missionDataOptional.isPresent()) return false;
        MissionData missionData = missionDataOptional.get();
        Island playerIsland = superiorPlayer.getIsland();
        if (missionData.isIslandMission()) {
            if (playerIsland != null) return playerIsland.canCompleteMissionAgain(mission);
        } else {
            return superiorPlayer.canCompleteMissionAgain(mission);
        }
        return false;
    }

    @Override
    public boolean hasAllRequiredMissions(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Objects.requireNonNull(superiorPlayer, "superiorPlayer");
        Objects.requireNonNull(mission, "mission");
        return mission.getRequiredMissions().stream().allMatch(_mission -> {
            Mission<?> missionToCheck = _mission == null ? null : plugin.getMissions().getMission(_mission);
            return missionToCheck != null && hasCompleted(superiorPlayer, missionToCheck);
        });
    }

    @Override
    public boolean canPassAllChecks(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Objects.requireNonNull(superiorPlayer, "superiorPlayer");
        Objects.requireNonNull(mission, "mission");
        OfflinePlayer offlinePlayer = superiorPlayer.asOfflinePlayer();
        return offlinePlayer != null && mission.getRequiredChecks().stream().allMatch(check -> {
            check = this.placeholdersService.get().parsePlaceholders(offlinePlayer, check);
            try {
                return Boolean.parseBoolean(plugin.getScriptEngine().eval(check) + "");
            } catch (ScriptException error) {
                Log.entering("ENTER", superiorPlayer.getName(), mission.getName());
                Log.error(error, "An unexpected error occurred while checking mission requirements:");
                return false;
            }
        });
    }

    @Override
    public void rewardMission(Mission<?> mission, SuperiorPlayer superiorPlayer, boolean checkAutoReward) {
        rewardMission(mission, superiorPlayer, checkAutoReward, false);
    }

    @Override
    public void rewardMission(Mission<?> mission, SuperiorPlayer superiorPlayer, boolean checkAutoReward, boolean forceReward) {
        rewardMission(mission, superiorPlayer, checkAutoReward, forceReward, null);
    }

    @Override
    public void rewardMission(Mission<?> mission, SuperiorPlayer superiorPlayer,
                              boolean checkAutoReward, boolean forceReward, @Nullable java.util.function.Consumer<Boolean> result) {
        if (Bukkit.isPrimaryThread()) {
            BukkitExecutor.async(() -> rewardMission(mission, superiorPlayer, checkAutoReward, forceReward, result));
            return;
        }

        Optional<MissionData> missionDataOptional = getMissionData(mission);
        if (!missionDataOptional.isPresent()) {
            if (result != null) result.accept(false);
            return;
        }

        Log.debug(Debug.REWARD_MISSION, mission.getName(), superiorPlayer.getName(), checkAutoReward, forceReward);

        synchronized (superiorPlayer) {
            MissionData missionData = missionDataOptional.get();
            IMissionsHolder missionsHolder = missionData.isIslandMission() ? superiorPlayer.getIsland() : superiorPlayer;

            if (missionsHolder == null) {
                mission.onCompleteFail(superiorPlayer);
                if (result != null) result.accept(false);
                throw new IllegalStateException("Cannot reward island mission " + mission.getName() + " as the player " + superiorPlayer.getName() + " does not have island.");
            }

            if (!forceReward) {
                if (!canCompleteAgain(superiorPlayer, mission)) {
                    mission.onCompleteFail(superiorPlayer);
                    if (result != null) result.accept(false);
                    return;
                }

                if (!canComplete(superiorPlayer, mission)) {
                    if (result != null) result.accept(false);
                    return;
                }

                if (checkAutoReward) {
                    boolean shouldAutoReward = isAutoReward(mission);
                    if (shouldAutoReward && !BuiltinModules.MISSIONS.getConfiguration().isAutoRewardOutsideIslands() &&
                            !plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld())) {
                        if (result != null) result.accept(false);
                        return;
                    }

                    if (!shouldAutoReward) {
                        if (canCompleteAgain(superiorPlayer, mission)) {
                            Message.MISSION_NO_AUTO_REWARD.send(superiorPlayer, mission.getName());
                            if (result != null) result.accept(false);
                            return;
                        }
                    }
                }
            }

            List<ItemStack> itemRewards = new ArrayList<>();
            List<String> commandRewards;

            synchronized (missionData) {
                for (ItemStack is : missionData.getItemRewards()) itemRewards.add(is.clone());
                commandRewards = new ArrayList<>(missionData.getCommandRewards());
            }

            PluginEvent<PluginEventArgs.MissionComplete> event = PluginEventsFactory.callMissionCompleteEvent(
                    superiorPlayer, missionsHolder, mission, itemRewards, commandRewards);

            if (event.isCancelled()) {
                if (!forceReward) mission.onCompleteFail(superiorPlayer);
                if (result != null) result.accept(false);
                return;
            }

            if (!forceReward) mission.onComplete(superiorPlayer);
            missionsHolder.completeMission(mission);
            if (result != null) result.accept(true);

            for (ItemStack itemStack : event.getArgs().itemRewards) {
                ItemStack toGive = new ItemBuilder(itemStack)
                        .replaceAll("{0}", mission.getName())
                        .replaceAll("{1}", superiorPlayer.getName())
                        .replaceAll("{2}", getIslandPlaceholder(missionsHolder))
                        .build();
                toGive.setAmount(itemStack.getAmount());
                BukkitExecutor.ensureMain(() -> superiorPlayer.runIfOnline(player -> {
                    try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                        BukkitItems.addItem(toGive, player.getInventory(), player.getLocation(wrapper.getHandle()));
                    }
                }));
            }

            BukkitExecutor.ensureMain(() -> {
                for (String command : event.getArgs().commandRewards) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                            .replace("%mission%", mission.getName())
                            .replace("%player%", superiorPlayer.getName())
                            .replace("%island%", getIslandPlaceholder(missionsHolder))
                    );
                }
            });

            for (Mission<?> otherMission : getAllMissions()) {
                if (otherMission.getRequiredMissions().contains(mission.getName()) && otherMission.canComplete(superiorPlayer)) {
                    rewardMission(otherMission, superiorPlayer, true);
                }
            }
        }
    }

    @Override
    public void saveMissionsData() {
        File dataFolder = BuiltinModules.MISSIONS.getDataStoreFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();

        YamlMissionStore store = new YamlMissionStore();

        for (Mission<?> mission : getAllMissions()) {
            File dataFile = new File(dataFolder, mission.getName() + ".yml");

            boolean wrote = false;

            try {
                UnifiedPayload out = new UnifiedPayload();
                out.schema  = 2;
                out.mission = mission.getName();
                out.scope   = mission.getIslandMission() ? "island" : "player";

                mission.saveProgress(out);

                if (hasProgress(out)) {
                    store.write(dataFile, out);
                    wrote = true;
                }
            } catch (Throwable ignored) {
            }

            if (!wrote) {
                YamlConfiguration data = new YamlConfiguration();
                try {
                    mission.saveProgress(data);

                    if (!data.getKeys(true).isEmpty()) {
                        if (!dataFile.exists()) dataFile.createNewFile();
                        synchronized (DATA_FOLDER_MUTEX) {
                            data.save(dataFile);
                        }
                        wrote = true;
                    }
                } catch (Throwable error) {
                    Log.error(error, "An unexpected error while saving mission data for ", mission.getName(), ":");
                }
            }

            // Optionally remove empty files (kept disabled to avoid surprising deletes)
            // if (!wrote && dataFile.exists()) dataFile.delete();
        }
    }

    private static boolean hasProgress(UnifiedPayload out) {
        if (out == null) return false;

        if (out.holders != null && !out.holders.isEmpty()) {
            for (Map.Entry<String, UnifiedPayload.HolderData> e : out.holders.entrySet()) {
                UnifiedPayload.HolderData hd = e.getValue();
                if (hd == null) continue;
                if ((hd.counts != null && !hd.counts.isEmpty()) ||
                        (hd.flags  != null && !hd.flags.isEmpty())  ||
                        (hd.items  != null && !hd.items.isEmpty())  ||
                        (hd.extras != null && !hd.extras.isEmpty())) {
                    return true;
                }
            }
        }


        return out.globals != null && !out.globals.isEmpty();
    }


    @Override
    public void loadMissionsData() {
        loadMissionsData(getAllMissions());
    }

    @Override
    public void loadMissionsData(List<Mission<?>> missionsList) {
        Objects.requireNonNull(missionsList, "missionsList");

        convertOldMissionsData();

        File dataFolder = BuiltinModules.MISSIONS.getDataStoreFolder();
        if (!moveOldDataFolder(dataFolder))
            throw new IllegalStateException("Failed moving old missions folder.");
        if (!dataFolder.exists()) return;

        YamlMissionStore store = new YamlMissionStore();

        for (Mission<?> mission : missionsList) {
            File dataFile = new File(dataFolder, mission.getName() + ".yml");
            if (!dataFile.exists()) continue;

            try {
                UnifiedPayload in = store.read(dataFile);
                if (in != null) {
                    if (in.mission == null) in.mission = mission.getName();
                    if (in.scope == null)   in.scope   = mission.getIslandMission() ? "island" : "player";
                    mission.loadProgress(in);
                    continue;
                }
            } catch (Throwable ignored) {
            }

            try {
                synchronized (DATA_FOLDER_MUTEX) {
                    mission.loadProgress(YamlConfiguration.loadConfiguration(dataFile));
                }
            } catch (Throwable error) {
                Log.error(error, "An unexpected error occurred while loading mission data for ", mission.getName(), ":");
            }
        }
    }

    public void convertPlayerData(SuperiorPlayer oldPlayer, SuperiorPlayer newPlayer) {
        getAllMissions().forEach(mission -> mission.transferData(oldPlayer, newPlayer));

        File dataFolder = BuiltinModules.MISSIONS.getDataStoreFolder();
        if (!dataFolder.exists()) return;

        BukkitExecutor.async(() -> {
            for (File file : com.bgsoftware.superiorskyblock.core.io.Files.listFolderFiles(dataFolder, false)) {
                synchronized (DATA_FOLDER_MUTEX) {
                    Files.replaceString(file, oldPlayer.getUniqueId() + "", newPlayer.getUniqueId() + "");
                }
            }
        });
    }

    public void loadMissionCategory(MissionCategory missionCategory) {
        this.missionsContainer.addMissionCategory(missionCategory);
    }

    public boolean hasAllRequirements(Mission<?> mission, SuperiorPlayer superiorPlayer) {
        return hasAllRequiredMissions(superiorPlayer, mission) && canPassAllChecks(superiorPlayer, mission);
    }

    public boolean canDisplayMission(Mission<?> mission, SuperiorPlayer superiorPlayer, boolean removeCompleted) {
        if (mission.isOnlyShowIfRequiredCompleted() && !hasAllRequirements(mission, superiorPlayer))
            return false;

        if (removeCompleted) {
            return mission.getIslandMission() ? superiorPlayer.getIsland() == null ||
                    superiorPlayer.getIsland().canCompleteMissionAgain(mission) : superiorPlayer.canCompleteMissionAgain(mission);
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    public Mission<?> loadMission(String missionName, String missionCategoryName, com.bgsoftware.superiorskyblock.core.io.loader.FilesLookup filesLookup, ConfigurationSection missionSection) {
        Mission<?> newMission = null;

        try {
            Mission<?> mission = plugin.getMissions().getMission(missionName);

            if (mission == null) {
                String missionJarName = missionSection.getString("mission-file") + ".jar";
                File missionJar = filesLookup.getFile(missionJarName);

                if (!missionJar.exists())
                    throw new RuntimeException("The mission file " + missionJar.getName() + " is not valid.");

                FileClassLoader missionClassLoader = this.missionTypesClassLoaders.computeIfAbsent(missionJarName, n -> {
                    try {
                        return new FileClassLoader(missionJar, plugin.getPluginClassLoader(),
                                plugin.getNMSAlgorithms().getClassProcessor());
                    } catch (IOException error) {
                        throw new RuntimeException(error);
                    }
                });

                Either<Class<?>, Throwable> missionClassLookup = JarFiles.getClass(missionJar.toURL(), Mission.class, missionClassLoader);

                if (missionClassLookup.getRight() != null)
                    throw new RuntimeException("An unexpected error occurred while reading " + missionJar.getName() + ".", missionClassLookup.getRight());

                Class<?> missionClass = missionClassLookup.getLeft();

                if (missionClass == null)
                    throw new RuntimeException("The mission file " + missionJar.getName() + " is not valid.");

                boolean islandMission = missionSection.getBoolean("island", false);
                List<String> requiredMissions = missionSection.getStringList("required-missions");
                List<String> requiredChecks = missionSection.getStringList("required-checks");
                boolean onlyShowIfRequiredCompleted = missionSection.getBoolean("only-show-if-required-completed", false);

                mission = createInstance(missionClass, missionName, islandMission, requiredMissions, requiredChecks, onlyShowIfRequiredCompleted);
                mission.load(plugin, missionSection);
                this.missionsContainer.addMission(mission);
                newMission = mission;
            }

            this.missionsContainer.addMissionData(new MissionData(mission, missionCategoryName, missionSection));

            Log.info("Registered mission " + missionName);
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while registering mission ", missionName, ":");
        }

        return newMission;
    }

    public Optional<MissionData> getMissionData(Mission<?> mission) {
        return Optional.ofNullable(this.missionsContainer.getMissionData(mission));
    }

    private boolean isAutoReward(Mission<?> mission) {
        Optional<MissionData> missionDataOptional = getMissionData(mission);
        return missionDataOptional.isPresent() && missionDataOptional.get().isAutoReward();
    }

    private Mission<?> createInstance(Class<?> clazz, String name, boolean islandMission, List<String> requiredMissions, List<String> requiredChecks, boolean onlyShowIfRequiredCompleted) throws Exception {
        if (!Mission.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Class " + clazz + " is not a Mission.");

        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                if (!constructor.isAccessible()) constructor.setAccessible(true);

                Mission<?> mission = (Mission<?>) constructor.newInstance();
                mission.setName(name);
                mission.setIslandMission(islandMission);
                mission.addRequiredMission(requiredMissions.toArray(new String[0]));
                mission.addRequiredCheck(requiredChecks.toArray(new String[0]));
                if (onlyShowIfRequiredCompleted) mission.toggleOnlyShowIfRequiredCompleted();

                return mission;
            }
        }

        throw new IllegalArgumentException("Class " + clazz + " has no valid constructors.");
    }

    private static String getIslandPlaceholder(@Nullable IMissionsHolder missionsHolder) {
        if (!(missionsHolder instanceof Island)) return "";
        Island island = (Island) missionsHolder;
        return island.getName().isEmpty() ? island.getOwner() == null ? "" :
                island.getOwner().getName() : island.getName();
    }

    private boolean moveOldDataFolder(File newDataFolder) {
        File oldDataFolder = new File(com.bgsoftware.superiorskyblock.module.BuiltinModules.MISSIONS.getModuleFolder(), "data");
        if (!oldDataFolder.exists()) return true;

        newDataFolder.mkdirs();
        for (File file : com.bgsoftware.superiorskyblock.core.io.Files.listFolderFiles(oldDataFolder, false)) {
            File targetFile = new File(newDataFolder, file.getName());
            if (!file.renameTo(targetFile)) return false;
        }
        com.bgsoftware.superiorskyblock.core.io.Files.deleteDirectory(oldDataFolder);
        return true;
    }

    private void convertOldMissionsData() {
        File file = new File(com.bgsoftware.superiorskyblock.module.BuiltinModules.MISSIONS.getModuleFolder(), "_data.yml");
        if (!file.exists()) return;

        File dataFolder = com.bgsoftware.superiorskyblock.module.BuiltinModules.MISSIONS.getDataStoreFolder();
        YamlConfiguration oldData = YamlConfiguration.loadConfiguration(file);

        for (Mission<?> mission : getAllMissions()) {
            if (oldData.isConfigurationSection(mission.getName())) {
                ConfigurationSection dataSection = oldData.getConfigurationSection(mission.getName());
                YamlConfiguration data = convertSectionToYaml(dataSection, new YamlConfiguration());
                if (data.getKeys(true).isEmpty()) continue;

                File dataFile = new File(dataFolder, mission.getName() + ".yml");
                try {
                    if (!dataFile.exists()) {
                        dataFile.getParentFile().mkdirs();
                        dataFile.createNewFile();
                    }
                    data.save(dataFile);
                } catch (IOException error) {
                    Log.error(error, "An unexpected error occurred while saving old mission file ", mission.getName(), ":");
                }
            }
        }
        file.delete();
    }

    private static YamlConfiguration convertSectionToYaml(ConfigurationSection section, YamlConfiguration config) {
        for (String key : section.getKeys(false))
            config.set(key, section.get(key));
        return config;
    }
}
