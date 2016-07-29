package cat.nyaa.nyaautils;

import org.bukkit.plugin.java.JavaPlugin;

public class NyaaUtils extends JavaPlugin {
    public static NyaaUtils instance;
    public I18n i18n;
    public CommandHandler commandHandler;
    public Configuration cfg;

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
    }

    @Override
    public void onEnable() {
        i18n.load(cfg.language);
        commandHandler = new CommandHandler(this, i18n);
        getCommand("nyaautils").setExecutor(commandHandler);
    }
}
