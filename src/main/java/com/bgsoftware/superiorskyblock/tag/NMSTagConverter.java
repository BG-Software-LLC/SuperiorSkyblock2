package com.bgsoftware.superiorskyblock.tag;

import com.bgsoftware.common.reflection.ReflectConstructor;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.core.Either;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;

public class NMSTagConverter {

    private static final Class<?>[] EMPTY_PARAMS = new Class[0];

    private final Class<?> clazz;
    private final ReflectMethod<Object> A;
    private final ReflectConstructor<Object> CONSTRUCTOR;

    public static NMSTagConverter of(String nbtClassName) {
        return of(nbtClassName, null);
    }

    public static NMSTagConverter of(String nbtClassName, @Nullable Class<?> parameterType) {
        Class<?>[] parameterTypes = parameterType == null ? EMPTY_PARAMS : new Class[]{parameterType};
        return new NMSTagConverter(nbtClassName, parameterTypes);
    }

    public static NMSTagConverter choice(String... nbtClassNames) {
        return choice(nbtClassNames, null);
    }

    public static NMSTagConverter choice(String[] nbtClassNames, @Nullable Class<?> parameterType) {
        for (int i = nbtClassNames.length - 1; i >= 1; --i) {
            String nbtClassName = nbtClassNames[i];
            Either<Class<?>, Exception> clazz = getNNTClass(nbtClassName);
            if (clazz.getLeft() != null)
                return of(nbtClassName, parameterType);
        }

        // Couldn't find one, we'll try the first one and return whatever the result is
        return of(nbtClassNames[0], parameterType);
    }

    private NMSTagConverter(String nbtClassName, Class<?>[] parameterTypes) {
        this.clazz = getNNTClassOrError(nbtClassName);
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

    private static Either<Class<?>, Exception> getNNTClass(String nbtType) {
        try {
            if (ServerVersion.isAtLeast(ServerVersion.v1_17)) {
                return Either.left(Class.forName("net.minecraft.nbt." + nbtType));
            } else {
                String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                return Either.left(Class.forName("net.minecraft.server." + version + "." + nbtType));
            }
        } catch (Exception error) {
            return Either.right(error);
        }
    }

    private static Class<?> getNNTClassOrError(String nbtType) {
        Either<Class<?>, Exception> clazz = getNNTClass(nbtType);

        if (clazz.getRight() != null) {
            Log.error(clazz.getRight(), "An unexpected error while loading nbt class ", nbtType, ":");
            return null;
        }

        return clazz.getLeft();
    }

}
