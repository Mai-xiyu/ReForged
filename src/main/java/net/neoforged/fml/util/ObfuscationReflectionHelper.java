package net.neoforged.fml.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;

/**
 * Reflection utilities. Delegates to Forge's implementation where possible.
 */
@SuppressWarnings({"unchecked", "unused"})
public class ObfuscationReflectionHelper {

    @Nullable
    public static <T, E> T getPrivateValue(Class<? super E> classToAccess, E instance, String fieldName) {
        try {
            return net.minecraftforge.fml.util.ObfuscationReflectionHelper.getPrivateValue(classToAccess, instance, fieldName);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get field " + fieldName + " from " + classToAccess.getName(), e);
        }
    }

    public static <T, E> void setPrivateValue(Class<? super E> classToAccess, E instance, @Nullable T value, String fieldName) {
        try {
            net.minecraftforge.fml.util.ObfuscationReflectionHelper.setPrivateValue(classToAccess, instance, value, fieldName);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set field " + fieldName + " in " + classToAccess.getName(), e);
        }
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        return net.minecraftforge.fml.util.ObfuscationReflectionHelper.findField(clazz, fieldName);
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return net.minecraftforge.fml.util.ObfuscationReflectionHelper.findMethod(clazz, methodName, parameterTypes);
    }

    public static <T> Constructor<T> findConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            Constructor<T> ctr = clazz.getDeclaredConstructor(parameterTypes);
            ctr.setAccessible(true);
            return ctr;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find constructor for " + clazz.getName(), e);
        }
    }
}
