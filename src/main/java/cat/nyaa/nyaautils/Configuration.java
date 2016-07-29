package cat.nyaa.nyaautils;

import cat.nyaa.utils.BasicItemMatcher;
import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class Configuration implements ISerializable {

    @Serializable
    public String language = "en_US";

    public List<BasicItemMatcher> enchantSrc = new ArrayList<>();

    private final NyaaUtils plugin;

    public Configuration(NyaaUtils plugin) {
        this.plugin = plugin;
    }

    public void save() {
        serialize(plugin.getConfig());
        plugin.saveConfig();
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        ISerializable.deserialize(config, this);

        enchantSrc = new ArrayList<>();
        if (config.isConfigurationSection("enchantSrc")) {
            ConfigurationSection src = config.getConfigurationSection("enchantSrc");
            for (String key : src.getKeys(false)) {
                if (src.isConfigurationSection(key)) {
                    BasicItemMatcher tmp = new BasicItemMatcher();
                    tmp.deserialize(src.getConfigurationSection(key));
                }
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);

        ConfigurationSection dst = config.createSection("enchantSrc");
        int idx = 0;
        for (BasicItemMatcher m : enchantSrc) {
            m.serialize(dst.createSection(Integer.toString(idx)));
            idx++;
        }
    }
}
