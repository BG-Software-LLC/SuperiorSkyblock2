package com.bgsoftware.superiorskyblock.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public enum Query {

    ISLAND_SET_CENTER("UPDATE islands SET center=? WHERE owner=?;"), //
    ISLAND_SET_TELEPORT_LOCATION("UPDATE islands SET teleportLocation=? WHERE owner=?;"),
    ISLAND_SET_BANK("UPDATE islands SET islandBank=? WHERE owner=?;"),
    ISLAND_SET_SIZE("UPDATE islands SET islandSize=? WHERE owner=?;"),
    ISLAND_SET_TEAM_LIMIT("UPDATE islands SET teamLimit=? WHERE owner=?;"),
    ISLAND_SET_CROP_GROWTH("UPDATE islands SET cropGrowth=? WHERE owner=?;"),
    ISLAND_SET_SPAWNER_RATES("UPDATE islands SET spawnerRates=? WHERE owner=?;"),
    ISLAND_SET_MOB_DROPS("UPDATE islands SET mobDrops=? WHERE owner=?;"),
    ISLAND_SET_DISCORD("UPDATE islands SET discord=? WHERE owner=?;"),
    ISLAND_SET_PAYPAL("UPDATE islands SET paypal=? WHERE owner=?;"),
    ISLAND_SET_WARPS_LIMIT("UPDATE islands SET warpsLimit=? WHERE owner=?;"),
    ISLAND_SET_BONUS_WORTH("UPDATE islands SET bonusWorth=? WHERE owner=?;"),
    ISLAND_SET_MEMBERS("UPDATE islands SET members=? WHERE owner=?;"),
    ISLAND_SET_BANNED("UPDATE islands SET banned=? WHERE owner=?;"),
    ISLAND_SET_PERMISSION_NODES("UPDATE islands SET permissionNodes=? WHERE owner=?;"),
    ISLAND_SET_UPGRADES("UPDATE islands SET upgrades=? WHERE owner=?;"),
    ISLAND_SET_WARPS("UPDATE islands SET warps=? WHERE owner=?;"),
    ISLAND_SET_BLOCK_LIMITS("UPDATE islands SET blockLimits=? WHERE owner=?;"),

    PLAYER_SET_LEADER("UPDATE players SET teamLeader=? WHERE player=?;"),
    PLAYER_SET_NAME("UPDATE players SET name=? WHERE player=?;"),
    PLAYER_SET_ROLE("UPDATE players SET islandRole=? WHERE player=?;"),
    PLAYER_SET_TEXTURE("UPDATE players SET textureValue=? WHERE player=?;"),
    PLAYER_SET_DISBANDS("UPDATE players SET disbands=? WHERE player=?;");

    private String query;

    Query(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    public PreparedStatement getStatement(Connection connection) throws SQLException {
        return connection.prepareStatement(query);
    }

    public StatementHolder getStatementHolder() throws SQLException {
        return new StatementHolder(this);
    }
}
