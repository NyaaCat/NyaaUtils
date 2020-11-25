package cat.nyaa.nyaautils;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.utils.HexColorUtils;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* Used by enchantment & repair system.
 * Forbid certain items from being enchanted or repaired
 * e.g. Much too powerful weapons
 */
public class GlobalLoreBlacklist extends FileConfigure {

    // true  = enchant/repair allowed
    // false = enchant/repair forbidden
    private static class Flags implements ISerializable {
        @Serializable
        boolean enchant = true;
        @Serializable
        boolean repair = true;
    }

    private boolean default_enchant = true;
    private boolean default_repair = true;
    private static final Map<String, Flags> aclMap = new HashMap<>();
    private final NyaaUtils plugin;

    public GlobalLoreBlacklist(NyaaUtils plugin) {
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
        // load default values
        if (config.isConfigurationSection("default")) {
            this.default_enchant = config.getBoolean("default.enchant", default_enchant);
            this.default_repair = config.getBoolean("default.repair", default_repair);
        }
        // load all lores
        for (String key : config.getKeys(false)) {
            if (!key.equals("default") && config.isConfigurationSection(key)) {
                Flags flags = new Flags();
                flags.deserialize(config.getConfigurationSection(key));
                aclMap.put(key, flags);
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        // save default
        ConfigurationSection sectionDefault = config.createSection("default");
        sectionDefault.set("enchant", this.default_enchant);
        sectionDefault.set("repair", this.default_repair);
        // save lores
        for (String lore : aclMap.keySet()) {
            aclMap.get(lore).serialize(config.createSection(lore));
        }
    }

    public boolean canEnchant(List<String> lore) {
        for (String s : lore) {
            if (aclMap.containsKey(HexColorUtils.stripEssentialsFormat(s))) {
                if (aclMap.get(HexColorUtils.stripEssentialsFormat(s)).enchant != this.default_enchant) {
                    return aclMap.get(HexColorUtils.stripEssentialsFormat(s)).enchant;
                }
            }
        }
        return this.default_enchant;

    }

    public boolean canRepair(List<String> lore) {
        for (String s : lore) {
            if (aclMap.containsKey(HexColorUtils.stripEssentialsFormat(s))) {
                if (aclMap.get(HexColorUtils.stripEssentialsFormat(s)).repair != this.default_repair) {
                    return aclMap.get(HexColorUtils.stripEssentialsFormat(s)).repair;
                }
            }
        }
        return this.default_repair;
    }
}
