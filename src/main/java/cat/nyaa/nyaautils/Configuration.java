package cat.nyaa.nyaautils;

import cat.nyaa.nyaautils.dropprotect.DropProtectMode;
import cat.nyaa.nyaautils.elytra.FuelConfig;
import cat.nyaa.nyaautils.enchant.EnchantSrcConfig;
import cat.nyaa.nyaautils.lootprotect.LootProtectMode;
import cat.nyaa.nyaautils.mailbox.MailboxLocations;
import cat.nyaa.nyaautils.realm.RealmConfig;
import cat.nyaa.nyaautils.repair.RepairConfig;
import cat.nyaa.nyaautils.timer.TimerConfig;
import cat.nyaa.utils.ISerializable;
import cat.nyaa.utils.MessageType;
import cat.nyaa.utils.PluginConfigure;
import org.bukkit.ChatColor;
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
    /* Enchantment Configurations */
    @Serializable(name = "enchant.cooldown", alias = "enchantCooldown")
    public long enchantCooldown = 60 * 20;
    @Serializable(name = "enchant.chance_success", alias = "chanceSuccess")
    public int chanceSuccess = 1;
    @Serializable(name = "enchant.chance_moderate", alias = "chanceModerate")
    public int chanceModerate = 1;
    @Serializable(name = "enchant.chance_fail", alias = "chanceFail")
    public int chanceFail = 1;
    @Serializable(name = "enchant.chance_destroy", alias = "chanceDestroy")
    public int chanceDestroy = 1;
    @Serializable(manualSerialization = true)
    public HashMap<Enchantment, Integer> enchantMaxLevel = new HashMap<>();
    /* Loot Protect */
    @Serializable
    public LootProtectMode lootProtectMode = OFF;
    /* Player Damage Statistic */
    @Serializable(name = "damage_statistic.ttl_minutes", alias = "damageStatCacheTTL")
    public int damageStatCacheTTL = 30; // in minutes, restart is required if changed
    @Serializable(name = "damage_statistic.enabled", alias = "damageStatEnabled")
    public boolean damageStatEnabled = true;
    /* Custom Affixes */
    @Serializable(name = "custom_prefix.format", alias = "custom_fixes_prefix_format")
    public String custom_fixes_prefix_format = "&r{prefix}&r ";
    @Serializable(name = "custom_prefix.money_cost", alias = "custom_fixes_prefix_moneyCost")
    public int custom_fixes_prefix_moneyCost = 100;
    @Serializable(name = "custom_prefix.exp_cost", alias = "custom_fixes_prefix_expCost")
    public int custom_fixes_prefix_expCost = 100;
    @Serializable(name = "custom_prefix.max_length", alias = "custom_fixes_prefix_maxlength")
    public int custom_fixes_prefix_maxlength = 10;
    @Serializable(name = "custom_prefix.disabled_color_codes", alias = "custom_fixes_prefix_disabledFormattingCodes")
    public List<String> custom_fixes_prefix_disabledFormattingCodes = new ArrayList<>();
    @Serializable(name = "custom_prefix.censored_words", alias = "custom_fixes_prefix_blockedWords")
    public List<String> custom_fixes_prefix_blockedWords = new ArrayList<>();
    @Serializable(name = "custom_suffix.format", alias = "custom_fixes_suffix_format")
    public String custom_fixes_suffix_format = " &r{suffix}&r";
    @Serializable(name = "custom_suffix.money_cost", alias = "custom_fixes_suffix_moneyCost")
    public int custom_fixes_suffix_moneyCost = 100;
    @Serializable(name = "custom_suffix.exp_cost", alias = "custom_fixes_suffix_expCost")
    public int custom_fixes_suffix_expCost = 100;
    @Serializable(name = "custom_suffix.max_length", alias = "custom_fixes_suffix_maxlength")
    public int custom_fixes_suffix_maxlength = 10;
    @Serializable(name = "custom_suffix.disabled_color_codes", alias = "custom_fixes_suffix_disabledFormattingCodes")
    public List<String> custom_fixes_suffix_disabledFormattingCodes = new ArrayList<>();
    @Serializable(name = "custom_suffix.censored_words", alias = "custom_fixes_suffix_blockedWords")
    public List<String> custom_fixes_suffix_blockedWords = new ArrayList<>();
    /* Elytra Enhancement */
    @Serializable(name = "elytra_enhance.enabled", alias = "elytra_enhance_enabled")
    public boolean elytra_enhance_enabled = true;
    @Serializable(name = "elytra_enhance.min_speed", alias = "elytra_min_velocity")
    public double elytra_min_velocity = 1.2;
    @Serializable(name = "elytra_enhance.max_speed", alias = "elytra_max_velocity")
    public double elytra_max_velocity = 1.6;
    @Serializable(name = "elytra_enhance.power_duration", alias = "elytra_power_duration")
    public int elytra_power_duration = 3;
    @Serializable(name = "elytra_enhance.max_height", alias = "elytra_boost_max_height")
    public int elytra_boost_max_height = 256;
    @Serializable(name = "elytra_enhance.durability_notify_threshold", alias = "elytra_durability_notify")
    public int elytra_durability_notify = 10;
    @Serializable(name = "elytra_enhance.fuel_notify_threshold", alias = "elytra_fuel_notify")
    public int elytra_fuel_notify = 10;
    @Serializable(name = "elytra_enhance.disabled_worlds", alias = "disabled_world")
    public List<String> disabled_world = new ArrayList<String>(Arrays.asList("world1", "world2"));
    /* Mailing System */
    @Serializable(name = "mail.handFee")
    public int mailHandFee = 10;
    @Serializable(name = "mail.chestFee")
    public int mailChestFee = 1000;
    @Serializable(name = "mail.cooldownTicks")
    public int mailCooldown = 20;
    @Serializable(name = "mail.timeoutTicks")
    public int mailTimeout = 200;
    /* Timer */
    @Serializable
    public int timerCheckInterval = -1;
    /* Teleport */
    @Serializable(name = "teleport.enable")
    public boolean teleportEnable = true;
    @Serializable(name = "teleport.home.max")
    public int homeMax = 300;
    @Serializable(name = "teleport.home.base")
    public int homeBase = 10;
    @Serializable(name = "teleport.home.world")
    public int homeWorld = 20;
    @Serializable(name = "teleport.home.distance")
    public int homeDistance = 200;
    @Serializable(name = "teleport.home.increment")
    public int homeIncrement = 5;
    @Serializable(name = "teleport.sethome.max")
    public int setHomeMax = 100;
    @Serializable(name = "teleport.sethome.min")
    public int setHomeMin = 10;
    @Serializable(name = "teleport.sethome.distance")
    public int setHomeDistance = 200;
    @Serializable(name = "teleport.sethome.decrement")
    public int setHomeDecrement = 10;
    @Serializable(name = "teleport.sethome.world")
    public int setHomeWorld = 50;
    @Serializable(name = "teleport.sethome.default_world")
    public String setHomeDefaultWorld = "world";
    @Serializable(name = "teleport.back.max")
    public int backMax = 300;
    @Serializable(name = "teleport.back.base")
    public int backBase = 100;
    @Serializable(name = "teleport.back.world")
    public int backWorld = 20;
    @Serializable(name = "teleport.back.distance")
    public int backDistance = 200;
    @Serializable(name = "teleport.back.increment")
    public int backIncrement = 20;

    @Serializable(name = "rename.character_limit")
    public int renameCharacterLimit = 50;
    @Serializable(name = "rename.disabled_color_codes")
    public List<String> renameDisabledFormattingCodes = new ArrayList<>();
    @Serializable(name = "rename.censored_words")
    public List<String> renameBlockedWords = new ArrayList<>();
    @Serializable(name = "rename.censored_words")
    public List<String> renameBlockedMaterials = new ArrayList<>();
    @Serializable(name = "rename.exp_cost_base")
    public int renameExpCostBase = 10;
    @Serializable(name = "rename.money_cost_base")
    public int renameMoneyCostBase = 20;
    @Serializable(name = "rename.exp_cost_per_item")
    public int renameExpCostPer = 10;
    @Serializable(name = "rename.money_cost_per_item")
    public int renameMoneyCostPer = 10;

    @Serializable(name = "drop_protect.maximum_item_in_world")
    public int dropProtectMaximumItem = 1000;
    @Serializable(name = "drop_protect.proctect_second")
    public int dropProtectSecond = 120;
    @Serializable
    public DropProtectMode dropProtectMode = DropProtectMode.ON;

    @Serializable(name = "i16r.lang_file_dir")
    public String langFileDir = "/i16r/";

    @Serializable(name = "realm.default_name")
    public String realm_default_name = ChatColor.GREEN + "Wilderness";
    @Serializable(name = "realm.notification_type")
    public MessageType realm_notification_type = MessageType.SUBTITLE;
    @Serializable(name = "realm.notification_title_fadein_tick")
    public int realm_notification_title_fadein_tick = 10;
    @Serializable(name = "realm.notification_title_stay_tick")
    public int realm_notification_title_stay_tick = 10;
    @Serializable(name = "realm.notification_title_fadeout_tick")
    public int realm_notification_title_fadeout_tick = 10;
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
    @StandaloneConfig
    public final TimerConfig timerConfig;
    @StandaloneConfig
    public final RealmConfig realmConfig;

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
        this.timerConfig = new TimerConfig(plugin);
        this.realmConfig = new RealmConfig(plugin);
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
        ConfigurationSection list = config.getConfigurationSection("enchant.max_level");
        if (list == null) list = config.getConfigurationSection("enchantMaxLevel");
        if (list != null) {
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
        ConfigurationSection list = config.createSection("enchant.max_level");
        for (Enchantment k : enchantMaxLevel.keySet()) {
            if (k == null || k.getName() == null) continue;
            list.set(k.getName(), enchantMaxLevel.get(k));
        }
    }
}
