package com.bgsoftware.superiorskyblock.api.missions;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Mission<V> {

    private final List<String> requiredMissions = new ArrayList<>();
    private final List<String> requiredChecks = new ArrayList<>();
    private final Map<SuperiorPlayer, V> missionData = new ConcurrentHashMap<>();

    private String name = null;
    private Consumer<V> clearMethod = null;
    private boolean onlyShowIfRequiredCompleted = false;
    private boolean islandMission = false;

    /**
     * Set the name of the mission.
     * @param name The name to set.
     */
    public void setName(String name){
        if(this.name == null)
            this.name = name;
    }

    /**
     * Get the name of the mission.
     */
    public String getName(){
        return name;
    }

    /**
     * Set whether or not this mission is an island mission or not.
     * @param islandMission The island-mission status.
     */
    public void setIslandMission(boolean islandMission){
        this.islandMission = islandMission;
    }

    /**
     * Get the island-mission of the mission.
     */
    public boolean getIslandMission(){
        return islandMission;
    }

    /**
     * Set the clear method for the data object.
     */
    public void setClearMethod(Consumer<V> clearMethod){
        this.clearMethod = clearMethod;
    }

    /**
     * Add required missions for completing this mission.
     * @param missions The array of required missions.
     */
    public void addRequiredMission(String... missions){
        requiredMissions.addAll(Arrays.asList(missions));
    }

    /**
     * Add required check for completing this mission.
     * These checks have placeholders support.
     * @param checks The array of required missions.
     */
    public void addRequiredCheck(String... checks){
        requiredChecks.addAll(Arrays.asList(checks));
    }

    /**
     * Get the required missions for completing this mission.
     */
    public List<String> getRequiredMissions(){
        return new ArrayList<>(requiredMissions);
    }

    /**
     * Get the required checks for completing this mission.
     */
    public List<String> getRequiredChecks() {
        return new ArrayList<>(requiredChecks);
    }

    /**
     * Toggle the onlyShowIfRequiredCompleted flag.
     */
    public void toggleOnlyShowIfRequiredCompleted(){
        onlyShowIfRequiredCompleted = !onlyShowIfRequiredCompleted;
    }

    /**
     * Check whether or not the item in the gui should be shown only
     * if all required missions are completed.
     */
    public boolean isOnlyShowIfRequiredCompleted(){
        return onlyShowIfRequiredCompleted;
    }

    /**
     * The load function of the mission.
     * @param plugin The plugin that loaded the mission (The SuperiorSkyblock's JavaPlugin class)
     * @param missionSection The configuration section of the mission from the config
     * @throws MissionLoadException if load was not success.
     */
    public abstract void load(JavaPlugin plugin, ConfigurationSection missionSection) throws MissionLoadException;

    /**
     * Get the progress of a specific player.
     * Method should return a value between 0.0 and 1.0
     * @param superiorPlayer The player to check.
     */
    public abstract double getProgress(SuperiorPlayer superiorPlayer);

    /**
     * Get the progress value of a specific player.
     * For example: amount of broken cobblestone, amount of kills, etc.
     * @param superiorPlayer The player to check.
     */
    public int getProgressValue(SuperiorPlayer superiorPlayer){
        return 0;
    }

    /**
     * Check whether or not a player can complete the mission.
     * @param superiorPlayer The player to check.
     */
    public boolean canComplete(SuperiorPlayer superiorPlayer){
        if(!SuperiorSkyblockAPI.getSuperiorSkyblock().getGrid().isIslandsWorld(superiorPlayer.getWorld()))
            return false;

        return getProgress(superiorPlayer) >= 1.0;
    }

    /**
     * Save mission's progress.
     * @param section The mission's section in the config.
     */
    public void saveProgress(ConfigurationSection section){

    }

    /**
     * Load mission's progress.
     * @param section The mission's section in the config.
     */
    public void loadProgress(ConfigurationSection section){

    }

    /**
     * A function that is called when a player is completing the mission.
     * @param superiorPlayer The player that completed the mission.
     */
    public abstract void onComplete(SuperiorPlayer superiorPlayer);

    /**
     * A function that is called when a player cannot complete the mission.
     * @param superiorPlayer The player that tried to complete the mission.
     */
    public abstract void onCompleteFail(SuperiorPlayer superiorPlayer);

    /**
     * A function that is called in order to clear progress of a player.
     * @param superiorPlayer The player to clear the data of.
     */
    public void clearData(SuperiorPlayer superiorPlayer){
        SuperiorPlayer dataKey = getDataKey(superiorPlayer);
        if(dataKey != null) {
            V data = missionData.remove(dataKey);
            if(data != null && clearMethod != null)
                clearMethod.accept(data);
        }
    }

    /**
     * A function that is called when islands are transferred.
     * @param oldPlayer The old owner of the player.
     * @param newPlayer The new owner of the player.
     */
    public void transferData(SuperiorPlayer oldPlayer, SuperiorPlayer newPlayer){
        V data = missionData.remove(oldPlayer);
        if(data != null)
            missionData.put(newPlayer, data);
    }

    /**
     * Get they data-key for the provided player.
     * @param superiorPlayer The player to check.
     */
    protected SuperiorPlayer getDataKey(SuperiorPlayer superiorPlayer){
        return islandMission ? superiorPlayer.getIsland() == null ? null : superiorPlayer.getIsland().getOwner() : superiorPlayer;
    }

    /**
     * Insert data to the mission data.
     * @param superiorPlayer The player to change it's data.
     * @param value The data to insert.
     */
    protected void insertData(SuperiorPlayer superiorPlayer, V value){
        SuperiorPlayer dataKey = getDataKey(superiorPlayer);
        if(dataKey != null)
            missionData.put(dataKey, value);
    }

    /**
     * Get or create data for a player.
     * @param superiorPlayer The player to get data from.
     * @param createFunction The function that will be run when data doesn't exist yet.
     */
    protected V getOrCreate(SuperiorPlayer superiorPlayer, Function<SuperiorPlayer, ? extends V> createFunction){
        SuperiorPlayer dataKey = getDataKey(superiorPlayer);

        if(dataKey == null)
            return null;

        return missionData.computeIfAbsent(dataKey, createFunction);
    }

    /**
     * Get data for a player.
     * @param superiorPlayer The player to get data from.
     */
    protected V get(SuperiorPlayer superiorPlayer){
        SuperiorPlayer dataKey = getDataKey(superiorPlayer);

        if(dataKey == null)
            return null;

        return missionData.get(dataKey);
    }

    /**
     * Get the entry set of the data map.
     */
    protected Set<Map.Entry<SuperiorPlayer,V>> entrySet(){
        return missionData.entrySet();
    }

    /**
     * A function that is called on every item of the menu.
     * This is used to inject custom placeholders into items.
     * The method is called async.
     * @param superiorPlayer The player that opens the menu.
     * @param itemStack The item of the mission.
     */
    public void formatItem(SuperiorPlayer superiorPlayer, ItemStack itemStack){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Mission<?> mission = (Mission<?>) o;
        return Objects.equals(name, mission.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
