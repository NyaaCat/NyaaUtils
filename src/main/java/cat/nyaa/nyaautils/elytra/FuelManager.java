package cat.nyaa.nyaautils.elytra;


import cat.nyaa.nyaacore.utils.InventoryUtils;
import cat.nyaa.nyaautils.I18n;
import cat.nyaa.nyaautils.NyaaUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FuelManager {
    private final NyaaUtils plugin;

    public String lore_prefix = ChatColor.translateAlternateColorCodes('&', "&r&9&e&a&1&4&0&2&r");

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
        String hex = toHexString(fuelID) + toHexString(durability) + toHexString(new Random().nextInt(65535));
        String str = "";
        for (int i = 0; i < hex.length(); i++) {
            str += ChatColor.COLOR_CHAR + hex.substring(i, i + 1);
        }
        str += ChatColor.COLOR_CHAR + "r";
        ItemMeta meta = fuel.getItem().getItemMeta();
        List<String> lore;
        if (meta.hasLore()) {
            lore = meta.getLore();
            lore.set(0, lore_prefix + str + lore.get(0));
            lore.add(lore_prefix + I18n.format("user.elytra_enhance.fuel_durability", durability, fuel.getMaxDurability()));
        } else {
            lore = new ArrayList<>();
            lore.add(lore_prefix + str + I18n.format("user.elytra_enhance.fuel_durability", durability, fuel.getMaxDurability()));
        }
        item.setType(fuel.getItem().getType());
        item.setDurability(fuel.getItem().getDurability());
        item.setData(fuel.getItem().getData());
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public int getFuelID(ItemStack item) {
        if (item != null && !item.getType().equals(Material.AIR) && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            String lore = item.getItemMeta().getLore().get(0);
            if (lore != null && lore.length() >= (lore_prefix.length() + 24 + 2) && lore.startsWith(lore_prefix)) {
                try {
                    return Integer.parseInt(lore.substring(lore_prefix.length(),
                            lore_prefix.length() + 8).replaceAll(String.valueOf(ChatColor.COLOR_CHAR), ""), 16);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
        }
        return -1;
    }

    public int getFuelDurability(ItemStack item) {
        if (item != null && !item.getType().equals(Material.AIR) && item.hasItemMeta() && item.getItemMeta().hasLore()) {
            String lore = item.getItemMeta().getLore().get(0);
            if (lore != null && lore.length() >= (lore_prefix.length() + 24 + 2) && lore.contains(lore_prefix)) {
                try {
                    return Integer.parseInt(lore.substring(lore_prefix.length() + 8,
                            lore_prefix.length() + 16).replaceAll(String.valueOf(ChatColor.COLOR_CHAR), ""), 16);
                } catch (NumberFormatException e) {
                    return -1;
                }
            }
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
