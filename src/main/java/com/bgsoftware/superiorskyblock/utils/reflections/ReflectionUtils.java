package com.bgsoftware.superiorskyblock.utils.reflections;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ReflectionUtils {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    private static Registry<Fields, Field> fieldsMap = Registry.createRegistry();
    private static Registry<Methods, Method> methodsMap = Registry.createRegistry();

    static {
        Class<?> chunkProviderClass = getClass("net.minecraft.server.VERSION.ChunkProviderServer"),
                blockFlowerPotClass = getClass("net.minecraft.server.VERSION.BlockFlowerPot"),
                craftInventoryClass = getClass("org.bukkit.craftbukkit.VERSION.inventory.CraftInventory"),
                biomeGridClass = getClass("org.bukkit.craftbukkit.VERSION.generator.CustomChunkGenerator$CustomBiomeGrid"),
                chunkSectionClass = getClass("net.minecraft.server.VERSION.ChunkSection"),
                worldClass = getClass("net.minecraft.server.VERSION.World"),
                playerChunkMapClass = getClass("net.minecraft.server.VERSION.PlayerChunkMap"),
                chunkCoordIntPairClass = getClass("net.minecraft.server.VERSION.ChunkCoordIntPair");

        if(ServerVersion.isAtLeast(ServerVersion.v1_15)){
            Class<?> biomeStorageClass = getClass("net.minecraft.server.VERSION.BiomeStorage");
            fieldsMap.add(Fields.BIOME_GRID_BIOME_STORAGE, getField(biomeGridClass, "biome"));
            Field field = getField(biomeStorageClass, "f");
            if(field != null && !field.getType().getName().contains("BiomeBase"))
                field = getField(biomeStorageClass, "g");
            fieldsMap.add(Fields.BIOME_STORAGE_BIOME_BASES, field);
        }

        if(ServerVersion.isAtLeast(ServerVersion.v1_14)){
            methodsMap.add(Methods.PLAYER_CHUNK_MAP_IS_OUTSIDE_OF_RANGE, getMethod(playerChunkMapClass, "isOutsideOfRange", chunkCoordIntPairClass));
        }

        if(ServerVersion.isAtLeast(ServerVersion.v1_13)) {
            fieldsMap.add(Fields.BLOCK_FLOWER_POT_CONTENT, getField(blockFlowerPotClass, "c"));
            fieldsMap.add(Fields.CRAFT_INVENTORY_INVENTORY, getField(craftInventoryClass, "inventory"));
        }

        if(ServerVersion.isEquals(ServerVersion.v1_11) || ServerVersion.isEquals(ServerVersion.v1_12)) {
            fieldsMap.add(Fields.CHUNK_SECTION_BLOCK_IDS, getField(chunkSectionClass, "blockIds"));
            fieldsMap.add(Fields.CHUNK_SECTION_EMITTED_LIGHT, getField(chunkSectionClass, "emittedLight"));
            fieldsMap.add(Fields.CHUNK_SECTION_NON_EMPTY_BLOCK_COUNT, getField(chunkSectionClass, "nonEmptyBlockCount"));
            fieldsMap.add(Fields.CHUNK_SECTION_SKY_LIGHT, getField(chunkSectionClass, "skyLight"));
            fieldsMap.add(Fields.CHUNK_SECTION_TICKING_BLOCK_COUNT, getField(chunkSectionClass, "tickingBlockCount"));
        }

        if(ServerVersion.isEquals(ServerVersion.v1_8)) {
            try { fieldsMap.add(Fields.CHUNK_PROVIDER_UNLOAD_QUEUE, getField(chunkProviderClass, "unloadQueue")); } catch (Exception ignored) { }
            try { fieldsMap.add(Fields.WORLD_CHUNK_TICK_LIST, getField(worldClass, "chunkTickList")); } catch (Exception ignored) { }
        }
    }

    public static Class<?> getClass(String classPath){
        try {
            return Class.forName(classPath.replace("VERSION", version));
        } catch(ClassNotFoundException ex){
            throw new NullPointerException(ex.getMessage());
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes){
        try{
            return clazz.getConstructor(parameterTypes);
        }catch(NoSuchMethodException ex){
            return null;
        }
    }

    public static Constructor<?> getDeclaredConstructor(Class<?> clazz, Class<?>... parameterTypes){
        try{
            Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        }catch(NoSuchMethodException ex){
            return null;
        }
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?> returnType, Class<?>... parameterTypes){
        try{
            Method method = clazz.getMethod(name, parameterTypes);
            return returnType == null || method.getReturnType().equals(returnType) ? method : null;
        }catch(NoSuchMethodException ex){
            return null;
        }
    }

    static Field getFromFields(Fields fields){
        return fieldsMap.get(fields);
    }

    static Method getFromMethod(Methods methods){
        return methodsMap.get(methods);
    }

    private static Field getField(Class<?> clazz, String fieldName){
        try{
            Field field =  clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("&cCouldn't find the field " + fieldName + " - Please contact Ome_R!");
            Executor.sync(() -> Bukkit.getPluginManager().disablePlugin(plugin));
            return null;
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static Method getMethod(Class<?> clazz, String methodName, Class<?> parameterTypes){
        try{
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method;
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("&cCouldn't find the method " + methodName + " - Please contact Ome_R!");
            Executor.sync(() -> Bukkit.getPluginManager().disablePlugin(plugin));
            return null;
        }
    }

}
