package cat.nyaa.nyaautils.extrabackpack;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.LinkedList;
import java.util.List;

public class ExtraBackpackInventory {
    int size = 0;
    List<Inventory> inventories;
    OfflinePlayer owner;
    InventoryHolder holder;
    ExtraBackpackGUI gui;

    ExtraBackpackInventory(InventoryHolder holder) {
        inventories = new LinkedList<>();
        this.holder = holder;
        this.gui = gui;
    }

    @Override
    public ExtraBackpackInventory clone(){
        ExtraBackpackInventory clone =  new ExtraBackpackInventory(holder);
        clone.size = size;
        clone.owner = owner;
        boolean empty = inventories.isEmpty();
        for (int i = 0; i < inventories.size(); i++) {
            Inventory inventory = inventories.get(i);
            // FIXME: Inventory inventory1 = Bukkit.createInventory(holder, inventory.getSize(), inventory.getTitle());
            String title = holder instanceof ExtraBackpackGUI ? ((ExtraBackpackGUI) holder).getInventoryTitle(i):"";
            Inventory inventory1 = Bukkit.createInventory(holder, inventory.getSize(), title);
            inventory1.setContents(inventory.getContents());
            clone.inventories.add(inventory1);
        }
        return clone;
    }
}

