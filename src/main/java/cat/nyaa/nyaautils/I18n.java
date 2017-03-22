package cat.nyaa.nyaautils;

import cat.nyaa.utils.Internationalization;
import org.bukkit.plugin.java.JavaPlugin;

public class I18n extends Internationalization {
    public static I18n instance = null;
    private String lang = null;
    private final NyaaUtils plugin;

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    protected String getLanguage() {
        return lang;
    }

    public I18n(NyaaUtils plugin, String lang) {
        instance = this;
        this.plugin = plugin;
        this.lang = lang;
        load();
    }

    public static String format(String key, Object... args) {
        return instance.get(key, args);
    }
}
