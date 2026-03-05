package com.zl.mjga.utils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Extended BeanUtils that supports ignoring null, empty, and blank string values when copying
 * properties.
 */
public final class BeanUtils {

    private BeanUtils() {}

    /**
     * Copy the property values of the given source bean into the target bean, ignoring null,
     * empty, and blank string values.
     *
     * @param source the source bean
     * @param target the target bean
     * @throws BeansException if the copying failed
     */
    public static void copyPropertiesIgnoreBlank(Object source, Object target) throws BeansException {
        copyPropertiesIgnoreBlank(source, target, null, (String[]) null);
    }

    /**
     * Copy the property values of the given source bean into the target bean, ignoring null,
     * empty, and blank string values.
     *
     * @param source the source bean
     * @param target the target bean
     * @param ignoreProperties array of property names to ignore
     * @throws BeansException if the copying failed
     */
    public static void copyPropertiesIgnoreBlank(
            Object source, Object target, String... ignoreProperties) throws BeansException {
        copyPropertiesIgnoreBlank(source, target, null, ignoreProperties);
    }

    /**
     * Copy the property values of the given source bean into the target bean, ignoring null,
     * empty, and blank string values.
     *
     * @param source the source bean
     * @param target the target bean
     * @param editable the class (or interface) to restrict property setting to
     * @param ignoreProperties array of property names to ignore
     * @throws BeansException if the copying failed
     */
    private static void copyPropertiesIgnoreBlank(
            Object source,
            Object target,
            Class<?> editable,
            String... ignoreProperties)
            throws BeansException {

        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        Class<?> actualEditable = target.getClass();
        if (editable != null) {
            if (!editable.isInstance(target)) {
                throw new IllegalArgumentException(
                        "Target class ["
                                + target.getClass().getName()
                                + "] not assignable to Editable class ["
                                + editable.getName()
                                + "]");
            }
            actualEditable = editable;
        }

        PropertyDescriptor[] targetPds =
                org.springframework.beans.BeanUtils.getPropertyDescriptors(actualEditable);
        Set<String> ignoreSet = ignoreProperties != null ? new HashSet<>(Arrays.asList(ignoreProperties)) : null;

        for (PropertyDescriptor targetPd : targetPds) {
            Method writeMethod = targetPd.getWriteMethod();
            if (writeMethod != null
                    && (ignoreSet == null || !ignoreSet.contains(targetPd.getName()))) {
                PropertyDescriptor sourcePd =
                        org.springframework.beans.BeanUtils.getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null && sourcePd.getReadMethod() != null) {
                    try {
                        Method readMethod = sourcePd.getReadMethod();
                        if (!ClassUtils.isAssignable(
                                writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
                            continue;
                        }
                        if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                            readMethod.setAccessible(true);
                        }
                        Object value = readMethod.invoke(source);
                        // Skip null, empty, or blank values
                        if (shouldSkipValue(value)) {
                            continue;
                        }
                        if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                            writeMethod.setAccessible(true);
                        }
                        writeMethod.invoke(target, value);
                    } catch (Throwable ex) {
                        throw new FatalBeanException(
                                "Could not copy property '" + targetPd.getName() + "' from source to target", ex);
                    }
                }
            }
        }
    }

    /**
     * Check if the value should be skipped (null, empty, or blank).
     */
    private static boolean shouldSkipValue(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String str) {
            return str.isBlank();
        }
        return false;
    }
}
