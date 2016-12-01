package cat.nyaa.nyaautils;

import cat.nyaa.nyaautils.elytra.FuelConfig;
import cat.nyaa.nyaautils.enchant.EnchantSrcConfig;
import cat.nyaa.nyaautils.lootprotect.LootProtectMode;
import cat.nyaa.nyaautils.mailbox.MailboxLocations;
import cat.nyaa.nyaautils.repair.RepairConfig;
import cat.nyaa.utils.ISerializable;
import cat.nyaa.utils.PluginConfigure;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static cat.nyaa.nyaautils.lootprotect.LootProtectMode.OFF;

public class Configuration extends PluginConfigure {
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
    @Serializable
    public boolean elytra_enhance_enabled = true;
    @Serializable
    public double elytra_min_velocity = 1.2;
    @Serializable
    public double elytra_max_velocity = 1.6;
    @Serializable
    public int elytra_power_duration = 3;
    @Serializable
    public int elytra_boost_max_height = 256;
    @Serializable
    public int elytra_durability_notify = 10;
    @Serializable
    public int elytra_fuel_notify = 10;
    @Serializable
    public List<String> disabled_world = new ArrayList<String>(Arrays.asList("world1", "world2"));
    @Serializable(name = "mail.handFee")
    public int mailHandFee = 10;
    @Serializable(name = "mail.chestFee")
    public int mailChestFee = 1000;
    @Serializable(name = "mail.cooldownTicks")
    public int mailCooldown = 20;
    @Serializable(name = "mail.timeoutTicks")
    public int mailTimeout = 200;
    @Serializable(manualSerialization = true)
    public HashMap<Enchantment, Integer> enchantMaxLevel = new HashMap<>();

    @StandaloneConfig
    public final MailboxLocations mailbox;
    @StandaloneConfig
    public final RepairConfig repair;
    @StandaloneConfig
    public final GlobalLoreBlacklist globalLoreBlacklist;
    @StandaloneConfig
    public final EnchantSrcConfig enchantSrcConfig;
    @StandaloneConfig
    public final FuelConfig fuelConfig;

    private final NyaaUtils plugin;

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    public Configuration(NyaaUtils plugin) {
        this.plugin = plugin;
        this.mailbox = new MailboxLocations(plugin);
        this.repair = new RepairConfig(plugin);
        this.globalLoreBlacklist = new GlobalLoreBlacklist(plugin);
        this.enchantSrcConfig = new EnchantSrcConfig(plugin);
        this.fuelConfig = new FuelConfig(plugin);
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

    @Override
    public void deserialize(ConfigurationSection config) {
        // general values load & standalone config load
        ISerializable.deserialize(config, this);

        // Enchantment Max Level constraint
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
        // general values save & standalone config save
        ISerializable.serialize(config, this);

        // Enchantment Max Level constraint
        ConfigurationSection list = config.createSection("enchantMaxLevel");
        for (Enchantment k : enchantMaxLevel.keySet()) {
            if (k == null || k.getName() == null) continue;
            list.set(k.getName(), enchantMaxLevel.get(k));
        }
    }
}
