package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.MissionCompleteEvent;
import com.bgsoftware.superiorskyblock.api.handlers.MissionsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

public final class MissionsHandler implements MissionsManager {

    private static Map<String, Mission> missionMap = new HashMap<>();
    private static Map<Mission, MissionData> missionDataMap = new HashMap<>();

    @SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
    public MissionsHandler(SuperiorSkyblockPlugin plugin){
        File missionsDict = new File(plugin.getDataFolder(), "missions");
        File file = new File(plugin.getDataFolder(), "missions/missions.yml");

        if(!missionsDict.exists()){
            missionsDict.mkdirs();
            plugin.saveResource("missions/CraftingMissions.jar", true);
            plugin.saveResource("missions/IslandMissions.jar", true);
            plugin.saveResource("missions/ItemsMissions.jar", true);
        }

        if(!file.exists())
            plugin.saveResource("missions/missions.yml", true);

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        for(String missionName : cfg.getConfigurationSection("").getKeys(false)){
            ConfigurationSection missionSection = cfg.getConfigurationSection(missionName);
            try {
                Mission mission = missionMap.get(missionName.toLowerCase());

                if(mission == null) {
                    File missionJar = new File(missionsDict, missionSection.getString("mission-file") + ".jar");
                    Optional<Class> missionClass = getMissionClasses(missionJar.toURL()).stream().findFirst();

                    if (!missionClass.isPresent())
                        throw new NullPointerException("The mission file " + missionJar.getName() + " is not valid.");

                    mission = createInstance(missionClass.get(), missionName);
                    mission.load(plugin, missionSection);
                    missionMap.put(missionName.toLowerCase(), mission);
                }

                missionDataMap.put(mission, new MissionData(mission, missionSection));

                SuperiorSkyblockPlugin.log("Registered mission " + missionName);
            }catch(Exception ex){
                SuperiorSkyblockPlugin.log("Couldn't register mission " + missionName + ": ");
                ex.printStackTrace();
            }
        }
    }

    @Override
    public Mission getMission(String name) {
        return missionMap.get(name);
    }

    @Override
    public List<Mission> getAllMissions() {
        return getFilteredMissions(missionData -> true);
    }

    @Override
    public List<Mission> getPlayerMissions() {
        return getFilteredMissions(missionData -> !missionData.islandMission);
    }

    @Override
    public List<Mission> getIslandMissions() {
        return getFilteredMissions(missionData -> missionData.islandMission);
    }

    @Override
    public boolean hasCompleted(SuperiorPlayer superiorPlayer, Mission mission) {
        MissionData missionData = getMissionData(mission);
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
    public void rewardMission(Mission mission, SuperiorPlayer superiorPlayer, boolean checkAutoReward) {
        MissionData missionData = getMissionData(mission);
        Island playerIsland = superiorPlayer.getIsland();

        if(hasCompleted(superiorPlayer, mission)) {
            mission.onCompleteFail(superiorPlayer);
            return;
        }

        if(missionData.islandMission && playerIsland == null) {
            mission.onCompleteFail(superiorPlayer);
            throw new IllegalStateException("Cannot reward island mission " + mission.getName() + " as the player " + superiorPlayer.getName() + " does not have island.");
        }

        if(checkAutoReward && !isAutoReward(mission)){
            if(!hasCompleted(superiorPlayer, mission)){
                Locale.MISSION_NO_AUTO_REWARD.send(superiorPlayer, mission.getName());
                return;
            }
        }

        List<ItemStack> itemRewards = new ArrayList<>();
        missionData.itemRewards.forEach(itemStack -> itemRewards.add(itemStack.clone()));
        List<String> commandRewards = new ArrayList<>(missionData.commandRewards);

        MissionCompleteEvent missionCompleteEvent = new MissionCompleteEvent(superiorPlayer, mission, missionData.islandMission, itemRewards, commandRewards);
        Bukkit.getPluginManager().callEvent(missionCompleteEvent);

        if(missionCompleteEvent.isCancelled()){
            mission.onCompleteFail(superiorPlayer);
            return;
        }

        mission.onComplete(superiorPlayer);

        for(ItemStack itemStack : missionCompleteEvent.getItemRewards()){
            ItemStack toGive = new ItemBuilder(itemStack)
                    .replaceAll("{0}", mission.getName())
                    .replaceAll("{1}", superiorPlayer.getName())
                    .replaceAll("{2}", playerIsland == null ? "" : playerIsland.getName().isEmpty() ? playerIsland.getOwner().getName() : playerIsland.getName())
                    .build();
            toGive.setAmount(itemStack.getAmount());
            superiorPlayer.asPlayer().getInventory().addItem(toGive);
        }

        for(String command : missionCompleteEvent.getCommandRewards()){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command
                    .replace("%mission%", mission.getName())
                    .replace("%player%", superiorPlayer.getName())
                    .replace("%island%", playerIsland == null ? "" : playerIsland.getName().isEmpty() ? playerIsland.getOwner().getName() : playerIsland.getName())
            );
        }

        if(missionData.islandMission){
            playerIsland.completeMission(mission);
        }
        else{
            superiorPlayer.completeMission(mission);
        }
    }

