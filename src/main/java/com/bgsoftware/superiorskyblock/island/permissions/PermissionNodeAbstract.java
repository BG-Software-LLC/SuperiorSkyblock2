package com.bgsoftware.superiorskyblock.island.permissions;

import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;

@SuppressWarnings("WeakerAccess")
public abstract class PermissionNodeAbstract implements PermissionNode {

    protected final Registry<IslandPrivilege, PrivilegeStatus> privileges;

    protected PermissionNodeAbstract(){
        this.privileges = Registry.createRegistry();
    }

    protected PermissionNodeAbstract(Registry<IslandPrivilege, PrivilegeStatus> privileges){
        this.privileges = Registry.createRegistry(privileges.toMap());
    }

    protected void setPermissions(String permissions, boolean checkDefaults){
        if(!permissions.isEmpty()) {
            String[] permission = permissions.split(";");
            for (String perm : permission) {
                String[] permissionSections = perm.split(":");
                try {
                    IslandPrivilege islandPrivilege = IslandPrivilege.getByName(permissionSections[0]);
                    if (permissionSections.length == 2) {
                        privileges.add(islandPrivilege, PrivilegeStatus.of(permissionSections[1]));
                    } else {
                        if(!checkDefaults || !isDefault(islandPrivilege))
                            privileges.add(islandPrivilege, PrivilegeStatus.ENABLED);
                    }
                }catch(Exception ignored){}
            }
        }
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
        return getStatus(IslandPrivileges.ALL) == PrivilegeStatus.ENABLED || getStatus(permission) == PrivilegeStatus.ENABLED;
    }

    @Override
    public void setPermission(IslandPrivilege permission, boolean value){
        privileges.add(permission, value ? PrivilegeStatus.ENABLED : PrivilegeStatus.DISABLED);
    }

    @Override
    public abstract PermissionNodeAbstract clone();

    public String getAsStatementString(){
        StringBuilder stringBuilder = new StringBuilder();
        privileges.entries().forEach(entry -> stringBuilder.append(";").append(entry.getKey().getName()).append(":").append(entry.getValue().toString()));
        return stringBuilder.length() == 0 ? "" : stringBuilder.substring(1);
    }

    protected abstract PrivilegeStatus getStatus(IslandPrivilege islandPrivilege);

    protected boolean isDefault(IslandPrivilege islandPrivilege){
        return false;
    }

    @Override
    public String toString() {
        return "PermissionNodeAbstract{" + privileges + "}";
    }

    protected enum PrivilegeStatus{

        ENABLED,
        DISABLED,
        DEFAULT;

        @Override
        public String toString() {
            switch (this){
                case ENABLED:
                    return "1";
                case DISABLED:
                    return "0";
                default:
                    return name();
            }
        }

        static PrivilegeStatus of(String value) throws IllegalArgumentException{
            switch (value){
                case "0":
                    return DISABLED;
                case "1":
                    return ENABLED;
                default:
                    return valueOf(value);
            }
        }

    }


}
