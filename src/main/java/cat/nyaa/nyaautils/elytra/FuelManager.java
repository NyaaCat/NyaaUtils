package cat.nyaa.nyaautils.elytra;


import cat.nyaa.nyaautils.NyaaUtils;
import cat.nyaa.utils.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import think.rpgitems.data.RPGMetadata;
import think.rpgitems.item.ItemManager;
import think.rpgitems.item.RPGItem;

public class FuelManager {
    private final NyaaUtils plugin;

    public FuelManager(NyaaUtils pl) {
        plugin = pl;
    }

    public int getFuelAmount(Player player) {
        int fuel = 0;
        if (InventoryUtils.hasItem(player, plugin.cfg.fuelConfig.elytra_fuel, 1)) {
            fuel = InventoryUtils.getAmount(player, plugin.cfg.fuelConfig.elytra_fuel);
        }
        if (plugin.rpgitem != null) {
            fuel += getRPGItemFuelAmount(player);
        }
        return fuel;
    }

    public boolean useFuel(Player player) {
        if (plugin.cfg.fuelConfig.elytra_fuel != null && plugin.cfg.fuelConfig.elytra_fuel.getType() != Material.AIR) {
            if (InventoryUtils.removeItem(player, plugin.cfg.fuelConfig.elytra_fuel, 1)) {
                return true;
            }
        }
        if (plugin.rpgitem != null) {
            return useRPGItemFuel(player);
        }
        return false;
    }

    public boolean useRPGItemFuel(Player player) {
        if (plugin.rpgitem == null || plugin.cfg.fuelConfig.rpgitem_fuel.isEmpty()) {
            return false;
        }
        for (int i = 0; i <= player.getInventory().getSize(); i++) {
            RPGItem fuel = ItemManager.toRPGItem(player.getInventory().getItem(i));
            if (fuel != null && plugin.cfg.fuelConfig.rpgitem_fuel.contains(fuel.getID()) && fuel.getMaxDurability() != -1) {
                RPGMetadata meta = RPGItem.getMetadata(player.getInventory().getItem(i));
                int durability = meta.containsKey(RPGMetadata.DURABILITY) ? ((Number) meta.get(RPGMetadata.DURABILITY)).intValue() : fuel.getMaxDurability();
                if (durability > fuel.getMaxDurability()) {
                    durability = fuel.getMaxDurability();
                }
                durability--;
                if (durability <= 0) {
                    player.getInventory().setItem(i, new ItemStack(Material.AIR));
                } else {
                    meta.put(RPGMetadata.DURABILITY, Integer.valueOf(durability));
                    RPGItem.updateItem(player.getInventory().getItem(i), meta);
                }
                return true;
            }
        }
        return false;
    }

    public int getRPGItemFuelAmount(Player player) {
        if (plugin.rpgitem == null || plugin.cfg.fuelConfig.rpgitem_fuel.isEmpty()) {
            return 0;
        }
        int amount = 0;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            RPGItem fuel = ItemManager.toRPGItem(player.getInventory().getItem(i));
            if (fuel != null && plugin.cfg.fuelConfig.rpgitem_fuel.contains(fuel.getID()) && fuel.getMaxDurability() != -1) {
                RPGMetadata meta = RPGItem.getMetadata(player.getInventory().getItem(i));
                int durability = meta.containsKey(RPGMetadata.DURABILITY) ? ((Number) meta.get(RPGMetadata.DURABILITY)).intValue() : fuel.getMaxDurability();
                amount += durability;
            }
        }
        return amount;
    }
}
