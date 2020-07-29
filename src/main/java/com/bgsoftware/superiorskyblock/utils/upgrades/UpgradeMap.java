package com.bgsoftware.superiorskyblock.utils.upgrades;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.utils.threads.SyncedObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class UpgradeMap<K> {

    private final SyncedObject<Map<K, Pair<Integer, Integer>>> value = SyncedObject.of(new HashMap<>());

    private UpgradeMap(){
    }

    public Integer get(K key, int def){
        return value.readAndGet(map -> {
            Pair<Integer, Integer> pair = map.get(key);
            return pair == null ? def : pair.getKey() < 0 ? pair.getValue() : pair.getKey();
        });
    }

    public boolean containsKey(K key){
        return value.readAndGet(map -> map.containsKey(key));
    }

    public Integer getValue(K key){
        return value.readAndGet(map -> {
            Pair<Integer, Integer> pair = map.get(key);
            return pair == null ? null : pair.getKey();
        });
    }

    public void set(K key, Integer value){
        this.value.write(map -> map.computeIfAbsent(key, k -> new Pair<>(-1, 0)).setKey(value));
    }

    public void set(Map<K, Integer> otherMap){
        this.value.write(map -> otherMap.forEach((key, value) ->
                map.computeIfAbsent(key, k -> new Pair<>(-1, 0)).setKey(value)));
    }

    public void remove(K key){
        this.value.write(map -> map.remove(key));
    }

    public void setUpgrade(Map<K, Integer> upgrades){
        Map<K, Pair<Integer, Integer>> map = this.value.readAndGet(HashMap::new);
        for(Map.Entry<K, Integer> entry : upgrades.entrySet()){
            Pair<Integer, Integer> pair = map.get(entry.getKey());
            if(pair == null || entry.getValue() > pair.getValue()){
                this.value.write(_map -> _map.computeIfAbsent(entry.getKey(), k -> new Pair<>(-1, 0))
                        .setValue(entry.getValue()));
            }
        }
    }

    public <R> R readAndGet(Function<Map<K, Pair<Integer, Integer>>, R> function){
        return this.value.readAndGet(function);
    }

    public void clearUpgrades(){
        this.value.write(map -> map.values().forEach(pair -> pair.setValue(0)));
    }

    public void clear(){
        this.value.write(Map::clear);
    }

    public Set<K> keySet(){
        return value.readAndGet(Map::keySet);
    }

    public Map<K, Integer> copy(){
        Map<K, Integer> copyMap = new HashMap<>();

        this.value.read(map -> {
            for(Map.Entry<K, Pair<Integer, Integer>> entry : map.entrySet()){
                if(entry.getValue().getKey() < 0)
                    copyMap.put(entry.getKey(), entry.getValue().getValue());
                else
                    copyMap.put(entry.getKey(), entry.getValue().getKey());
            }
        });

        return copyMap;
    }

    public static <K> UpgradeMap<K> createMap(){
        return new UpgradeMap<>();
    }

}