    public MissionData getMissionData(Mission mission){
        return missionDataMap.get(mission);
    }

    private boolean isAutoReward(Mission mission){
        return getMissionData(mission).autoReward;
    }

    private List<Mission> getFilteredMissions(Predicate<MissionData> predicate) {
        return missionDataMap.values().stream().filter(predicate)
                .sorted(Comparator.comparingInt(o -> o.index))
                .map(missionData -> missionData.mission)
                .collect(Collectors.toList());
    }

    private List<Class> getMissionClasses(URL jar) {
        List<Class> list = new ArrayList<>();

        try (URLClassLoader cl = new URLClassLoader(new URL[]{jar}, Mission.class.getClassLoader()); JarInputStream jis = new JarInputStream(jar.openStream())) {
            JarEntry jarEntry;
            while ((jarEntry = jis.getNextJarEntry()) != null){
                String name = jarEntry.getName();

                if (name == null || name.isEmpty() || !name.endsWith(".class")) {
                    continue;
                }

                name = name.replace("/", ".");
                String clazzName = name.substring(0, name.lastIndexOf(".class"));

                Class c = cl.loadClass(clazzName);

                if (Mission.class.isAssignableFrom(c)) {
                    list.add(c);
                }
            }
        } catch (Throwable ignored) { }

        return list;
    }

    private Mission createInstance(Class clazz, String name) throws Exception{
        if(!Mission.class.isAssignableFrom(clazz))
            throw new IllegalArgumentException("Class " + clazz + " is not a Mission.");

        for(Constructor constructor : clazz.getConstructors()){
            if(constructor.getParameterCount() == 0) {
                if(!constructor.isAccessible())
                    constructor.setAccessible(true);

                Mission mission = (Mission) constructor.newInstance();
                mission.setName(name);

                return mission;
            }
        }

        throw new IllegalArgumentException("Class " + clazz + " has no valid constructors.");
    }

    private static int currentIndex = 0;

    public static class MissionData{

        private final int index;
        private final Mission mission;
        private final List<ItemStack> itemRewards = new ArrayList<>();
        private final List<String> commandRewards = new ArrayList<>();
        private final boolean autoReward;
        private final boolean islandMission;
        public final ItemStack notCompleted, canComplete, completed;

        MissionData(Mission mission, ConfigurationSection section){
            this.index = currentIndex++;
            this.mission = mission;
            this.islandMission = section.getBoolean("island", false);
            this.autoReward = section.getBoolean("auto-reward", true);

            if(section.contains("rewards.items")){
                for(String key : section.getConfigurationSection("rewards.items").getKeys(false)) {
                    ItemStack itemStack = FileUtil.getItemStack(section.getConfigurationSection("rewards.items." + key));
                    itemStack.setAmount(section.getInt("rewards.items." + key + ".amount", 1));
                    this.itemRewards.add(itemStack);
                }
            }

            this.commandRewards.addAll(section.getStringList("rewards.commands"));

            this.notCompleted = FileUtil.getItemStack(section.getConfigurationSection("icons.not-completed"));
            this.canComplete = FileUtil.getItemStack(section.getConfigurationSection("icons.can-complete"));
            this.completed = FileUtil.getItemStack(section.getConfigurationSection("icons.completed"));
        }

    }

}
