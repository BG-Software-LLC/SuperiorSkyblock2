package com.ome_r.superiorskyblock.island;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.utils.jnbt.ListTag;
import com.ome_r.superiorskyblock.utils.jnbt.StringTag;
import com.ome_r.superiorskyblock.utils.jnbt.Tag;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class PermissionNode implements Cloneable {

    private Set<IslandPermission> nodes = new HashSet<>();

    public PermissionNode(ListTag tag){
        List<Tag> list = tag.getValue();

        for(Tag _tag : list)
            nodes.add(IslandPermission.valueOf(((StringTag) _tag).getValue()));

    }

    public PermissionNode(PermissionNode other, List<String> permissions){
        if(other != null) {
            this.nodes = new HashSet<>(other.nodes);
        }

        List<IslandPermission> permissionList = new ArrayList<>();

        for(String permission : permissions)
            permissionList.add(IslandPermission.valueOf(permission));

        nodes.addAll(permissionList);
    }

    public boolean hasPermission(IslandPermission permission){
        return nodes.contains(IslandPermission.ALL) || nodes.contains(permission);
    }

    public void setPermission(IslandPermission permission, boolean value){
        if(value){
            nodes.add(permission);
        }else{
            nodes.remove(permission);
        }
    }

    public String getColoredPermissions(){
        StringBuilder stringBuilder = new StringBuilder();

        for(IslandPermission islandPermission : IslandPermission.values()){
            boolean isEnabled = hasPermission(islandPermission);
            stringBuilder.append(Locale.PERMISSION_SPACER.getMessage()).append(isEnabled ? "&a" : "&c").append(islandPermission.name().toLowerCase());
        }

        String message = stringBuilder.toString().substring(Locale.PERMISSION_SPACER.getMessage().length());

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public PermissionNode clone() {
        try {
            PermissionNode permissionNode = (PermissionNode) super.clone();
            permissionNode.nodes = new HashSet<>(nodes);
            return permissionNode;
        }catch(CloneNotSupportedException ex){
            return new PermissionNode(this, new ArrayList<>());
        }
    }

    public Tag getAsTag(){
        List<Tag> enabledPermissions = new ArrayList<>();

        nodes.forEach(islandPermission -> enabledPermissions.add(new StringTag(islandPermission.name())));

        return new ListTag(StringTag.class, enabledPermissions);
    }


}
