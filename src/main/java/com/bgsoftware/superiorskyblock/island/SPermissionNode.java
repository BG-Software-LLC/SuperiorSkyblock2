package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public final class SPermissionNode implements PermissionNode {

    private Set<IslandPrivilege> nodes = new HashSet<>();
    private SPermissionNode previousNode;

    public SPermissionNode(List<String> permissions, SPermissionNode previousNode){
        this(parse(permissions), previousNode);
    }

    public SPermissionNode(String permissions, SPermissionNode previousNode){
        if(!permissions.isEmpty()) {
            for (String permission : permissions.split(";")) {
                try {
                    nodes.add(IslandPrivilege.getByName(permission));
                } catch (Exception ignored) {
                }
            }
        }
        this.previousNode = previousNode;
    }

    @Override
    @Deprecated
    public boolean hasPermission(IslandPermission permission) {
        return hasPermission(IslandPrivilege.getByName(permission.name()));
    }

    @Override
    @Deprecated
    public void setPermission(IslandPermission permission, boolean value) {
        setPermission(IslandPrivilege.getByName(permission.name()), value);
    }

    @Override
    public boolean hasPermission(IslandPrivilege permission){
        return nodes.contains(IslandPrivileges.ALL) || nodes.contains(permission) || (previousNode != null && previousNode.hasPermission(permission));
    }

    @Override
    public void setPermission(IslandPrivilege permission, boolean value){
        if(value){
            nodes.add(permission);
            if(previousNode != null)
                previousNode.setPermission(permission, false);
        }else{
            nodes.remove(permission);
        }
    }

    @Override
    public SPermissionNode clone() {
        try {
            SPermissionNode permissionNode = (SPermissionNode) super.clone();
            permissionNode.nodes = new HashSet<>(nodes);
            return permissionNode;
        }catch(CloneNotSupportedException ex){
            return new SPermissionNode("", null);
        }
    }

    public String getAsStatementString(){
        StringBuilder stringBuilder = new StringBuilder();
        nodes.forEach(islandPrivilege -> stringBuilder.append(";").append(islandPrivilege.getName()));
        return stringBuilder.length() == 0 ? "" : stringBuilder.substring(1);
    }

    private static String parse(List<String> permissions){
        StringBuilder stringBuilder = new StringBuilder();
        permissions.forEach(permission -> stringBuilder.append(";").append(permission));
        return stringBuilder.length() == 0 ? stringBuilder.toString() : stringBuilder.substring(1);
    }


}
