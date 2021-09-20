package com.bgsoftware.superiorskyblock.key.dataset;

import com.bgsoftware.superiorskyblock.key.Key;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public final class KeySet extends AbstractSet<com.bgsoftware.superiorskyblock.api.key.Key> implements Set<com.bgsoftware.superiorskyblock.api.key.Key> {

    private final Set<String> set;

    public KeySet(){
        this.set = new HashSet<>();
    }

    public KeySet(List<String> keys){
        this.set = new HashSet<>(keys);
    }

    @Override
    public Iterator<com.bgsoftware.superiorskyblock.api.key.Key> iterator() {
        return asKeySet().iterator();
    }

    @Override
    public int size() {
        return set.size();
    }


    public boolean contains(Block block) {
        return contains(Key.of(block));
    }
    public boolean contains(ItemStack itemStack) {
        return contains(Key.of(itemStack));
    }

    public boolean contains(Material material, short data) {
        return contains(Key.of(material, data));
    }

    public boolean contains(String key) {
        return contains(Key.of(key));
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Key && (set.contains(o.toString()) || (!((Key) o).getSubKey().isEmpty() && set.contains(((Key) o).getGlobalKey())));
    }

    public Key getKey(com.bgsoftware.superiorskyblock.api.key.Key key){
        if(set.contains(key.toString()))
            return (Key) key;
        else if(set.contains(key.getGlobalKey()))
            return Key.of(key.getGlobalKey(), "");
        else
            return (Key) key;
    }

    @Override
    public boolean add(com.bgsoftware.superiorskyblock.api.key.Key key) {
        return set.add(key.toString());
    }

    @Override
    public boolean remove(Object o) {
        return o instanceof Key ? set.remove(o.toString()) : set.remove(o);
    }

    private Set<com.bgsoftware.superiorskyblock.api.key.Key> asKeySet(){
        Set<com.bgsoftware.superiorskyblock.api.key.Key> set = new HashSet<>();
        this.set.forEach(string -> set.add(Key.of(string)));
        return set;
    }

}
