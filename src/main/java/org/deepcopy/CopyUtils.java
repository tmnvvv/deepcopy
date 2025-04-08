package org.deepcopy;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CopyUtils {

    private static final Set<Class<?>> BASE_TYPES = Set.of(
            String.class, Integer.class, Long.class, Short.class,
            Byte.class, Float.class, Double.class, Boolean.class,
            Character.class, BigDecimal.class, BigInteger.class, UUID.class
    );

    private static final Map<Class<?>, Constructor<?>> constructorCache = new ConcurrentHashMap<>();

    public static <T> T deepCopy(T original) {
        Map<Object, Object> visited = new IdentityHashMap<>();
        return deepCopyInternal(original, visited);
    }

    @SuppressWarnings("unchecked")
    private static <T> T deepCopyInternal(T original, Map<Object, Object> visited) {
        if (original == null) return null;

        if (BASE_TYPES.contains(original.getClass()) || original.getClass().isEnum() || original.getClass().isPrimitive()) {
            return original;
        }

        if (visited.containsKey(original)) {
            return (T) visited.get(original);
        }

        if (original.getClass().isRecord()) {
            return copyRecord(original, visited);
        }

        if (original instanceof Collection<?>) {
            Collection<?> collection = (Collection<?>) original;
            Collection<Object> copy = createCollectionInstance(original.getClass());
            visited.put(original, copy);
            for (Object item : collection) {
                copy.add(deepCopyInternal(item, visited));
            }
            return (T) copy;
        }

        if (original instanceof Map<?, ?>) {
            Map<Object, Object> copy = createMapInstance(original.getClass());
            visited.put(original, copy);
            ((Map<?, ?>) original).forEach((k, v) -> {
                Object keyCopy = deepCopyInternal(k, visited);
                Object valueCopy = deepCopyInternal(v, visited);
                copy.put(keyCopy, valueCopy);
            });
            return (T) copy;
        }

        if (original.getClass().isArray()) {
            int length = Array.getLength(original);
            Object copy = Array.newInstance(original.getClass().getComponentType(), length);
            visited.put(original, copy);
            for (int i = 0; i < length; i++) {
                Array.set(copy, i, deepCopyInternal(Array.get(original, i), visited));
            }
            return (T) copy;
        }

        if (original instanceof Cloneable) {
            try {
                Method cloneMethod = original.getClass().getDeclaredMethod("clone");
                cloneMethod.setAccessible(true);
                Object cloned = cloneMethod.invoke(original);
                visited.put(original, cloned);
                return (T) cloned;
            } catch (Exception ignored) {
            }
        }

        try {
            Constructor<?> constructor = constructorCache.computeIfAbsent(original.getClass(), cls -> {
                try {
                    Constructor<?> ctor = cls.getDeclaredConstructor();
                    ctor.setAccessible(true);
                    return ctor;
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException("No default constructor for " + cls, e);
                }
            });

            Object copy = constructor.newInstance();
            visited.put(original, copy);

            for (Field field : getAllFields(original.getClass())) {
                field.setAccessible(true);
                if (Modifier.isFinal(field.getModifiers())) continue;

                Object fieldValue = field.get(original);
                Object copiedValue = deepCopyInternal(fieldValue, visited);

                if (fieldValue instanceof List) {
                    List<?> list = (List<?>) fieldValue;
                    List<Object> deepCopiedList = new ArrayList<>(list.size());
                    for (Object item : list) {
                        deepCopiedList.add(deepCopyInternal(item, visited));
                    }
                    field.set(copy, deepCopiedList);
                } else {
                    field.set(copy, copiedValue);
                }
            }

            return (T) copy;
        } catch (Exception e) {
            throw new DeepCopyException("Failed to deep copy object of type: " + original.getClass().getName(), e);
        }
    }


    private static <T> T copyRecord(T original, Map<Object, Object> visited) {
        try {
            Class<?> clazz = original.getClass();
            var recordComponents = clazz.getRecordComponents();

            Object[] values = new Object[recordComponents.length];
            for (int i = 0; i < recordComponents.length; i++) {
                Method getterMethod = recordComponents[i].getAccessor();
                Object fieldValue = getterMethod.invoke(original);

                if (fieldValue instanceof Collection || fieldValue instanceof Map || fieldValue.getClass().isArray()) {
                    values[i] = deepCopyInternal(fieldValue, visited);
                } else {
                    values[i] = fieldValue;
                }
            }

            var constructor = clazz.getDeclaredConstructor(getTypes(recordComponents));
            return (T) constructor.newInstance(values);
        } catch (Exception e) {
            throw new DeepCopyException("Failed to copy record: " + original.getClass().getName(), e);
        }
    }

    private static Class<?>[] getTypes(java.lang.reflect.RecordComponent[] recordComponents) {
        Class<?>[] types = new Class<?>[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
            types[i] = recordComponents[i].getType();
        }
        return types;
    }

    private static Collection<Object> createCollectionInstance(Class<?> cls) {
        if (SortedSet.class.isAssignableFrom(cls)) return new TreeSet<>();
        if (Set.class.isAssignableFrom(cls)) return new HashSet<>();
        if (List.class.isAssignableFrom(cls)) return new ArrayList<>();
        return new LinkedList<>();
    }

    private static Map<Object, Object> createMapInstance(Class<?> cls) {
        if (SortedMap.class.isAssignableFrom(cls)) return new TreeMap<>();
        return new HashMap<>();
    }

    private static List<Field> getAllFields(Class<?> cls) {
        List<Field> fields = new ArrayList<>();
        while (cls != null && cls != Object.class) {
            fields.addAll(Arrays.asList(cls.getDeclaredFields()));
            cls = cls.getSuperclass();
        }
        return fields;
    }
}

