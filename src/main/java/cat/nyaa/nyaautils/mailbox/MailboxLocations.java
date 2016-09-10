package cat.nyaa.nyaautils.mailbox;

import cat.nyaa.nyaautils.NyaaUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MailboxLocations {
    public final static String CFG_FILE_NAME = "mailbox_location.yml";
    private final NyaaUtils plugin;

    private final BiMap<UUID, String> nameMap = HashBiMap.create();
    private final Map<UUID, Location> locationMap = new HashMap<>();

    public MailboxLocations(NyaaUtils plugin) {
        this.plugin = plugin;
    }

    private File ensureFile() {
        File cfgFile = new File(plugin.getDataFolder(), CFG_FILE_NAME);
        if (!cfgFile.exists()) {
            cfgFile.getParentFile().mkdirs();
            try {
                cfgFile.createNewFile();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return cfgFile;
    }

    public void save() {
        YamlConfiguration cfg = new YamlConfiguration();
        ConfigurationSection locationSection = cfg.createSection("locations");
        for (UUID uuid : locationMap.keySet()) {
            locationSection.set(uuid.toString(), locationMap.get(uuid));
        }
        ConfigurationSection nameSection = cfg.createSection("names");
        for (UUID uuid : nameMap.keySet()) {
            nameSection.set(uuid.toString(), nameMap.get(uuid));
        }

        try {
            cfg.save(ensureFile());
        } catch (IOException ex) {
            plugin.getLogger().severe("Cannot save Mailbox location info. Emergency dump:");
            plugin.getLogger().severe("\n" + cfg.saveToString());
            plugin.getLogger().severe("Cannot save Mailbox location info. Emergency dump End.");
            throw new RuntimeException(ex);
        }
    }

    public void load() {
        YamlConfiguration cfg = new YamlConfiguration();
        try {
            cfg.load(ensureFile());
        } catch (IOException | InvalidConfigurationException ex) {
            throw new RuntimeException(ex);
        }
        locationMap.clear();
        nameMap.clear();
        ConfigurationSection locations = cfg.getConfigurationSection("locations");
        ConfigurationSection names = cfg.getConfigurationSection("names");
        if (locations != null) {
            for (String uuid_s : locations.getKeys(false)) {
                locationMap.put(UUID.fromString(uuid_s), (Location) locations.get(uuid_s));
            }
        }
        if (names != null) {
            for (String uuid_s : names.getKeys(false)) {
                nameMap.put(UUID.fromString(uuid_s), names.getString(uuid_s).toLowerCase());
            }
        }

        for (Player p : plugin.getServer().getOnlinePlayers()) {
            nameMap.put(p.getUniqueId(), p.getName().toLowerCase());
        }

        save();
    }

    public void updateNameMapping(UUID uuid, String name) {
        Validate.notNull(uuid);
        Validate.notEmpty(name);
        name = name.toLowerCase();
        if (name.equals(nameMap.get(uuid))) return;
        if (nameMap.containsValue(name)) return;
        nameMap.put(uuid, name);
        save();
    }

    public void updateLocationMapping(UUID uuid, Location location) {
        Validate.notNull(uuid);
        if (location == null) { // unset
            if (locationMap.containsKey(uuid)) {
                locationMap.remove(uuid);
                save();
            }
        } else {
            if (!location.equals(locationMap.get(uuid))) {
                locationMap.put(uuid, location);
                save();
            }
        }
    }

    public Location getMailboxLocation(String name) {
        return getMailboxLocation(getUUIDbyName(name));
    }

    public Location getMailboxLocation(UUID uuid) {
        if (uuid == null) return null;
        return locationMap.get(uuid);
    }

    public UUID getUUIDbyName(String name) {
        if (name == null) return null;
        return nameMap.inverse().get(name.toLowerCase());
    }

    public void importMailboxLocations(Map<UUID, Location> mapping) {
        for (Map.Entry<UUID, Location> entry : mapping.entrySet()) {
            locationMap.putIfAbsent(entry.getKey(), entry.getValue());
        }
        save();
    }

    public void importUUIDMapping(Map<String, UUID> mapping) {
        for (Map.Entry<String, UUID> entry : mapping.entrySet()) {
            try {
                nameMap.put(entry.getValue(), entry.getKey());
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
        save();
    }
}
