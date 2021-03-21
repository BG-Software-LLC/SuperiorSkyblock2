package com.bgsoftware.superiorskyblock.island.permissions;

import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PermissionNode;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.google.common.base.Preconditions;

@SuppressWarnings("WeakerAccess")
public abstract class PermissionNodeAbstract implements PermissionNode {

    protected final Registry<IslandPrivilege, PrivilegeStatus> privileges;

    protected PermissionNodeAbstract(){
        this.privileges = Registry.createRegistry();
    }

    protected PermissionNodeAbstract(Registry<IslandPrivilege, PrivilegeStatus> privileges){
        this.privileges = Registry.createRegistry(privileges);
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
    public abstract boolean hasPermission(IslandPrivilege permission);

    @Override
    public void setPermission(IslandPrivilege islandPrivilege, boolean value){
        Preconditions.checkNotNull(islandPrivilege, "islandPrivilege parameter cannot be null.");
        privileges.add(islandPrivilege, value ? PrivilegeStatus.ENABLED : PrivilegeStatus.DISABLED);
    }

    @Override
    public abstract PermissionNodeAbstract clone();

    protected boolean isDefault(IslandPrivilege islandPrivilege){
        return false;
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
