package com.bgsoftware.superiorskyblock.mission;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.MissionsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.handler.AbstractHandler;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import com.bgsoftware.superiorskyblock.mission.container.MissionsContainer;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.handler.HandlerLoadException;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class MissionsHandler extends AbstractHandler implements MissionsManager {

    private final MissionsContainer missionsContainer;

    public MissionsHandler(SuperiorSkyblockPlugin plugin, MissionsContainer missionsContainer) {
        super(plugin);
        this.missionsContainer = missionsContainer;
    }

    @Override
    public void loadData() {
        if (!BuiltinModules.MISSIONS.isEnabled())
            return;

        Executor.asyncTimer(this::saveMissionsData, 6000L); // Save missions data every 5 minutes
    }

    @Override
    public Mission<?> getMission(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
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

    @Override
    public boolean hasCompleted(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");

        Optional<MissionData> missionDataOptional = getMissionData(mission);

        if (!missionDataOptional.isPresent())
            return false;

        MissionData missionData = missionDataOptional.get();

        Island playerIsland = superiorPlayer.getIsland();

        if (missionData.isIslandMission()) {
            if (playerIsland != null)
                return playerIsland.hasCompletedMission(mission);
        } else {
            return superiorPlayer.hasCompletedMission(mission);
        }

        return false;
    }

    @Override
    public boolean canCompleteAgain(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");

        Optional<MissionData> missionDataOptional = getMissionData(mission);

        if (!missionDataOptional.isPresent())
            return false;

        MissionData missionData = missionDataOptional.get();

        Island playerIsland = superiorPlayer.getIsland();

        if (missionData.isIslandMission()) {
            if (playerIsland != null)
                return playerIsland.canCompleteMissionAgain(mission);
        } else {
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
    public boolean hasAllRequiredMissions(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        return mission.getRequiredMissions().stream().allMatch(_mission -> {
            Mission<?> missionToCheck = _mission == null ? null : plugin.getMissions().getMission(_mission);
            return missionToCheck != null && hasCompleted(superiorPlayer, missionToCheck);
        });
    }

    @Override
    public boolean canPassAllChecks(SuperiorPlayer superiorPlayer, Mission<?> mission) {
        Preconditions.checkNotNull(superiorPlayer, "superiorPlayer parameter cannot be null.");
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        return mission.getRequiredChecks().stream().allMatch(check -> {
            check = PlaceholderHook.parse(superiorPlayer, check);
            try {
                return Boolean.parseBoolean(plugin.getScriptEngine().eval(check) + "");
            } catch (ScriptException ex) {
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

        if (Bukkit.isPrimaryThread()) {
            Executor.async(() -> rewardMission(mission, superiorPlayer, checkAutoReward, forceReward, result));
            return;
        }

        Optional<MissionData> missionDataOptional = getMissionData(mission);

        if (!missionDataOptional.isPresent()) {
            if (result != null)
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
                    if (result != null)
                        result.accept(false);
                    return;
                }

                if (!canComplete(superiorPlayer, mission)) {
                    if (result != null)
                        result.accept(false);
                    return;
                }

                if (missionData.isIslandMission() && playerIsland == null) {
                    mission.onCompleteFail(superiorPlayer);
                    if (result != null)
                        result.accept(false);
                    throw new IllegalStateException("Cannot reward island mission " + mission.getName() + " as the player " + superiorPlayer.getName() + " does not have island.");
                }

                if (checkAutoReward && !isAutoReward(mission)) {
                    if (canCompleteAgain(superiorPlayer, mission)) {
                        Locale.MISSION_NO_AUTO_REWARD.send(superiorPlayer, mission.getName());
                        if (result != null)
                            result.accept(false);
                        return;
                    }
                }
            }

            List<ItemStack> itemRewards = new ArrayList<>();
            List<String> commandRewards;

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (missionData) {
                missionData.getItemRewards().forEach(itemStack -> itemRewards.add(itemStack.clone()));
                commandRewards = new ArrayList<>(missionData.getCommandRewards());
            }

            EventResult<Pair<List<ItemStack>, List<String>>> event = EventsCaller.callMissionCompleteEvent(
                    superiorPlayer, mission, missionData.isIslandMission(), itemRewards, commandRewards);

            if (event.isCancelled()) {
                if (!forceReward)
                    mission.onCompleteFail(superiorPlayer);
                if (result != null)
                    result.accept(false);
                return;
            }

            if (!forceReward)
                mission.onComplete(superiorPlayer);

            if (missionData.isIslandMission()) {
                assert playerIsland != null;
                playerIsland.completeMission(mission);
            } else {
                superiorPlayer.completeMission(mission);
            }

            if (result != null)
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
        File file = new File(BuiltinModules.MISSIONS.getDataFolder(), "_data.yml");

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        YamlConfiguration data = new YamlConfiguration();

        for (Mission<?> mission : getAllMissions()) {
            ConfigurationSection section = data.createSection(mission.getName());
            mission.saveProgress(section);
            data.set(mission.getName(), section);
        }

        try {
            data.save(file);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void loadMissionsData() {
        loadMissionsData(getAllMissions());
    }

    public boolean canDisplayMission(Mission<?> mission, SuperiorPlayer superiorPlayer, boolean removeCompleted){
        if(mission.isOnlyShowIfRequiredCompleted()) {
            if (!hasAllRequiredMissions(superiorPlayer, mission))
                return false;

            if (!canPassAllChecks(superiorPlayer, mission))
                return false;
        }

        if(removeCompleted){
            if(mission.getIslandMission() ? superiorPlayer.getIsland() != null &&
                    !superiorPlayer.getIsland().canCompleteMissionAgain(mission) :
                    !superiorPlayer.canCompleteMissionAgain(mission))
                return false;
        }

        return true;
    }

    @SuppressWarnings("deprecation")
    public Mission<?> loadMission(String missionName, File missionsFolder, ConfigurationSection missionSection) {
        Mission<?> newMission = null;

        try {
            Mission<?> mission = plugin.getMissions().getMission(missionName);

            if (mission == null) {
                File missionJar = new File(missionsFolder, missionSection.getString("mission-file") + ".jar");
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
                this.missionsContainer.addMission(mission);
                newMission = mission;
            }

            this.missionsContainer.addMissionData(new MissionData(mission, missionSection));

            SuperiorSkyblockPlugin.log("Registered mission " + missionName);
        } catch (Exception ex) {
            SuperiorSkyblockPlugin.log("Couldn't register mission " + missionName + ": ");
            new HandlerLoadException(ex, "Couldn't register mission " + missionName + ".", HandlerLoadException.ErrorLevel.CONTINUE).printStackTrace();
        }

        return newMission;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void loadMissionsData(List<Mission<?>> missionsList) {
        Preconditions.checkNotNull(missionsList, "missionsList parameter cannot be null.");

        File file = new File(BuiltinModules.MISSIONS.getDataFolder(), "_data.yml");

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        YamlConfiguration data = YamlConfiguration.loadConfiguration(file);

        for (Mission<?> mission : missionsList) {
            if (data.contains(mission.getName()))
                mission.loadProgress(data.getConfigurationSection(mission.getName()));
        }
    }

    public Optional<MissionData> getMissionData(Mission<?> mission) {
        return Optional.ofNullable(this.missionsContainer.getMissionData(mission));
    }

    private boolean isAutoReward(Mission<?> mission) {
        Optional<MissionData> missionDataOptional = getMissionData(mission);
        return missionDataOptional.isPresent() && missionDataOptional.get().isAutoReward();
    }

    private Mission<?> createInstance(Class<?> clazz, String name, boolean islandMission, List<String> requiredMissions, List<String> requiredChecks, boolean onlyShowIfRequiredCompleted) throws Exception {
        Preconditions.checkArgument(Mission.class.isAssignableFrom(clazz), "Class " + clazz + " is not a Mission.");

        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                if (!constructor.isAccessible())
                    constructor.setAccessible(true);

                Mission<?> mission = (Mission<?>) constructor.newInstance();
                mission.setName(name);
                mission.setIslandMission(islandMission);
                mission.addRequiredMission(requiredMissions.toArray(new String[0]));
                mission.addRequiredCheck(requiredChecks.toArray(new String[0]));
                if (onlyShowIfRequiredCompleted)
                    mission.toggleOnlyShowIfRequiredCompleted();

                return mission;
            }
        }

        throw new IllegalArgumentException("Class " + clazz + " has no valid constructors.");
    }

}
