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
    private final JavaPlugin plugin;
    private String lang = null;

    public Internationalization(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    private void appendStrings(JavaPlugin plugin, ConfigurationSection section) {
        appendStrings(plugin, section, "");
    }

    private void appendStrings(JavaPlugin plugin, ConfigurationSection section, String prefix) {
        for (String key : section.getKeys(false)) {
            String path = prefix + key;
            if (section.isString(key)) {
                if (!map.containsKey(path)) {
                    map.put(path, ChatColor.translateAlternateColorCodes('&', section.getString(key)));
                }
            } else if (section.isConfigurationSection(key)) {
                appendStrings(plugin, section.getConfigurationSection(key), path + ".");
            } else {
                plugin.getLogger().warning("Skipped language section: " + path);
            }
        }

    }

    public void load(String language) {
        map.clear();
        File localLangFile = new File(plugin.getDataFolder(), language + ".yml");
        if (localLangFile.exists()) {
            appendStrings(plugin, YamlConfiguration.loadConfiguration(localLangFile));
        }
        InputStream stream = plugin.getResource("lang/" + language + ".yml");
        if (stream != null) appendStrings(plugin, YamlConfiguration.loadConfiguration(new InputStreamReader(stream)));
        stream = plugin.getResource("lang/" + DEFAULT_LANGUAGE + ".yml");
        if (stream != null) appendStrings(plugin, YamlConfiguration.loadConfiguration(new InputStreamReader(stream)));

        YamlConfiguration yaml = new YamlConfiguration();
        for (String key : map.keySet()) {
            yaml.set(key, map.get(key));
        }

        try {
            yaml.save(localLangFile);
        } catch (IOException ex) {
            plugin.getLogger().warning("Cannot save language file: " + language + ".yml");
        }

        lang = language;
        plugin.getLogger().info(get("internal.info.using_language", lang));
    }

    public String get(String key, Object... para) {
        String val = map.get(key);
        if (val == null) {
            plugin.getLogger().warning("Missing language key: " + key);
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
        return map.containsKey(key);
    }

    public void reset() {
        map.clear();
        lang = null;
    }
}
