package cat.nyaa.utils;


import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryUtils {

    public static boolean hasItem(Inventory inv, ItemStack item, int amount) {
        return inv.containsAtLeast(item, amount);
    }

    public static boolean removeItem(Player player, ItemStack item, int amount) {
        Inventory inv = player.getInventory();
        ItemStack[] items = new ItemStack[inv.getSize()];
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) != null &&
                    inv.getItem(i).getType() != Material.AIR) {
                items[i] = inv.getItem(i).clone();
            } else {
                items[i] = new ItemStack(Material.AIR);
            }
        }
        boolean success = false;
        for (int slot = 0; slot < items.length; slot++) {
            ItemStack tmp = items[slot];
            if (tmp != null && tmp.isSimilar(item) && tmp.getAmount() > 0) {
                if (tmp.getAmount() < amount) {
                    amount = amount - tmp.getAmount();
                    items[slot] = new ItemStack(Material.AIR);
                    continue;
                } else if (tmp.getAmount() > amount) {
                    tmp.setAmount(tmp.getAmount() - amount);
                    amount = 0;
                    success = true;
                    break;
                } else {
                    items[slot] = new ItemStack(Material.AIR);
                    amount = 0;
                    success = true;
                    break;
                }
            }
        }
        if (success) {
            player.getInventory().setContents(items);
            return true;
        }
        return false;
    }

    public static int getAmount(Player p, ItemStack item) {
        int amount = 0;
        Inventory inv = p.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) != null &&
                    inv.getItem(i).getType() != Material.AIR &&
                    inv.getItem(i).isSimilar(item)) {
                amount += inv.getItem(i).getAmount();
            }
        }
        return amount;
    }

}
