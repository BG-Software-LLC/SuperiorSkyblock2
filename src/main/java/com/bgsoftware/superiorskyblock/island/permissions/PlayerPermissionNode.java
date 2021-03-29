package com.bgsoftware.superiorskyblock.island.permissions;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class PlayerPermissionNode extends PermissionNodeAbstract {

    protected final SuperiorPlayer superiorPlayer;
    protected final Island island;

    public PlayerPermissionNode(SuperiorPlayer superiorPlayer, Island island){
        this(superiorPlayer, island, (JsonArray) null);
    }

    public PlayerPermissionNode(SuperiorPlayer superiorPlayer, Island island, JsonArray permsArray){
        this.superiorPlayer = superiorPlayer;
        this.island = island;
        if(permsArray != null)
            deserialize(permsArray);
    }

    public PlayerPermissionNode(SuperiorPlayer superiorPlayer, Island island, String permissions){
        this.superiorPlayer = superiorPlayer;
        this.island = island;
        setPermissions(permissions, false);
    }

    private PlayerPermissionNode(Registry<IslandPrivilege, PrivilegeStatus> privileges, SuperiorPlayer superiorPlayer, Island island){
        super(privileges);

        this.superiorPlayer = superiorPlayer;
        this.island = island;
    }

    @Override
    public boolean hasPermission(IslandPrivilege islandPrivilege) {
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
        return getStatus(IslandPrivileges.ALL) == PrivilegeStatus.ENABLED || getStatus(islandPrivilege) == PrivilegeStatus.ENABLED;
    }

    @Override
    public PermissionNodeAbstract clone() {
        return new PlayerPermissionNode(privileges, superiorPlayer, island);
    }

    public JsonArray serialize() {
        JsonArray permsArray = new JsonArray();
        privileges.entries().forEach(entry -> {
            JsonObject permObject = new JsonObject();
            permObject.addProperty("name", entry.getKey().getName());
            permObject.addProperty("status", entry.getValue().toString());
            permsArray.add(permObject);
        });
        return permsArray;
    }

    private void deserialize(JsonArray permsArray){
        for(JsonElement permElement : permsArray){
            try {
                JsonObject permObject = permElement.getAsJsonObject();
                IslandPrivilege islandPrivilege = IslandPrivilege.getByName(permObject.get("name").getAsString());
                PrivilegeStatus privilegeStatus = PrivilegeStatus.of(permObject.get("status").getAsString());
                privileges.add(islandPrivilege, privilegeStatus);
            }catch (Exception ignored){}
        }
    }

    protected PrivilegeStatus getStatus(IslandPrivilege islandPrivilege) {
        PlayerRole playerRole = island.isMember(superiorPlayer) ? superiorPlayer.getPlayerRole() : island.isCoop(superiorPlayer) ? SPlayerRole.coopRole() : SPlayerRole.guestRole();

        if(island.hasPermission(playerRole, islandPrivilege))
            return PrivilegeStatus.ENABLED;

        return privileges.get(islandPrivilege, PrivilegeStatus.DISABLED);
    }

    public static class EmptyPlayerPermissionNode extends PlayerPermissionNode{

        public static final EmptyPlayerPermissionNode INSTANCE;

        static {
            INSTANCE = new EmptyPlayerPermissionNode();
        }

        EmptyPlayerPermissionNode(){
            this(null, null);
        }

        EmptyPlayerPermissionNode(SuperiorPlayer superiorPlayer, Island island){
            super(null, superiorPlayer, island);
        }

        @Override
        public boolean hasPermission(IslandPrivilege islandPrivilege) {
            Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
            return superiorPlayer != null && island != null && super.hasPermission(islandPrivilege);
        }

        @Override
        public void setPermission(IslandPrivilege permission, boolean value) {

        }

        @Override
        protected PrivilegeStatus getStatus(IslandPrivilege islandPrivilege) {
            PlayerRole playerRole = island.isMember(superiorPlayer) ? superiorPlayer.getPlayerRole() : island.isCoop(superiorPlayer) ? SPlayerRole.coopRole() : SPlayerRole.guestRole();

            if(island.hasPermission(playerRole, islandPrivilege))
                return PrivilegeStatus.ENABLED;

            return PrivilegeStatus.DISABLED;
        }
    }

}
