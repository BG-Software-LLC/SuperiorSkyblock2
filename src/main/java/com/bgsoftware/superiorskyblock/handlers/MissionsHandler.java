package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.MissionsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.exceptions.HandlerLoadException;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class MissionsHandler extends AbstractHandler implements MissionsManager {

    private final static ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    private final static Registry<String, Mission<?>> missionMap = Registry.createRegistry();
    private final static Registry<Mission<?>, MissionData> missionDataMap = Registry.createRegistry();

    public MissionsHandler(SuperiorSkyblockPlugin plugin){
        super(plugin);
    }

    @Override
    @SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
    public void loadData(){
        File missionsDict = new File(plugin.getDataFolder(), "missions");
        File file = new File(plugin.getDataFolder(), "missions/missions.yml");

        if(!missionsDict.exists())
            missionsDict.mkdirs();

        FileUtils.copyResource("missions/BlocksMissions");
        FileUtils.copyResource("missions/CraftingMissions");
        FileUtils.copyResource("missions/EnchantingMissions");
        FileUtils.copyResource("missions/IslandMissions");
        FileUtils.copyResource("missions/ItemsMissions");
        FileUtils.copyResource("missions/KillsMissions");
        FileUtils.copyResource("missions/StatisticsMissions");

        if(!file.exists())
            FileUtils.saveResource("missions/missions.yml");

        YamlConfiguration cfg = new YamlConfiguration();

        try {
            FileInputStream stream = new FileInputStream(file);
            cfg.load((new InputStreamReader(stream, Charsets.UTF_8)));
        }catch(Exception ex){
            cfg = YamlConfiguration.loadConfiguration(file);
        }

        List<Mission<?>> missionsToLoad = new ArrayList<>();

        for(String missionName : cfg.getConfigurationSection("").getKeys(false)){
            ConfigurationSection missionSection = cfg.getConfigurationSection(missionName);
            try {
                Mission<?> mission = missionMap.get(missionName.toLowerCase());

                if(mission == null) {
                    File missionJar = new File(missionsDict, missionSection.getString("mission-file") + ".jar");
                    Optional<Class<?>> missionClass = FileUtils.getClasses(missionJar.toURL(), Mission.class).stream().findFirst();

                    if (!missionClass.isPresent())
                        throw new NullPointerException("The mission file " + missionJar.getName() + " is not valid.");

                    boolean islandMission = missionSection.getBoolean("island", false);
                    List<String> requiredMissions = missionSection.getStringList("required-missions");
                    List<String> requiredChecks = missionSection.getStringList("required-checks");

                    boolean onlyShowIfRequiredCompleted = missionSection.contains("only-show-if-required-completed") &&
                            missionSection.getBoolean("only-show-if-required-completed");

                    mission = createInstance(missionClass.get(), missionName, islandMission, requiredMissions, requiredChecks, onlyShowIfRequiredCompleted);
                    mission.load(plugin, missionSection);
                    missionMap.add(missionName.toLowerCase(), mission);
                    missionsToLoad.add(mission);
                }

                missionDataMap.add(mission, new MissionData(mission, missionSection));

                SuperiorSkyblockPlugin.log("Registered mission " + missionName);
            }catch(Exception ex){
                SuperiorSkyblockPlugin.log("Couldn't register mission " + missionName + ": ");
                new HandlerLoadException(ex, "Couldn't register mission " + missionName + ".", HandlerLoadException.ErrorLevel.CONTINUE).printStackTrace();
            }
        }

        Executor.sync(() -> loadMissionsData(missionsToLoad), 10L);
        Executor.asyncTimer(this::saveMissionsData, 6000L); // Save missions data every 5 minutes
    }

    @Override
    public Mission<?> getMission(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        return missionMap.get(name.toLowerCase());
    }

    @Override
    public List<Mission<?>> getAllMissions() {
        return getFilteredMissions(missionData -> true);
    }

    @Override
    public List<Mission<?>> getPlayerMissions() {
        return getFilteredMissions(missionData -> !missionData.islandMission);
    }

    @Override
    public List<Mission<?>> getIslandMissions() {
        return getFilteredMissions(missionData -> missionData.islandMission);
    }

    @Override
    public boolean hasCompleted(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");

        Optional<MissionData> missionDataOptional = getMissionData(mission);

        if(!missionDataOptional.isPresent())
            return false;

        MissionData missionData = missionDataOptional.get();

        Island playerIsland = superiorPlayer.getIsland();

        if(missionData.islandMission){
            if(playerIsland != null)
                return playerIsland.hasCompletedMission(mission);
        }
        else{
            return superiorPlayer.hasCompletedMission(mission);
        }

        return false;
    }

    @Override
    public boolean canCompleteAgain(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");

        Optional<MissionData> missionDataOptional = getMissionData(mission);

        if(!missionDataOptional.isPresent())
            return false;

        MissionData missionData = missionDataOptional.get();

        Island playerIsland = superiorPlayer.getIsland();

        if(missionData.islandMission){
            if(playerIsland != null)
                return playerIsland.canCompleteMissionAgain(mission);
        }
        else{
            return superiorPlayer.canCompleteMissionAgain(mission);
        }

        return false;
    }

    @Override
    public boolean canComplete(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        return canCompleteNoProgress(superiorPlayer, mission) && mission.getProgress(superiorPlayer) >= 1.0;
    }

    @Override
    public boolean canCompleteNoProgress(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        return canCompleteAgain(superiorPlayer, mission) && hasAllRequiredMissions(superiorPlayer, mission) &&
                canPassAllChecks(superiorPlayer, mission);
    }

    @Override
    public boolean hasAllRequiredMissions(SuperiorPlayer superiorPlayer, Mission<?> mission){
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        return mission.getRequiredMissions().stream().allMatch(_mission -> {
            Mission<?> missionToCheck = _mission == null ? null : plugin.getMissions().getMission(_mission);
            return missionToCheck != null && hasCompleted(superiorPlayer, missionToCheck);
        });
    }

    @Override
    public boolean canPassAllChecks(SuperiorPlayer superiorPlayer, Mission<?> mission){
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        return mission.getRequiredChecks().stream().allMatch(check -> {
            check = PlaceholderHook.parse(superiorPlayer, check);
            try {
                return Boolean.parseBoolean(engine.eval(check) + "");
            }catch(Throwable ex){
                return false;
            }
        });
    }

    @Override
    public void rewardMission(Mission<?> mission, SuperiorPlayer superiorPlayer, boolean checkAutoReward) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        rewardMission(mission, superiorPlayer, checkAutoReward, false);
    }

    @Override
    public void rewardMission(Mission<?> mission, SuperiorPlayer superiorPlayer, boolean checkAutoReward, boolean forceReward) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        rewardMission(mission, superiorPlayer, checkAutoReward, forceReward, null);
    }

    @Override
    public void rewardMission(Mission<?> mission, SuperiorPlayer superiorPlayer, boolean checkAutoReward, boolean forceReward, @Nullable Consumer<Boolean> result) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");

        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> rewardMission(mission, superiorPlayer, checkAutoReward, forceReward, result));
            return;
        }

        Optional<MissionData> missionDataOptional = getMissionData(mission);

        if(!missionDataOptional.isPresent()) {
            if(result != null)
                result.accept(false);
            return;
        }

        SuperiorSkyblockPlugin.debug("Action: Reward Mission, Mission: " + mission.getName() + ", Target: " + superiorPlayer.getName() + ", Auto Reward: " + checkAutoReward + ", Force Reward: " + forceReward);

        synchronized (superiorPlayer) {
            MissionData missionData = missionDataOptional.get();
            Island playerIsland = superiorPlayer.getIsland();

            if (!forceReward) {
                if (!canCompleteAgain(superiorPlayer, mission)) {
                    mission.onCompleteFail(superiorPlayer);
                    if(result != null)
                        result.accept(false);
                    return;
                }

                if (!canComplete(superiorPlayer, mission)) {
                    if(result != null)
                        result.accept(false);
                    return;
                }

                if (missionData.islandMission && playerIsland == null) {
                    mission.onCompleteFail(superiorPlayer);
                    if(result != null)
                        result.accept(false);
                    throw new IllegalStateException("Cannot reward island mission " + mission.getName() + " as the player " + superiorPlayer.getName() + " does not have island.");
                }

                if (checkAutoReward && !isAutoReward(mission)) {
                    if (canCompleteAgain(superiorPlayer, mission)) {
                        Locale.MISSION_NO_AUTO_REWARD.send(superiorPlayer, mission.getName());
                        if(result != null)
                            result.accept(false);
                        return;
                    }
                }
            }

            List<ItemStack> itemRewards = new ArrayList<>();
            List<String> commandRewards;

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (missionData) {
                missionData.itemRewards.forEach(itemStack -> itemRewards.add(itemStack.clone()));
                commandRewards = new ArrayList<>(missionData.commandRewards);
            }

            EventResult<Pair<List<ItemStack>, List<String>>> event = EventsCaller
                    .callMissionCompleteEvent(superiorPlayer, mission, missionData.islandMission, itemRewards, commandRewards);

            if (event.isCancelled()) {
                if (!forceReward)
                    mission.onCompleteFail(superiorPlayer);
                if(result != null)
                    result.accept(false);
                return;
            }

            if (!forceReward)
                mission.onComplete(superiorPlayer);

            if (missionData.islandMission) {
                assert playerIsland != null;
                playerIsland.completeMission(mission);
            } else {
                superiorPlayer.completeMission(mission);
            }

            if(result != null)
                result.accept(true);

            for (ItemStack itemStack : event.getResult().getKey()) {
                ItemStack toGive = new ItemBuilder(itemStack)
                        .replaceAll("{0}", mission.getName())
                        .replaceAll("{1}", superiorPlayer.getName())
                        .replaceAll("{2}", playerIsland == null ? "" : playerIsland.getName().isEmpty() ?
                                playerIsland.getOwner() == null ? "" : playerIsland.getOwner().getName() : playerIsland.getName())
                        .build();
                toGive.setAmount(itemStack.getAmount());
                superiorPlayer.runIfOnline(player -> player.getInventory().addItem(toGive));
            }

            Executor.sync(() -> {
                for (String command : event.getResult().getValue()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                            .replace("%mission%", mission.getName())
                            .replace("%player%", superiorPlayer.getName())
                            .replace("%island%", playerIsland == null ? "" : playerIsland.getName().isEmpty() ?
                                    playerIsland.getOwner() == null ? "" : playerIsland.getOwner().getName() : playerIsland.getName())
                    );
                }
            });
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void saveMissionsData() {
        File file = new File(plugin.getDataFolder(), "missions/_data.yml");

        if(!file.exists()){
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }

        YamlConfiguration data = new YamlConfiguration();

        for(Mission<?> mission : getAllMissions()){
            ConfigurationSection section = data.createSection(mission.getName());
            mission.saveProgress(section);
            data.set(mission.getName(), section);
        }

        try{
            data.save(file);
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void loadMissionsData() {
        loadMissionsData(getAllMissions());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void loadMissionsData(List<Mission<?>> missionsList) {
        Preconditions.checkNotNull(missionsList, "missionsList parameter cannot be null.");

        File file = new File(plugin.getDataFolder(), "missions/_data.yml");

        if(!file.exists()){
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }

        YamlConfiguration data = YamlConfiguration.loadConfiguration(file);

        for(Mission<?> mission : missionsList){
            if(data.contains(mission.getName()))
                mission.loadProgress(data.getConfigurationSection(mission.getName()));
        }
    }

    public Optional<MissionData> getMissionData(Mission<?> mission){
        MissionData missionData = missionDataMap.get(mission);

        if(missionData == null){
            missionDataMap.remove(mission);
            if(mission != null)
                missionMap.remove(mission.getName());
            return Optional.empty();
        }

        return Optional.of(missionData);
    }

    private boolean isAutoReward(Mission<?> mission){
        Optional<MissionData> missionDataOptional = getMissionData(mission);
        return missionDataOptional.isPresent() && missionDataOptional.get().autoReward;
    }

    private List<Mission<?>> getFilteredMissions(Predicate<MissionData> predicate) {
        return missionDataMap.values().stream().filter(predicate)
                .sorted(Comparator.comparingInt(o -> o.index))
                .map(missionData -> missionData.mission)
                .collect(Collectors.toList());
    }

    private Mission<?> createInstance(Class<?> clazz, String name, boolean islandMission, List<String> requiredMissions, List<String> requiredChecks, boolean onlyShowIfRequiredCompleted) throws Exception{
        Preconditions.checkArgument(Mission.class.isAssignableFrom(clazz), "Class " + clazz + " is not a Mission.");

        for(Constructor<?> constructor : clazz.getConstructors()){
            if(constructor.getParameterCount() == 0) {
                if(!constructor.isAccessible())
                    constructor.setAccessible(true);

                Mission<?> mission = (Mission<?>) constructor.newInstance();
                mission.setName(name);
                mission.setIslandMission(islandMission);
                mission.addRequiredMission(requiredMissions.toArray(new String[0]));
                mission.addRequiredCheck(requiredChecks.toArray(new String[0]));
                if(onlyShowIfRequiredCompleted)
                    mission.toggleOnlyShowIfRequiredCompleted();

                return mission;
            }
        }

        throw new IllegalArgumentException("Class " + clazz + " has no valid constructors.");
    }

    private static int currentIndex = 0;

    public static class MissionData{

        private final int index;
        private final Mission<?> mission;
        private final List<ItemStack> itemRewards = new ArrayList<>();
        private final List<String> commandRewards = new ArrayList<>();
        private final boolean autoReward, islandMission;
        public final boolean disbandReset, leaveReset;
        public final ItemBuilder notCompleted, canComplete, completed;
        public final int resetAmount;

        MissionData(Mission<?> mission, ConfigurationSection section){
            this.index = currentIndex++;
            this.mission = mission;
            this.islandMission = section.getBoolean("island", false);
            this.autoReward = section.getBoolean("auto-reward", true);
            this.disbandReset = section.getBoolean("disband-reset", false);
            this.leaveReset = section.getBoolean("leave-reset", false);
            this.resetAmount = section.getInt("reset-amount", 1);

            if(section.contains("rewards.items")){
                for(String key : section.getConfigurationSection("rewards.items").getKeys(false)) {
                    ItemStack itemStack = FileUtils.getItemStack("missions.yml", section.getConfigurationSection("rewards.items." + key)).build();
                    itemStack.setAmount(section.getInt("rewards.items." + key + ".amount", 1));
                    this.itemRewards.add(itemStack);
                }
            }

            this.commandRewards.addAll(section.getStringList("rewards.commands"));

            this.notCompleted = FileUtils.getItemStack("missions.yml", section.getConfigurationSection("icons.not-completed"));
            this.canComplete = FileUtils.getItemStack("missions.yml", section.getConfigurationSection("icons.can-complete"));
            this.completed = FileUtils.getItemStack("missions.yml", section.getConfigurationSection("icons.completed"));
        }

        @Override
        public String toString() {
            return "MissionData{name=" + mission.getName() + "}";
        }
    }

}
