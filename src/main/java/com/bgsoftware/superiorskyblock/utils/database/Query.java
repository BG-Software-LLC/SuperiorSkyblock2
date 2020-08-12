package com.bgsoftware.superiorskyblock.utils.database;

public enum Query {

    ISLAND_SET_TELEPORT_LOCATION("UPDATE {prefix}islands SET teleportLocation=? WHERE owner=?;"),
    ISLAND_SET_VISITORS_LOCATION("UPDATE {prefix}islands SET visitorsLocation=? WHERE owner=?;"),
    ISLAND_SET_BANK("UPDATE {prefix}islands SET islandBank=? WHERE owner=?;"),
    ISLAND_SET_SIZE("UPDATE {prefix}islands SET islandSize=? WHERE owner=?;"),
    ISLAND_SET_TEAM_LIMIT("UPDATE {prefix}islands SET teamLimit=? WHERE owner=?;"),
    ISLAND_SET_CROP_GROWTH("UPDATE {prefix}islands SET cropGrowth=? WHERE owner=?;"),
    ISLAND_SET_SPAWNER_RATES("UPDATE {prefix}islands SET spawnerRates=? WHERE owner=?;"),
    ISLAND_SET_MOB_DROPS("UPDATE {prefix}islands SET mobDrops=? WHERE owner=?;"),
    ISLAND_SET_DISCORD("UPDATE {prefix}islands SET discord=? WHERE owner=?;"),
    ISLAND_SET_PAYPAL("UPDATE {prefix}islands SET paypal=? WHERE owner=?;"),
    ISLAND_SET_WARPS_LIMIT("UPDATE {prefix}islands SET warpsLimit=? WHERE owner=?;"),
    ISLAND_SET_BONUS_WORTH("UPDATE {prefix}islands SET bonusWorth=? WHERE owner=?;"),
    ISLAND_SET_MEMBERS("UPDATE {prefix}islands SET members=? WHERE owner=?;"),
    ISLAND_SET_BANNED("UPDATE {prefix}islands SET banned=? WHERE owner=?;"),
    ISLAND_SET_PERMISSION_NODES("UPDATE {prefix}islands SET permissionNodes=? WHERE owner=?;"),
    ISLAND_SET_UPGRADES("UPDATE {prefix}islands SET upgrades=? WHERE owner=?;"),
    ISLAND_SET_WARPS("UPDATE {prefix}islands SET warps=? WHERE owner=?;"),
    ISLAND_SET_BLOCK_LIMITS("UPDATE {prefix}islands SET blockLimits=? WHERE owner=?;"),
    ISLAND_SET_LOCKED("UPDATE {prefix}islands SET locked=? WHERE owner=?;"),
    ISLAND_SET_BLOCK_COUNTS("UPDATE {prefix}islands SET blockCounts=? WHERE owner=?;"),
    ISLAND_SET_NAME("UPDATE {prefix}islands SET name=? WHERE owner=?;"),
    ISLAND_SET_DESCRIPTION("UPDATE {prefix}islands SET description=? WHERE owner=?;"),
    ISLAND_SET_RATINGS("UPDATE {prefix}islands SET ratings=? WHERE owner=?;"),
    ISLAND_SET_MISSIONS("UPDATE {prefix}islands SET missions=? WHERE owner=?;"),
    ISLAND_SET_SETTINGS("UPDATE {prefix}islands SET settings=? WHERE owner=?;"),
    ISLAND_SET_IGNORED("UPDATE {prefix}islands SET ignored=? WHERE owner=?;"),
    ISLAND_SET_GENERATOR("UPDATE {prefix}islands SET generator=? WHERE owner=?;"),
    ISLAND_SET_GENERATED_SCHEMATICS("UPDATE {prefix}islands SET generatedSchematics=? WHERE owner=?;"),
    ISLAND_SET_VISITORS("UPDATE {prefix}islands SET uniqueVisitors=? WHERE owner=?;"),
    ISLAND_SET_UNLOCK_WORLDS("UPDATE {prefix}islands SET unlockedWorlds=? WHERE owner=?;"),
    ISLAND_SET_LAST_TIME_UPDATE("UPDATE {prefix}islands SET lastTimeUpdate=? WHERE owner=?;"),
    ISLAND_SET_DIRTY_CHUNKS("UPDATE {prefix}islands SET dirtyChunks=? WHERE owner=?;"),
    ISLAND_SET_ENTITY_LIMITS("UPDATE {prefix}islands SET entityLimits=? WHERE owner=?;"),
    ISLAND_SET_BONUS_LEVEL("UPDATE {prefix}islands SET bonusLevel=? WHERE owner=?;"),
    ISLAND_SET_COOP_LIMIT("UPDATE {prefix}islands SET coopLimit=? WHERE owner=?;"),
    ISLAND_SET_ISLAND_EFFECTS("UPDATE {prefix}islands SET islandEffects=? WHERE owner=?;"),
    ISLAND_SET_ISLAND_CHEST("UPDATE {prefix}islands SET islandChest=? WHERE owner=?;"),
    ISLAND_SET_UUID("UPDATE {prefix}islands SET uuid=? WHERE owner=?;"),
    ISLAND_UPDATE("UPDATE {prefix}islands SET teleportLocation=?,visitorsLocation=?,members=?,banned=?,permissionNodes=?,upgrades=?,warps=?,islandBank=?,islandSize=?,blockLimits=?,teamLimit=?,cropGrowth=?,spawnerRates=?,mobDrops=?,discord=?,paypal=?,warpsLimit=?,bonusWorth=?,locked=?,blockCounts=?,name=?,description=?,ratings=?,missions=?,settings=?,ignored=?,generator=?,generatedSchematics=?,schemName=?,uniqueVisitors=?,unlockedWorlds=?,lastTimeUpdate=?,dirtyChunks=?,entityLimits=?,bonusLevel=?,creationTime=?,coopLimit=?,islandEffects=?,islandChest=?,uuid=? WHERE owner=?;"),
    ISLAND_INSERT("REPLACE INTO {prefix}islands (owner,center,teleportLocation,members,banned,permissionNodes,upgrades,warps,islandBank,islandSize,blockLimits,teamLimit,cropGrowth,spawnerRates,mobDrops,discord,paypal,warpsLimit,bonusWorth,locked,blockCounts,name,visitorsLocation,description,ratings,missions,settings,ignored,generator,generatedSchematics,schemName,uniqueVisitors,unlockedWorlds,lastTimeUpdate,dirtyChunks,entityLimits,bonusLevel,creationTime,coopLimit,islandEffects,islandChest,uuid) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"),
    ISLAND_DELETE("DELETE FROM {prefix}islands WHERE owner=?;"),

