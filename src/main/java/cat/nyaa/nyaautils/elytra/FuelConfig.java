package cat.nyaa.nyaautils.elytra;

import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.utils.FileConfigure;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class FuelConfig extends FileConfigure {
    private final NyaaUtils plugin;
    @Serializable
    public ItemStack elytra_fuel = new ItemStack(Material.SULPHUR);
    @Serializable
    public List<Integer> rpgitem_fuel = new ArrayList<>();

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

}
