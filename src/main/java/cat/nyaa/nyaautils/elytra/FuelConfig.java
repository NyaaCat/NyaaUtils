package cat.nyaa.nyaautils.elytra;

import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.nyaacore.configuration.FileConfigure;
import cat.nyaa.nyaacore.configuration.ISerializable;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class FuelConfig extends FileConfigure {
    private final NyaaUtils plugin;
    @Serializable
    public ItemStack elytra_fuel = new ItemStack(Material.GUNPOWDER);
    @Serializable
    public int pos = 0;

    public HashMap<Integer, FuelItem> fuel = new HashMap<>();

    public FuelConfig(NyaaUtils pl) {
        this.plugin = pl;
    }

    @Override
    protected String getFileName() {
        return "fuel.yml";
    }

    @Override
    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    @Override
    public void deserialize(ConfigurationSection config) {
        fuel.clear();
        ISerializable.deserialize(config, this);
        if (config.isConfigurationSection("fuel")) {
            ConfigurationSection fuelList = config.getConfigurationSection("fuel");
            for (String k : fuelList.getKeys(false)) {
                FuelItem fuel = new FuelItem();
                fuel.deserialize(fuelList.getConfigurationSection(k));
                this.fuel.put(fuel.getItemID(), fuel.clone());
            }
        }
    }

    @Override
    public void serialize(ConfigurationSection config) {
        ISerializable.serialize(config, this);
        config.set("fuel", null);
        ConfigurationSection fuelList = config.createSection("fuel");
        for (int k : fuel.keySet()) {
            FuelItem fuel = this.fuel.get(k).clone();
            fuel.serialize(fuelList.createSection(String.valueOf(k)));
        }
    }

}