    PLAYER_SET_LEADER("UPDATE {prefix}players SET teamLeader=? WHERE player=?;"),
    PLAYER_SET_NAME("UPDATE {prefix}players SET name=? WHERE player=?;"),
    PLAYER_SET_ROLE("UPDATE {prefix}players SET islandRole=? WHERE player=?;"),
    PLAYER_SET_TEXTURE("UPDATE {prefix}players SET textureValue=? WHERE player=?;"),
    PLAYER_SET_DISBANDS("UPDATE {prefix}players SET disbands=? WHERE player=?;"),
    PLAYER_SET_TOGGLED_PANEL("UPDATE {prefix}players SET toggledPanel=? WHERE player=?;"),
    PLAYER_SET_ISLAND_FLY("UPDATE {prefix}players SET islandFly=? WHERE player=?;"),
    PLAYER_SET_BORDER("UPDATE {prefix}players SET borderColor=? WHERE player=?;"),
    PLAYER_SET_LAST_STATUS("UPDATE {prefix}players SET lastTimeStatus=? WHERE player=?;"),
    PLAYER_SET_MISSIONS("UPDATE {prefix}players SET missions=? WHERE player=?;"),
    PLAYER_SET_LANGUAGE("UPDATE {prefix}players SET language=? WHERE player=?;"),
    PLAYER_SET_TOGGLED_BORDER("UPDATE {prefix}players SET toggledBorder=? WHERE player=?;"),
    PLAYER_UPDATE("UPDATE {prefix}players SET teamLeader=?,name=?,islandRole=?,textureValue=?,disbands=?,toggledPanel=?,islandFly=?,borderColor=?,lastTimeStatus=?,missions=?,language=?,toggledBorder=? WHERE player=?;"),
    PLAYER_INSERT("REPLACE INTO {prefix}players (player,teamLeader,name,islandRole,textureValue,disbands,toggledPanel,islandFly,borderColor,lastTimeStatus,missions,language,toggledBorder) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?);"),
    PLAYER_DELETE("DELETE FROM {prefix}players WHERE player=?;"),

    STACKED_BLOCKS_UPDATE("UPDATE {prefix}stackedBlocks SET amount=? WHERE world=? AND x=? AND y=? AND z=?;"),
    STACKED_BLOCKS_INSERT("REPLACE INTO {prefix}stackedBlocks (world,x,y,z,amount,item) VALUES(?,?,?,?,?,?);"),
    STACKED_BLOCKS_DELETE("DELETE FROM {prefix}stackedBlocks WHERE world=? AND x=? AND y=? AND z=?;"),

    GRID_UPDATE_LAST_ISLAND("UPDATE {prefix}grid SET lastIsland=?;"),
    GRID_INSERT("REPLACE INTO {prefix}grid (lastIsland,stackedBlocks,maxIslandSize,world,dirtyChunks) VALUES(?,?,?,?,?);");

    private final String query;

    Query(String query) {
        this.query = query;
    }

    public String getStatement(){
        return query;
    }

    public StatementHolder getStatementHolder(DatabaseObject databaseObject){
        return new StatementHolder(databaseObject, this);
    }
}
