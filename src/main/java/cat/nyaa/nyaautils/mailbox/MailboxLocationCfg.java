package cat.nyaa.nyaautils.mailbox;

import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.utils.ISerializable;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MailboxLocationCfg{
    public final static String CFG_FILE_NAME = "mailbox_location.yml";
    private final NyaaUtils plugin;

    private final Map<UUID, Location> locationMap = new HashMap<>();

    public MailboxLocationCfg(NyaaUtils plugin) {
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
        for (UUID uuid : locationMap.keySet()) {
            cfg.set(uuid.toString(), locationMap.get(uuid));
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
        } catch (IOException|InvalidConfigurationException ex) {
            throw new RuntimeException(ex);
        }
        locationMap.clear();
        for (String uuid : cfg.getKeys(false)) {
            locationMap.put(UUID.fromString(uuid), (Location) cfg.get(uuid));
        }
    }
}
