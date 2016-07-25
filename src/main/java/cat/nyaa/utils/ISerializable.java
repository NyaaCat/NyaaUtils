package cat.nyaa.utils;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.logging.Level;

public interface ISerializable {
    /**
     * For informative only
     */
    @Target(ElementType.FIELD)
    public @interface Ephemeral {
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Serializable {
        String name() default "";
    }

    default void deserialize(ConfigurationSection config) {
        deserialize(config, this);
    }

    default void serialize(ConfigurationSection config) {
        serialize(config, this);
    }

    static void deserialize(ConfigurationSection config, Object obj) {
        Class<?> clz = obj.getClass();
        for (Field f : clz.getDeclaredFields()) {
            Serializable anno = f.getAnnotation(Serializable.class);
            if (anno == null) continue;
            f.setAccessible(true);
            String cfgName = anno.name().equals("") ? f.getName() : anno.name();
            try {
                Object origValue = f.get(obj);
                Object newValue;
                if (f.getType().isEnum()) {
                    try {
                        newValue = Enum.valueOf((Class<? extends Enum>) f.getType(), config.getString(cfgName));
                    } catch (Exception ex) {
                        newValue = origValue;
                    }
                } else {
                    newValue = config.get(cfgName, origValue);
                }
                f.set(obj, newValue);
            } catch (ReflectiveOperationException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to deserialize object", ex);
            }
        }
    }

    static void serialize(ConfigurationSection config, Object obj) {
        Class<?> clz = obj.getClass();
        for (Field f : clz.getDeclaredFields()) {
            Serializable anno = f.getAnnotation(Serializable.class);
            if (anno == null) continue;
            f.setAccessible(true);
            String cfgName = anno.name().equals("") ? f.getName() : anno.name();
            try {
                if (f.getType().isEnum()) {
                    Enum e = (Enum) f.get(obj);
                    config.set(cfgName, e.name());
                } else {
                    Object origValue = f.get(obj);
                    config.set(cfgName, origValue);
                }
            } catch (ReflectiveOperationException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "Failed to serialize object", ex);
            }
        }
    }
}
