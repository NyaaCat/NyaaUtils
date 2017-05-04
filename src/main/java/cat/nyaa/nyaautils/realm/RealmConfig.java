package cat.nyaa.nyaautils.realm;


import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class RealmConfig extends FileConfigure {
    private final NyaaUtils plugin;
    public HashMap<String, Realm> realmList = new HashMap<>();

    public RealmConfig(NyaaUtils pl) {
        this.plugin = pl;
    }

    @Override
    protected String getFileName() {
        return "realm.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        realmList.clear();
        ISerializable.deserialize(config, this);
        if (config.isConfigurationSection("realms")) {
            ConfigurationSection list = config.getConfigurationSection("realms");
            for (String k : list.getKeys(false)) {
                Realm realm = new Realm();
                realm.deserialize(list.getConfigurationSection(k));
                realmList.put(realm.getName(), realm);
            }
        }
        Realm realm = new Realm();
        realm.setWorld("world");
        realm.setMaxPos(0, 0, 0);
        realm.setMinPos(0, 0, 0);
        realm.setName(Realm.__DEFAULT__);
        realm.setPriority(-65535);
        realmList.put(realm.getName(), realm);
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("realms", null);
        if (!realmList.isEmpty()) {
            ConfigurationSection list = config.createSection("realms");
            for (String k : realmList.keySet()) {
                realmList.get(k).serialize(list.createSection(k));
            }
        }
    }
}
