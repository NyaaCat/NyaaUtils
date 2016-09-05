package cat.nyaa.nyaautils;

import cat.nyaa.nyaautils.exhibition.ExhibitionListener;
import cat.nyaa.utils.VaultUtil;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class NyaaUtils extends JavaPlugin {
    public static NyaaUtils instance;
    public I18n i18n;
    public CommandHandler commandHandler;
    public Configuration cfg;
    public HashMap<UUID, Long> enchantCooldown = new HashMap<>();
    public LootProtectListener lpListener;
    public DamageStatListener dsListener;
    public ExhibitionListener exhibitionListener;
    public VaultUtil vaultUtil;
    public ElytraEnhanceListener elytraEnhanceListener;

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        cfg = new Configuration(this);
        cfg.deserialize(getConfig());
        cfg.serialize(getConfig());
        saveConfig();
        i18n = new I18n(this, cfg.language);
    }

    @Override
    public void onDisable() {
        cfg.serialize(getConfig());
        saveConfig();
        I18n.instance.reset();
        enchantCooldown.clear();
    }

    @Override
    public void onEnable() {
        i18n.load(cfg.language);
        commandHandler = new CommandHandler(this, i18n);
        getCommand("nyaautils").setExecutor(commandHandler);
        lpListener = new LootProtectListener(this);
        dsListener = new DamageStatListener(this);
        elytraEnhanceListener = new ElytraEnhanceListener(this);
        exhibitionListener = new ExhibitionListener(this);
        vaultUtil = new VaultUtil(this);
    }
}
