package cat.nyaa.nyaautils;

import cat.nyaa.nyaacore.Message.MessageType;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import cat.nyaa.nyaautils.dropprotect.DropProtectMode;
import cat.nyaa.nyaautils.elytra.FuelConfig;
import cat.nyaa.nyaautils.enchant.EnchantSrcConfig;
import cat.nyaa.nyaautils.lootprotect.LootProtectMode;
import cat.nyaa.nyaautils.mailbox.MailboxLocations;
import cat.nyaa.nyaautils.mention.MentionNotification;
import cat.nyaa.nyaautils.particle.ParticleConfig;
import cat.nyaa.nyaautils.particle.ParticleLimit;
import cat.nyaa.nyaautils.particle.ParticleType;
import cat.nyaa.nyaautils.realm.RealmConfig;
import cat.nyaa.nyaautils.repair.RepairConfig;
import cat.nyaa.nyaautils.timer.TimerConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

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
    public List<String> disabled_world = new ArrayList<>(Arrays.asList("world1", "world2"));
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
    /* TeleportCmdWarpper */
    @Serializable(name = "teleportCmdWarpper.enable")
    public boolean teleportEnable = true;
    @Serializable(name = "teleportCmdWarpper.home.max")
    public int homeMax = 300;
    @Serializable(name = "teleportCmdWarpper.home.base")
    public int homeBase = 10;
    @Serializable(name = "teleportCmdWarpper.home.world")
    public int homeWorld = 20;
    @Serializable(name = "teleportCmdWarpper.home.distance")
    public int homeDistance = 200;
    @Serializable(name = "teleportCmdWarpper.home.increment")
    public int homeIncrement = 5;
    @Serializable(name = "teleportCmdWarpper.sethome.max")
    public int setHomeMax = 100;
    @Serializable(name = "teleportCmdWarpper.sethome.min")
    public int setHomeMin = 10;
    @Serializable(name = "teleportCmdWarpper.sethome.distance")
    public int setHomeDistance = 200;
    @Serializable(name = "teleportCmdWarpper.sethome.decrement")
    public int setHomeDecrement = 10;
    @Serializable(name = "teleportCmdWarpper.sethome.world")
    public int setHomeWorld = 50;
    @Serializable(name = "teleportCmdWarpper.sethome.default_world")
    public String setHomeDefaultWorld = "world";
    @Serializable(name = "teleportCmdWarpper.back.max")
    public int backMax = 300;
    @Serializable(name = "teleportCmdWarpper.back.base")
    public int backBase = 100;
    @Serializable(name = "teleportCmdWarpper.back.world")
    public int backWorld = 20;
    @Serializable(name = "teleportCmdWarpper.back.distance")
    public int backDistance = 200;
    @Serializable(name = "teleportCmdWarpper.back.increment")
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
    @Serializable(name = "drop_protect.mode", alias = "dropProtectMode")
    public DropProtectMode dropProtectMode = DropProtectMode.ON;

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

    @Serializable(name = "particles.type.player")
    public boolean particles_type_player = false;
    @Serializable(name = "particles.type.elytra")
    public boolean particles_type_elytra = true;
    @Serializable(name = "particles.type.other")
    public boolean particles_type_other = false;
    @Serializable(name = "particles.enabled")
    public List<String> particles_enabled = new ArrayList<>(Arrays.asList("FLAME", "WATER_SPLASH"));
    @Serializable(name = "particles.limits", manualSerialization = true)
    public Map<ParticleType, ParticleLimit> particlesLimits = new HashMap<>();
    
    @Serializable(name = "signedit.disabledFormattingCodes")
    public List<String> signedit_disabledFormattingCodes = new ArrayList<>(Collections.singletonList("k"));
    @Serializable(name = "signedit.max_length")
    public int signedit_max_length = 15;

    @Serializable(name = "mention.enable")
    public Boolean mention_enable = true;
    @Serializable(name = "mention.sound")
    public List<String> mention_sound = new ArrayList<>(Collections.singletonList("entity.experience_orb.pickup"));
    @Serializable(name = "mention.pitch")
    public List<Double> mention_pitch = new ArrayList<>(Collections.singletonList(1d));
    @Serializable(name = "mention.notification")
    public MentionNotification mention_notification = MentionNotification.ACTION_BAR;
    @Serializable(name = "mention.blink")
    public Boolean mention_blink = true;
    @Serializable(name = "mention.blink_chars")
    public String mention_blink_char = "b0fc";


    @Serializable
    public Material expCapsuleType = Material.EXPERIENCE_BOTTLE;

    @Serializable(name = "vote.enable")
    public boolean vote_enable = true;
    @Serializable(name = "vote.timeout")
    public int vote_timeout = 1200;
    @Serializable(name = "vote.max_options")
    public int vote_max_options = 8;
    @Serializable(name = "vote.broadcast_interval")
    public int vote_broadcast_interval = -1;

    @Serializable(name = "message_queue.enable")
    public Boolean message_queue_enable = true;

    @Serializable(name = "redstone_control.enable")
    public boolean redstoneControl;

    @Serializable(name = "redstone_control.material")
    public Material redstoneControlMaterial = Material.AIR;

    @Serializable(name = "enhanced_ping.enable")
    public boolean ping_enable = true;

    @Serializable(name = "enhanced_ping.override")
    public boolean ping_override = false;

    @Serializable(name = "enhanced_ping.show_in_tab")
    public boolean ping_tab = false;

    @Serializable(name = "enhanced_tps.enable")
    public boolean tps_enable = true;

    @Serializable(name = "enhanced_tps.history")
    public int tps_history = 10;

    @Serializable(name = "enhanced_tps.override")
    public boolean tps_override = false;


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
    @StandaloneConfig
    public final ParticleConfig particleConfig;

    private final NyaaUtils plugin;

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @SuppressWarnings("deprecation")
    public Configuration(NyaaUtils plugin) {
        this.plugin = plugin;
        this.mailbox = new MailboxLocations(plugin);
        this.repair = new RepairConfig(plugin);
        this.globalLoreBlacklist = new GlobalLoreBlacklist(plugin);
        this.enchantSrcConfig = new EnchantSrcConfig(plugin);
        this.fuelConfig = new FuelConfig(plugin);
        this.timerConfig = new TimerConfig(plugin);
        this.realmConfig = new RealmConfig(plugin);
        this.particleConfig = new ParticleConfig(plugin);
        //TODO: Key based enchantment store
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

    @SuppressWarnings("deprecation")
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
        if (config.isConfigurationSection("enchant.max_level")) {
            ConfigurationSection maxLevel = config.getConfigurationSection("enchant.max_level");
            for (String enchName : maxLevel.getKeys(false)) {
                Enchantment ench = null;
                try {
                    ench = Enchantment.getByKey(NamespacedKey.minecraft(enchName));
                } catch (IllegalArgumentException e) {
                    ench = Enchantment.getByName(enchName);
                }
                if (ench == null || !NamespacedKey.MINECRAFT.equals(ench.getKey().getNamespace()))
                    continue;
                if (maxLevel.isInt(enchName)) {
                    enchantMaxLevel.put(ench, maxLevel.getInt(enchName));
                }
            }
        }
        for (ParticleType type : ParticleType.values()) {
            particlesLimits.put(type, new ParticleLimit());
            if (config.isConfigurationSection("particles.limits." + type.name())) {
                particlesLimits.get(type).deserialize(config.getConfigurationSection("particles.limits." + type.name()));
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
            if (k == null || k.getKey() == null || k.getKey().getKey() == null) continue;
            list.set(k.getKey().getKey(), enchantMaxLevel.get(k));
        }
        config.set("particles.limits", null);
        for (ParticleType type : particlesLimits.keySet()) {
            particlesLimits.get(type).serialize(config.createSection("particles.limits." + type.name()));
        }
    }
}
