package cat.nyaa.utils;

public abstract class PluginConfigure extends FileConfigure {
    @Override
    protected final String getFileName() {
        return "config.yml";
    }

    @Override
    public void save() {
        serialize(getPlugin().getConfig());
        getPlugin().saveConfig();
    }

    @Override
    public void load() {
        getPlugin().saveDefaultConfig();
        deserialize(getPlugin().getConfig());
        save();
    }
}
