package cat.nyaa.nyaautils.mailbox;

import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MailboxLocations extends FileConfigure {
    public final static String CFG_FILE_NAME = "mailbox_location.yml";
    private final NyaaUtils plugin;

    private final BiMap<UUID, String> nameMap = HashBiMap.create();
    private final Map<UUID, Location> locationMap = new HashMap<>();

    public MailboxLocations(NyaaUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getFileName() {
        return CFG_FILE_NAME;
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        locationMap.clear();
        nameMap.clear();
        ConfigurationSection locations = config.getConfigurationSection("locations");
        ConfigurationSection names = config.getConfigurationSection("names");
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
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ConfigurationSection locationSection = config.createSection("locations");
        for (UUID uuid : locationMap.keySet()) {
            locationSection.set(uuid.toString(), locationMap.get(uuid));
        }
        ConfigurationSection nameSection = config.createSection("names");
        for (UUID uuid : nameMap.keySet()) {
            nameSection.set(uuid.toString(), nameMap.get(uuid));
        }
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
}
