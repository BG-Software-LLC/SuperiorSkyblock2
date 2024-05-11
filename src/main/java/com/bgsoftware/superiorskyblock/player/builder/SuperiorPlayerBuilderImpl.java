package com.bgsoftware.superiorskyblock.player.builder;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.mission.MissionReference;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.google.common.base.Preconditions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class SuperiorPlayerBuilderImpl implements SuperiorPlayer.Builder {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public UUID uuid = null;
    public String name = "null";
    public PlayerRole playerRole = SPlayerRole.guestRole();
    public int disbands = plugin.getSettings().getDisbandCount();
    public Locale locale = PlayerLocales.getDefaultLocale();
    public String textureValue = "";
    public long lastTimeUpdated = -1;
    public boolean toggledPanel = plugin.getSettings().isDefaultToggledPanel();
    public boolean islandFly = plugin.getSettings().isDefaultIslandFly();
    public BorderColor borderColor = BorderColor.safeValue(plugin.getSettings().getDefaultBorderColor(), BorderColor.BLUE);
    public boolean worldBorderEnabled = plugin.getSettings().isDefaultWorldBorder();
    public Map<MissionReference, Counter> completedMissions = new LinkedHashMap<>();
    public byte[] persistentData = new byte[0];

    public SuperiorPlayerBuilderImpl() {

    }

    @Override
    public SuperiorPlayer.Builder setUniqueId(UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid parameter cannot be null.");
        Preconditions.checkState(plugin.getPlayers().getPlayersContainer().getSuperiorPlayer(uuid) == null,
                "The provided uuid is not unique.");
        this.uuid = uuid;
        return this;
    }

    @Override
    public UUID getUniqueId() {
        return this.uuid;
    }

    @Override
    public SuperiorPlayer.Builder setName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        this.name = name;
        return this;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public SuperiorPlayer.Builder setPlayerRole(PlayerRole playerRole) {
        Preconditions.checkNotNull(playerRole, "playerRole parameter cannot be null.");
        this.playerRole = playerRole;
        return this;
    }

    @Override
    public PlayerRole getPlayerRole() {
        return this.playerRole;
    }

    @Override
    public SuperiorPlayer.Builder setDisbands(int disbands) {
        Preconditions.checkArgument(disbands >= 0, "Cannot set negative disbands count.");
        this.disbands = disbands;
        return this;
    }

    @Override
    public int getDisbands() {
        return this.disbands;
    }

    @Override
    public SuperiorPlayer.Builder setLocale(Locale locale) {
        Preconditions.checkNotNull(locale, "locale parameter cannot be null.");
        Preconditions.checkArgument(PlayerLocales.isValidLocale(locale), "Locale " + locale + " is not a valid locale.");
        this.locale = locale;
        return this;
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }

    @Override
    public SuperiorPlayer.Builder setTextureValue(String textureValue) {
        Preconditions.checkNotNull(textureValue, "textureValue parameter cannot be null.");
        this.textureValue = textureValue;
        return this;
    }

    @Override
    public String getTextureValue() {
        return this.textureValue;
    }

    @Override
    public SuperiorPlayer.Builder setLastTimeUpdated(long lastTimeUpdated) {
        this.lastTimeUpdated = lastTimeUpdated;
        return this;
    }

    @Override
    public long getLastTimeUpdated() {
        return this.lastTimeUpdated;
    }

    @Override
    public SuperiorPlayer.Builder setToggledPanel(boolean toggledPanel) {
        this.toggledPanel = toggledPanel;
        return this;
    }

    @Override
    public boolean hasToggledPanel() {
        return this.toggledPanel;
    }

    @Override
    public SuperiorPlayer.Builder setIslandFly(boolean islandFly) {
        this.islandFly = islandFly;
        return this;
    }

    @Override
    public boolean hasIslandFly() {
        return this.islandFly;
    }

    @Override
    public SuperiorPlayer.Builder setBorderColor(BorderColor borderColor) {
        Preconditions.checkNotNull(borderColor, "borderColor parameter cannot be null.");
        this.borderColor = borderColor;
        return this;
    }

    @Override
    public BorderColor getBorderColor() {
        return this.borderColor;
    }

    @Override
    public SuperiorPlayer.Builder setWorldBorderEnabled(boolean worldBorderEnabled) {
        this.worldBorderEnabled = worldBorderEnabled;
        return this;
    }

    @Override
    public boolean hasWorldBorderEnabled() {
        return this.worldBorderEnabled;
    }

    @Override
    public SuperiorPlayer.Builder setCompletedMission(Mission<?> mission, int finishCount) {
        Preconditions.checkNotNull(mission, "mission parameter cannot be null.");
        this.completedMissions.put(new MissionReference(mission), new Counter(finishCount));
        return this;
    }

    @Override
    public Map<Mission<?>, Integer> getCompletedMissions() {
        Map<Mission<?>, Integer> completedMissions = new LinkedHashMap<>();

        this.completedMissions.forEach((mission, finishCount) -> {
            if (mission.isValid())
                completedMissions.put(mission.getMission(), finishCount.get());
        });

        return completedMissions.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(completedMissions);
    }

    @Override
    public SuperiorPlayer.Builder setPersistentData(byte[] persistentData) {
        Preconditions.checkNotNull(persistentData, "persistentData parameter cannot be null.");
        this.persistentData = persistentData;
        return this;
    }

    @Override
    public byte[] getPersistentData() {
        return this.persistentData;
    }

    @Override
    public SuperiorPlayer build() {
        if (this.uuid == null)
            throw new IllegalStateException("Cannot create a player with invalid uuid.");

        return plugin.getFactory().createPlayer(this);
    }

}
