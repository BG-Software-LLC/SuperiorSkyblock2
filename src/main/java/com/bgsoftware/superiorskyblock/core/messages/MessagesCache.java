package com.bgsoftware.superiorskyblock.core.messages;

import com.bgsoftware.common.collections.Maps;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;

import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

public class MessagesCache {

    public static String UPGRADE_NAMES = "";
    public static String ISLAND_PRIVILEGES = "";
    public static String ISLAND_FLAGS = "";
    public static String PLAYER_ROLES = "";
    public static String RATINGS = "";
    public static Map<PlayerRole, StringBuilder> ISLAND_INFO_ROLE_LINE = Maps.emptyMap();

    private MessagesCache() {

    }

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void reload() {
        UPGRADE_NAMES = Formatters.COMMA_FORMATTER.format(plugin.getUpgrades().getUpgrades().stream().map(Upgrade::getName));
        ISLAND_PRIVILEGES = Formatters.COMMA_FORMATTER.format(IslandPrivilege.values().stream()
                .sorted(Comparator.comparing(IslandPrivilege::getName))
                .map(_islandPrivilege -> _islandPrivilege.toString().toLowerCase(Locale.ENGLISH)));
        ISLAND_FLAGS = Formatters.COMMA_FORMATTER.format(IslandFlag.values().stream()
                .sorted(Comparator.comparing(IslandFlag::getName))
                .map(_islandFlag -> _islandFlag.getName().toLowerCase(Locale.ENGLISH)));
        PLAYER_ROLES = buildPlayerRoles();
        RATINGS = Rating.getValuesString();
    }

    private static String buildPlayerRoles() {
        StringBuilder stringBuilder = new StringBuilder();
        plugin.getRoles().getRoles().forEach(playerRole -> stringBuilder.append(", ").append(playerRole.toString().toLowerCase(Locale.ENGLISH)));
        return stringBuilder.substring(2);
    }

    private static Map<PlayerRole, StringBuilder> buildIslandInfoRoleLines() {
        Map<PlayerRole, StringBuilder> roleLines = Maps.newArrayMap();
        plugin.getRoles().getRoles().stream().filter(playerRole -> playerRole.isRoleLadder() && !playerRole.isLastRole())
                .forEach(playerRole -> roleLines.put(playerRole, new StringBuilder()));
        return roleLines;
    }

}
