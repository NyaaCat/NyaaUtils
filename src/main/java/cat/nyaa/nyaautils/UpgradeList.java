package cat.nyaa.nyaautils;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class UpgradeList extends FileConfigure {
    public static UpgradeList instance = new UpgradeList();

    private UpgradeList(){}

    public static UpgradeList getInstance(){
        return instance;
    }

    @Serializable
    Set<String> Ids = new HashSet<>();

    @Override
    protected String getFileName() {
        return "upgrade.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return NyaaUtils.instance;
    }
}
