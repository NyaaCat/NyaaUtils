package cat.nyaa.nyaautils;

import cat.nyaa.utils.FileConfigure;
import cat.nyaa.utils.ISerializable;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Acl extends FileConfigure {
    
    public static class list implements ISerializable {
        @Serializable
        boolean enchant = true;
        @Serializable
        boolean repair = true;

        public list normalize() {
            return this;
        }
    }
    
    private static final Map<String, list> aclMap = new HashMap<>();
    private final NyaaUtils plugin;
    public Acl(NyaaUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getFileName() {
        return "acl.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        aclMap.clear();
        for (String key : config.getKeys(false)) {
            if (!config.isConfigurationSection(key)) continue;
            list list = new list();
            list.deserialize(config.getConfigurationSection(key));
            aclMap.put(key, list.normalize());
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        Set<String> tmp = new HashSet<>(config.getKeys(false));
        for (String key : tmp) { // clear section
            config.set(key, null);
        }

        for (Map.Entry<String, list> pair : aclMap.entrySet()) {
            ConfigurationSection section = config.createSection(pair.getKey());
            pair.getValue().normalize().serialize(section);
        }
    }

    public boolean canEnchant(List<String> lore) {
        for (String s : lore) {
            if (aclMap.containsKey(ChatColor.stripColor(s))) {
                if (aclMap.get(ChatColor.stripColor(s)).enchant != plugin.cfg.acl_default_enchant) {
                    return aclMap.get(ChatColor.stripColor(s)).enchant;
                }
            }
        }
        return plugin.cfg.acl_default_enchant;

    }

    public boolean canRepair(List<String> lore) {
        for (String s : lore) {
            if (aclMap.containsKey(ChatColor.stripColor(s))) {
                if (aclMap.get(ChatColor.stripColor(s)).repair != plugin.cfg.acl_default_repair) {
                    return aclMap.get(ChatColor.stripColor(s)).repair;
                }
            }
        }
        return plugin.cfg.acl_default_repair;
    }
}
