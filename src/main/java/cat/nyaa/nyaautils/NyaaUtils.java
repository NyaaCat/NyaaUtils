package cat.nyaa.nyaautils;

import cat.nyaa.nyaautils.exhibition.ExhibitionListener;
import cat.nyaa.nyaautils.mailbox.MailboxListener;
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
    public MailboxListener mailboxListener;
    public VaultUtil vaultUtil;
    public ElytraEnhanceListener elytraEnhanceListener;

    private boolean serverEnabled = false;

    public boolean isServerEnabled() {
        return serverEnabled;
    }

    @Override
    public void onLoad() {
        serverEnabled = false;
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
        serverEnabled = false;
    }

    @Override
    public void onEnable() {
        serverEnabled = true;
        cfg.mailbox.load(); // mailbox location deserialize fails when worlds aren't loaded.
        i18n.load(cfg.language);
        commandHandler = new CommandHandler(this, i18n);
        getCommand("nyaautils").setExecutor(commandHandler);
        lpListener = new LootProtectListener(this);
        dsListener = new DamageStatListener(this);
        elytraEnhanceListener = new ElytraEnhanceListener(this);
        exhibitionListener = new ExhibitionListener(this);
        mailboxListener = new MailboxListener(this);
        vaultUtil = new VaultUtil(this);
    }
}
