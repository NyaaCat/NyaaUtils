package cat.nyaa.utils;


import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryUtils {

    public static boolean hasItem(Player player, ItemStack item, int amount) {
        return hasItem(player.getInventory(), item, amount);
    }

    public static boolean hasItem(Inventory inv, ItemStack item, int amount) {
        return inv.containsAtLeast(item, amount);
    }

    public static boolean addItem(Player player, ItemStack item) {
        return addItem(player.getInventory(), item.clone(), item.getAmount());
    }

    public static boolean addItem(Inventory inventory, ItemStack item) {
        return addItem(inventory, item.clone(), item.getAmount());
    }

    private static boolean addItem(Inventory inventory, ItemStack item, int amount) {
        ItemStack[] items = new ItemStack[inventory.getSize()];
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i >= 36 && i <= 39 && inventory instanceof PlayerInventory) {
                items[i] = null;
                continue;
            }
            if (inventory.getItem(i) != null && inventory.getItem(i).getType() != Material.AIR) {
                items[i] = inventory.getItem(i).clone();
            } else {
                items[i] = new ItemStack(Material.AIR);
            }
        }
        boolean success = false;
        for (int slot = 0; slot < items.length; slot++) {
            ItemStack tmp = items[slot];
            if (tmp == null) {
                continue;
            }
            if (item.isSimilar(tmp) && tmp.getAmount() < item.getMaxStackSize()) {
                if ((tmp.getAmount() + amount) <= item.getMaxStackSize()) {
                    tmp.setAmount(amount + tmp.getAmount());
                    items[slot] = tmp;
                    success = true;
                    break;
                } else {
                    amount = amount - (item.getMaxStackSize() - tmp.getAmount());
                    tmp.setAmount(item.getMaxStackSize());
                    items[slot] = tmp;
                    continue;
                }
            }
        }
        if (!success) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null && items[i].getType() == Material.AIR) {
                    item.setAmount(amount);
                    items[i] = item;
                    success = true;
                    break;
                }
            }
        }
        if (success) {
            for (int i = 0; i < items.length; i++) {
                if (items[i] != null && !items[i].equals(inventory.getItem(i))) {
                    inventory.setItem(i, items[i]);
                }
            }
            return true;
        }
        return false;
    }

    public static boolean removeItem(Player player, ItemStack item, int amount) {
        return removeItem(player.getInventory(), item, amount);
    }

    public static boolean removeItem(Inventory inventory, ItemStack item, int amount) {
        ItemStack[] items = new ItemStack[inventory.getSize()];
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) != null &&
                    inventory.getItem(i).getType() != Material.AIR) {
                items[i] = inventory.getItem(i).clone();
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
            for (int i = 0; i < items.length; i++) {
                if (!items[i].equals(inventory.getItem(i))) {
                    inventory.setItem(i, items[i]);
                }
            }
            return true;
        }
        return false;
    }

    public static int getAmount(Player p, ItemStack item) {
        return getAmount(p.getInventory(), item);
    }

    public static int getAmount(Inventory inventory, ItemStack item) {
        int amount = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) != null &&
                    inventory.getItem(i).getType() != Material.AIR &&
                    inventory.getItem(i).isSimilar(item)) {
                amount += inventory.getItem(i).getAmount();
            }
        }
        return amount;
    }

    public static boolean hasEnoughSpace(Player player, ItemStack item, int amount) {
        return hasEnoughSpace(player.getInventory(), item, amount);
    }

    public static boolean hasEnoughSpace(Inventory inventory, ItemStack item) {
        return hasEnoughSpace(inventory, item, item.getAmount());
    }

    public static boolean hasEnoughSpace(Inventory inventory, ItemStack item, int amount) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (i >= 36 && i <= 39 && inventory instanceof PlayerInventory) {
                continue;
            }
            if (inventory.getItem(i) != null && item.isSimilar(inventory.getItem(i)) &&
                    inventory.getItem(i).getAmount() < item.getMaxStackSize()) {
                amount -= item.getMaxStackSize() - inventory.getItem(i).getAmount();
            } else if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {
                amount = 0;
            }
            if (amount < 1) {
                return true;
            }
        }
        return false;
    }
}
