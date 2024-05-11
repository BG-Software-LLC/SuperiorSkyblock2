package com.bgsoftware.superiorskyblock.mission.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.mission.MissionData;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

public class DefaultMissionsContainer implements MissionsContainer {

    private final Map<String, Mission<?>> missionMap = new HashMap<>();
    private final Map<String, MissionData> missionDataMap = new HashMap<>();
    private final Map<String, MissionCategory> missionCategoryMap = new HashMap<>();

    @Override
    public void addMission(Mission<?> mission) {
        this.missionMap.put(mission.getName().toLowerCase(Locale.ENGLISH), mission);
    }

    @Nullable
    @Override
    public Mission<?> getMission(String name) {
        return this.missionMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public List<Mission<?>> getAllMissions() {
        return getFilteredMissions(missionData -> true);
    }

    @Override
    public List<Mission<?>> getPlayerMissions() {
        return getFilteredMissions(missionData -> !missionData.isIslandMission());
    }

    @Override
    public List<Mission<?>> getIslandMissions() {
        return getFilteredMissions(MissionData::isIslandMission);
    }

    @Override
    public void addMissionData(MissionData missionData) {
        this.missionDataMap.put(missionData.getMissionName(), missionData);
    }

    @Nullable
    @Override
    public MissionData getMissionData(Mission<?> mission) {
        return this.missionDataMap.get(mission.getName());
    }

    @Override
    public void addMissionCategory(MissionCategory missionCategory) {
        this.missionCategoryMap.put(missionCategory.getName().toLowerCase(Locale.ENGLISH), missionCategory);
    }

    @Nullable
    @Override
    public MissionCategory getMissionCategory(String name) {
        return this.missionCategoryMap.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public List<MissionCategory> getMissionCategories() {
        return new SequentialListBuilder<MissionCategory>().build(missionCategoryMap.values());
    }

    @Override
    public void clearMissionsData() {
        this.missionMap.clear();
        this.missionDataMap.clear();
        this.missionCategoryMap.clear();
    }

    private List<Mission<?>> getFilteredMissions(Predicate<MissionData> predicate) {
        return new SequentialListBuilder<MissionData>()
                .filter(predicate)
                .sorted(Comparator.comparingInt(MissionData::getIndex))
                .map(missionDataMap.values(), missionData -> getMission(missionData.getMissionName()));
    }

}
