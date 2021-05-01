package com.bgsoftware.superiorskyblock.utils.islands;

import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;

public final class IslandPrivileges {

    private IslandPrivileges(){

    }

    public static IslandPrivilege ALL = IslandPrivilege.getByName("ALL");
    public static IslandPrivilege ANIMAL_BREED = IslandPrivilege.getByName("ANIMAL_BREED");
    public static IslandPrivilege ANIMAL_DAMAGE = IslandPrivilege.getByName("ANIMAL_DAMAGE");
    public static IslandPrivilege ANIMAL_SHEAR = IslandPrivilege.getByName("ANIMAL_SHEAR");
    public static IslandPrivilege ANIMAL_SPAWN = IslandPrivilege.getByName("ANIMAL_SPAWN");
    public static IslandPrivilege BAN_MEMBER = IslandPrivilege.getByName("BAN_MEMBER");
    public static IslandPrivilege BREAK = IslandPrivilege.getByName("BREAK");
    public static IslandPrivilege BUILD = IslandPrivilege.getByName("BUILD");
    public static IslandPrivilege CHANGE_NAME = IslandPrivilege.getByName("CHANGE_NAME");
    public static IslandPrivilege CHEST_ACCESS = IslandPrivilege.getByName("CHEST_ACCESS");
    public static IslandPrivilege CLOSE_BYPASS = IslandPrivilege.getByName("CLOSE_BYPASS");
    public static IslandPrivilege CLOSE_ISLAND = IslandPrivilege.getByName("CLOSE_ISLAND");
    public static IslandPrivilege COOP_MEMBER = IslandPrivilege.getByName("COOP_MEMBER");
    public static IslandPrivilege DELETE_WARP = IslandPrivilege.getByName("DELETE_WARP");
    public static IslandPrivilege DEMOTE_MEMBERS = IslandPrivilege.getByName("DEMOTE_MEMBERS");
    public static IslandPrivilege DEPOSIT_MONEY = IslandPrivilege.getByName("DEPOSIT_MONEY");
    public static IslandPrivilege DISBAND_ISLAND = IslandPrivilege.getByName("DISBAND_ISLAND");
    public static IslandPrivilege DISCORD_SHOW = IslandPrivilege.getByName("DISCORD_SHOW");
    public static IslandPrivilege DROP_ITEMS = IslandPrivilege.getByName("DROP_ITEMS");
    public static IslandPrivilege ENDER_PEARL = IslandPrivilege.getByName("ENDER_PEARL");
    public static IslandPrivilege EXPEL_BYPASS = IslandPrivilege.getByName("EXPEL_BYPASS");
    public static IslandPrivilege EXPEL_PLAYERS = IslandPrivilege.getByName("EXPEL_PLAYERS");
    public static IslandPrivilege FARM_TRAMPING = IslandPrivilege.getByName("FARM_TRAMPING");
    public static IslandPrivilege FERTILIZE = IslandPrivilege.getByName("FERTILIZE");
    public static IslandPrivilege FISH = IslandPrivilege.getByName("FISH");
    public static IslandPrivilege FLY = IslandPrivilege.getByName("FLY");
    public static IslandPrivilege HORSE_INTERACT = IslandPrivilege.getByName("HORSE_INTERACT");
    public static IslandPrivilege INTERACT = IslandPrivilege.getByName("INTERACT");
    public static IslandPrivilege INVITE_MEMBER = IslandPrivilege.getByName("INVITE_MEMBER");
    public static IslandPrivilege ISLAND_CHEST = IslandPrivilege.getByName("ISLAND_CHEST");
    public static IslandPrivilege ITEM_FRAME = IslandPrivilege.getByName("ITEM_FRAME");
    public static IslandPrivilege KICK_MEMBER = IslandPrivilege.getByName("KICK_MEMBER");
    public static IslandPrivilege LEASH = IslandPrivilege.getByName("LEASH");
    public static IslandPrivilege MINECART_DAMAGE = IslandPrivilege.getByName("MINECART_DAMAGE");
    public static IslandPrivilege MINECART_ENTER = IslandPrivilege.getByName("MINECART_ENTER");
    public static IslandPrivilege MINECART_OPEN = IslandPrivilege.getByName("MINECART_OPEN");
    public static IslandPrivilege MINECART_PLACE = IslandPrivilege.getByName("MINECART_PLACE");
    public static IslandPrivilege MONSTER_DAMAGE = IslandPrivilege.getByName("MONSTER_DAMAGE");
    public static IslandPrivilege MONSTER_SPAWN = IslandPrivilege.getByName("MONSTER_SPAWN");
    public static IslandPrivilege NAME_ENTITY = IslandPrivilege.getByName("NAME_ENTITY");
    public static IslandPrivilege OPEN_ISLAND = IslandPrivilege.getByName("OPEN_ISLAND");
    public static IslandPrivilege PAINTING = IslandPrivilege.getByName("PAINTING");
    public static IslandPrivilege PAYPAL_SHOW = IslandPrivilege.getByName("PAYPAL_SHOW");
    public static IslandPrivilege PICKUP_DROPS = IslandPrivilege.getByName("PICKUP_DROPS");
    public static IslandPrivilege PICKUP_FISH = getSafe("PICKUP_FISH");
    public static IslandPrivilege PROMOTE_MEMBERS = IslandPrivilege.getByName("PROMOTE_MEMBERS");
    public static IslandPrivilege RANKUP = IslandPrivilege.getByName("RANKUP");
    public static IslandPrivilege RATINGS_SHOW = IslandPrivilege.getByName("RATINGS_SHOW");
    public static IslandPrivilege SET_BIOME = IslandPrivilege.getByName("SET_BIOME");
    public static IslandPrivilege SET_DISCORD = IslandPrivilege.getByName("SET_DISCORD");
    public static IslandPrivilege SET_HOME = IslandPrivilege.getByName("SET_HOME");
    public static IslandPrivilege SET_PAYPAL = IslandPrivilege.getByName("SET_PAYPAL");
    public static IslandPrivilege SET_PERMISSION = IslandPrivilege.getByName("SET_PERMISSION");
    public static IslandPrivilege SET_ROLE = IslandPrivilege.getByName("SET_ROLE");
    public static IslandPrivilege SET_SETTINGS = IslandPrivilege.getByName("SET_SETTINGS");
    public static IslandPrivilege SET_WARP = IslandPrivilege.getByName("SET_WARP");
    public static IslandPrivilege SIGN_INTERACT = IslandPrivilege.getByName("SIGN_INTERACT");
    public static IslandPrivilege SPAWNER_BREAK = IslandPrivilege.getByName("SPAWNER_BREAK");
    public static IslandPrivilege TURTLE_EGG_TRAMPING = getSafe("TURTLE_EGG_TRAMPING");
    public static IslandPrivilege UNCOOP_MEMBER = IslandPrivilege.getByName("UNCOOP_MEMBER");
    public static IslandPrivilege USE = IslandPrivilege.getByName("USE");
    public static IslandPrivilege VALUABLE_BREAK = IslandPrivilege.getByName("VALUABLE_BREAK");
    public static IslandPrivilege VILLAGER_TRADING = IslandPrivilege.getByName("VILLAGER_TRADING");
    public static IslandPrivilege WITHDRAW_MONEY = IslandPrivilege.getByName("WITHDRAW_MONEY");

    private static IslandPrivilege getSafe(String name){
        try{
            return IslandPrivilege.getByName(name);
        }catch (Exception ex){
            return null;
        }
    }

}
