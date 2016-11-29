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
        String[] alias() default {};
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface StandaloneConfig {
        boolean manualSerialization() default false;
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
            // standalone config
            StandaloneConfig standaloneAnno = f.getAnnotation(StandaloneConfig.class);
            if (standaloneAnno != null && !standaloneAnno.manualSerialization()) {
                if (FileConfigure.class.isAssignableFrom(f.getType())) {
                    FileConfigure standaloneCfg = null;
                    f.setAccessible(true);
                    try {
                        standaloneCfg = (FileConfigure) f.get(obj);
                    } catch (ReflectiveOperationException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "Failed to deserialize object", ex);
                        standaloneCfg = null;
                    }
                    if (standaloneCfg != null) {
                        standaloneCfg.load();
                        continue;
                    }
                }
            }

            // Normal fields
            Serializable anno = f.getAnnotation(Serializable.class);
            if (anno == null) continue;
            f.setAccessible(true);
            String cfgName = anno.name().equals("") ? f.getName() : anno.name();
            try {
                Object origValue = f.get(obj);
                Object newValue = null;
                boolean hasValue = false;
                for (String key : anno.alias()) {
                    if (config.contains(key)) {
                        newValue = config.get(key);
                        hasValue = true;
                        break;
                    }
                }
                if (!hasValue && config.contains(f.getName())) {
                    newValue = config.get(f.getName());
                    hasValue = true;
                }
                if (!hasValue && anno.name().length() >0 && config.contains(anno.name())) {
                    newValue = config.get(anno.name());
                    hasValue = true;
                }
                if (!hasValue) {
                    continue;
                }

                if (f.getType().isEnum()) {
                    try {
                        newValue = Enum.valueOf((Class<? extends Enum>) f.getType(), (String)newValue);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        continue;
                    }
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
            // standalone config
            StandaloneConfig standaloneAnno = f.getAnnotation(StandaloneConfig.class);
            if (standaloneAnno != null && !standaloneAnno.manualSerialization()) {
                if (FileConfigure.class.isAssignableFrom(f.getType())) {
                    FileConfigure standaloneCfg = null;
                    f.setAccessible(true);
                    try {
                        standaloneCfg = (FileConfigure) f.get(obj);
                    } catch (ReflectiveOperationException ex) {
                        Bukkit.getLogger().log(Level.SEVERE, "Failed to serialize object", ex);
                        standaloneCfg = null;
                    }
                    if (standaloneCfg != null) {
                        standaloneCfg.save();
                        continue;
                    }
                }
            }

            // Normal fields
            Serializable anno = f.getAnnotation(Serializable.class);
            if (anno == null) continue;
            f.setAccessible(true);
            String cfgName;
            if (anno.name().equals("")) {
                cfgName = f.getName();
            } else {
                cfgName = anno.name();
                config.set(f.getName(), null);
            }
            for (String key : anno.alias()) {
                config.set(key, null);
            }
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
