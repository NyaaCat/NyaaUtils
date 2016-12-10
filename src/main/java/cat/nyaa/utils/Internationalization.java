package cat.nyaa.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public abstract class Internationalization {
    private final String DEFAULT_LANGUAGE = "en_US";
    private final Map<String, String> map = new HashMap<>();
    private static Map<String, String> internalMap = null;

    protected abstract JavaPlugin getPlugin();
    protected abstract String getLanguage();

    public void load() {
        String language = getLanguage();
        JavaPlugin plugin = getPlugin();
        if (language == null) language = DEFAULT_LANGUAGE;
        map.clear();
        // setup common (internal) language section
        if (internalMap == null) {
            internalMap = new HashMap<>();
            File localLangFile = new File(plugin.getDataFolder(), language + ".yml");
            if (localLangFile.exists()) {
                ConfigurationSection section = YamlConfiguration.loadConfiguration(localLangFile);
                loadLanguageSection(internalMap, section.getConfigurationSection("internal"), "internal.", false);
            }
            InputStream stream = plugin.getResource("lang/" + language + ".yml");
            if (stream != null) {
                ConfigurationSection section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
                loadLanguageSection(internalMap, section.getConfigurationSection("internal"), "internal.", false);
            }
            stream = plugin.getResource("lang/" + DEFAULT_LANGUAGE + ".yml");
            if (stream != null) {
                ConfigurationSection section = YamlConfiguration.loadConfiguration(new InputStreamReader(stream));
                loadLanguageSection(internalMap, section.getConfigurationSection("internal"), "internal.", false);
            }
        }
        // load modified language file from disk
        File localLangFile = new File(plugin.getDataFolder(), language + ".yml");
        if (localLangFile.exists()) {
            loadLanguageSection(map, YamlConfiguration.loadConfiguration(localLangFile), "", false);
        }
        // load same language from jar
        InputStream stream = plugin.getResource("lang/" + language + ".yml");
        if (stream != null) loadLanguageSection(map, YamlConfiguration.loadConfiguration(new InputStreamReader(stream)), "", true);
        // load default language from jar
        stream = plugin.getResource("lang/" + DEFAULT_LANGUAGE + ".yml");
        if (stream != null) loadLanguageSection(map, YamlConfiguration.loadConfiguration(new InputStreamReader(stream)), "", true);
        // save (probably) modified language file back to disk
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            for (String key : map.keySet()) {
                yaml.set(key, map.get(key));
            }
            yaml.save(localLangFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Cannot save language file: " + language + ".yml");
        }
        plugin.getLogger().info(get("internal.info.using_language", language));
    }

    /**
     * add all language items from section into language map recursively
     * existing items won't be overwritten
     *
     * @param section source section
     * @param prefix used in recursion to determine the proper prefix
     * @param ignoreInternal ignore keys prefixed with `internal'
     */
    private void loadLanguageSection(Map<String, String> map, ConfigurationSection section, String prefix, boolean ignoreInternal) {
        if (map == null || section == null || prefix == null) return;
        for (String key : section.getKeys(false)) {
            String path = prefix + key;
            if (section.isString(key)) {
                if (!map.containsKey(path) && (!ignoreInternal || !path.startsWith("internal."))) {
                    map.put(path, ChatColor.translateAlternateColorCodes('&', section.getString(key)));
                }
            } else if (section.isConfigurationSection(key)) {
                loadLanguageSection(map, section.getConfigurationSection(key), path + ".", ignoreInternal);
            } else {
                getPlugin().getLogger().warning("Skipped language section: " + path);
            }
        }
    }


    public String get(String key, Object... para) {
        String val = map.get(key);
        if (val == null || val.startsWith("internal.")) val = internalMap.get(key);
        if (val == null) {
            getPlugin().getLogger().warning("Missing language key: " + key);
            key = "MISSING_LANG<" + key + ">";
            for (Object obj : para) {
                key += "#<" + obj.toString() + ">";
            }
            return key;
        } else {
            return String.format(val, para);
        }
    }

    public boolean hasKey(String key) {
        return map.containsKey(key) || internalMap.containsKey(key);
    }

    public void reset() {
        map.clear();
    }
}
