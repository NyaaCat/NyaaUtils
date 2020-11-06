package cat.nyaa.nyaautils.elytra;


import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FuelManager {
    private final NyaaUtils plugin;

    private static final NamespacedKey keyFuelId = new NamespacedKey(NyaaUtils.instance, "fuelId");
    private static final NamespacedKey keyFuelDurability = new NamespacedKey(NyaaUtils.instance, "fuelDurability");

    public FuelManager(NyaaUtils pl) {
        plugin = pl;
    }

    public int getFuelAmount(Player player, boolean exact) {
        int fuel = 0;
        if (InventoryUtils.hasItem(player, plugin.cfg.fuelConfig.elytra_fuel, 1)) {
            fuel = InventoryUtils.getAmount(player, plugin.cfg.fuelConfig.elytra_fuel);
        }
        for (int i = 0; i <= player.getInventory().getSize(); i++) {
            if (!exact && fuel > plugin.cfg.elytra_fuel_notify) {
                return fuel;
            }
            ItemStack item = player.getInventory().getItem(i);
            int fuelID = getFuelID(item);
            if (fuelID != -1 && plugin.cfg.fuelConfig.fuel.containsKey(fuelID)) {
                fuel += getFuelDurability(item);
            }
        }
        return fuel;
    }

    public boolean useFuel(Player player) {
        if (plugin.cfg.fuelConfig.elytra_fuel != null && plugin.cfg.fuelConfig.elytra_fuel.getType() != Material.AIR) {
            if (InventoryUtils.removeItem(player, plugin.cfg.fuelConfig.elytra_fuel, 1)) {
                return true;
            }
        }
        for (int i = 0; i <= player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            int fuelID = getFuelID(item);
            if (fuelID != -1 && getFuel(fuelID) != null) {
                int durability = getFuelDurability(item);
                FuelItem fuel = getFuel(fuelID);
                if (durability > fuel.getMaxDurability()) {
                    durability = fuel.getMaxDurability();
                }
                durability--;
                if (durability <= 0) {
                    player.getInventory().setItem(i, new ItemStack(Material.AIR));
                } else {
                    updateItem(item, fuelID, durability);
                }
                return true;
            }
        }
        return false;
    }

    public void updateItem(ItemStack item, int fuelID, int durability) {
        FuelItem fuel = plugin.cfg.fuelConfig.fuel.get(fuelID);
        if (fuel == null) {
            return;
        }
        ItemMeta meta = fuel.getItem().getItemMeta();
        PersistentDataContainer persistentDataContainer = meta.getPersistentDataContainer();
        persistentDataContainer.set(keyFuelId, PersistentDataType.INTEGER, fuelID);
        persistentDataContainer.set(keyFuelDurability, PersistentDataType.INTEGER, durability);

        List<String> lore;
        String fuelLore = I18n.format("user.elytra_enhance.fuel_durability", durability, fuel.getMaxDurability());
        if (meta.hasLore()) {
            lore = meta.getLore();
            if (lore.size() == 0) {
                lore.add("");
            }
            lore.set(0, fuelLore);
        } else {
            lore = new ArrayList<>();
            lore.add(fuelLore);
        }
        item.setType(fuel.getItem().getType());
        item.setData(fuel.getItem().getData());
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public int getFuelID(ItemStack item) {
        if (item != null && !item.getType().equals(Material.AIR) && item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
            Integer id = persistentDataContainer.get(keyFuelId, PersistentDataType.INTEGER);
            if (id == null) {
                return -1;
            }
            return id;
        }
        return -1;
    }

    public int getFuelDurability(ItemStack item) {
        if (item != null && !item.getType().equals(Material.AIR) && item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            PersistentDataContainer persistentDataContainer = itemMeta.getPersistentDataContainer();
            Integer durability = persistentDataContainer.get(keyFuelDurability, PersistentDataType.INTEGER);
            if (durability == null) {
                return -1;
            }
            return durability;
        }
        return -1;
    }

    public FuelItem getFuel(int fuelID) {
        if (plugin.cfg.fuelConfig.fuel.containsKey(fuelID)) {
            return plugin.cfg.fuelConfig.fuel.get(fuelID);
        }
        return null;
    }

    public String toHexString(int i) {
        String string = Integer.toHexString(i);
        if (string.length() < 4) {
            return "0000".substring(0, 4 - string.length()) + string;
        }
        return string;
    }
}
