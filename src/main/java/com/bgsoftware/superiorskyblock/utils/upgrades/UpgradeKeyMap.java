package com.bgsoftware.superiorskyblock.utils.upgrades;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class UpgradeKeyMap {

    private final SyncedObject<KeyMap<Pair<Integer, Integer>>> value = SyncedObject.of(new KeyMap<>());

    private UpgradeKeyMap(){
    }

    public Integer get(Key key, int def){
        return value.readAndGet(map -> {
            Pair<Integer, Integer> pair = map.get(key);
            return pair == null ? def : pair.getKey() < 0 ? pair.getValue() : pair.getKey();
        });
    }

    public Integer getRaw(Key key, int def){
        return value.readAndGet(map -> {
            Pair<Integer, Integer> pair = map.getRaw(key, null);
            return pair == null ? def : pair.getKey() < 0 ? pair.getValue() : pair.getKey();
        });
    }

    public boolean containsKey(Key key){
        return value.readAndGet(map -> map.containsKey(key));
    }

    public int getValue(Key key, int def){
        return value.readAndGet(map -> {
            Pair<Integer, Integer> pair = map.get(key);
            return pair == null ? def : pair.getKey();
        });
    }

    public void set(Key key, Integer value){
        this.value.write(map -> map.computeIfAbsent(key, k -> new Pair<>(-1, 0)).setKey(value));
    }

    public void set(KeyMap<Integer> otherMap){
        this.value.write(map -> otherMap.forEach((key, value) ->
                map.computeIfAbsent(key, k -> new Pair<>(-1, 0)).setKey(value)));
    }

    public void remove(Key key){
        this.value.write(map -> map.remove(key));
    }

    public void setUpgradeString(Map<String, Integer> upgrades, boolean checkMax){
        KeyMap<Pair<Integer, Integer>> map = this.value.readAndGet(KeyMap::new);
        for(Map.Entry<String, Integer> entry : upgrades.entrySet()){
            Pair<Integer, Integer> pair = map.get(entry.getKey());
            if(pair == null || (!checkMax || entry.getValue() > pair.getValue())){
                this.value.write(_map -> _map.computeIfAbsent(Key.of(entry.getKey()), k -> new Pair<>(-1, 0))
                        .setValue(entry.getValue()));
            }
        }
    }

    public void setUpgrade(Map<Key, Integer> upgrades, boolean checkMax){
        KeyMap<Pair<Integer, Integer>> map = this.value.readAndGet(KeyMap::new);
        for(Map.Entry<Key, Integer> entry : upgrades.entrySet()){
            Pair<Integer, Integer> pair = map.getRaw(entry.getKey(), null);
            if(pair == null || (!checkMax || entry.getValue() > pair.getValue())){
                this.value.write(_map -> _map.computeIfAbsent(entry.getKey(), k -> new Pair<>(-1, 0))
                        .setValue(entry.getValue()));
            }
        }
    }

    public com.bgsoftware.superiorskyblock.utils.key.Key getKey(com.bgsoftware.superiorskyblock.utils.key.Key originalKey){
        return value.readAndGet(map -> map.getKey(originalKey));
    }

    public <R> R readAndGet(Function<KeyMap<Pair<Integer, Integer>>, R> function){
        return this.value.readAndGet(function);
    }

    public void clear(){
        this.value.write(Map::clear);
    }

    public void clearUpgrades(){
        this.value.write(map -> {
            for(Key key : map.keySet()){
                Pair<Integer, Integer> pair = map.getRaw(key, null);
                if(pair != null && pair.getKey() < 0)
                    map.remove(key);
            }
        });
    }

    public void map(Function<Integer, Integer> function){
        this.value.write(map -> map.keySet().forEach(mat -> {
            Pair<Integer, Integer> pair = map.get(mat);
            pair.setKey(function.apply(pair.getKey()));
        }));
    }

    public Set<Key> keySet(){
        return value.readAndGet(Map::keySet);
    }

    public KeyMap<Integer> copy(){
        KeyMap<Integer> copyMap = new KeyMap<>();

        this.value.read(map -> {
            for(Map.Entry<Key, Pair<Integer, Integer>> entry : map.entrySet()){
                if(entry.getValue().getKey() < 0)
                    copyMap.put(entry.getKey(), entry.getValue().getValue());
                else
                    copyMap.put(entry.getKey(), entry.getValue().getKey());
            }
        });

        return copyMap;
    }

    public static UpgradeKeyMap createMap(){
        return new UpgradeKeyMap();
    }

}
