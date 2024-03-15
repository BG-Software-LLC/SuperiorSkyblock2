package com.bgsoftware.superiorskyblock.island.privilege;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.core.ServerVersion;

import java.util.Objects;

public class IslandPrivileges {

    public static final IslandPrivilege ALL = register("ALL");
    public static final IslandPrivilege ANIMAL_BREED = register("ANIMAL_BREED");
    public static final IslandPrivilege ANIMAL_DAMAGE = register("ANIMAL_DAMAGE");
    public static final IslandPrivilege ANIMAL_SHEAR = register("ANIMAL_SHEAR");
    public static final IslandPrivilege ANIMAL_SPAWN = register("ANIMAL_SPAWN");
    public static final IslandPrivilege BAN_MEMBER = register("BAN_MEMBER");
    public static final IslandPrivilege BREAK = register("BREAK");
    public static final IslandPrivilege BRUSH = register("BRUSH", ServerVersion.isAtLeast(ServerVersion.v1_20));
    public static final IslandPrivilege BUILD = register("BUILD");
    public static final IslandPrivilege CHANGE_NAME = register("CHANGE_NAME");
    public static final IslandPrivilege CHEST_ACCESS = register("CHEST_ACCESS");
    public static final IslandPrivilege CHORUS_FRUIT = register("CHORUS_FRUIT", ServerVersion.isAtLeast(ServerVersion.v1_9));
    public static final IslandPrivilege CLOSE_BYPASS = register("CLOSE_BYPASS");
    public static final IslandPrivilege CLOSE_ISLAND = register("CLOSE_ISLAND");
    public static final IslandPrivilege COOP_MEMBER = register("COOP_MEMBER");
    public static final IslandPrivilege DELETE_WARP = register("DELETE_WARP");
    public static final IslandPrivilege DEMOTE_MEMBERS = register("DEMOTE_MEMBERS");
    public static final IslandPrivilege DEPOSIT_MONEY = register("DEPOSIT_MONEY");
    public static final IslandPrivilege DISBAND_ISLAND = register("DISBAND_ISLAND");
    public static final IslandPrivilege DISCORD_SHOW = register("DISCORD_SHOW");
    public static final IslandPrivilege DROP_ITEMS = register("DROP_ITEMS");
    public static final IslandPrivilege ENDER_PEARL = register("ENDER_PEARL");
    public static final IslandPrivilege EXPEL_BYPASS = register("EXPEL_BYPASS");
    public static final IslandPrivilege EXPEL_PLAYERS = register("EXPEL_PLAYERS");
    public static final IslandPrivilege FARM_TRAMPING = register("FARM_TRAMPING");
    public static final IslandPrivilege FERTILIZE = register("FERTILIZE");
    public static final IslandPrivilege FISH = register("FISH");
    public static final IslandPrivilege FLY = register("FLY");
    public static final IslandPrivilege HORSE_INTERACT = register("HORSE_INTERACT");
    public static final IslandPrivilege IGNITE_CREEPER = register("IGNITE_CREEPER");
    public static final IslandPrivilege INTERACT = register("INTERACT");
    public static final IslandPrivilege INVITE_MEMBER = register("INVITE_MEMBER");
    public static final IslandPrivilege ISLAND_CHEST = register("ISLAND_CHEST");
    public static final IslandPrivilege ITEM_FRAME = register("ITEM_FRAME");
    public static final IslandPrivilege KICK_MEMBER = register("KICK_MEMBER");
    public static final IslandPrivilege LEASH = register("LEASH");
    public static final IslandPrivilege MINECART_DAMAGE = register("MINECART_DAMAGE");
    public static final IslandPrivilege MINECART_ENTER = register("MINECART_ENTER");
    public static final IslandPrivilege MINECART_OPEN = register("MINECART_OPEN");
    public static final IslandPrivilege MINECART_PLACE = register("MINECART_PLACE");
    public static final IslandPrivilege MONSTER_DAMAGE = register("MONSTER_DAMAGE");
    public static final IslandPrivilege MONSTER_SPAWN = register("MONSTER_SPAWN");
    public static final IslandPrivilege NAME_ENTITY = register("NAME_ENTITY");
    public static final IslandPrivilege OPEN_ISLAND = register("OPEN_ISLAND");
    public static final IslandPrivilege PAINTING = register("PAINTING");
    public static final IslandPrivilege PAYPAL_SHOW = register("PAYPAL_SHOW");
    @Nullable
    public static final IslandPrivilege PICKUP_AXOLOTL = register("PICKUP_AXOLOTL", ServerVersion.isAtLeast(ServerVersion.v1_17));
    public static final IslandPrivilege PICKUP_DROPS = register("PICKUP_DROPS");
    @Nullable
    public static final IslandPrivilege PICKUP_FISH = register("PICKUP_FISH", !ServerVersion.isLegacy());
    @Nullable
    public static final IslandPrivilege PICKUP_LECTERN_BOOK = register("PICKUP_LECTERN_BOOK", ServerVersion.isAtLeast(ServerVersion.v1_14));
    public static final IslandPrivilege PROMOTE_MEMBERS = register("PROMOTE_MEMBERS");
    public static final IslandPrivilege RANKUP = register("RANKUP");
    public static final IslandPrivilege RATINGS_SHOW = register("RATINGS_SHOW");
    public static final IslandPrivilege SET_BIOME = register("SET_BIOME");
    public static final IslandPrivilege SET_DISCORD = register("SET_DISCORD");
    public static final IslandPrivilege SET_HOME = register("SET_HOME");
    public static final IslandPrivilege SET_PAYPAL = register("SET_PAYPAL");
    public static final IslandPrivilege SET_PERMISSION = register("SET_PERMISSION");
    public static final IslandPrivilege SET_ROLE = register("SET_ROLE");
    public static final IslandPrivilege SET_SETTINGS = register("SET_SETTINGS");
    public static final IslandPrivilege SET_WARP = register("SET_WARP");
    public static final IslandPrivilege SIGN_INTERACT = register("SIGN_INTERACT");
    public static final IslandPrivilege SPAWNER_BREAK = register("SPAWNER_BREAK");
    @Nullable
    public static final IslandPrivilege TURTLE_EGG_TRAMPING = register("TURTLE_EGG_TRAMPING", !ServerVersion.isLegacy());
    public static final IslandPrivilege UNCOOP_MEMBER = register("UNCOOP_MEMBER");
    public static final IslandPrivilege USE = register("USE");
    public static final IslandPrivilege VALUABLE_BREAK = register("VALUABLE_BREAK");
    public static final IslandPrivilege VILLAGER_TRADING = register("VILLAGER_TRADING");
    public static final IslandPrivilege WITHDRAW_MONEY = register("WITHDRAW_MONEY");

    private IslandPrivileges() {

    }

    public static void registerPrivileges() {
        // Do nothing, only trigger all the register calls
    }

    @NotNull
    private static IslandPrivilege register(String name) {
        return Objects.requireNonNull(register(name, true));
    }

    @Nullable
    private static IslandPrivilege register(String name, boolean registerCheck) {
        if (!registerCheck)
            return null;

        IslandPrivilege.register(name);
        return IslandPrivilege.getByName(name);
    }

}
