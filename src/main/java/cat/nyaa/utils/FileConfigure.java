package cat.nyaa.utils;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public abstract class FileConfigure implements ISerializable {
    protected abstract String getFileName();

    protected abstract JavaPlugin getPlugin();

    private File ensureFile() {
        File cfgFile = new File(getPlugin().getDataFolder(), getFileName());
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
        serialize(cfg);
        try {
            cfg.save(ensureFile());
        } catch (IOException ex) {
            getPlugin().getLogger().severe("Cannot save " + getFileName() + ". Emergency dump:");
            getPlugin().getLogger().severe("\n" + cfg.saveToString());
            getPlugin().getLogger().severe("Cannot save " + getFileName() + ". Emergency dump End.");
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
        deserialize(cfg);
        save();
    }
}
