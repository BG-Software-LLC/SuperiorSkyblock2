package com.bgsoftware.superiorskyblock.tag;

import com.bgsoftware.common.reflection.ReflectConstructor;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Bukkit;

public class NMSTagConverter {

    private final Class<?> clazz;
    private final ReflectMethod<Object> A;
    private final ReflectConstructor<Object> CONSTRUCTOR;

    public NMSTagConverter(String nbtClassName, Class<?>... parameterTypes) {
        this.clazz = getNNTClass(nbtClassName);
        this.A = new ReflectMethod<>(this.clazz, this.clazz, "a", parameterTypes);
        this.CONSTRUCTOR = new ReflectConstructor<>(this.clazz, parameterTypes);
    }

    public Class<?> getNBTClass() {
        return clazz;
    }

    public Object toNBT(Object... args) {
        if (A.isValid()) {
            return A.invoke(null, args);
        } else {
            return CONSTRUCTOR.newInstance(args);
        }
    }

    private static Class<?> getNNTClass(String nbtType) {
        try {
            if (ServerVersion.isAtLeast(ServerVersion.v1_17)) {
                return Class.forName("net.minecraft.nbt." + nbtType);
            } else {
                String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                return Class.forName("net.minecraft.server." + version + "." + nbtType);
            }
        } catch (Exception error) {
            Log.error(error, "An unexpected error while loading nbt class ", nbtType, ":");
            return null;
        }
    }

}
