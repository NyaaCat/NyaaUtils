package cat.nyaa.nyaautils.repair;

import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RepairConfig extends FileConfigure {
    public static class RepairConfigItem implements ISerializable {
        @Serializable
        Material material = Material.STONE;
        @Serializable
        int fullRepairCost = 1;
        @Serializable
        int expCost = 0;
        @Serializable
        double enchantCostPerLv = 0;
        @Serializable
        int repairLimit = 0;

        public RepairConfigItem normalize() {
            if (material == null) material = Material.STONE;
            if (fullRepairCost <= 0) fullRepairCost = 1;
            if (expCost < 0) expCost = 0;
            if (enchantCostPerLv < 0) enchantCostPerLv = 0;
            if (repairLimit <= 0) repairLimit = 0;
            return this;
        }
    }

    private final NyaaUtils plugin;

    private final Map<Material, RepairConfigItem> repairMap = new HashMap<>();

    public RepairConfig(NyaaUtils plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getFileName() {
        return "repair.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        repairMap.clear();
        for (String key : config.getKeys(false)) {
            Material m;
            try {
                m = Material.valueOf(key);
            } catch (IllegalArgumentException ex) {
                continue;
            }
            if (!config.isConfigurationSection(key)) continue;
            RepairConfigItem item = new RepairConfigItem();
            item.deserialize(config.getConfigurationSection(key));
            repairMap.put(m, item.normalize());
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        Set<String> tmp = new HashSet<>(config.getKeys(false));
        for (String key : tmp) { // clear section
            config.set(key, null);
        }

        for (Map.Entry<Material, RepairConfigItem> pair : repairMap.entrySet()) {
            ConfigurationSection section = config.createSection(pair.getKey().name());
            pair.getValue().normalize().serialize(section);
        }
    }

    public void addItem(Material m, RepairConfigItem item) {
        repairMap.put(m, item);
        save();
    }

    public RepairConfigItem getRepairConfig(Material toolMaterial) {
        return repairMap.get(toolMaterial);
    }
}
