package cat.nyaa.nyaautils;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UpgradeList extends FileConfigure {
    public static UpgradeList instance = new UpgradeList();

    private UpgradeList(){}

    public static UpgradeList getInstance(){
        return instance;
    }

    @Serializable(manualSerialization = true)
    Set<String> ids = new HashSet<>();

    @Override
    public void deserialize(ConfigurationSection config) {
        List<String> idsList = (List<String>) config.get("ids");
        this.ids = new HashSet<>();
        if (idsList !=null){
            ids.addAll(idsList);
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        config.set("ids", new ArrayList<>(ids));
    }

    @Override
    protected String getFileName() {
        return "upgrade.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return NyaaUtils.instance;
    }
}
