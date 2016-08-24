package cat.nyaa.nyaautils;

import cat.nyaa.utils.BasicItemMatcher;
import cat.nyaa.utils.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static cat.nyaa.nyaautils.Configuration.LootProtectMode.OFF;

public class Configuration implements ISerializable {

    @Serializable
    public String language = "en_US";
    @Serializable
    public long enchantCooldown = 60 * 20;
    @Serializable
    public int chanceSuccess = 1;
    @Serializable
    public int chanceModerate = 1;
    @Serializable
    public int chanceFail = 1;
    @Serializable
    public int chanceDestroy = 1;
    @Serializable
    public LootProtectMode lootProtectMode = OFF;
    @Serializable
    public int damageStatCacheTTL = 30; // in minutes, restart is required if changed
    @Serializable
    public boolean damageStatEnabled = true;
    @Serializable
    public String custom_fixes_prefix_format = "&r{prefix}&r ";
    @Serializable
    public int custom_fixes_prefix_moneyCost = 100;
    @Serializable
    public int custom_fixes_prefix_expCost = 100;
    @Serializable
    public int custom_fixes_prefix_maxlength = 10;
    @Serializable
    public List<String> custom_fixes_prefix_disabledFormattingCodes = new ArrayList<>();
    @Serializable
    public List<String> custom_fixes_prefix_blockedWords = new ArrayList<>();
    @Serializable
    public String custom_fixes_suffix_format = " &r{suffix}&r";
    @Serializable
    public int custom_fixes_suffix_moneyCost = 100;
    @Serializable
    public int custom_fixes_suffix_expCost = 100;
    @Serializable
    public int custom_fixes_suffix_maxlength = 10;
    @Serializable
    public List<String> custom_fixes_suffix_disabledFormattingCodes = new ArrayList<>();
    @Serializable
    public List<String> custom_fixes_suffix_blockedWords = new ArrayList<>();

    public List<BasicItemMatcher> enchantSrc = new ArrayList<>();
    public HashMap<Enchantment, Integer> enchantMaxLevel = new HashMap<>();

    private final NyaaUtils plugin;

    public Configuration(NyaaUtils plugin) {
        this.plugin = plugin;
        for (Enchantment e : Enchantment.values()) {
            if (e == null) {
                plugin.getLogger().warning("Bad enchantment: null");
            } else if (e.getName() == null) {
                plugin.getLogger().warning(String.format("Bad enchantment: %s: %s", e.getClass().getName(), e.toString()));
            } else {
                enchantMaxLevel.put(e, e.getMaxLevel());
            }
        }
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
                    enchantSrc.add(tmp);
                }
            }
        }

        enchantMaxLevel = new HashMap<>();
        for (Enchantment e : Enchantment.values()) {
            if (e == null || e.getName() == null) continue;
            enchantMaxLevel.put(e, e.getMaxLevel());
        }
        if (config.isConfigurationSection("enchantMaxLevel")) {
            ConfigurationSection list = config.getConfigurationSection("enchantMaxLevel");
            for (String enchName : list.getKeys(false)) {
                Enchantment e = Enchantment.getByName(enchName);
                if (e == null || e.getName() == null) continue;
                if (list.isInt(enchName)) {
                    enchantMaxLevel.put(e, list.getInt(enchName));
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

        ConfigurationSection list = config.createSection("enchantMaxLevel");
        for (Enchantment k : enchantMaxLevel.keySet()) {
            if (k == null || k.getName() == null) continue;
            list.set(k.getName(), enchantMaxLevel.get(k));
        }
    }

    public enum LootProtectMode {
        OFF,
        MAX_DAMAGE,
        FINAL_DAMAGE;
    }
}
