package cat.nyaa.nyaautils.timer;


import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class TimerConfig extends FileConfigure {
    private final NyaaUtils plugin;
    public HashMap<String, Timer> timers = new HashMap<>();

    public TimerConfig(NyaaUtils pl) {
        this.plugin = pl;
    }

    @Override
    protected String getFileName() {
        return "timers.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        timers.clear();
        ISerializable.deserialize(config, this);
        if (config.isConfigurationSection("timers")) {
            ConfigurationSection list = config.getConfigurationSection("timers");
            for (String k : list.getKeys(false)) {
                Timer timer = new Timer();
                timer.deserialize(list.getConfigurationSection(k));
                timers.put(timer.getName(), timer.clone());
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("timers", null);
        if (!timers.isEmpty()) {
            ConfigurationSection list = config.createSection("timers");
            for (String k : timers.keySet()) {
                timers.get(k).serialize(list.createSection(k));
            }
        }

    }

}