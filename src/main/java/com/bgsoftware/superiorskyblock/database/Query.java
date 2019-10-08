package com.bgsoftware.superiorskyblock.database;

public enum Query {

    ISLAND_SET_TELEPORT_LOCATION("UPDATE islands SET teleportLocation=? WHERE owner=?;"),
    ISLAND_SET_VISITORS_LOCATION("UPDATE islands SET visitorsLocation=? WHERE owner=?;"),
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
    ISLAND_SET_LOCKED("UPDATE islands SET locked=? WHERE owner=?;"),
    ISLAND_SET_BLOCK_COUNTS("UPDATE islands SET blockCounts=? WHERE owner=?;"),
    ISLAND_SET_NAME("UPDATE islands SET name=? WHERE owner=?;"),
    ISLAND_SET_DESCRIPTION("UPDATE islands SET description=? WHERE owner=?;"),
    ISLAND_SET_RATINGS("UPDATE islands SET ratings=? WHERE owner=?;"),
    ISLAND_SET_MISSIONS("UPDATE islands SET missions=? WHERE owner=?;"),
    ISLAND_UPDATE("UPDATE islands SET teleportLocation=?,visitorsLocation=?,members=?,banned=?,permissionNodes=?,upgrades=?,warps=?,islandBank=?,islandSize=?,blockLimits=?,teamLimit=?,cropGrowth=?,spawnerRates=?,mobDrops=?,discord=?,paypal=?,warpsLimit=?,bonusWorth=?,locked=?,blockCounts=?,name=?,description=?,ratings=?,missions=? WHERE owner=?;"),
    ISLAND_INSERT("INSERT INTO islands (owner,center,teleportLocation,members,banned,permissionNodes,upgrades,warps,islandBank,islandSize,blockLimits,teamLimit,cropGrowth,spawnerRates,mobDrops,discord,paypal,warpsLimit,bonusWorth,locked,blockCounts,name,visitorsLocation,description,ratings,missions) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"),
    ISLAND_DELETE("DELETE FROM islands WHERE owner=?;"),

    PLAYER_SET_LEADER("UPDATE players SET teamLeader=? WHERE player=?;"),
    PLAYER_SET_NAME("UPDATE players SET name=? WHERE player=?;"),
    PLAYER_SET_ROLE("UPDATE players SET islandRole=? WHERE player=?;"),
    PLAYER_SET_TEXTURE("UPDATE players SET textureValue=? WHERE player=?;"),
    PLAYER_SET_DISBANDS("UPDATE players SET disbands=? WHERE player=?;"),
    PLAYER_SET_TOGGLED_PANEL("UPDATE players SET toggledPanel=? WHERE player=?;"),
    PLAYER_SET_ISLAND_FLY("UPDATE players SET islandFly=? WHERE player=?;"),
    PLAYER_SET_BORDER("UPDATE players SET borderColor=? WHERE player=?;"),
    PLAYER_SET_LAST_STATUS("UPDATE players SET lastTimeStatus=? WHERE player=?;"),
    PLAYER_SET_MISSIONS("UPDATE players SET missions=? WHERE player=?;"),
    PLAYER_UPDATE("UPDATE players SET teamLeader=?,name=?,islandRole=?,textureValue=?,disbands=?,toggledPanel=?,islandFly=?,borderColor=?,lastTimeStatus=?,missions=? WHERE player=?;"),
    PLAYER_INSERT("INSERT INTO players (player,teamLeader,name,islandRole,textureValue,disbands,toggledPanel,islandFly,borderColor,lastTimeStatus,missions) VALUES(?,?,?,?,?,?,?,?,?,?,?);"),

    STACKED_BLOCKS_UPDATE("UPDATE stackedBlocks SET amount=? WHERE world=? AND x=? AND y=? AND z=?;"),
    STACKED_BLOCKS_INSERT("INSERT INTO stackedBlocks (world,x,y,z,amount) VALUES(?,?,?,?,?);"),
    STACKED_BLOCKS_DELETE("DELETE FROM stackedBlocks WHERE world=? AND x=? AND y=? AND z=?;"),

    GRID_UPDATE("UPDATE grid SET lastIsland=?;"),
    GRID_INSERT("INSERT INTO (lastIsland,stackedBlocks,maxIslandSize,world) grid VALUES(?,?,?,?);");

    private String query;

    Query(String query) {
        this.query = query;
    }

    public String getStatement(){
        return query;
    }

    public StatementHolder getStatementHolder(){
        return new StatementHolder(this);
    }
}
