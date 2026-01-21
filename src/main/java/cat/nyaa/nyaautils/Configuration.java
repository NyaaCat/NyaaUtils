package cat.nyaa.nyaautils;

import cat.nyaa.nyaacore.Message.MessageType;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaacore.configuration.PluginConfigure;
import cat.nyaa.nyaacore.orm.backends.BackendConfig;
import cat.nyaa.nyaautils.dropprotect.DropProtectMode;
import cat.nyaa.nyaautils.elytra.FuelConfig;
import cat.nyaa.nyaautils.lootprotect.LootProtectMode;
import cat.nyaa.nyaautils.mailbox.MailboxLocations;
import cat.nyaa.nyaautils.mention.MentionNotification;
import cat.nyaa.nyaautils.realm.RealmConfig;
import cat.nyaa.nyaautils.repair.RepairConfig;
import cat.nyaa.nyaautils.sit.SitLocation;
import cat.nyaa.nyaautils.timer.TimerConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

import static cat.nyaa.nyaautils.lootprotect.LootProtectMode.OFF;

public class Configuration extends PluginConfigure {
    @Serializable
    public String language = "en_US";
    /* Loot Protect */
    @Serializable
    public LootProtectMode lootProtectMode = OFF;
    /* Player Damage Statistic */
    @Serializable(name = "damage_statistic.ttl_minutes", alias = "damageStatCacheTTL")
    public int damageStatCacheTTL = 30; // in minutes, restart is required if changed
    @Serializable(name = "damage_statistic.enabled", alias = "damageStatEnabled")
    public boolean damageStatEnabled = true;
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
    @Serializable(name = "rename.exp_cost_per_item")
    public int renameExpCostPer = 10;

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

    @Serializable(name = "expcap.thrown_orb.maxAmount")
    public int expcap_max_orb_amount = 30;

    @Serializable(name = "expcap.thrown_orb.minAmount")
    public int expcap_min_orb_amount = 1;

    @Serializable(name = "expcap.thrown_orb.ticksBetweenSpawn")
    public int expcap_orb_ticksBetweenSpawn = 1;

    @Serializable(name = "expcap.max_stored_exp")
    public int expcap_max_stored_exp = Integer.MAX_VALUE;

    @Serializable(name = "expcap.thrown_orb.enabled")
    public boolean expcap_thron_enabled = true;

    @Serializable(name = "vote.enable")
    public boolean vote_enable = true;
    @Serializable(name = "vote.timeout")
    public int vote_timeout = 1200;
    @Serializable(name = "vote.max_options")
    public int vote_max_options = 8;
    @Serializable(name = "vote.broadcast_interval")
    public int vote_broadcast_interval = -1;

    @Serializable(name = "redstone_control.enable")
    public boolean redstoneControl;

    @Serializable(name = "redstone_control.material")
    public Material redstoneControlMaterial = Material.AIR;

    public Set<Material> sit_blocks = new HashSet<>();
    @Serializable(name = "sit.locations")
    public Map<String, SitLocation> sit_locations = new HashMap<>();


    @Serializable(name = "extra_backpack.enable")
    public boolean bp_enable = false;

    @Serializable(name = "extra_backpack.start_line")
    public int bp_default_lines = 0;

    @Serializable(name = "extra_backpack.max_line")
    public int bp_max_lines = 6;

    @Serializable(name = "extra_backpack.require_nearby_block")
    public Material bp_require_nearby_block = Material.AIR;

    @Serializable(name = "extra_backpack.require_nearby_distance")
    public int bp_require_nearby_distance = 5;

    @StandaloneConfig
    public final MailboxLocations mailbox;
    @StandaloneConfig
    public final RepairConfig repair;
    @StandaloneConfig
    public final GlobalLoreBlacklist globalLoreBlacklist;
    @StandaloneConfig
    public final FuelConfig fuelConfig;
    @StandaloneConfig
    public final TimerConfig timerConfig;
    @StandaloneConfig
    public final RealmConfig realmConfig;

    private final NyaaUtils plugin;
    public BackendConfig backpackBackendConfig;

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
        this.fuelConfig = new FuelConfig(plugin);
        this.timerConfig = new TimerConfig(plugin);
        this.realmConfig = new RealmConfig(plugin);
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        // general values load & standalone config load
        ISerializable.deserialize(config, this);
        if (bp_enable) {
            backpackBackendConfig = BackendConfig.sqliteBackend("Backpack.db");
            if (config.isConfigurationSection("database.extrabackpack")) {
                backpackBackendConfig.deserialize(config.getConfigurationSection("database.extrabackpack"));
            }
        }
        for (SitLocation s : sit_locations.values()) {
            if (s.blocks != null) {
                for (String name : s.blocks) {
                    sit_blocks.add(Material.valueOf(name));
                }
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        // general values save & standalone config save
        ISerializable.serialize(config, this);
    }
}
